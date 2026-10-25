<?php

namespace App\Models;

use App\Enums\EstadoPedidoWeb;
use App\Enums\MetodoPago;
use Database\Factories\PedidoWebFactory;
use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use RuntimeException;

/**
 * Pedido (carrito) que un visitante arma en la página de inicio. Cada línea guarda una copia del
 * producto y del precio al momento de pedir, para que el vendedor lo vea tal como se pidió aunque
 * luego cambie.
 *
 * @property int $id
 * @property int|null $venta_id
 * @property string $total
 * @property string $nombre
 * @property string|null $celular
 * @property string $metodo_pago
 * @property string|null $codigo_operacion
 * @property string $token
 * @property EstadoPedidoWeb $estado
 */
#[Fillable(['venta_id', 'total', 'nombre', 'celular', 'metodo_pago', 'codigo_operacion', 'token', 'estado'])]
class PedidoWeb extends Model
{
    /** @use HasFactory<PedidoWebFactory> */
    use HasFactory;

    protected $table = 'pedidos_web';

    /**
     * Todo pedido nace con un token secreto para que el cliente abra su comprobante sin iniciar sesión.
     */
    protected static function booted(): void
    {
        static::creating(function (PedidoWeb $pedido): void {
            $pedido->token ??= Str::random(40);
        });
    }

    /**
     * Dirección pública del comprobante (o de la boleta, una vez confirmado el pago).
     */
    public function urlBoleta(): string
    {
        return route('pedidos-web.boleta', $this->token);
    }

    /**
     * @return HasMany<PedidoWebItem, $this>
     */
    public function items(): HasMany
    {
        return $this->hasMany(PedidoWebItem::class);
    }

    /**
     * @return BelongsTo<Venta, $this>
     */
    public function venta(): BelongsTo
    {
        return $this->belongsTo(Venta::class);
    }

    /**
     * Confirma el pedido registrando la venta con su boleta. Descuenta el stock de cada producción
     * (lo hace el ítem de venta al crearse) y deja el pedido ligado a esa venta. Todo o nada: si una
     * línea ya no tiene stock, no se registra nada.
     *
     * @throws RuntimeException si el pedido ya fue atendido o ya no alcanza el stock.
     */
    public function confirmarComoVenta(User $vendedor, ?MetodoPago $metodo = null): Venta
    {
        return DB::transaction(function () use ($vendedor, $metodo): Venta {
            $pedido = static::query()->lockForUpdate()->findOrFail($this->id);
            $metodo ??= MetodoPago::tryFrom($pedido->metodo_pago) ?? MetodoPago::Efectivo;

            if ($pedido->estado !== EstadoPedidoWeb::Pendiente) {
                throw new RuntimeException('Ese pedido ya fue atendido.');
            }

            $items = $pedido->items()->get();

            if ($items->isEmpty()) {
                throw new RuntimeException('Este pedido no tiene productos.');
            }

            // Cantidad pedida por producción (por si el mismo producto aparece en dos líneas).
            $pedidoPorProduccion = $items->groupBy('produccion_id')->map(fn ($lineas) => $lineas->sum('cantidad'));

            foreach ($items as $item) {
                $produccion = $item->produccion_id
                    ? Produccion::query()->lockForUpdate()->find($item->produccion_id)
                    : null;

                if ($produccion === null || $produccion->cantidadDisponible() < $pedidoPorProduccion[$item->produccion_id]) {
                    throw new RuntimeException('Ya no hay suficiente stock de '.$item->producto.' para este pedido.');
                }
            }

            $total = (float) $items->sum('subtotal');
            $enEfectivo = $metodo === MetodoPago::Efectivo;

            $venta = Venta::create([
                'numero_boleta' => Venta::siguienteNumeroBoleta(),
                'cliente_nombre_libre' => $pedido->nombre,
                'vendedor_id' => $vendedor->id,
                'fecha' => now()->toDateString(),
                'subtotal' => $total,
                'descuento' => 0,
                'total' => $total,
                'metodo_pago' => $metodo->value,
                'efectivo_recibido' => $enEfectivo ? $total : null,
                'vuelto' => $enEfectivo ? 0 : null,
            ]);

            foreach ($items as $item) {
                VentaItem::create([
                    'venta_id' => $venta->id,
                    'produccion_id' => $item->produccion_id,
                    'nombre' => $item->producto,
                    'precio_unitario' => $item->precio_unitario,
                    'cantidad' => $item->cantidad,
                    'subtotal' => $item->subtotal,
                ]);
            }

            $pedido->update(['estado' => EstadoPedidoWeb::Confirmado, 'venta_id' => $venta->id]);

            return $venta;
        });
    }

    /**
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'estado' => EstadoPedidoWeb::class,
            'total' => 'decimal:2',
        ];
    }
}

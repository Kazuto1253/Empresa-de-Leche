<?php

namespace App\Models;

use Database\Factories\VentaFactory;
use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

/**
 * @property-read Cliente|null $cliente
 */
#[Fillable([
    'numero_boleta', 'cliente_id', 'cliente_nombre_libre', 'cliente_doc_libre', 'vendedor_id',
    'fecha', 'subtotal', 'descuento', 'descuento_tipo', 'total', 'metodo_pago',
    'efectivo_recibido', 'vuelto',
])]
class Venta extends Model
{
    /** @use HasFactory<VentaFactory> */
    use HasFactory;

    /**
     * @return BelongsTo<Cliente, $this>
     */
    public function cliente(): BelongsTo
    {
        return $this->belongsTo(Cliente::class);
    }

    /**
     * @return BelongsTo<User, $this>
     */
    public function vendedor(): BelongsTo
    {
        return $this->belongsTo(User::class, 'vendedor_id');
    }

    /**
     * @return HasMany<VentaItem, $this>
     */
    public function items(): HasMany
    {
        return $this->hasMany(VentaItem::class);
    }

    /**
     * Siguiente número correlativo de boleta, con formato B001-000123.
     */
    public static function siguienteNumeroBoleta(): string
    {
        $correlativo = static::query()->count() + 1;

        return sprintf('B001-%06d', $correlativo);
    }

    /**
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'fecha' => 'date',
            'subtotal' => 'decimal:2',
            'descuento' => 'decimal:2',
            'total' => 'decimal:2',
            'efectivo_recibido' => 'decimal:2',
            'vuelto' => 'decimal:2',
        ];
    }
}

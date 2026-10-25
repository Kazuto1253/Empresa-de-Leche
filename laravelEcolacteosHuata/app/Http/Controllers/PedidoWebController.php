<?php

namespace App\Http\Controllers;

use App\Enums\EstadoProduccion;
use App\Enums\MetodoPago;
use App\Models\PedidoWeb;
use App\Models\Produccion;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

class PedidoWebController extends Controller
{
    /**
     * Guarda el carrito que el visitante armó en la página de inicio. Los productos y los precios se
     * toman del servidor, nunca de lo que envíe el navegador.
     */
    public function store(Request $request): JsonResponse
    {
        $datos = $request->validate([
            'items' => ['required', 'array', 'min:1', 'max:30'],
            'items.*.produccion_id' => ['required', 'integer', 'distinct'],
            'items.*.cantidad' => ['required', 'integer', 'min:1', 'max:1000'],
            'nombre' => ['required', 'string', 'min:2', 'max:80'],
            'celular' => ['nullable', 'string', 'max:20', 'regex:/^[0-9+\s-]{6,20}$/'],
            'metodo_pago' => ['required', Rule::in([MetodoPago::Yape->value, MetodoPago::Plin->value, MetodoPago::Efectivo->value])],
            'codigo_operacion' => ['nullable', 'required_if:metodo_pago,'.MetodoPago::Yape->value, 'string', 'max:30', 'regex:/^[A-Za-z0-9\s-]{3,30}$/'],
        ], [
            'codigo_operacion.required_if' => 'Después de pagar con Yape, escribe el N.º de operación de tu comprobante.',
            'metodo_pago.required' => 'Elige cómo vas a pagar.',
            'metodo_pago.in' => 'Elige cómo vas a pagar: Yape, Plin o efectivo.',
            'codigo_operacion.regex' => 'El número de operación solo puede llevar letras y números.',
            'items.required' => 'Tu carrito está vacío.',
            'items.min' => 'Tu carrito está vacío.',
            'nombre.required' => 'Escribe tu nombre para que sepamos quién pide.',
            'nombre.min' => 'Escribe tu nombre para que sepamos quién pide.',
            'celular.regex' => 'El celular solo puede llevar números, espacios y +.',
        ]);

        $producciones = Produccion::query()
            ->with('receta.detalles.insumo')
            ->where('estado', EstadoProduccion::Completada)
            ->whereIn('id', collect($datos['items'])->pluck('produccion_id'))
            ->get()
            ->keyBy('id');

        $lineas = [];

        foreach ($datos['items'] as $item) {
            $produccion = $producciones->get($item['produccion_id']);

            if ($produccion === null || $produccion->receta === null) {
                return response()->json(['message' => 'Uno de los productos de tu carrito ya no está disponible. Quítalo e inténtalo de nuevo.'], 422);
            }

            $nombre = $produccion->nombre ?? $produccion->receta->nombre;

            if ($item['cantidad'] > $produccion->cantidadDisponible()) {
                return response()->json(['message' => 'No hay tantas unidades de '.$nombre.'. Baja la cantidad.'], 422);
            }

            $precio = $produccion->receta->precioVenta();

            $lineas[] = [
                'produccion_id' => $produccion->id,
                'producto' => $nombre,
                'cantidad' => $item['cantidad'],
                'precio_unitario' => $precio,
                'subtotal' => round($precio * $item['cantidad'], 2),
            ];
        }

        $pedido = DB::transaction(function () use ($datos, $lineas): PedidoWeb {
            $pedido = PedidoWeb::create([
                'total' => collect($lineas)->sum('subtotal'),
                'nombre' => trim($datos['nombre']),
                'celular' => $datos['celular'] ?? null,
                'metodo_pago' => $datos['metodo_pago'],
                'codigo_operacion' => $datos['metodo_pago'] === MetodoPago::Efectivo->value ? null : ($datos['codigo_operacion'] ?? null),
            ]);

            $pedido->items()->createMany($lineas);

            return $pedido;
        });

        return response()->json([
            'id' => $pedido->id,
            'total' => $pedido->total,
            'metodo_pago' => $pedido->metodo_pago,
            'url_boleta' => $pedido->urlBoleta(),
            'lineas' => collect($lineas)->map(fn (array $l) => ['producto' => $l['producto'], 'cantidad' => $l['cantidad']])->all(),
        ], 201);
    }
}

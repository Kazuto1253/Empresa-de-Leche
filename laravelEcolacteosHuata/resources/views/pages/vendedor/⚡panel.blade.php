<?php

use App\Enums\EstadoProduccion;
use App\Enums\Rol;
use App\Models\Produccion;
use App\Models\Venta;
use Livewire\Component;

new class extends Component
{
    public function with(): array
    {
        $ventasHoy = Venta::query()->whereDate('fecha', now()->toDateString())->get();

        $disponibles = Produccion::query()
            ->where('estado', EstadoProduccion::Completada)
            ->with('receta.detalles.insumo')
            ->get()
            ->map(fn (Produccion $p) => [
                'nombre' => $p->nombre ?? $p->receta->nombre,
                'disponible' => $p->cantidadDisponible(),
                'valor' => $p->cantidadDisponible() * $p->receta->costoTotal(),
            ]);

        return [
            'totalVentasHoy' => $ventasHoy->sum('total'),
            'boletasHoy' => $ventasHoy->count(),
            'produccionHoy' => Produccion::query()->whereDate('fecha', now()->toDateString())->sum('cantidad_producida'),
            'valorInventario' => $disponibles->sum('valor'),
            'productosStockBajo' => $disponibles->filter(fn (array $d) => $d['disponible'] <= 5)->sortBy('disponible')->values(),
            'ultimasVentas' => Venta::query()->with('cliente')->latest('id')->limit(5)->get(),
        ];
    }
};
?>

<div class="flex flex-col gap-6">
    <div>
        <flux:heading size="xl">Panel de control</flux:heading>
        <flux:subheading>Bienvenido, {{ auth()->user()->nombreCompleto() }}.</flux:subheading>
    </div>

    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div class="animate-huata-in rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Ventas de hoy</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">S/ {{ number_format((float) $totalVentasHoy, 2) }}</p>
            <p class="text-xs text-zinc-500">{{ $boletasHoy }} boleta(s)</p>
        </div>
        <div class="animate-huata-in-delay-1 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Producción de hoy</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ number_format((float) $produccionHoy, 2) }}</p>
            <p class="text-xs text-zinc-500">unidades producidas</p>
        </div>
        <div class="animate-huata-in-delay-2 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Valor del inventario</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">S/ {{ number_format((float) $valorInventario, 2) }}</p>
            <p class="text-xs text-zinc-500">al precio de venta</p>
        </div>
        <div class="animate-huata-in-delay-3 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Productos con stock bajo</flux:text>
            <p class="mt-1 text-3xl font-bold {{ $productosStockBajo->isNotEmpty() ? 'text-huata-rojo-600' : 'text-huata-verde-700' }}">
                {{ $productosStockBajo->count() }}
            </p>
            <p class="text-xs text-zinc-500">stock ≤ 5 unidades</p>
        </div>
    </div>

    @if ($productosStockBajo->isNotEmpty())
        <div class="rounded-xl border border-huata-rojo-500 bg-huata-rojo-50 p-4 dark:bg-huata-rojo-500/10">
            <flux:heading size="sm" class="mb-2 text-huata-rojo-700">Stock bajo — reponer pronto</flux:heading>
            <div class="flex flex-wrap gap-2">
                @foreach ($productosStockBajo as $item)
                    <flux:badge color="red">{{ $item['nombre'] }} · {{ number_format($item['disponible'], 2) }}</flux:badge>
                @endforeach
            </div>
        </div>
    @endif

    <div>
        <flux:heading size="lg" class="mb-3">Últimas ventas</flux:heading>
        <div class="max-h-72 overflow-x-auto overflow-y-auto rounded-xl border border-huata-verde-100 dark:border-zinc-700">
            <flux:table>
                <flux:table.columns sticky class="bg-white dark:bg-zinc-800">
                    <flux:table.column>Boleta</flux:table.column>
                    <flux:table.column>Cliente</flux:table.column>
                    <flux:table.column>Total</flux:table.column>
                    <flux:table.column>Fecha</flux:table.column>
                </flux:table.columns>
                <flux:table.rows>
                    @forelse ($ultimasVentas as $venta)
                        <flux:table.row wire:key="uv-{{ $venta->id }}">
                            <flux:table.cell>{{ $venta->numero_boleta }}</flux:table.cell>
                            <flux:table.cell>{{ $venta->cliente?->nombres.' '.$venta->cliente?->apellidos ?: ($venta->cliente_nombre_libre ?: 'Venta anónima') }}</flux:table.cell>
                            <flux:table.cell>S/ {{ number_format((float) $venta->total, 2) }}</flux:table.cell>
                            <flux:table.cell>{{ $venta->fecha->format('d/m/Y') }}</flux:table.cell>
                        </flux:table.row>
                    @empty
                        <flux:table.row><flux:table.cell colspan="4" class="text-center text-zinc-500">Todavía no hay ventas registradas.</flux:table.cell></flux:table.row>
                    @endforelse
                </flux:table.rows>
            </flux:table>
        </div>
    </div>

    <div>
        <flux:heading size="lg" class="mb-3">Accesos rápidos</flux:heading>
        <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            @foreach (auth()->user()->navegacionDeRol(Rol::Vendedor) as $item)
                @if ($item['route'] !== 'vendedor.panel')
                    <flux:button as="a" :href="route($item['route'])" wire:navigate variant="subtle" :icon="$item['icon']" class="justify-start">
                        {{ $item['label'] }}
                    </flux:button>
                @endif
            @endforeach
        </div>
    </div>
</div>

<?php

use App\Enums\EstadoProduccion;
use App\Enums\Rol;
use App\Models\Insumo;
use App\Models\Produccion;
use App\Models\Receta;
use Livewire\Component;

new class extends Component
{
    public function with(): array
    {
        return [
            'totalInsumos' => Insumo::query()->count(),
            'totalRecetas' => Receta::query()->count(),
            'produccionHoy' => Produccion::query()->whereDate('fecha', now()->toDateString())->sum('cantidad_producida'),
            'produccionesPendientes' => Produccion::query()->where('estado', '!=', EstadoProduccion::Completada->value)->count(),
            'insumosStockBajo' => Insumo::query()->with('unidadMedida')->where('stock_actual', '<=', 10)->orderBy('stock_actual')->get(),
            'ultimasProducciones' => Produccion::query()->with('receta')->latest('fecha')->latest('id')->limit(5)->get(),
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
            <flux:text>Producción de hoy</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ number_format((float) $produccionHoy, 2) }}</p>
            <p class="text-xs text-zinc-500">unidades producidas</p>
        </div>
        <div class="animate-huata-in-delay-1 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Producciones pendientes</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ $produccionesPendientes }}</p>
            <p class="text-xs text-zinc-500">planificadas o en proceso</p>
        </div>
        <div class="animate-huata-in-delay-2 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Recetas registradas</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ $totalRecetas }}</p>
        </div>
        <div class="animate-huata-in-delay-3 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Insumos con stock bajo</flux:text>
            <p class="mt-1 text-3xl font-bold {{ $insumosStockBajo->isNotEmpty() ? 'text-huata-rojo-600' : 'text-huata-verde-700' }}">
                {{ $insumosStockBajo->count() }}
            </p>
            <p class="text-xs text-zinc-500">de {{ $totalInsumos }} insumos</p>
        </div>
    </div>

    @if ($insumosStockBajo->isNotEmpty())
        <div class="rounded-xl border border-huata-rojo-500 bg-huata-rojo-50 p-4 dark:bg-huata-rojo-500/10">
            <flux:heading size="sm" class="mb-2 text-huata-rojo-700">Stock bajo — reponer pronto</flux:heading>
            <div class="flex flex-wrap gap-2">
                @foreach ($insumosStockBajo as $insumo)
                    <flux:badge color="red">{{ $insumo->nombre }} · {{ number_format((float) $insumo->stock_actual, 2) }} {{ $insumo->unidadMedida->simbolo }}</flux:badge>
                @endforeach
            </div>
        </div>
    @endif

    <div>
        <flux:heading size="lg" class="mb-3">Últimas producciones</flux:heading>
        <div class="max-h-72 overflow-x-auto overflow-y-auto rounded-xl border border-huata-verde-100 dark:border-zinc-700">
            <flux:table>
                <flux:table.columns sticky class="bg-white dark:bg-zinc-800">
                    <flux:table.column>Fecha</flux:table.column>
                    <flux:table.column>Receta</flux:table.column>
                    <flux:table.column>Lote</flux:table.column>
                    <flux:table.column>Cantidad</flux:table.column>
                    <flux:table.column>Estado</flux:table.column>
                </flux:table.columns>
                <flux:table.rows>
                    @forelse ($ultimasProducciones as $produccion)
                        <flux:table.row wire:key="up-{{ $produccion->id }}">
                            <flux:table.cell>{{ $produccion->fecha->format('d/m/Y') }}</flux:table.cell>
                            <flux:table.cell>{{ $produccion->receta->nombre }}</flux:table.cell>
                            <flux:table.cell>{{ $produccion->lote }}</flux:table.cell>
                            <flux:table.cell>{{ number_format((float) $produccion->cantidad_producida, 2) }}</flux:table.cell>
                            <flux:table.cell>
                                <flux:badge :color="$produccion->estado->value === 'completada' ? 'green' : 'amber'" size="sm">
                                    {{ $produccion->estado->label() }}
                                </flux:badge>
                            </flux:table.cell>
                        </flux:table.row>
                    @empty
                        <flux:table.row><flux:table.cell colspan="5" class="text-center text-zinc-500">Todavía no hay producciones registradas.</flux:table.cell></flux:table.row>
                    @endforelse
                </flux:table.rows>
            </flux:table>
        </div>
    </div>

    <div>
        <flux:heading size="lg" class="mb-3">Accesos rápidos</flux:heading>
        <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            @foreach (auth()->user()->navegacionDeRol(Rol::Produccion) as $item)
                @if ($item['route'] !== 'produccion.panel')
                    <flux:button as="a" :href="route($item['route'])" wire:navigate variant="subtle" :icon="$item['icon']" class="justify-start">
                        {{ $item['label'] }}
                    </flux:button>
                @endif
            @endforeach
        </div>
    </div>
</div>

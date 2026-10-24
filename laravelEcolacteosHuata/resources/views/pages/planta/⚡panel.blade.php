<?php

use App\Enums\EstadoProduccion;
use App\Enums\Rol;
use App\Models\Acopiador;
use App\Models\Produccion;
use App\Models\Proveedor;
use App\Models\Recojo;
use Livewire\Component;

new class extends Component
{
    public function with(): array
    {
        return [
            'litrosHoy' => Recojo::query()->whereDate('fecha', now()->toDateString())->sum('litros'),
            'totalAcopiadores' => Acopiador::query()->count(),
            'totalProveedores' => Proveedor::query()->count(),
            'totalProductos' => Produccion::query()->where('estado', EstadoProduccion::Completada)->count(),
        ];
    }
};
?>

<div class="flex flex-col gap-6">
    <div>
        <flux:heading size="xl">Panel de planta</flux:heading>
        <flux:subheading>Resumen de acopio y producción de hoy.</flux:subheading>
    </div>

    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div class="animate-huata-in rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Litros recibidos hoy</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ number_format((float) $litrosHoy, 2) }} L</p>
        </div>
        <div class="animate-huata-in-delay-1 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Acopiadores</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ $totalAcopiadores }}</p>
        </div>
        <div class="animate-huata-in-delay-2 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Proveedores</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ $totalProveedores }}</p>
        </div>
        <div class="animate-huata-in-delay-3 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Productos activos</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ $totalProductos }}</p>
        </div>
    </div>

    <div>
        <flux:heading size="lg" class="mb-3">Accesos rápidos</flux:heading>
        <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            @foreach (auth()->user()->navegacionDeRol(Rol::PersonalPlanta) as $item)
                @if ($item['route'] !== 'planta.panel')
                    <flux:button as="a" :href="route($item['route'])" wire:navigate variant="subtle" :icon="$item['icon']" class="justify-start">
                        {{ $item['label'] }}
                    </flux:button>
                @endif
            @endforeach
        </div>
    </div>
</div>

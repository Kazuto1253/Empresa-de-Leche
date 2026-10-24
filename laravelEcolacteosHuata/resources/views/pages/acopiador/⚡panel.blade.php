<?php

use App\Enums\Rol;
use App\Models\Recojo;
use Livewire\Component;

new class extends Component
{
    public function with(): array
    {
        $acopiador = auth()->user()->acopiador;

        $recojosHoy = Recojo::query()
            ->where('acopiador_id', $acopiador?->id)
            ->whereDate('fecha', now()->toDateString());

        $proveedoresAsignados = $acopiador?->proveedores()->count() ?? 0;
        $proveedoresRecogidosHoy = (clone $recojosHoy)->distinct('proveedor_id')->count('proveedor_id');

        return [
            'acopiador' => $acopiador,
            'litrosHoy' => (clone $recojosHoy)->sum('litros'),
            'totalProveedores' => $proveedoresAsignados,
            'proveedoresPendientes' => max($proveedoresAsignados - $proveedoresRecogidosHoy, 0),
        ];
    }
};
?>

<div class="flex flex-col gap-6">
    <div>
        <flux:heading size="xl">Panel del acopiador</flux:heading>
        <flux:subheading>{{ $acopiador?->ruta_nombre ?? 'Sin ruta asignada' }}</flux:subheading>
    </div>

    <div class="grid gap-4 sm:grid-cols-3">
        <div class="animate-huata-in rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Litros recogidos hoy</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ number_format((float) $litrosHoy, 2) }} L</p>
        </div>
        <div class="animate-huata-in-delay-1 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Proveedores asignados</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ $totalProveedores }}</p>
        </div>
        <div class="animate-huata-in-delay-2 rounded-xl border border-huata-rojo-500/30 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Faltan por recoger hoy</flux:text>
            <p class="mt-1 text-3xl font-bold {{ $proveedoresPendientes > 0 ? 'text-huata-rojo-600' : 'text-huata-verde-700' }}">
                {{ $proveedoresPendientes }}
            </p>
        </div>
    </div>

    <div>
        <flux:heading size="lg" class="mb-3">Accesos rápidos</flux:heading>
        <div class="grid gap-3 sm:grid-cols-3">
            @foreach (auth()->user()->navegacionDeRol(Rol::Acopiador) as $item)
                @if ($item['route'] !== 'acopiador.panel')
                    <flux:button as="a" :href="route($item['route'])" wire:navigate variant="subtle" :icon="$item['icon']" class="justify-start">
                        {{ $item['label'] }}
                    </flux:button>
                @endif
            @endforeach
        </div>
    </div>
</div>

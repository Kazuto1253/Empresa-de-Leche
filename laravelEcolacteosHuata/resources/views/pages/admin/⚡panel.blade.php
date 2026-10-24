<?php

use App\Enums\Rol;
use App\Models\User;
use Livewire\Component;

new class extends Component
{
    public function with(): array
    {
        return [
            'totalUsuarios' => User::query()->count(),
            'usuariosActivos' => User::query()->where('activo', true)->count(),
            'porRol' => collect(Rol::cases())->map(fn (Rol $rol) => [
                'label' => $rol->label(),
                'total' => User::query()->where('rol', $rol)->count(),
            ]),
        ];
    }
};
?>

<div class="flex flex-col gap-6">
    <div>
        <flux:heading size="xl">Panel del administrador</flux:heading>
        <flux:subheading>Resumen general de Ecolácteos Huata.</flux:subheading>
    </div>

    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div class="animate-huata-in rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Usuarios totales</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ $totalUsuarios }}</p>
        </div>
        <div class="animate-huata-in-delay-1 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Usuarios activos</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ $usuariosActivos }}</p>
        </div>
        @foreach ($porRol->take(2) as $item)
            <div class="animate-huata-in-delay-2 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
                <flux:text>{{ $item['label'] }}</flux:text>
                <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ $item['total'] }}</p>
            </div>
        @endforeach
    </div>

    @foreach (\App\Enums\Rol::Administrador->navegacionAgrupada() as $titulo => $items)
        <div>
            <flux:heading size="lg" class="mb-3">{{ $titulo }}</flux:heading>
            <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                @foreach ($items as $item)
                    @if ($item['route'] !== 'admin.panel')
                        <flux:button
                            as="a"
                            :href="route($item['route'])"
                            wire:navigate
                            variant="subtle"
                            :icon="$item['icon']"
                            class="justify-start"
                        >
                            {{ $item['label'] }}
                        </flux:button>
                    @endif
                @endforeach
            </div>
        </div>
    @endforeach
</div>

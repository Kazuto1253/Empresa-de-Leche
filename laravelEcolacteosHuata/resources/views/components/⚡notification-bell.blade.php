<?php

use Livewire\Attributes\On;
use Livewire\Component;

new class extends Component
{
    public function marcarTodasLeidas(): void
    {
        auth()->user()->unreadNotifications->markAsRead();
    }

    #[On('notificaciones-actualizadas')]
    public function refrescar(): void {}
};
?>

<div>
    @php
        $notificaciones = auth()->user()->notificacionesNoLeidas();
        $total = auth()->user()->unreadNotifications()->count();
    @endphp

    <flux:dropdown position="bottom" align="end">
        <flux:button variant="ghost" square icon="bell" data-test="notification-bell">
            @if ($total > 0)
                <span class="absolute -top-0.5 -right-0.5 flex size-4 items-center justify-center rounded-full bg-huata-rojo-600 text-[10px] font-semibold text-white">
                    {{ $total > 9 ? '9+' : $total }}
                </span>
            @endif
        </flux:button>

        <flux:menu class="w-80">
            <div class="flex items-center justify-between px-2 py-1.5">
                <flux:heading size="sm">Notificaciones</flux:heading>
                @if ($total > 0)
                    <flux:link wire:click="marcarTodasLeidas" class="text-xs cursor-pointer">
                        Marcar todas como leídas
                    </flux:link>
                @endif
            </div>

            <flux:menu.separator />

            @forelse ($notificaciones as $notificacion)
                <div class="flex flex-col gap-0.5 px-2 py-2 text-sm {{ $notificacion->read_at ? 'opacity-60' : '' }}">
                    <span class="font-medium">{{ $notificacion->data['titulo'] ?? 'Notificación' }}</span>
                    @if (! empty($notificacion->data['mensaje']))
                        <span class="text-xs text-zinc-500 dark:text-zinc-400">{{ $notificacion->data['mensaje'] }}</span>
                    @endif
                    <span class="text-xs text-zinc-400">{{ $notificacion->created_at->diffForHumans() }}</span>
                </div>
            @empty
                <div class="px-2 py-4 text-center text-sm text-zinc-500">
                    No tienes notificaciones.
                </div>
            @endforelse
        </flux:menu>
    </flux:dropdown>
</div>

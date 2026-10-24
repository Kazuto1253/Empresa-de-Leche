<!DOCTYPE html>
<html lang="{{ str_replace('_', '-', app()->getLocale()) }}">
    <head>
        @include('partials.head')
    </head>
    <body
        class="relative min-h-screen bg-cover bg-center bg-fixed"
        style="background-image: url('{{ asset('images/fondo-huata.jpg') }}');"
    >
        <div
            class="pointer-events-none fixed inset-0 -z-10 dark:hidden"
            style="background-image: linear-gradient(160deg, rgba(240, 253, 244, .93), rgba(214, 244, 222, .88) 50%, rgba(240, 253, 244, .93));"
        ></div>
        <div
            class="pointer-events-none fixed inset-0 -z-10 hidden dark:block"
            style="background-image: linear-gradient(160deg, rgba(7, 20, 16, .95), rgba(10, 40, 28, .9) 50%, rgba(7, 20, 16, .95));"
        ></div>

        @php
            $usuarioActual = auth()->user();
            $rol = $usuarioActual?->rol;
        @endphp

        <flux:sidebar sticky collapsible="mobile" class="border-e border-huata-verde-100 bg-white dark:border-zinc-700 dark:bg-zinc-900">
            <flux:sidebar.header>
                <x-app-logo :sidebar="true" href="{{ route('dashboard') }}" wire:navigate />
                <flux:sidebar.collapse class="lg:hidden" />
            </flux:sidebar.header>

            <flux:sidebar.nav>
                @php $secciones = $usuarioActual?->navegacionAgrupada(); @endphp
                @if ($secciones)
                    @foreach ($secciones as $titulo => $items)
                        <flux:sidebar.group :heading="$titulo" class="grid">
                            @foreach ($items as $item)
                                <flux:sidebar.item
                                    :icon="$item['icon']"
                                    :href="route($item['route'])"
                                    :current="request()->routeIs($item['route'])"
                                    wire:navigate
                                >
                                    {{ $item['label'] }}
                                </flux:sidebar.item>
                            @endforeach
                        </flux:sidebar.group>
                    @endforeach
                @else
                    <flux:sidebar.group :heading="$rol?->label()" class="grid">
                        @foreach ($rol ? $usuarioActual->navegacionDeRol($rol) : [] as $item)
                            <flux:sidebar.item
                                :icon="$item['icon']"
                                :href="route($item['route'])"
                                :current="request()->routeIs($item['route'])"
                                wire:navigate
                            >
                                {{ $item['label'] }}
                            </flux:sidebar.item>
                        @endforeach
                    </flux:sidebar.group>
                @endif
            </flux:sidebar.nav>

            <flux:spacer />
        </flux:sidebar>

        <flux:header container sticky class="border-b border-huata-verde-100 bg-white dark:border-zinc-700 dark:bg-zinc-900">
            <flux:sidebar.toggle class="lg:hidden mr-2" icon="bars-2" inset="left" />

            <flux:heading size="lg" class="max-lg:hidden">{{ $title ?? config('app.name') }}</flux:heading>

            <flux:spacer />

            <livewire:notification-bell />

            <x-desktop-user-menu />
        </flux:header>

        {{ $slot }}

        @persist('toast')
            <flux:toast.group>
                <flux:toast />
            </flux:toast.group>
        @endpersist

        @fluxScripts
    </body>
</html>

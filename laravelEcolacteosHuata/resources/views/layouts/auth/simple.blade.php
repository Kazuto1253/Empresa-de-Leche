<!DOCTYPE html>
<html lang="{{ str_replace('_', '-', app()->getLocale()) }}">
    <head>
        @include('partials.head')
    </head>
    <body class="min-h-screen antialiased">
        <div
            class="relative flex min-h-svh flex-col items-center justify-center gap-6 bg-huata-verde-950 bg-cover bg-center p-6 md:p-10"
            style="background-image: linear-gradient(160deg, rgba(7, 38, 26, .88), rgba(10, 60, 40, .78) 45%, rgba(4, 30, 45, .85)), url('{{ asset('images/fondo-huata.jpg') }}');"
        >
            <div class="flex w-full max-w-md flex-col gap-6">
                <div class="animate-huata-in flex flex-col items-center gap-3 text-center">
                    <span class="flex size-16 items-center justify-center rounded-full bg-white p-1.5 shadow-lg ring-4 ring-white/20">
                        <x-app-logo-icon class="size-full rounded-full" />
                    </span>
                    <div>
                        <p class="text-xl font-bold tracking-tight text-white">{{ config('app.name') }}</p>
                        <p class="text-sm text-huata-verde-100">Acopio &middot; Producción &middot; Ventas</p>
                    </div>
                </div>

                <div class="animate-huata-in-delay-1 flex flex-col gap-6">
                    <div class="rounded-2xl border border-white/10 bg-white shadow-xl dark:bg-stone-950">
                        <div class="px-8 py-8 sm:px-10">{{ $slot }}</div>
                    </div>
                </div>

                <p class="animate-huata-in-delay-2 text-center text-xs text-huata-verde-100/80">
                    &copy; {{ now()->year }} {{ config('app.name') }} &mdash; Huata, Puno, Perú
                </p>
            </div>
        </div>

        @persist('toast')
            <flux:toast.group>
                <flux:toast />
            </flux:toast.group>
        @endpersist

        @fluxScripts
    </body>
</html>

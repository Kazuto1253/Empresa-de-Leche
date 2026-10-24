<x-layouts::app.sidebar :title="$title ?? null">
    <flux:main class="animate-huata-in">
        {{ $slot }}
    </flux:main>
</x-layouts::app.sidebar>

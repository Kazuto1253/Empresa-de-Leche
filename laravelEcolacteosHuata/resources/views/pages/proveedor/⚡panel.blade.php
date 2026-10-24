<?php

use App\Enums\Rol;
use App\Models\Acopiador;
use App\Models\Proveedor;
use App\Models\Recojo;
use Flux\Flux;
use Livewire\Component;

new class extends Component
{
    public string $finca_ubicacion = '';

    public string $finca_referencia = '';

    public ?float $latitud = null;

    public ?float $longitud = null;

    public ?int $acopiador_id = null;

    public function with(): array
    {
        $proveedor = auth()->user()->proveedor;

        return [
            'proveedor' => $proveedor,
            'entregadoHoy' => Recojo::query()->where('proveedor_id', $proveedor?->id)->whereDate('fecha', now()->toDateString())->sum('litros'),
            'entregadoSemana' => Recojo::query()->where('proveedor_id', $proveedor?->id)->whereBetween('fecha', [now()->startOfWeek()->toDateString(), now()->endOfWeek()->toDateString()])->sum('litros'),
            'acopiadores' => Acopiador::with('user')->get()->sortBy(fn (Acopiador $acopiador) => $acopiador->user->nombreCompleto()),
            'entregadoMes' => Recojo::query()->where('proveedor_id', $proveedor?->id)->whereBetween('fecha', [now()->startOfMonth()->toDateString(), now()->endOfMonth()->toDateString()])->sum('litros'),
        ];
    }

    public function editarUbicacion(): void
    {
        $proveedor = auth()->user()->proveedor;

        $this->finca_ubicacion = $proveedor?->finca_ubicacion ?? '';
        $this->finca_referencia = $proveedor?->finca_referencia ?? '';
        $this->latitud = $proveedor?->latitud ? (float) $proveedor->latitud : null;
        $this->longitud = $proveedor?->longitud ? (float) $proveedor->longitud : null;
        $this->acopiador_id = $proveedor?->acopiador_id;

        $this->resetValidation();
        Flux::modal('ubicacion-form')->show();
        $this->mostrarRutasDelAcopiador();
    }

    public function updatedAcopiadorId(): void
    {
        $this->mostrarRutasDelAcopiador();
    }

    /**
     * Envía al mapa del formulario el camino de las rutas activas del acopiador elegido, para que
     * el proveedor ubique su finca sobre (o cerca de) la ruta por donde recoge.
     */
    private function mostrarRutasDelAcopiador(): void
    {
        $this->dispatch('mapa-ubicacion-rutas',
            lat: $this->latitud,
            lng: $this->longitud,
            rutas: Acopiador::find($this->acopiador_id)?->rutasParaMapa() ?? [],
        );
    }

    public function guardarUbicacion(): void
    {
        $data = $this->validate([
            'finca_ubicacion' => 'required|string|max:255',
            'finca_referencia' => 'nullable|string|max:255',
            'acopiador_id' => 'required|exists:acopiadores,id',
        ]);

        abort_if(! auth()->user()->proveedor, 404);

        $proveedor = Proveedor::guardarElegidoPorProveedor(auth()->id(), [
            'finca_ubicacion' => $data['finca_ubicacion'],
            'finca_referencia' => $data['finca_referencia'] ?? '',
            'latitud' => $this->latitud,
            'longitud' => $this->longitud,
            'acopiador_id' => $data['acopiador_id'],
        ]);

        Flux::modal('ubicacion-form')->close();
        Flux::toast($proveedor->rutas()->exists() ? 'Ubicación guardada. Ya estás en la ruta de tu acopiador.' : 'Ubicación guardada. Tu acopiador aún no tiene ruta: se avisó a planta.', variant: 'success');
    }
};
?>

<div class="flex flex-col gap-6">
    <div>
        <flux:heading size="xl">Panel del proveedor</flux:heading>
        <flux:subheading>Bienvenido, {{ auth()->user()->nombreCompleto() }}.</flux:subheading>
    </div>

    @if ($proveedor?->bloqueado)
        <div class="rounded-xl border border-huata-rojo-500 bg-huata-rojo-50 p-4 text-huata-rojo-700 dark:bg-huata-rojo-500/10">
            <flux:heading size="sm">Tu leche no está siendo recogida</flux:heading>
            <flux:text class="text-huata-rojo-700 dark:text-huata-rojo-400">
                Se detectaron {{ $proveedor->alteraciones }} alteraciones de calidad en tu leche. Comunícate con planta para resolverlo.
            </flux:text>
        </div>
    @endif

    <div class="rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
        <flux:text>Acopiador asignado</flux:text>
        <p class="mt-1 text-xl font-bold text-huata-verde-700">
            {{ $proveedor?->acopiador?->user?->nombreCompleto() ?? 'Sin asignar' }}
        </p>
        @if ($proveedor?->acopiador)
            <p class="text-sm text-zinc-500">{{ $proveedor->acopiador->ruta_nombre }}</p>
        @endif
    </div>

    <div class="rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
        <div class="flex flex-wrap items-center justify-between gap-3">
            <div>
                <flux:text>Ubicación de tu finca</flux:text>
                <p class="mt-1 text-lg font-bold text-huata-verde-700">
                    {{ $proveedor?->finca_ubicacion ?: 'Sin registrar' }}
                </p>
                @if ($proveedor?->finca_referencia)
                    <p class="text-sm text-zinc-500">{{ $proveedor->finca_referencia }}</p>
                @endif
            </div>
            <flux:modal.trigger name="ubicacion-form">
                <flux:button size="sm" variant="primary" icon="map-pin" wire:click="editarUbicacion">
                    Editar ubicación
                </flux:button>
            </flux:modal.trigger>
        </div>
    </div>

    <div class="grid gap-4 sm:grid-cols-3">
        <div class="animate-huata-in rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Entregado hoy</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ number_format((float) $entregadoHoy, 2) }} L</p>
        </div>
        <div class="animate-huata-in-delay-1 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Entregado esta semana</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ number_format((float) $entregadoSemana, 2) }} L</p>
        </div>
        <div class="animate-huata-in-delay-2 rounded-xl border border-huata-verde-100 p-5 dark:border-zinc-700 huata-cuadro">
            <flux:text>Entregado este mes</flux:text>
            <p class="mt-1 text-3xl font-bold text-huata-verde-700">{{ number_format((float) $entregadoMes, 2) }} L</p>
        </div>
    </div>

    <div>
        <flux:heading size="lg" class="mb-3">Accesos rápidos</flux:heading>
        <div class="grid gap-3 sm:grid-cols-3">
            @foreach (auth()->user()->navegacionDeRol(Rol::Proveedor) as $item)
                @if ($item['route'] !== 'proveedor.panel')
                    <flux:button as="a" :href="route($item['route'])" wire:navigate variant="subtle" :icon="$item['icon']" class="justify-start">
                        {{ $item['label'] }}
                    </flux:button>
                @endif
            @endforeach
        </div>
    </div>

    <flux:modal name="ubicacion-form" class="md:w-[28rem]">
        <form wire:submit="guardarUbicacion" class="flex flex-col gap-4">
            <div>
                <flux:heading size="lg">Editar ubicación</flux:heading>
                <flux:subheading>Al guardar, quedarás en la ruta del acopiador que elijas.</flux:subheading>
            </div>
            <flux:input label="Ubicación / dirección" wire:model="finca_ubicacion" placeholder="Comunidad de Huata" />
            <flux:input label="Referencia" wire:model="finca_referencia" placeholder="A 200m de la escuela..." />
            <flux:select label="Acopiador que recoge tu leche" wire:model.live="acopiador_id">
                <flux:select.option value="">Selecciona...</flux:select.option>
                @foreach ($acopiadores as $acopiador)
                    <flux:select.option value="{{ $acopiador->id }}" wire:key="acopiador-{{ $acopiador->id }}">{{ $acopiador->user->nombreCompleto() }}</flux:select.option>
                @endforeach
            </flux:select>
            @if ($acopiador_id)
                <p class="-mt-2 text-xs text-zinc-500">
                    <span class="mr-1 inline-block h-1 w-5 rounded bg-blue-600 align-middle"></span>
                    La línea azul es la ruta por donde recoge este acopiador. Marca tu finca y guarda: quedarás en su ruta automáticamente.
                </p>
            @endif
            <x-mapa-ubicacion :lat="$latitud" :lng="$longitud" :editable="true" lat-model="latitud" lng-model="longitud" height="200px" />
            <div class="flex justify-end gap-2">
                <flux:modal.close><flux:button variant="ghost">Cancelar</flux:button></flux:modal.close>
                <flux:button type="submit" variant="primary">Guardar</flux:button>
            </div>
        </form>
    </flux:modal>
</div>

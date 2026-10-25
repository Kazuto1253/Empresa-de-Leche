<?php

use App\Enums\Rol;
use App\Enums\TeamRole;
use App\Models\Acopiador;
use App\Models\Proveedor;
use App\Models\Team;
use App\Models\User;
use Flux\Flux;
use Illuminate\Support\Facades\Hash;
use Livewire\Attributes\Validate;
use Livewire\Component;
use Livewire\WithPagination;

new class extends Component
{
    use WithPagination;

    public string $busqueda = '';

    public ?int $userId = null;

    public ?int $eliminarId = null;

    #[Validate('required|string|max:255')]
    public string $nombres = '';

    #[Validate('required|string|max:255')]
    public string $apellidos = '';

    #[Validate('required|string|max:15')]
    public string $documento = '';

    #[Validate('nullable|string|max:20')]
    public string $celular = '';

    #[Validate('required|string')]
    public string $rol = '';

    #[Validate('required|string|max:255')]
    public string $usuario = '';

    #[Validate('nullable|string|min:4')]
    public string $password = '';

    public string $finca_ubicacion = '';

    public string $finca_referencia = '';

    public ?float $latitud = null;

    public ?float $longitud = null;

    public ?int $proveedorAcopiadorId = null;

    public bool $activo = true;

    public function with(): array
    {
        return [
            'usuarios' => User::query()
                ->when($this->busqueda, fn ($q) => $q->where(function ($q) {
                    $q->where('nombres', 'like', "%{$this->busqueda}%")
                        ->orWhere('apellidos', 'like', "%{$this->busqueda}%")
                        ->orWhere('usuario', 'like', "%{$this->busqueda}%")
                        ->orWhere('documento', 'like', "%{$this->busqueda}%");
                }))
                ->orderBy('nombres')
                ->paginate(10),
            'roles' => Rol::options(),
            'acopiadores' => Acopiador::with('user')->orderBy('id')->get(),
        ];
    }

    public function nuevo(): void
    {
        $this->reset(['userId', 'nombres', 'apellidos', 'documento', 'celular', 'rol', 'usuario', 'password', 'finca_ubicacion', 'finca_referencia', 'latitud', 'longitud', 'proveedorAcopiadorId']);
        $this->activo = true;
        $this->resetValidation();
        $this->mostrarMapa();
        Flux::modal('usuario-form')->show();
    }

    public function editar(int $id): void
    {
        $user = User::findOrFail($id);
        $this->userId = $user->id;
        $this->nombres = $user->nombres;
        $this->apellidos = $user->apellidos;
        $this->documento = (string) $user->documento;
        $this->celular = (string) $user->celular;
        $this->rol = $user->rol?->value ?? '';
        $this->usuario = (string) $user->usuario;
        $this->password = '';
        $this->activo = $user->activo;

        $proveedor = $user->proveedor;
        $this->finca_ubicacion = $proveedor?->finca_ubicacion ?? '';
        $this->finca_referencia = $proveedor?->finca_referencia ?? '';
        $this->latitud = $proveedor?->latitud ? (float) $proveedor->latitud : null;
        $this->longitud = $proveedor?->longitud ? (float) $proveedor->longitud : null;
        $this->proveedorAcopiadorId = $proveedor?->acopiador_id;

        $this->resetValidation();
        $this->mostrarMapa();
        Flux::modal('usuario-form')->show();
    }

    public function updatedRol(): void
    {
        $this->mostrarMapa();
    }

    public function updatedProveedorAcopiadorId(): void
    {
        $this->mostrarMapa();
    }

    /**
     * El mapa usa wire:ignore y se dibuja una sola vez: sin este evento seguía mostrando el
     * marcador del usuario abierto antes. Le manda la finca actual y, como referencia para
     * ubicarla, el camino de las rutas del acopiador elegido.
     */
    private function mostrarMapa(): void
    {
        $this->dispatch('mapa-ubicacion-rutas',
            lat: $this->latitud,
            lng: $this->longitud,
            rutas: $this->proveedorAcopiadorId ? (Acopiador::find($this->proveedorAcopiadorId)?->rutasParaMapa() ?? []) : [],
        );
    }

    public function guardar(): void
    {
        $rules = [
            'nombres' => 'required|string|max:255',
            'apellidos' => 'required|string|max:255',
            'documento' => 'required|string|max:15|unique:users,documento,'.$this->userId,
            'celular' => 'nullable|string|max:20',
            'rol' => 'required|string',
            'usuario' => 'required|string|max:255|unique:users,usuario,'.$this->userId,
            'password' => $this->userId ? 'nullable|string|min:4' : 'required|string|min:4',
        ];

        if ($this->rol === Rol::Proveedor->value) {
            $rules['finca_ubicacion'] = 'required|string|max:255';
            $rules['proveedorAcopiadorId'] = 'required|exists:acopiadores,id';
        }

        $data = $this->validate($rules);

        $rolEnum = Rol::from($data['rol']);

        $payload = [
            'name' => "{$data['nombres']} {$data['apellidos']}",
            'nombres' => $data['nombres'],
            'apellidos' => $data['apellidos'],
            'documento' => $data['documento'],
            'celular' => $data['celular'] ?? '',
            'rol' => $rolEnum,
            'usuario' => $data['usuario'],
        ];

        if (! empty($data['password'])) {
            $payload['password'] = Hash::make($data['password']);
        }

        $payload['activo'] = $this->activo;

        if ($this->userId) {
            $user = User::findOrFail($this->userId);
            $user->update($payload);
        } else {
            $payload['email'] = $data['usuario'].'@ecolacteoshuata.pe';
            $payload['email_verified_at'] = now();
            $user = User::create($payload);

            $team = Team::principal();
            $team->members()->attach($user, ['role' => TeamRole::Owner->value]);
            $user->switchTeam($team);
        }

        if ($rolEnum === Rol::Acopiador) {
            Acopiador::firstOrCreate(['user_id' => $user->id], ['ruta_nombre' => '']);
        }

        if ($rolEnum === Rol::Proveedor) {
            Proveedor::guardarConUbicacion($user->id, [
                'finca_ubicacion' => $data['finca_ubicacion'] ?? '',
                'finca_referencia' => $this->finca_referencia,
                'latitud' => $this->latitud,
                'longitud' => $this->longitud,
                'acopiador_id' => $data['proveedorAcopiadorId'] ?? null,
            ]);
        }

        Flux::modal('usuario-form')->close();
        Flux::toast('Usuario guardado correctamente.', variant: 'success');
    }

    public function alternarActivo(int $id): void
    {
        $user = User::findOrFail($id);
        $user->update(['activo' => ! $user->activo]);
    }

    public function pedirEliminar(int $id): void
    {
        $this->eliminarId = $id;
        Flux::modal('confirmar-eliminar-usuario')->show();
    }

    public function eliminar(): void
    {
        User::whereKey($this->eliminarId)->delete();

        Flux::modal('confirmar-eliminar-usuario')->close();
        Flux::toast('Usuario eliminado.', variant: 'success');
    }
};
?>

<div class="flex flex-col gap-6">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <x-ui.titulo titulo="Usuarios">Cuentas y roles de acceso al sistema.</x-ui.titulo>

        <flux:modal.trigger name="usuario-form">
            <flux:button variant="primary" icon="plus" wire:click="nuevo">Nuevo usuario</flux:button>
        </flux:modal.trigger>
    </div>

    <flux:input
        wire:model.live.debounce.300ms="busqueda"
        icon="magnifying-glass"
        placeholder="Buscar por nombre, usuario o documento..."
        class="max-w-sm"
    />

    <div class="overflow-x-auto rounded-xl border border-huata-verde-100 px-3 dark:border-zinc-700 huata-cuadro">
    <flux:table :paginate="$usuarios">
        <flux:table.columns class="bg-white/80 dark:bg-zinc-800/80">
            <flux:table.column>Nombre</flux:table.column>
            <flux:table.column>Rol</flux:table.column>
            <flux:table.column>Documento</flux:table.column>
            <flux:table.column>Usuario</flux:table.column>
            <flux:table.column>Estado</flux:table.column>
            <flux:table.column class="w-px whitespace-nowrap">Acciones</flux:table.column>
        </flux:table.columns>

        <flux:table.rows>
            @forelse ($usuarios as $user)
                <flux:table.row wire:key="usuario-{{ $user->id }}">
                    <flux:table.cell class="font-medium">{{ $user->nombreCompleto() }}</flux:table.cell>
                    <flux:table.cell>{{ $user->rol?->label() ?? '—' }}</flux:table.cell>
                    <flux:table.cell>{{ $user->documento }}</flux:table.cell>
                    <flux:table.cell>{{ $user->usuario }}</flux:table.cell>
                    <flux:table.cell>
                        <flux:badge
                            :color="$user->activo ? 'green' : 'zinc'"
                            size="sm"
                            class="cursor-pointer"
                            wire:click="alternarActivo({{ $user->id }})"
                        >
                            {{ $user->activo ? 'Activo' : 'Inactivo' }}
                        </flux:badge>
                    </flux:table.cell>
                    <flux:table.cell>
                        <div class="flex gap-2">
                            <flux:modal.trigger name="usuario-form">
                                <flux:button size="sm" variant="ghost" icon="pencil" wire:click="editar({{ $user->id }})" />
                            </flux:modal.trigger>
                            <flux:modal.trigger name="confirmar-eliminar-usuario">
                                <flux:button size="sm" variant="ghost" icon="trash" wire:click="pedirEliminar({{ $user->id }})" />
                            </flux:modal.trigger>
                        </div>
                    </flux:table.cell>
                </flux:table.row>
            @empty
                <flux:table.row>
                    <flux:table.cell colspan="6" class="text-center text-zinc-500">
                        No hay usuarios registrados todavía.
                    </flux:table.cell>
                </flux:table.row>
            @endforelse
        </flux:table.rows>
    </flux:table>
    </div>

    <flux:modal name="usuario-form" class="md:w-[28rem]">
        <form wire:submit="guardar" class="flex flex-col gap-6">
            <flux:heading size="lg">{{ $userId ? 'Editar usuario' : 'Nuevo usuario' }}</flux:heading>

            <div class="grid grid-cols-2 gap-4">
                <flux:input label="Nombres" wire:model="nombres" />
                <flux:input label="Apellidos" wire:model="apellidos" />
            </div>

            <div class="grid grid-cols-2 gap-4">
                <flux:input label="DNI/RUC" wire:model="documento" />
                <flux:input label="Celular" wire:model="celular" />
            </div>

            <flux:select label="Rol" wire:model.live="rol" placeholder="Selecciona un rol...">
                @foreach ($roles as $r)
                    <flux:select.option value="{{ $r['value'] }}">{{ $r['label'] }}</flux:select.option>
                @endforeach
            </flux:select>

            @if ($rol === \App\Enums\Rol::Proveedor->value)
                <div class="flex flex-col gap-4 rounded-lg border border-huata-verde-100 p-3">
                    <flux:select label="Acopiador" wire:model.live="proveedorAcopiadorId" placeholder="Selecciona un acopiador...">
                        @foreach ($acopiadores as $a)
                            <flux:select.option value="{{ $a->id }}" wire:key="prov-acop-opt-{{ $a->id }}">{{ $a->user->nombreCompleto() }}</flux:select.option>
                        @endforeach
                    </flux:select>
                    <flux:heading size="sm">Ubicación de la finca</flux:heading>
                    <flux:input label="Ubicación / dirección" wire:model="finca_ubicacion" placeholder="Comunidad de Huata" />
                    <flux:input label="Referencia" wire:model="finca_referencia" placeholder="A 200m de la escuela..." />
                    <x-mapa-ubicacion :lat="$latitud" :lng="$longitud" :editable="true" lat-model="latitud" lng-model="longitud" height="200px" />
                </div>
            @endif

            <div class="grid grid-cols-2 gap-4">
                <flux:input label="Usuario" wire:model="usuario" placeholder="usuario de acceso" />
                <flux:input label="Contraseña" type="password" wire:model="password" :placeholder="$userId ? 'Dejar en blanco para no cambiar' : ''" />
            </div>

            <flux:switch label="Usuario activo" wire:model="activo" />

            <div class="flex justify-end gap-2">
                <flux:modal.close>
                    <flux:button variant="ghost">Cancelar</flux:button>
                </flux:modal.close>
                <flux:button type="submit" variant="primary">Guardar</flux:button>
            </div>
        </form>
    </flux:modal>

    <x-confirm-modal
        name="confirmar-eliminar-usuario"
        accion="eliminar"
        titulo="¿Eliminar este usuario?"
        mensaje="Esta acción no se puede deshacer."
    />
</div>

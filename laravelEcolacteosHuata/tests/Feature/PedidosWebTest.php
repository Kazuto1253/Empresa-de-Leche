<?php

use App\Enums\EstadoPedidoWeb;
use App\Enums\EstadoProduccion;
use App\Enums\Rol;
use App\Enums\TeamRole;
use App\Models\Insumo;
use App\Models\PedidoWeb;
use App\Models\Produccion;
use App\Models\Receta;
use App\Models\Team;
use App\Models\UnidadMedida;
use App\Models\User;
use App\Models\Venta;
use Livewire\Livewire;

function produccionPublicada(string $nombre = 'Queso fresco', float $precio = 7.5, float $producida = 5): Produccion
{
    $unidad = UnidadMedida::factory()->create();
    $insumo = Insumo::factory()->create(['unidad_medida_id' => $unidad->id, 'costo_unitario' => 2]);
    $receta = Receta::factory()->create(['nombre' => $nombre, 'precio_venta' => $precio]);
    $receta->detalles()->create(['insumo_id' => $insumo->id, 'cantidad' => 10, 'unidad_medida_id' => $unidad->id]);

    return Produccion::factory()->create([
        'nombre' => $nombre,
        'receta_id' => $receta->id,
        'cantidad_producida' => $producida,
        'estado' => EstadoProduccion::Completada,
    ]);
}

function vendedorConEquipo(): User
{
    $vendedor = User::factory()->rol(Rol::Vendedor)->create();
    $team = Team::principal();
    $team->members()->attach($vendedor, ['role' => TeamRole::Owner->value]);
    $vendedor->switchTeam($team);

    return $vendedor;
}

test('un visitante guarda su carrito con varios productos y los precios del servidor', function () {
    $queso = produccionPublicada('Queso fresco', precio: 7.5);
    $yogurt = produccionPublicada('Yogurt natural', precio: 4);

    $this->postJson(route('pedidos-web.store'), [
        'items' => [
            ['produccion_id' => $queso->id, 'cantidad' => 2, 'precio_unitario' => 0.01],
            ['produccion_id' => $yogurt->id, 'cantidad' => 3],
        ],
        'nombre' => 'María Quispe',
        'celular' => '951 234 567',
        'metodo_pago' => 'yape',
        'codigo_operacion' => 'OP-998877',
        'total' => 1,
    ])->assertCreated()->assertJsonCount(2, 'lineas')->assertJsonStructure(['id', 'total', 'url_boleta']);

    $pedido = PedidoWeb::with('items')->firstOrFail();

    expect((float) $pedido->total)->toBe(27.0)
        ->and($pedido->metodo_pago)->toBe('yape')
        ->and($pedido->codigo_operacion)->toBe('OP-998877')
        ->and($pedido->token)->toHaveLength(40)
        ->and($pedido->estado)->toBe(EstadoPedidoWeb::Pendiente)
        ->and($pedido->items)->toHaveCount(2)
        ->and($pedido->items->firstWhere('producto', 'Queso fresco')->cantidad)->toBe(2)
        ->and((float) $pedido->items->firstWhere('producto', 'Queso fresco')->precio_unitario)->toBe(7.5)
        ->and((float) $pedido->items->firstWhere('producto', 'Yogurt natural')->subtotal)->toBe(12.0);
});

test('no se guarda un carrito vacío, sin nombre, con exceso de stock, repetido o de un producto no publicado', function () {
    $produccion = produccionPublicada(producida: 3);
    $pendiente = produccionPublicada('Mantequilla');
    $pendiente->update(['estado' => EstadoProduccion::Planificada]);

    $linea = ['produccion_id' => $produccion->id, 'cantidad' => 1];

    $this->postJson(route('pedidos-web.store'), ['items' => [], 'nombre' => 'Luis', 'metodo_pago' => 'efectivo'])
        ->assertUnprocessable()->assertJsonValidationErrors('items');

    $this->postJson(route('pedidos-web.store'), ['items' => [$linea], 'nombre' => '', 'metodo_pago' => 'efectivo'])
        ->assertUnprocessable()->assertJsonValidationErrors('nombre');

    $this->postJson(route('pedidos-web.store'), ['items' => [['produccion_id' => $produccion->id, 'cantidad' => 4]], 'nombre' => 'Luis', 'metodo_pago' => 'efectivo'])
        ->assertUnprocessable();

    $this->postJson(route('pedidos-web.store'), ['items' => [$linea, $linea], 'nombre' => 'Luis', 'metodo_pago' => 'efectivo'])
        ->assertUnprocessable()->assertJsonValidationErrors('items.1.produccion_id');

    $this->postJson(route('pedidos-web.store'), ['items' => [$linea, ['produccion_id' => $pendiente->id, 'cantidad' => 1]], 'nombre' => 'Luis', 'metodo_pago' => 'efectivo'])
        ->assertUnprocessable();

    $this->postJson(route('pedidos-web.store'), ['items' => [$linea], 'nombre' => 'Luis', 'metodo_pago' => 'efectivo', 'celular' => 'abc'])
        ->assertUnprocessable()->assertJsonValidationErrors('celular');

    expect(PedidoWeb::count())->toBe(0);
});

test('el vendedor ve los pedidos pendientes y los cancela', function () {
    $vendedor = vendedorConEquipo();

    $uno = PedidoWeb::factory()->conItem(['producto' => 'Yogurt natural'])->create(['nombre' => 'Rosa Mamani']);
    $dos = PedidoWeb::factory()->conItem()->create(['nombre' => 'Pedro Flores']);

    $this->actingAs($vendedor)
        ->get(route('vendedor.pedidos-web'))
        ->assertOk()
        ->assertSee('Rosa Mamani')
        ->assertSee('Yogurt natural');

    Livewire::test('pages::vendedor.pedidos-web')
        ->call('cancelar', $dos->id)
        ->call('pedirConfirmar', $dos->id)
        ->call('confirmar');

    expect($dos->fresh()->estado)->toBe(EstadoPedidoWeb::Cancelado)
        ->and($uno->fresh()->estado)->toBe(EstadoPedidoWeb::Pendiente)
        ->and(Venta::count())->toBe(0);
});

test('confirmar un pedido registra una venta con boleta, todas sus líneas, y descuenta el stock', function () {
    $vendedor = vendedorConEquipo();

    $queso = produccionPublicada('Queso fresco', precio: 7.5, producida: 5);
    $yogurt = produccionPublicada('Yogurt natural', precio: 4, producida: 10);

    $pedido = PedidoWeb::factory()->create(['nombre' => 'Rosa Mamani', 'total' => 27]);
    $pedido->items()->createMany([
        ['produccion_id' => $queso->id, 'producto' => 'Queso fresco', 'cantidad' => 2, 'precio_unitario' => 7.5, 'subtotal' => 15],
        ['produccion_id' => $yogurt->id, 'producto' => 'Yogurt natural', 'cantidad' => 3, 'precio_unitario' => 4, 'subtotal' => 12],
    ]);

    $this->actingAs($vendedor);

    Livewire::test('pages::vendedor.pedidos-web')
        ->call('pedirConfirmar', $pedido->id)
        ->set('metodoPago', 'yape')
        ->call('confirmar');

    $pedido->refresh();
    $venta = Venta::with('items')->firstOrFail();

    expect($pedido->estado)->toBe(EstadoPedidoWeb::Confirmado)
        ->and($pedido->venta_id)->toBe($venta->id)
        ->and($venta->numero_boleta)->toStartWith('B001-')
        ->and($venta->cliente_nombre_libre)->toBe('Rosa Mamani')
        ->and($venta->metodo_pago)->toBe('yape')
        ->and((float) $venta->total)->toBe(27.0)
        ->and($venta->vendedor_id)->toBe($vendedor->id)
        ->and($venta->items)->toHaveCount(2)
        ->and($queso->fresh()->cantidadDisponible())->toBe(3.0)
        ->and($yogurt->fresh()->cantidadDisponible())->toBe(7.0);

    $this->get(route('vendedor.ventas.boleta', $venta->id))->assertOk();

    // Confirmar de nuevo no duplica la venta ni vuelve a descontar.
    Livewire::test('pages::vendedor.pedidos-web')->call('pedirConfirmar', $pedido->id)->call('confirmar');

    expect(Venta::count())->toBe(1)
        ->and($queso->fresh()->cantidadDisponible())->toBe(3.0);
});

test('no se confirma un pedido si una de sus líneas ya no tiene stock, y no se registra nada', function () {
    $vendedor = vendedorConEquipo();

    $queso = produccionPublicada('Queso fresco', producida: 5);
    $yogurt = produccionPublicada('Yogurt natural', producida: 1);

    $pedido = PedidoWeb::factory()->create(['total' => 20]);
    $pedido->items()->createMany([
        ['produccion_id' => $queso->id, 'producto' => 'Queso fresco', 'cantidad' => 2, 'precio_unitario' => 7.5, 'subtotal' => 15],
        ['produccion_id' => $yogurt->id, 'producto' => 'Yogurt natural', 'cantidad' => 3, 'precio_unitario' => 4, 'subtotal' => 12],
    ]);

    $this->actingAs($vendedor);

    Livewire::test('pages::vendedor.pedidos-web')->call('pedirConfirmar', $pedido->id)->call('confirmar');

    expect($pedido->fresh()->estado)->toBe(EstadoPedidoWeb::Pendiente)
        ->and(Venta::count())->toBe(0)
        ->and($queso->fresh()->cantidadDisponible())->toBe(5.0)
        ->and($yogurt->fresh()->cantidadDisponible())->toBe(1.0);
});

test('quienes no son vendedor ni administrador no ven los pedidos de la web', function () {
    $planta = User::factory()->rol(Rol::PersonalPlanta)->create();
    $team = Team::principal();
    $team->members()->attach($planta, ['role' => TeamRole::Owner->value]);
    $planta->switchTeam($team);

    $this->actingAs($planta)->get(route('vendedor.pedidos-web'))->assertForbidden();
});

test('la portada ofrece el carrito cuando hay productos', function () {
    produccionPublicada('Queso fresco', precio: 7.5);

    $this->get(route('home'))
        ->assertOk()
        ->assertSee('id="carrito"', escape: false)
        ->assertSee('Agregar')
        ->assertSee('Tu carrito');
});

test('el pago debe ser Yape, Plin o efectivo', function () {
    $produccion = produccionPublicada();
    $linea = ['produccion_id' => $produccion->id, 'cantidad' => 1];

    $this->postJson(route('pedidos-web.store'), ['items' => [$linea], 'nombre' => 'Luis'])
        ->assertUnprocessable()->assertJsonValidationErrors('metodo_pago');

    $this->postJson(route('pedidos-web.store'), ['items' => [$linea], 'nombre' => 'Luis', 'metodo_pago' => 'tarjeta'])
        ->assertUnprocessable()->assertJsonValidationErrors('metodo_pago');

    $this->postJson(route('pedidos-web.store'), ['items' => [$linea], 'nombre' => 'Luis', 'metodo_pago' => 'efectivo', 'codigo_operacion' => 'ignorado'])
        ->assertCreated();

    expect(PedidoWeb::firstOrFail()->codigo_operacion)->toBeNull();
});

test('el cliente abre su comprobante sin iniciar sesión y luego la boleta oficial', function () {
    $queso = produccionPublicada('Queso fresco', precio: 7.5, producida: 5);

    $url = $this->postJson(route('pedidos-web.store'), [
        'items' => [['produccion_id' => $queso->id, 'cantidad' => 2]],
        'nombre' => 'Rosa Mamani',
        'metodo_pago' => 'plin',
    ])->assertCreated()->json('url_boleta');

    // Visitante sin sesión: comprobante del pedido.
    $this->get($url)->assertOk()->assertHeader('content-type', 'application/pdf');
    expect(PedidoWeb::firstOrFail()->venta_id)->toBeNull();

    // El vendedor confirma el pago (con el método elegido por el cliente) y la misma dirección da la boleta.
    $vendedor = vendedorConEquipo();
    $this->actingAs($vendedor);
    $pedido = PedidoWeb::firstOrFail();

    Livewire::test('pages::vendedor.pedidos-web')->call('pedirConfirmar', $pedido->id)->call('confirmar');

    $venta = Venta::firstOrFail();

    expect($venta->metodo_pago)->toBe('plin')
        ->and($pedido->fresh()->venta_id)->toBe($venta->id);

    auth()->logout();
    $this->get($url)->assertOk()->assertHeader('content-type', 'application/pdf');
    $this->get(route('pedidos-web.boleta', 'token-que-no-existe'))->assertNotFound();
});

test('la portada ofrece pagar con Yape, Plin o efectivo en el carrito', function () {
    produccionPublicada();

    $this->get(route('home'))
        ->assertOk()
        ->assertSee('¿Cómo vas a pagar?')
        ->assertSee('Yape')
        ->assertSee('Plin')
        ->assertSee('Confirmar pedido');
});

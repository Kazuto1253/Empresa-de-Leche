<?php

use App\Http\Middleware\EnsureTeamMembership;
use App\Models\Empresa;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

Route::get('/', fn () => view('welcome', ['empresa' => Empresa::actual()]))->name('home');

Route::prefix('{current_team}')
    ->middleware(['auth', EnsureTeamMembership::class])
    ->group(function () {
        Route::get('dashboard', function (Request $request) {
            $rol = $request->user()->rol;

            abort_if($rol === null, 403, 'Tu usuario todavía no tiene un rol asignado. Contacta al administrador.');

            return redirect()->route("{$rol->routePrefix()}.panel");
        })->name('dashboard');
    });

require __DIR__.'/settings.php';
require __DIR__.'/admin.php';
require __DIR__.'/planta.php';
require __DIR__.'/acopiador.php';
require __DIR__.'/proveedor.php';
require __DIR__.'/vendedor.php';
require __DIR__.'/produccion.php';
require __DIR__.'/asistencia.php';

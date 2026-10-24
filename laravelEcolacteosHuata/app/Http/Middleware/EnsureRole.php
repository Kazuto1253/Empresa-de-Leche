<?php

namespace App\Http\Middleware;

use App\Enums\Rol;
use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsureRole
{
    /**
     * Permite el acceso solo a usuarios cuyo rol de negocio coincide con uno de los indicados.
     *
     * Uso: ->middleware('role:administrador') o ->middleware('role:personal_planta,administrador')
     *
     * @param  Closure(Request): (Response)  $next
     */
    public function handle(Request $request, Closure $next, string ...$roles): Response
    {
        $user = $request->user();

        abort_if($user === null || $user->rol === null, 403);

        $permitidos = array_map(fn (string $valor) => Rol::from($valor), $roles);
        $delUsuario = $user->rolesAsignados();

        abort_unless(collect($permitidos)->contains(fn (Rol $rol) => $delUsuario->contains($rol)), 403);

        return $next($request);
    }
}

<?php

namespace App\Models;

use Database\Factories\RecetaFactory;
use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

#[Fillable(['nombre'])]
class Receta extends Model
{
    /** @use HasFactory<RecetaFactory> */
    use HasFactory;

    /**
     * @return HasMany<RecetaDetalle, $this>
     */
    public function detalles(): HasMany
    {
        return $this->hasMany(RecetaDetalle::class);
    }

    /**
     * @return HasMany<Produccion, $this>
     */
    public function producciones(): HasMany
    {
        return $this->hasMany(Produccion::class);
    }

    /**
     * Costo total de la receta: suma de (cantidad × costo unitario) de cada insumo.
     * Ficha de costeo estilo ejem2.png.
     */
    public function costoTotal(): float
    {
        return $this->detalles
            ->loadMissing('insumo')
            ->sum(fn (RecetaDetalle $detalle) => (float) $detalle->cantidad * (float) $detalle->insumo->costo_unitario);
    }
}

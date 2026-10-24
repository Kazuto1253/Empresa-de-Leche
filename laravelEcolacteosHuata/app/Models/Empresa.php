<?php

namespace App\Models;

use Database\Factories\EmpresaFactory;
use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

#[Fillable(['razon_social', 'ruc', 'telefono', 'direccion'])]
class Empresa extends Model
{
    /** @use HasFactory<EmpresaFactory> */
    use HasFactory;

    protected $table = 'empresa';

    /**
     * La configuración de la empresa es una fila única; la crea con valores por defecto
     * si todavía no existe.
     */
    public static function actual(): self
    {
        return static::query()->firstOrCreate([], [
            'razon_social' => 'Ecolácteos Huata',
            'ruc' => '',
            'telefono' => '',
            'direccion' => 'Huata, Puno, Perú',
        ]);
    }
}

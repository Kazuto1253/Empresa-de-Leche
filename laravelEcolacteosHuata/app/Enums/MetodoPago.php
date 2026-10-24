<?php

namespace App\Enums;

enum MetodoPago: string
{
    case Efectivo = 'efectivo';
    case Yape = 'yape';
    case Plin = 'plin';
    case Tarjeta = 'tarjeta';
    case Otro = 'otro';

    public function label(): string
    {
        return match ($this) {
            self::Efectivo => 'Efectivo',
            self::Yape => 'Yape',
            self::Plin => 'Plin',
            self::Tarjeta => 'Tarjeta',
            self::Otro => 'Otro',
        };
    }

    /**
     * @return array<int, array{value: string, label: string}>
     */
    public static function options(): array
    {
        return collect(self::cases())
            ->map(fn (self $metodo) => ['value' => $metodo->value, 'label' => $metodo->label()])
            ->values()
            ->toArray();
    }
}

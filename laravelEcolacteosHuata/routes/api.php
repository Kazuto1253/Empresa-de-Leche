<?php

use App\Http\Controllers\Api\MobileAuthController;
use App\Http\Controllers\Api\MobileCambiosController;
use App\Http\Controllers\Api\MobileSyncController;
use App\Http\Middleware\AuthenticateMobileToken;
use Illuminate\Support\Facades\Route;

Route::post('/mobile/login', [MobileAuthController::class, 'login'])->middleware('throttle:30,1');
Route::get('/mobile/sync', [MobileSyncController::class, 'sync'])->middleware(AuthenticateMobileToken::class);
Route::post('/mobile/cambios', [MobileCambiosController::class, 'store'])->middleware(AuthenticateMobileToken::class);

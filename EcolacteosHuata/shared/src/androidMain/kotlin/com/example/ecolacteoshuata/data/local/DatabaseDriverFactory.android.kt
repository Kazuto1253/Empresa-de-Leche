package com.example.ecolacteoshuata.data.local

import android.content.Context
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.ecolacteoshuata.db.AppDatabase

private const val NOMBRE_BD = "ecolacteoshuata.db"

actual class DatabaseDriverFactory(private val context: Context) {
    /**
     * Abre la base SQLite (creándola o migrándola si hace falta). Si no se puede abrir —por
     * ejemplo, un teléfono con una versión vieja de la app cuya migración falla— se borra y se
     * crea vacía en vez de cerrar la app: todos los datos vuelven a bajar desde Laravel en la
     * siguiente sincronización.
     */
    actual fun createDriver(): SqlDriver {
        val driver = AndroidSqliteDriver(AppDatabase.Schema, context, NOMBRE_BD)
        return try {
            // La apertura (y la migración) es perezosa: se fuerza aquí para detectar el error ya.
            driver.executeQuery(null, "SELECT 1", { QueryResult.Value(it.next().value) }, 0)
            driver
        } catch (e: Exception) {
            println("DatabaseDriverFactory: no se pudo abrir la base (${e.message}); se recrea vacía")
            runCatching { driver.close() }
            context.deleteDatabase(NOMBRE_BD)
            AndroidSqliteDriver(AppDatabase.Schema, context, NOMBRE_BD)
        }
    }
}

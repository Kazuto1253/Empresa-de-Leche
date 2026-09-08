package pe.gob.huata.ecolactea.server

import java.nio.file.Files
import java.nio.file.Path

/** Literal UTF-8 KEY=value file; no interpolation, shell evaluation or secret logging. */
object ExternalConfig {
    fun load(env: Map<String, String> = System.getenv()): Map<String, String> {
        val file = env["ECOLACTEA_CONFIG_FILE"]?.let(Path::of) ?: return env
        require(Files.isRegularFile(file)) { "External configuration file not found" }
        val values = parse(Files.readString(file))
        return values + env // Explicit environment always wins.
    }

    internal fun parse(text: String): Map<String, String> = buildMap {
        text.removePrefix("\uFEFF").lineSequence().forEachIndexed { index, line ->
            if (line.isBlank() || line.trimStart().startsWith('#')) return@forEachIndexed
            val separator = line.indexOf('=')
            require(separator > 0) { "Invalid configuration at line ${index + 1}" }
            val key = line.substring(0, separator).trim()
            require(key.matches(Regex("[A-Z][A-Z0-9_]*")) && key !in this) { "Invalid or duplicate configuration key at line ${index + 1}" }
            put(key, line.substring(separator + 1))
        }
    }
}

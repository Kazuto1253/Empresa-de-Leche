package pe.gob.huata.ecolactea.server

import java.nio.file.Files
import kotlin.test.*

class ExternalConfigTest {
    @Test fun externalValuesAreLiteralAndEnvironmentWins() {
        val path = Files.createTempFile("ecolactea-config", ".properties")
        try {
            Files.writeString(path, "\uFEFF# local\nDB_USER=local-user\nDB_PASSWORD=synthetic=with#characters\nSERVER_PORT=8081\n")
            val values = ExternalConfig.load(mapOf("ECOLACTEA_CONFIG_FILE" to path.toString(), "DB_USER" to "env-user"))
            assertEquals("env-user", values["DB_USER"])
            assertEquals("synthetic=with#characters", values["DB_PASSWORD"])
            assertEquals("8081", values["SERVER_PORT"])
        } finally { Files.deleteIfExists(path) }
    }
    @Test fun malformedConfigDoesNotExposeValues() {
        val error = assertFailsWith<IllegalArgumentException> { ExternalConfig.parse("DB_PASSWORD=private\nDB_PASSWORD=private") }
        assertFalse(error.message.orEmpty().contains("private"))
    }
}

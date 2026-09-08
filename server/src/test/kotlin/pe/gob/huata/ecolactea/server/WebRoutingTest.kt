package pe.gob.huata.ecolactea.server

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import java.nio.file.Files
import kotlin.test.*

class WebRoutingTest {
    @Test fun webAssetsAndApiKeepSeparateRoutes() = testApplication {
        val root = Files.createTempDirectory("ecolactea-web")
        Files.writeString(root.resolve("index.html"), "<html>Ecoláctea test</html>")
        try {
            application { module(ServerConfig("127.0.0.1", 0, null, webRoot = root.toString())) }
            assertEquals(HttpStatusCode.OK, client.get("/").status)
            assertTrue(client.get("/").bodyAsText().contains("Ecoláctea test"))
            assertEquals(HttpStatusCode.OK, client.get("/health").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/missing").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/missing.js").status)
        } finally { Files.deleteIfExists(root.resolve("index.html")); Files.deleteIfExists(root) }
    }
}

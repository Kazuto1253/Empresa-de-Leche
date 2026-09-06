package pe.gob.huata.ecolactea.server

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun healthEndpointDoesNotRequireDatabase() = testApplication {
        application {
            module(
                ServerConfig(
                    host = "127.0.0.1",
                    port = 0,
                    database = null,
                ),
            )
        }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"status":"ok","service":"ecolactea-server"}""", response.bodyAsText())
    }
}

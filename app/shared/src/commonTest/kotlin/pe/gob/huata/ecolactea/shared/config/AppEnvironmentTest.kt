package pe.gob.huata.ecolactea.shared.config

import kotlin.test.Test
import kotlin.test.assertEquals

class AppEnvironmentTest {
    @Test
    fun localDevelopmentUsesConfigurableServerBaseUrl() {
        assertEquals("http://localhost:8080", AppEnvironment.LocalDevelopment.apiBaseUrl)
    }
}

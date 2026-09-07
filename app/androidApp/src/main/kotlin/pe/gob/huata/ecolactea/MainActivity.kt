package pe.gob.huata.ecolactea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pe.gob.huata.ecolactea.shared.auth.AndroidSessionStore
import pe.gob.huata.ecolactea.shared.config.AppEnvironment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val sessionStore = AndroidSessionStore(applicationContext)
        setContent {
            App(sessionStore, AppEnvironment(BuildConfig.API_BASE_URL))
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

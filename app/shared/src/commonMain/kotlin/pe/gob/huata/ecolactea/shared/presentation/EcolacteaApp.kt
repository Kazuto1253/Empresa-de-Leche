package pe.gob.huata.ecolactea.shared.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EcolacteaApp(controller: AuthController) {
    val state by controller.state.collectAsState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(controller) { controller.restore() }
    LaunchedEffect(state is AuthState.Authorized) {
        while (state is AuthState.Authorized) { delay(60_000); controller.restore() }
    }
    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF226343), background = Color(0xFFF7F9F6))) {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding().imePadding().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(Modifier.widthIn(max = 440.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text("Ecoláctea Digital", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold, color = Color(0xFF18392B))
                    Text("Planta municipal de Huata", color = Color(0xFF53665B))
                    when (val current = state) {
                        AuthState.Starting -> { CircularProgressIndicator(); Text("Comprobando tu sesión…") }
                        is AuthState.Login -> LoginForm(current) { username, password ->
                            scope.launch { controller.login(username, password) }
                        }
                        is AuthState.Unavailable -> {
                            Text(current.error.message(), color = MaterialTheme.colorScheme.error)
                            Button(onClick = { scope.launch { controller.restore() } }) { Text("Reintentar") }
                            TextButton(onClick = { scope.launch { controller.logout() } }) { Text("Cerrar sesión local") }
                        }
                        is AuthState.Authorized -> {
                            Text(RoleDestination.forRole(current.user.role).title, style = MaterialTheme.typography.headlineSmall)
                            Text("Bienvenido, ${current.user.username}")
                            Card(Modifier.fillMaxWidth()) {
                                Text("Tu sesión está activa. Las funciones de esta área se incorporarán en próximos incrementos.", Modifier.padding(20.dp))
                            }
                            Button(enabled = !current.signingOut, onClick = { scope.launch { controller.logout() } }) {
                                Text(if (current.signingOut) "Cerrando sesión…" else "Cerrar sesión")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginForm(state: AuthState.Login, onLogin: (String, String) -> Unit) {
    // Credentials never enter saved instance state.
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    Text("Inicia sesión", style = MaterialTheme.typography.headlineSmall)
    OutlinedTextField(username, { username = it.take(100) }, label = { Text("Usuario") },
        modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !state.loading)
    OutlinedTextField(password, { password = it.take(256) }, label = { Text("Contraseña") },
        modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !state.loading,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = { TextButton(onClick = { visible = !visible }) { Text(if (visible) "Ocultar" else "Mostrar") } })
    state.error?.let { Text(it.message(), color = MaterialTheme.colorScheme.error) }
    Button(modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = !state.loading,
        onClick = { val submitted = password; password = ""; visible = false; onLogin(username, submitted) }) {
        if (state.loading) { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp); Spacer(Modifier.width(12.dp)) }
        Text(if (state.loading) "Ingresando…" else "Ingresar")
    }
    Text("Si necesitas acceso o ayuda con tu cuenta, contacta al administrador.", style = MaterialTheme.typography.bodySmall)
}

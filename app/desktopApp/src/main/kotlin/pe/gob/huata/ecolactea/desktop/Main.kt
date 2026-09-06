package pe.gob.huata.ecolactea.desktop

import pe.gob.huata.ecolactea.App
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Ecoláctea Digital",
    ) {
        App()
    }
}

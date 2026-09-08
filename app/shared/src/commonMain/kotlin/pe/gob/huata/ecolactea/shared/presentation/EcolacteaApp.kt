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
import io.ktor.client.HttpClient
import pe.gob.huata.ecolactea.core.application.AppResult
import pe.gob.huata.ecolactea.core.network.ProviderCreateCommand
import pe.gob.huata.ecolactea.core.network.ProviderListItem
import pe.gob.huata.ecolactea.core.network.OperationalItem
import pe.gob.huata.ecolactea.core.network.*
import pe.gob.huata.ecolactea.shared.network.OperationsRepository
import pe.gob.huata.ecolactea.core.application.sync.OfflineCollectionStore
import pe.gob.huata.ecolactea.shared.sync.CollectionSubmissionService

@Composable
fun EcolacteaApp(controller: AuthController, client: HttpClient, offlineStore: OfflineCollectionStore? = null, syncPending: (suspend () -> AppResult<Int>)? = null) {
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
                            OperationsPanel(controller, client)
                            CollectionPanel(controller, client, current.user.role.name, offlineStore, syncPending)
                            QualityPanel(controller, client, current.user.role.name)
                            FinancePanel(controller, client, current.user.role.name)
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
private fun CollectionPanel(controller: AuthController, client: HttpClient, role: String, offlineStore: OfflineCollectionStore?, syncPending: (suspend () -> AppResult<Int>)?) {
    val scope = rememberCoroutineScope()
    val repository = remember(client) { OperationsRepository(client) }
    val submission = remember(repository, offlineStore) { CollectionSubmissionService(repository, offlineStore) }
    var providerId by remember { mutableStateOf("") }
    var routeId by remember { mutableStateOf("") }
    var workdayId by remember { mutableStateOf("") }
    var liters by remember { mutableStateOf("") }
    var occurredAt by remember { mutableStateOf("") }
    var operationId by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var collections by remember { mutableStateOf<List<CollectionRecord>>(emptyList()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Acopio y recepción", style = MaterialTheme.typography.titleLarge)
        if (role == "ACOPIADOR" || role == "ADMINISTRADOR_GENERAL") {
            OutlinedTextField(providerId, { providerId = it }, label = { Text("Proveedor UUID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(routeId, { routeId = it }, label = { Text("Ruta UUID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(workdayId, { workdayId = it }, label = { Text("Jornada UUID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(liters, { liters = it }, label = { Text("Litros") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(occurredAt, { occurredAt = it }, label = { Text("Fecha/hora ISO-8601") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(operationId, { operationId = it }, label = { Text("Client operation UUID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { scope.launch { controller.currentSession()?.let { session ->
                when (val result = submission.submit(session.token.accessToken, CollectionEntryCommand(providerId, routeId, workdayId, occurredAt, liters, clientOperationId = operationId))) {
                    is AppResult.Success -> message = "Acopio guardado"
                    is AppResult.Failure -> message = if (result.error is pe.gob.huata.ecolactea.core.application.AppError.Offline && offlineStore != null) "Acopio guardado como pendiente" else result.error.message()
                }
            } } }) { Text("Registrar acopio") }
            if(syncPending!=null) Button(onClick={scope.launch{message=when(val result=syncPending()){is AppResult.Success->"${result.value} pendientes sincronizados";is AppResult.Failure->result.error.message()}}}){Text("Sincronizar pendientes")}
        }
        if (role == "PERSONAL_PLANTA" || role == "ADMINISTRADOR_GENERAL") {
            Button(onClick = { scope.launch { controller.currentSession()?.let { session ->
                when (val result = repository.createDirectDelivery(session.token.accessToken, DirectDeliveryCommand(providerId, occurredAt, liters, operationId))) {
                    is AppResult.Success -> message = "Entrega directa guardada"
                    is AppResult.Failure -> message = result.error.message()
                }
            } } }) { Text("Registrar entrega directa") }
            Button(onClick = { scope.launch { controller.currentSession()?.let { session ->
                when (val result = repository.createReception(session.token.accessToken, PlantReceptionCommand(workdayId = workdayId, routeId = routeId, receivedAt = occurredAt, litersReceived = liters, measurementSource = "MANUAL", clientOperationId = operationId))) {
                    is AppResult.Success -> message = "Recepción guardada"
                    is AppResult.Failure -> message = result.error.message()
                }
            } } }) { Text("Registrar recepción") }
        }
        Button(onClick = { scope.launch { controller.currentSession()?.let { session ->
            when (val result = repository.collections(session.token.accessToken)) {
                is AppResult.Success -> { collections = result.value; message = "Acopios consultados" }
                is AppResult.Failure -> message = result.error.message()
            }
        } } }) { Text("Cargar acopios") }
        Text(message)
        collections.forEach { item -> Text("${item.occurredAt}  ${item.liters} L  ${item.status}") }
    }
}

@Composable
private fun QualityPanel(controller: AuthController, client: HttpClient, role: String) {
    val scope = rememberCoroutineScope()
    val repository = remember(client) { OperationsRepository(client) }
    var providerId by remember { mutableStateOf("") }
    var sourceType by remember { mutableStateOf("MILK_COLLECTION") }
    var sourceId by remember { mutableStateOf("") }
    var analyzedAt by remember { mutableStateOf("") }
    var parameterKey by remember { mutableStateOf("") }
    var parameterValue by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var analyses by remember { mutableStateOf<List<QualityListItem>>(emptyList()) }
    var incidents by remember { mutableStateOf<List<QualityListItem>>(emptyList()) }
    var events by remember { mutableStateOf<List<QualityListItem>>(emptyList()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Calidad y comunicaciones", style = MaterialTheme.typography.titleLarge)
        if (role == "ADMINISTRADOR_GENERAL" || role == "PERSONAL_PLANTA") {
            OutlinedTextField(providerId, { providerId = it }, label = { Text("Proveedor UUID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sourceType, { sourceType = it }, label = { Text("Fuente MILK_COLLECTION/DIRECT_DELIVERY") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sourceId, { sourceId = it }, label = { Text("Fuente UUID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(analyzedAt, { analyzedAt = it }, label = { Text("Fecha/hora ISO-8601") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { scope.launch { controller.currentSession()?.let { s ->
                when (val result = repository.createQualityAnalysis(s.token.accessToken, QualityAnalysisCommand(providerId, sourceType, sourceId, analyzedAt = analyzedAt))) {
                    is AppResult.Success -> message = "Análisis registrado"
                    is AppResult.Failure -> message = result.error.message()
                }
            } } }) { Text("Registrar análisis") }
            OutlinedTextField(parameterKey, { parameterKey = it }, label = { Text("Clave de parámetro") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(parameterValue, { parameterValue = it }, label = { Text("Valor medido") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { scope.launch { controller.currentSession()?.let { s ->
                when (val result = repository.createQualityResult(s.token.accessToken, sourceId, QualityResultCommand(parameterKey, decimalValue = parameterValue))) {
                    is AppResult.Success -> message = "Resultado registrado"
                    is AppResult.Failure -> message = result.error.message()
                }
            } } }) { Text("Registrar resultado") }
        }
        Button(onClick = { scope.launch { controller.currentSession()?.let { s ->
            val a = repository.qualityAnalyses(s.token.accessToken)
            val i = repository.qualityIncidents(s.token.accessToken)
            val e = repository.communicationEvents(s.token.accessToken)
            if (a is AppResult.Success) analyses = a.value
            if (i is AppResult.Success) incidents = i.value
            if (e is AppResult.Success) events = e.value
            message = "Información consultada"
        } } }) { Text("Cargar calidad y eventos") }
        Text(message)
        analyses.forEach { Text("Análisis ${it.id}: ${it.name} ${it.status}") }
        incidents.forEach { Text("Incidencia ${it.id}: ${it.name} ${it.status}") }
        events.forEach { Text("Evento ${it.id}: ${it.name} ${it.status}") }
    }
}

@Composable
private fun FinancePanel(controller: AuthController, client: HttpClient, role: String) {
    val scope=rememberCoroutineScope();val repository=remember(client){OperationsRepository(client)}
    var id1 by remember{mutableStateOf("")};var id2 by remember{mutableStateOf("")};var value1 by remember{mutableStateOf("")};var value2 by remember{mutableStateOf("")};var date1 by remember{mutableStateOf("")};var date2 by remember{mutableStateOf("")};var currency by remember{mutableStateOf("")};var option by remember{mutableStateOf("")};var inputUnit by remember{mutableStateOf("")};var reason by remember{mutableStateOf("")};var message by remember{mutableStateOf("")};var rows by remember{mutableStateOf<List<FinancialRecord>>(emptyList())};var envelope by remember{mutableStateOf<PaymentEnvelope?>(null)}
    fun show(result:AppResult<FinancialRecord>,success:String){message=if(result is AppResult.Success)success else (result as AppResult.Failure).error.message()}
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text("Finanzas, producción y ventas",style=MaterialTheme.typography.titleLarge)
        if(role=="ADMINISTRADOR_GENERAL"||role=="ACOPIADOR"){
            OutlinedTextField(value1,{value1=it},label={Text("Precio por litro")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(currency,{currency=it},label={Text("Moneda")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(date1,{date1=it},label={Text("Vigencia desde YYYY-MM-DD")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(reason,{reason=it},label={Text("Motivo")},modifier=Modifier.fillMaxWidth())
            Button(onClick={scope.launch{controller.currentSession()?.let{s->show(repository.createMilkPrice(s.token.accessToken,MilkPriceCommand(amount=value1,currency=currency,effectiveFrom=date1,reason=reason)),"Precio por litro registrado")}}}){Text("Publicar precio de leche")}
        }
        if(role=="ADMINISTRADOR_GENERAL"||role=="PERSONAL_PLANTA"){
            OutlinedTextField(id1,{id1=it},label={Text("ID principal / proveedor / cliente / producto")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(id2,{id2=it},label={Text("ID secundario / cierre / liquidación")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value1,{value1=it},label={Text("Importe / cantidad de entrada")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value2,{value2=it},label={Text("Cantidad de salida")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(date1,{date1=it},label={Text("Fecha inicial / fecha-hora ISO")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(date2,{date2=it},label={Text("Fecha final")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(currency,{currency=it},label={Text("Moneda")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(option,{option=it},label={Text("Método / tipo cliente / unidad salida")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(inputUnit,{inputUnit=it},label={Text("Unidad de entrada")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(reason,{reason=it},label={Text("Motivo")},modifier=Modifier.fillMaxWidth())
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                Button(onClick={scope.launch{controller.currentSession()?.let{s->show(repository.closeWeek(s.token.accessToken,WeeklyClosureCommand(date1,date2)),"Semana cerrada")}}}){Text("Cerrar semana")}
            }
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                Button(onClick={scope.launch{controller.currentSession()?.let{s->show(repository.calculateSettlement(s.token.accessToken,SettlementCommand(id1,id2)),"Liquidación calculada")}}}){Text("Liquidar")}
                Button(onClick={option="CASH"}){Text("Efectivo")}
                Button(onClick={option="YAPE"}){Text("Yape")}
                Button(onClick={option="TRANSFER"}){Text("Transferencia")}
                Button(onClick={scope.launch{controller.currentSession()?.let{s->show(repository.registerPayment(s.token.accessToken,PaymentCommand(id2,value1,currency,date1,option)),"Pago registrado")}}}){Text("Registrar pago")}
            }
            Button(onClick={scope.launch{controller.currentSession()?.let{s->show(repository.createProductionBatch(s.token.accessToken,ProductionBatchCommand(id2,date1,inputQuantity=value1,inputUnit=inputUnit,productId=id1,outputQuantity=value2,outputUnit=option)),"Lote registrado")}}}){Text("Registrar lote")}
            Button(onClick={scope.launch{controller.currentSession()?.let{s->show(repository.createSale(s.token.accessToken,SaleCommand(id2,date1,currency,listOf(SaleItemCommand(id1,value2,option)))),"Venta registrada")}}}){Text("Registrar venta")}
        }
        Button(onClick={scope.launch{controller.currentSession()?.let{s->val settlements=repository.settlements(s.token.accessToken);rows=if(settlements is AppResult.Success)settlements.value else emptyList();message="Consulta completada"}}}){Text("Cargar liquidaciones")}
        Button(onClick={scope.launch{controller.currentSession()?.let{s->val detail=repository.paymentEnvelope(s.token.accessToken,id2);if(detail is AppResult.Success){envelope=detail.value;val payments=repository.payments(s.token.accessToken,id2);rows=if(payments is AppResult.Success)payments.value else emptyList();message="Liquidación y pagos cargados"}else message=(detail as AppResult.Failure).error.message()}}}){Text("Ver liquidación y pagos")}
        Button(onClick={scope.launch{controller.currentSession()?.let{s->val report=repository.financialReport(s.token.accessToken);rows=if(report is AppResult.Success)report.value else emptyList();message="Reporte financiero cargado"}}}){Text("Reporte financiero")}
        envelope?.let{Text("Total: ${it.netAmount} ${it.currency} | Pagado: ${it.totalPaid} | Saldo: ${it.pendingBalance} | ${it.status}")}
        Text(message);rows.forEach{Text("${it.name}  ${it.amount}  ${it.status}")}
    }
}

@Composable
private fun OperationsPanel(controller: AuthController, client: HttpClient) {
    val scope = rememberCoroutineScope()
    val repository = remember(client) { OperationsRepository(client) }
    var providers by remember { mutableStateOf<List<ProviderListItem>>(emptyList()) }
    var message by remember { mutableStateOf("Pulsa cargar para consultar el padrón") }
    var dni by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedResource by remember { mutableStateOf<String?>(null) }
    var resourceItems by remember { mutableStateOf<List<OperationalItem>>(emptyList()) }
    val resources = listOf("Zonas" to "/api/v1/zones", "Rutas" to "/api/v1/routes", "Unidades" to "/api/v1/collection-units", "Jornadas" to "/api/v1/workdays", "Productos" to "/api/v1/catalog/products", "Clientes" to "/api/v1/catalog/customers", "Precios" to "/api/v1/catalog/prices", "Parámetros" to "/api/v1/parameters")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Padrón de proveedores", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(dni, { dni = it }, label = { Text("DNI") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(firstName, { firstName = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(lastName, { lastName = it }, label = { Text("Apellido") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(phone, { phone = it }, label = { Text("Celular") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { scope.launch { controller.currentSession()?.let { session ->
                when (val result = repository.createProvider(session.token.accessToken, ProviderCreateCommand(dni, firstName, lastName, phone))) {
                    is AppResult.Success -> {
                        when (val refreshed = repository.providers(session.token.accessToken)) {
                            is AppResult.Success -> {
                                providers = refreshed.value
                                message = "Proveedor guardado y lista actualizada"
                            }
                            is AppResult.Failure -> {
                                providers = providers + result.value
                                message = "Proveedor guardado; no se pudo actualizar la lista"
                            }
                        }
                    }
                    is AppResult.Failure -> message = result.error.message()
                }
            } } }) { Text("Guardar") }
            OutlinedButton(onClick = { scope.launch { controller.currentSession()?.let { session ->
                when (val result = repository.providers(session.token.accessToken)) {
                    is AppResult.Success -> { providers = result.value; message = "Consulta completada" }
                    is AppResult.Failure -> message = result.error.message()
                }
            } } }) { Text("Cargar") }
        }
        Text(message)
        providers.forEach { provider -> Text("${provider.dni}  ${provider.firstName} ${provider.lastName}  ${provider.status}") }
        Text("Secciones operativas", style = MaterialTheme.typography.titleMedium)
        resources.forEach { (label, path) ->
            OutlinedButton(onClick = { scope.launch { controller.currentSession()?.let { session ->
                when (val result = repository.items(session.token.accessToken, path)) {
                    is AppResult.Success -> { selectedResource = label; resourceItems = result.value; message = "$label consultado" }
                    is AppResult.Failure -> message = result.error.message()
                }
            } } }) { Text("Cargar $label") }
        }
        ResourceCreateForm(selectedResource, controller, repository) { text -> message = text }
        selectedResource?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        resourceItems.forEach { item -> Text("${item.code}  ${item.name}  ${item.status}") }
    }
}

@Composable
private fun ResourceCreateForm(resource: String?, controller: AuthController, repository: OperationsRepository, onMessage: (String) -> Unit) {
    if (resource == null || resource == "Precios") return
    val scope = rememberCoroutineScope()
    var code by remember(resource) { mutableStateOf("") }
    var name by remember(resource) { mutableStateOf("") }
    var extra by remember(resource) { mutableStateOf("") }
    var extra2 by remember(resource) { mutableStateOf("") }
    var extra3 by remember(resource) { mutableStateOf("") }
    var extra4 by remember(resource) { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Crear $resource", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(code, { code = it }, label = { Text("Código / clave") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(name, { name = it }, label = { Text("Nombre / descripción") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (resource == "Rutas") OutlinedTextField(extra, { extra = it }, label = { Text("Zone ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (resource == "Jornadas") {
            OutlinedTextField(extra, { extra = it }, label = { Text("Route ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(extra2, { extra2 = it }, label = { Text("Unit ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(extra3, { extra3 = it }, label = { Text("Fecha YYYY-MM-DD") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(extra4, { extra4 = it }, label = { Text("Inicio-Fin HH:MM") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        if (resource == "Productos") {
            OutlinedTextField(extra, { extra = it }, label = { Text("Categoría") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(extra2, { extra2 = it }, label = { Text("Unidad") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        if (resource == "Clientes") OutlinedTextField(extra, { extra = it }, label = { Text("Tipo WHOLESALE/PROVIDER/PUBLIC") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (resource == "Parámetros") {
            OutlinedTextField(extra, { extra = it }, label = { Text("Valor") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(extra2, { extra2 = it }, label = { Text("Tipo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(extra3, { extra3 = it }, label = { Text("Vigencia YYYY-MM-DDTHH:MM:SS") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        Button(onClick = { scope.launch { controller.currentSession()?.let { session ->
            val result = when (resource) {
                "Zonas" -> repository.createZone(session.token.accessToken, ZoneCommand(code, name))
                "Rutas" -> repository.createRoute(session.token.accessToken, RouteCommand(code, name, extra, validFrom = extra2))
                "Unidades" -> repository.createUnit(session.token.accessToken, UnitCommand(code, description = name))
                "Jornadas" -> repository.createWorkday(session.token.accessToken, WorkdayCommand(code, extra, extra2, extra3, extra4.substringBefore('-'), extra4.substringAfter('-', "")))
                "Productos" -> repository.createProduct(session.token.accessToken, ProductCommand(code, name, extra, extra2))
                "Clientes" -> repository.createCustomer(session.token.accessToken, CustomerCommand(code, name, customerType = extra))
                "Parámetros" -> repository.createParameter(session.token.accessToken, ParameterCommand(code, extra, extra2, effectiveFrom = extra3))
                else -> null
            }
            onMessage(if (result is AppResult.Success) "$resource guardado" else "No se pudo guardar $resource")
        } } }) { Text("Guardar") }
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

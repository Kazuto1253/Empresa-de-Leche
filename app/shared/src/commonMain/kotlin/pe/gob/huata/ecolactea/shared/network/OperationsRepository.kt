package pe.gob.huata.ecolactea.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.gob.huata.ecolactea.core.application.AppError
import pe.gob.huata.ecolactea.core.application.AppResult
import pe.gob.huata.ecolactea.core.network.*

class OperationsRepository(private val client: HttpClient) {
    suspend fun providers(token: String): AppResult<List<ProviderListItem>> = request { client.get("/api/v1/providers") { bearerAuth(token) } }
    suspend fun createProvider(token: String, command: ProviderCreateCommand): AppResult<ProviderListItem> = request { client.post("/api/v1/providers") { bearerAuth(token); contentType(ContentType.Application.Json); setBody(command) } }
    suspend fun items(token: String, path: String): AppResult<List<OperationalItem>> = request { client.get(path) { bearerAuth(token) } }
    suspend fun users(token: String): AppResult<List<UserItem>> = request { client.get("/api/v1/users") { bearerAuth(token) } }
    suspend fun createUnit(token: String, value: UnitCommand) = mutate<UnitCommand, OperationalItem>(token, "/api/v1/collection-units", value)
    suspend fun createWorkday(token: String, value: WorkdayCommand) = mutate<WorkdayCommand, OperationalItem>(token, "/api/v1/workdays", value)
    suspend fun createProduct(token: String, value: ProductCommand) = mutate<ProductCommand, OperationalItem>(token, "/api/v1/catalog/products", value)
    suspend fun createCustomer(token: String, value: CustomerCommand) = mutate<CustomerCommand, OperationalItem>(token, "/api/v1/catalog/customers", value)
    suspend fun createPrice(token: String, value: PriceCommand) = mutate<PriceCommand, OperationalItem>(token, "/api/v1/catalog/prices", value)
    suspend fun createParameter(token: String, value: ParameterCommand) = mutate<ParameterCommand, OperationalItem>(token, "/api/v1/parameters", value)
    suspend fun collections(token: String) = request<List<CollectionRecord>> { client.get("/api/v1/collections") { bearerAuth(token) } }
    suspend fun createCollection(token: String, value: CollectionEntryCommand) = mutate<CollectionEntryCommand, CollectionRecord>(token, "/api/v1/collections", value)
    suspend fun createDirectDelivery(token: String, value: DirectDeliveryCommand) = mutate<DirectDeliveryCommand, CollectionRecord>(token, "/api/v1/direct-deliveries", value)
    suspend fun createReception(token: String, value: PlantReceptionCommand) = mutate<PlantReceptionCommand, CollectionRecord>(token, "/api/v1/plant-receptions", value)
    suspend fun reconciliations(token: String) = request<List<ReconciliationRecord>> { client.get("/api/v1/reconciliations") { bearerAuth(token) } }
    suspend fun reconcile(token: String, value: ReconciliationCommand) = mutate<ReconciliationCommand, ReconciliationRecord>(token, "/api/v1/reconciliations", value)
    suspend fun resolveDiscrepancy(token: String, id: String, value: DiscrepancyResolutionCommand) = mutate<DiscrepancyResolutionCommand, ReconciliationRecord>(token, "/api/v1/reconciliations/$id/resolutions", value)
    suspend fun qualityAnalyses(token: String) = request<List<QualityListItem>>(token, "/api/v1/quality/analyses")
    suspend fun qualityParameters(token: String) = request<List<QualityListItem>>(token, "/api/v1/quality/parameters")
    suspend fun qualityIncidents(token: String) = request<List<QualityListItem>>(token, "/api/v1/quality/incidents")
    suspend fun communicationEvents(token: String) = request<List<QualityListItem>>(token, "/api/v1/communication-events")
    suspend fun notifications(token: String) = request<List<QualityListItem>>(token, "/api/v1/provider-notifications")
    suspend fun createQualityAnalysis(token: String, value: QualityAnalysisCommand) = mutate<QualityAnalysisCommand, QualityListItem>(token, "/api/v1/quality/analyses", value)
    suspend fun createQualityResult(token: String, id: String, value: QualityResultCommand) = mutate<QualityResultCommand, QualityListItem>(token, "/api/v1/quality/analyses/$id/results", value)
    suspend fun classifyQuality(token: String, id: String, value: QualityClassificationCommand) = mutate<QualityClassificationCommand, QualityListItem>(token, "/api/v1/quality/analyses/$id/classify", value)
    suspend fun createQualityIncident(token: String, value: QualityIncidentCommand) = mutate<QualityIncidentCommand, QualityListItem>(token, "/api/v1/quality/incidents", value)
    suspend fun createQualityMeasure(token:String,id:String,value:QualityMeasureCommand)=mutate<QualityMeasureCommand,QualityListItem>(token,"/api/v1/quality/incidents/$id/measures",value)
    suspend fun createTechnicalFollowUp(token:String,id:String,value:TechnicalFollowUpCommand)=mutate<TechnicalFollowUpCommand,QualityListItem>(token,"/api/v1/quality/incidents/$id/follow-ups",value)
    suspend fun createProviderNotification(token:String,value:ProviderNotificationCommand)=mutate<ProviderNotificationCommand,QualityListItem>(token,"/api/v1/provider-notifications",value)
    suspend fun createEvent(token: String, value: CommunicationEventCommand) = mutate<CommunicationEventCommand, QualityListItem>(token, "/api/v1/communication-events", value)
    suspend fun publishEvent(token: String, id: String) = request<QualityListItem>(token, "/api/v1/communication-events/$id/publish", method = "POST")
    suspend fun markAttendance(token:String,id:String,value:AttendanceCommand)=mutate<AttendanceCommand,QualityListItem>(token,"/api/v1/communication-events/$id/attendance",value)
    suspend fun weeklyClosures(token:String)=request<List<FinancialRecord>>(token,"/api/v1/weekly-closures")
    suspend fun closeWeek(token:String,value:WeeklyClosureCommand)=mutate<WeeklyClosureCommand,FinancialRecord>(token,"/api/v1/weekly-closures",value)
    suspend fun milkPrices(token:String)=request<List<FinancialRecord>>(token,"/api/v1/milk-prices")
    suspend fun createMilkPrice(token:String,value:MilkPriceCommand)=mutate<MilkPriceCommand,FinancialRecord>(token,"/api/v1/milk-prices",value)
    suspend fun settlements(token:String)=request<List<FinancialRecord>>(token,"/api/v1/settlements")
    suspend fun calculateSettlement(token:String,value:SettlementCommand)=mutate<SettlementCommand,FinancialRecord>(token,"/api/v1/settlements",value)
    suspend fun paymentEnvelope(token:String,id:String)=request<PaymentEnvelope>(token,"/api/v1/settlements/$id/envelope")
    suspend fun payments(token:String)=request<List<FinancialRecord>>(token,"/api/v1/payments")
    suspend fun payments(token:String,settlementId:String)=request<List<FinancialRecord>>(token,"/api/v1/payments?settlementId=$settlementId")
    suspend fun registerPayment(token:String,value:PaymentCommand)=mutate<PaymentCommand,FinancialRecord>(token,"/api/v1/payments",value)
    suspend fun cancelPayment(token:String,id:String,value:PaymentStatusCommand)=mutate<PaymentStatusCommand,FinancialRecord>(token,"/api/v1/payments/$id/cancel",value)
    suspend fun reopenWeek(token:String,id:String,value:SettlementStatusCommand)=mutate<SettlementStatusCommand,FinancialRecord>(token,"/api/v1/weekly-closures/$id/reopen",value)
    suspend fun productionBatches(token:String)=request<List<FinancialRecord>>(token,"/api/v1/production-batches")
    suspend fun createProductionBatch(token:String,value:ProductionBatchCommand)=mutate<ProductionBatchCommand,FinancialRecord>(token,"/api/v1/production-batches",value)
    suspend fun sales(token:String)=request<List<FinancialRecord>>(token,"/api/v1/sales")
    suspend fun createSale(token:String,value:SaleCommand)=mutate<SaleCommand,FinancialRecord>(token,"/api/v1/sales",value)
    suspend fun financialReport(token:String)=request<List<FinancialRecord>>(token,"/api/v1/reports/financial")
    suspend fun productionReport(token:String)=request<List<FinancialRecord>>(token,"/api/v1/reports/production")
    private suspend inline fun <reified T> request(token: String, path: String, method: String = "GET"): AppResult<T> = request { if (method == "POST") client.post(path) { bearerAuth(token) } else client.get(path) { bearerAuth(token) } }
    suspend fun createZone(token: String, value: ZoneCommand) = mutate<ZoneCommand, OperationalItem>(token, "/api/v1/zones", value)
    suspend fun createRoute(token: String, value: RouteCommand) = mutate<RouteCommand, OperationalItem>(token, "/api/v1/routes", value)
    private suspend inline fun <reified B, reified T> mutate(token: String, path: String, value: B): AppResult<T> = request { client.post(path) { bearerAuth(token); contentType(ContentType.Application.Json); setBody(value) } }
    private suspend inline fun <reified T> request(block: () -> HttpResponse): AppResult<T> = try {
        val response = block()
        if (response.status.value in 200..299) response.body<ApiEnvelope<T>>().data?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.Unexpected("Respuesta inválida"))
        else AppResult.Failure(when (response.status.value) {
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden
            500, 502, 503 -> AppError.Remote(response.status.value, "Error del servidor")
            else -> AppError.Remote(response.status.value, "No se pudo completar la operación")
        })
    } catch (_: Exception) { AppResult.Failure(AppError.Offline) }
}

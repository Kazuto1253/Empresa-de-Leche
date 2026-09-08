package pe.gob.huata.ecolactea.server.collection

import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pe.gob.huata.ecolactea.core.application.AppResult
import pe.gob.huata.ecolactea.core.model.AuthenticatedUser
import pe.gob.huata.ecolactea.core.model.Permission
import pe.gob.huata.ecolactea.core.network.*
import pe.gob.huata.ecolactea.server.auth.AuthService
import java.math.BigDecimal
import java.sql.Connection
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.util.UUID

fun Route.collectionRoutes(dataSource: HikariDataSource?, auth: AuthService?) {
    route("/api/v1") {
        post("/collections") { val r = call.receive<CollectionEntryCommand>(); authorized(auth, call, Permission.MANAGE_COLLECTIONS) { u -> database(dataSource, call) { it.createCollection(r, u) } } }
        get("/collections") { authorized(auth, call, Permission.VIEW_COLLECTIONS) { u -> database(dataSource, call) { it.collections(u) } } }
        post("/direct-deliveries") { val r = call.receive<DirectDeliveryCommand>(); authorized(auth, call, Permission.MANAGE_RECEPTION) { u -> database(dataSource, call) { it.createDirectDelivery(r, u) } } }
        post("/plant-receptions") { val r = call.receive<PlantReceptionCommand>(); authorized(auth, call, Permission.MANAGE_RECEPTION) { u -> database(dataSource, call) { it.createReception(r, u) } } }
        post("/reconciliations") { val r = call.receive<ReconciliationCommand>(); authorized(auth, call, Permission.VIEW_RECONCILIATION) { u -> database(dataSource, call) { it.reconcile(r, u) } } }
        get("/reconciliations") { authorized(auth, call, Permission.VIEW_RECONCILIATION) { u -> database(dataSource, call) { it.reconciliations(u) } } }
        post("/reconciliations/{id}/resolutions") { val r = call.receive<DiscrepancyResolutionCommand>(); authorized(auth, call, Permission.RESOLVE_DISCREPANCIES) { u -> database(dataSource, call) { it.resolve(call.parameters["id"]!!, r, u) } } }
    }
}

private suspend fun authorized(auth: AuthService?, call: ApplicationCall, permission: Permission, block: suspend (AuthenticatedUser) -> Unit) {
    val user = (auth?.let { withContext(Dispatchers.IO) { it.me(call.token()) } } as? AppResult.Success)?.value
    if (user == null || !pe.gob.huata.ecolactea.core.model.Authorization.allows(user, permission)) {
        call.respond(HttpStatusCode.Forbidden, ApiEnvelope<Unit>(error = ApiErrorDto("FORBIDDEN", "No tienes permiso para esta operación")))
    } else block(user)
}

private fun ApplicationCall.token() = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ").orEmpty()
private suspend fun <T> database(ds: HikariDataSource?, call: ApplicationCall, block: (CollectionRepository) -> T) {
    if (ds == null) { call.respond(HttpStatusCode.ServiceUnavailable, ApiEnvelope<Unit>(error = ApiErrorDto("UNAVAILABLE", "Base de datos no configurada"))); return }
    try { call.respond(ApiEnvelope(data = withContext(Dispatchers.IO) { block(CollectionRepository(ds)) })) }
    catch (e: IllegalArgumentException) { call.respond(HttpStatusCode.BadRequest, ApiEnvelope<Unit>(error = ApiErrorDto("VALIDATION", e.message ?: "Solicitud inválida"))) }
    catch (e: IllegalStateException) { call.respond(HttpStatusCode.Conflict, ApiEnvelope<Unit>(error = ApiErrorDto("CONFLICT", e.message ?: "Operación no permitida"))) }
}

private class CollectionRepository(private val ds: HikariDataSource) {
    fun createCollection(r: CollectionEntryCommand, u: AuthenticatedUser): CollectionRecord = tx { c ->
        val liters = r.liters.toBigDecimalOrNull() ?: throw IllegalArgumentException("liters inválido")
        require(liters > BigDecimal.ZERO) { "liters debe ser mayor que cero" }
        require(UUID.fromString(r.clientOperationId) != null) { "clientOperationId inválido" }
        if (u.role.name == "ACOPIADOR") {
            check(exists(c, "SELECT 1 FROM collection_workday_staff WHERE workday_id=? AND user_id=?", r.workdayId, u.userId)) { "Acopiador no asignado a la jornada" }
        }
        check(exists(c, "SELECT 1 FROM provider WHERE id=? AND status='ACTIVE'", r.providerId)) { "Proveedor inexistente o inactivo" }
        check(exists(c, "SELECT 1 FROM route WHERE id=? AND status='ACTIVE'", r.routeId)) { "Ruta inexistente o inactiva" }
        check(exists(c, "SELECT 1 FROM collection_workday WHERE id=? AND route_id=? AND status IN ('PLANNED','ACTIVE')", r.workdayId, r.routeId)) { "Jornada inválida" }
        val existing = c.prepareStatement("SELECT id,liters,status,collected_at FROM milk_collection WHERE client_operation_id=?").use { s -> s.setString(1, r.clientOperationId); s.executeQuery().use { q -> if (q.next()) CollectionRecord(q.getString(1), r.providerId, r.routeId, r.workdayId, q.getBigDecimal(2).toPlainString(), r.origin, q.getString(3), q.getTimestamp(4).toInstant().toString()) else null } }
        if (existing != null) return@tx existing
        val id = UUID.randomUUID().toString()
        c.prepareStatement("INSERT INTO milk_collection(id,provider_id,route_id,workday_id,collector_user_id,collected_at,liters,origin,client_operation_id) VALUES(?,?,?,?,?,?,?,?,?)").use { s ->
            listOf(id, r.providerId, r.routeId, r.workdayId, u.userId).forEachIndexed { i, v -> s.setString(i + 1, v) }; s.setTimestamp(6, Timestamp.from(Instant.parse(r.collectedAt))); s.setBigDecimal(7, liters); s.setString(8, r.origin); s.setString(9, r.clientOperationId); s.executeUpdate()
        }
        c.prepareStatement("INSERT INTO processed_client_operation(operation_id,actor_user_id,aggregate_type,aggregate_id,server_version) VALUES(?,?,?,?,1)").use { s -> s.setString(1,r.clientOperationId);s.setString(2,u.userId);s.setString(3,"MILK_COLLECTION");s.setString(4,id);s.executeUpdate() }
        audit(c, u.userId, "COLLECTION_CREATED", "MILK_COLLECTION", id, r.clientOperationId)
        CollectionRecord(id, r.providerId, r.routeId, r.workdayId, liters.toPlainString(), r.origin, "SYNCED", r.collectedAt)
    }

    fun collections(u: AuthenticatedUser): List<CollectionRecord> = ds.connection.use { c -> c.prepareStatement("SELECT id,provider_id,route_id,workday_id,liters,origin,status,collected_at FROM milk_collection WHERE collector_user_id=? ORDER BY collected_at DESC LIMIT 200").use { s -> s.setString(1,u.userId); s.executeQuery().use { q -> buildList { while(q.next()) add(CollectionRecord(q.getString(1),q.getString(2),q.getString(3),q.getString(4),q.getBigDecimal(5).toPlainString(),q.getString(6),q.getString(7),q.getTimestamp(8).toInstant().toString())) } } } }

    fun createDirectDelivery(r: DirectDeliveryCommand, u: AuthenticatedUser): CollectionRecord = tx { c ->
        val liters = r.liters.toBigDecimalOrNull() ?: throw IllegalArgumentException("liters inválido"); require(liters > BigDecimal.ZERO)
        check(exists(c,"SELECT 1 FROM provider WHERE id=? AND status='ACTIVE'",r.providerId)) { "Proveedor inexistente o inactivo" }
        c.prepareStatement("SELECT id,liters,delivered_at,status FROM direct_delivery WHERE client_operation_id=?").use { s -> s.setString(1,r.clientOperationId); s.executeQuery().use { q -> if (q.next()) return@tx CollectionRecord(q.getString(1), r.providerId, null, null, q.getBigDecimal(2).toPlainString(), "DIRECT_DELIVERY", q.getString(4), q.getTimestamp(3).toInstant().toString()) } }
        val id=UUID.randomUUID().toString(); c.prepareStatement("INSERT INTO direct_delivery(id,provider_id,delivered_at,liters,responsible_user_id,client_operation_id) VALUES(?,?,?,?,?,?)").use { s -> s.setString(1,id);s.setString(2,r.providerId);s.setTimestamp(3,Timestamp.from(Instant.parse(r.deliveredAt)));s.setBigDecimal(4,liters);s.setString(5,u.userId);s.setString(6,r.clientOperationId);s.executeUpdate() }; audit(c,u.userId,"DIRECT_DELIVERY_CREATED","DIRECT_DELIVERY",id,r.clientOperationId); CollectionRecord(id,r.providerId,null,null,liters.toPlainString(),"DIRECT_DELIVERY","CONFIRMED",r.deliveredAt)
    }

    fun createReception(r: PlantReceptionCommand, u: AuthenticatedUser): CollectionRecord = tx { c ->
        val liters=r.litersReceived.toBigDecimalOrNull() ?: throw IllegalArgumentException("litersReceived inválido"); require(liters > BigDecimal.ZERO); require(r.measurementSource in setOf("MANUAL","FLOW_METER")) { "measurementSource inválido" }
        c.prepareStatement("SELECT id,route_id,workday_id,liters_received,measurement_source,status,received_at FROM plant_reception WHERE client_operation_id=?").use { s -> s.setString(1,r.clientOperationId); s.executeQuery().use { q -> if (q.next()) return@tx CollectionRecord(q.getString(1), null, q.getString(2), q.getString(3), q.getBigDecimal(4).toPlainString(), q.getString(5), q.getString(6), q.getTimestamp(7).toInstant().toString()) } }
        val id=UUID.randomUUID().toString(); c.prepareStatement("INSERT INTO plant_reception(id,workday_id,route_id,collection_unit_id,direct_delivery_id,received_at,liters_received,measurement_source,responsible_user_id,client_operation_id) VALUES(?,?,?,?,?,?,?,?,?,?)").use { s -> s.setString(1,id); nullable(s,2,r.workdayId); nullable(s,3,r.routeId); nullable(s,4,r.collectionUnitId); nullable(s,5,r.directDeliveryId);s.setTimestamp(6,Timestamp.from(Instant.parse(r.receivedAt)));s.setBigDecimal(7,liters);s.setString(8,r.measurementSource);s.setString(9,u.userId);s.setString(10,r.clientOperationId);s.executeUpdate() }; audit(c,u.userId,"PLANT_RECEPTION_CREATED","PLANT_RECEPTION",id,r.clientOperationId); CollectionRecord(id,null,r.routeId,r.workdayId,liters.toPlainString(),r.measurementSource,"CONFIRMED",r.receivedAt)
    }

    fun reconcile(r: ReconciliationCommand, u: AuthenticatedUser): ReconciliationRecord = tx { c ->
        val field = sum(c,"SELECT COALESCE(SUM(liters),0) FROM milk_collection WHERE workday_id=?",r.workdayId); val direct=sum(c,"SELECT COALESCE(SUM(d.liters),0) FROM direct_delivery d JOIN plant_reception p ON p.direct_delivery_id=d.id WHERE p.workday_id=?",r.workdayId); val received=sum(c,"SELECT COALESCE(SUM(liters_received),0) FROM plant_reception WHERE workday_id=?",r.workdayId); val expected=field+direct; val diff=received-expected; val status=if(diff.compareTo(BigDecimal.ZERO)==0)"MATCHED" else "DISCREPANCY"; val id=UUID.randomUUID().toString(); c.prepareStatement("INSERT INTO collection_reconciliation(id,workday_id,route_id,total_field,total_direct,total_expected,total_received,difference,status,created_by) VALUES(?,?,?,?,?,?,?,?,?,?)").use { s -> s.setString(1,id);s.setString(2,r.workdayId);s.setString(3,r.routeId);s.setBigDecimal(4,field);s.setBigDecimal(5,direct);s.setBigDecimal(6,expected);s.setBigDecimal(7,received);s.setBigDecimal(8,diff);s.setString(9,status);s.setString(10,u.userId);s.executeUpdate() }; audit(c,u.userId,"RECONCILIATION_CREATED","COLLECTION_RECONCILIATION",id); ReconciliationRecord(id,r.workdayId,r.routeId,field.toPlainString(),direct.toPlainString(),expected.toPlainString(),received.toPlainString(),diff.toPlainString(),status)
    }
    fun reconciliations(u: AuthenticatedUser): List<ReconciliationRecord> = ds.connection.use { c -> c.prepareStatement("SELECT id,workday_id,route_id,total_field,total_direct,total_expected,total_received,difference,status FROM collection_reconciliation ORDER BY created_at DESC LIMIT 200").use { s -> s.executeQuery().use { q -> buildList { while(q.next()) add(ReconciliationRecord(q.getString(1),q.getString(2),q.getString(3),q.getBigDecimal(4).toPlainString(),q.getBigDecimal(5).toPlainString(),q.getBigDecimal(6).toPlainString(),q.getBigDecimal(7).toPlainString(),q.getBigDecimal(8).toPlainString(),q.getString(9))) } } } }
    fun resolve(id:String,r:DiscrepancyResolutionCommand,u:AuthenticatedUser): ReconciliationRecord = tx { c -> c.prepareStatement("INSERT INTO discrepancy_resolution(id,reconciliation_id,reason,observations,authorized_correction,resolved_by,status) VALUES(?,?,?,?,?,?,?)").use { s -> s.setString(1,UUID.randomUUID().toString());s.setString(2,id);s.setString(3,r.reason);s.setString(4,r.observations);s.setBigDecimal(5,r.authorizedCorrection?.toBigDecimalOrNull());s.setString(6,u.userId);s.setString(7,r.status);s.executeUpdate() }; audit(c,u.userId,"DISCREPANCY_RESOLVED","COLLECTION_RECONCILIATION",id,r.reason); c.prepareStatement("SELECT id,workday_id,route_id,total_field,total_direct,total_expected,total_received,difference,status FROM collection_reconciliation WHERE id=?").use { s -> s.setString(1,id);s.executeQuery().use { q -> check(q.next()); ReconciliationRecord(q.getString(1),q.getString(2),q.getString(3),q.getBigDecimal(4).toPlainString(),q.getBigDecimal(5).toPlainString(),q.getBigDecimal(6).toPlainString(),q.getBigDecimal(7).toPlainString(),q.getBigDecimal(8).toPlainString(),q.getString(9)) } } }

    private fun <T> tx(block:(Connection)->T):T=ds.connection.use { c -> c.autoCommit=false; try { val v=block(c);c.commit();v } catch(e:Exception){c.rollback();throw e} }
    private fun exists(c:Connection,sql:String,vararg args:String)=c.prepareStatement(sql).use{s->args.forEachIndexed{i,v->s.setString(i+1,v)};s.executeQuery().use{it.next()}}
    private fun sum(c:Connection,sql:String,id:String?):BigDecimal=c.prepareStatement(sql).use{s->s.setString(1,id);s.executeQuery().use{it.next();it.getBigDecimal(1)}}
    private fun audit(c:Connection,actor:String,action:String,type:String,id:String,operation:String?=null){c.prepareStatement("INSERT INTO audit_event(id,actor_user_id,action,entity_type,entity_id,client_operation_id) VALUES(?,?,?,?,?,?)").use{s->s.setString(1,UUID.randomUUID().toString());s.setString(2,actor);s.setString(3,action);s.setString(4,type);s.setString(5,id);s.setString(6,operation);s.executeUpdate()}}
    private fun nullable(s: java.sql.PreparedStatement, index: Int, value: String?) { if (value == null) s.setNull(index, Types.VARCHAR) else s.setString(index, value) }
}

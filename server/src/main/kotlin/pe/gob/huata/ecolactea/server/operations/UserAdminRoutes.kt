package pe.gob.huata.ecolactea.server.operations

import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import pe.gob.huata.ecolactea.core.application.AppResult
import pe.gob.huata.ecolactea.core.model.Permission
import pe.gob.huata.ecolactea.core.model.Role
import pe.gob.huata.ecolactea.core.network.ApiEnvelope
import pe.gob.huata.ecolactea.core.network.ApiErrorDto
import pe.gob.huata.ecolactea.server.auth.AuthService
import pe.gob.huata.ecolactea.server.auth.PasswordHasher
import java.sql.Connection
import java.util.UUID
import javax.sql.DataSource

@Serializable data class UserCreateRequest(val username: String, val password: String, val role: Role, val providerId: String? = null)
@Serializable data class UserResetRequest(val password: String)
@Serializable data class UserDto(val id: String, val username: String, val role: Role, val active: Boolean, val providerId: String? = null)
@Serializable data class UserActionDto(val id: String, val active: Boolean? = null, val reset: Boolean? = null)

fun Route.userAdminRoutes(dataSource: HikariDataSource?, auth: AuthService?) {
    route("/api/v1/users") {
        get { adminUser(auth, call, Permission.VIEW_USERS) { _ -> dbUsers(dataSource, call) { it.list() } } }
        post { val request=call.receive<UserCreateRequest>(); adminUser(auth, call, Permission.MANAGE_USERS) { actor -> dbUsers(dataSource, call) { it.create(request, actor) } } }
        post("/{id}/activate") { adminUser(auth, call, Permission.MANAGE_USERS) { actor -> dbUsers(dataSource, call) { it.status(call.parameters["id"]!!, true, actor) } } }
        post("/{id}/deactivate") { adminUser(auth, call, Permission.MANAGE_USERS) { actor -> dbUsers(dataSource, call) { it.status(call.parameters["id"]!!, false, actor) } } }
        post("/{id}/reset-password") { val request=call.receive<UserResetRequest>(); adminUser(auth, call, Permission.MANAGE_USERS) { actor -> dbUsers(dataSource, call) { it.reset(call.parameters["id"]!!, request.password, actor) } } }
    }
}

private suspend fun adminUser(auth: AuthService?, call: ApplicationCall, permission: Permission, block: suspend (String) -> Unit) {
    if (auth == null) { call.respond(HttpStatusCode.ServiceUnavailable, ApiEnvelope<Unit>(error=ApiErrorDto("UNAVAILABLE","Servicio no disponible"))); return }
    val user=(withContext(Dispatchers.IO){auth.me(call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ").orEmpty())} as? AppResult.Success)?.value
    if (user == null || !pe.gob.huata.ecolactea.core.model.Authorization.allows(user, permission)) { call.respond(HttpStatusCode.Forbidden, ApiEnvelope<Unit>(error=ApiErrorDto("FORBIDDEN","No tienes permiso"))); return }
    block(user.userId)
}
private suspend fun <T> dbUsers(source: DataSource?, call: ApplicationCall, block: (UserAdminRepository) -> T) {
    if (source == null) { call.respond(HttpStatusCode.ServiceUnavailable, ApiEnvelope<Unit>(error=ApiErrorDto("UNAVAILABLE","Base de datos no configurada"))); return }
    try { call.respond(ApiEnvelope(data=withContext(Dispatchers.IO){block(UserAdminRepository(source))})) } catch (e: IllegalArgumentException) { call.respond(HttpStatusCode.BadRequest,ApiEnvelope<Unit>(error=ApiErrorDto("VALIDATION",e.message ?: "Solicitud inválida"))) } catch (_: java.sql.SQLIntegrityConstraintViolationException) { call.respond(HttpStatusCode.Conflict,ApiEnvelope<Unit>(error=ApiErrorDto("CONFLICT","El usuario ya existe o viola una relación"))) }
}
private class UserAdminRepository(private val source: DataSource) {
    private fun <T> tx(block:(Connection)->T):T=source.connection.use{c->c.autoCommit=false;try{block(c).also{c.commit()}}catch(e:Exception){c.rollback();throw e}}
    fun list(): List<UserDto> = source.connection.use { c -> c.prepareStatement("SELECT id,username,role_code,active,provider_id FROM app_user ORDER BY username").use { s -> s.executeQuery().use { r -> buildList { while (r.next()) add(UserDto(r.getString(1), r.getString(2), Role.valueOf(r.getString(3)), r.getBoolean(4), r.getString(5))) } } } }
    fun create(req:UserCreateRequest,actor:String):UserDto=tx{c->val id=UUID.randomUUID().toString();val hash=PasswordHasher().hash(req.password);c.prepareStatement("INSERT INTO app_user(id,username,password_hash,role_code,active,provider_id) VALUES(?,?,?,?,TRUE,?)").use{s->s.setString(1,id);s.setString(2,req.username.trim());s.setString(3,hash);s.setString(4,req.role.name);s.setString(5,req.providerId);s.executeUpdate()};audit(c,actor,"USER_CREATED",id);UserDto(id,req.username,req.role,true,req.providerId)}
    fun status(id:String,active:Boolean,actor:String)=tx{c->c.prepareStatement("UPDATE app_user SET active=? WHERE id=?").use{s->s.setBoolean(1,active);s.setString(2,id);check(s.executeUpdate()==1){"Usuario inexistente"}};if(!active)c.prepareStatement("UPDATE auth_session SET revoked_at=UNIX_TIMESTAMP() WHERE user_id=? AND revoked_at IS NULL").use{s->s.setString(1,id);s.executeUpdate()};audit(c,actor,if(active)"USER_ACTIVATED" else "USER_DEACTIVATED",id);UserActionDto(id,active=active)}
    fun reset(id:String,password:String,actor:String)=tx{c->c.prepareStatement("UPDATE app_user SET password_hash=?,updated_at=CURRENT_TIMESTAMP(6) WHERE id=?").use{s->s.setString(1,PasswordHasher().hash(password));s.setString(2,id);check(s.executeUpdate()==1){"Usuario inexistente"}};c.prepareStatement("UPDATE auth_session SET revoked_at=UNIX_TIMESTAMP() WHERE user_id=? AND revoked_at IS NULL").use{s->s.setString(1,id);s.executeUpdate()};audit(c,actor,"USER_PASSWORD_RESET",id);UserActionDto(id,reset=true)}
    private fun audit(c:Connection,actor:String,action:String,id:String){if(actor.isNotBlank())c.prepareStatement("INSERT INTO audit_event(id,actor_user_id,action,entity_type,entity_id) VALUES(?,?,?,?,?)").use{s->s.setString(1,UUID.randomUUID().toString());s.setString(2,actor);s.setString(3,action);s.setString(4,"APP_USER");s.setString(5,id);s.executeUpdate()}}
}

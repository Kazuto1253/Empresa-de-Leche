package pe.gob.huata.ecolactea.server

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

object BusinessCalendar {
    val zone: ZoneId = ZoneId.of("America/Lima")
    const val mysqlOffset: String = "-05:00"

    fun requireOperationalWeek(start: LocalDate, end: LocalDate) {
        require(start.dayOfWeek == DayOfWeek.MONDAY) { "La semana debe iniciar un lunes" }
        require(end.dayOfWeek == DayOfWeek.SUNDAY && end == start.plusDays(6)) {
            "La semana debe terminar el domingo inmediato"
        }
    }
}

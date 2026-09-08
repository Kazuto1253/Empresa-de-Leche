package pe.gob.huata.ecolactea.server

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFailsWith

class BusinessCalendarTest {
    @Test fun acceptsMondayThroughSunday(){BusinessCalendar.requireOperationalWeek(LocalDate.parse("2026-09-07"),LocalDate.parse("2026-09-13"))}
    @Test fun rejectsOtherRanges(){assertFailsWith<IllegalArgumentException>{BusinessCalendar.requireOperationalWeek(LocalDate.parse("2026-09-08"),LocalDate.parse("2026-09-14"))}}
}

package pe.gob.huata.ecolactea.server.finance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.math.BigDecimal

class PaymentPolicyTest {
    @Test fun partialThenFullPayment(){
        val first=PaymentPolicy.apply(BigDecimal("500.0000"),BigDecimal.ZERO,BigDecimal("250.0000"))
        assertEquals("PARTIALLY_PAID",first.settlementStatus)
        assertEquals(BigDecimal("250.0000"),first.pending)
        val second=PaymentPolicy.apply(BigDecimal("500.0000"),BigDecimal("250.0000"),BigDecimal("250.0000"))
        assertEquals("PAID",second.settlementStatus)
        assertEquals(BigDecimal("0.0000"),second.pending)
    }

    @Test fun rejectsPaymentAboveBalance(){
        assertFailsWith<IllegalArgumentException>{PaymentPolicy.apply(BigDecimal("500"),BigDecimal("250"),BigDecimal("251"))}
    }
}

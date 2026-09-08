package pe.gob.huata.ecolactea.server.finance

import java.math.BigDecimal

data class PaymentBalance(val pending:BigDecimal,val settlementStatus:String)

object PaymentPolicy {
    fun apply(netAmount:BigDecimal,paidAmount:BigDecimal,newPayment:BigDecimal):PaymentBalance {
        require(netAmount>=BigDecimal.ZERO)
        require(paidAmount>=BigDecimal.ZERO)
        require(newPayment>BigDecimal.ZERO){"El pago debe ser mayor que cero"}
        val currentBalance=netAmount.subtract(paidAmount)
        require(newPayment<=currentBalance){"El pago excede el saldo pendiente"}
        val pending=currentBalance.subtract(newPayment)
        return PaymentBalance(pending,if(pending.signum()==0)"PAID" else "PARTIALLY_PAID")
    }
}

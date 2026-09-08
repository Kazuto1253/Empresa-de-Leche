package pe.gob.huata.ecolactea.core.network

import kotlinx.serialization.Serializable

@Serializable data class WeeklyClosureCommand(val weekStart:String,val weekEnd:String)
@Serializable data class MilkPriceCommand(val criterion:String="BASE",val amount:String,val currency:String,val effectiveFrom:String,val effectiveTo:String?=null,val reason:String)
@Serializable data class SettlementCommand(val providerId:String,val weeklyClosureId:String)
@Serializable data class SettlementStatusCommand(val reason:String)
@Serializable data class PaymentCommand(val settlementId:String,val amount:String,val currency:String,val paidAt:String,val method:String,val reference:String?=null,val notes:String?=null)
@Serializable data class PaymentStatusCommand(val reason:String)
@Serializable data class ProductionBatchCommand(val batchCode:String,val productionDate:String,val notes:String?=null,val inputQuantity:String,val inputUnit:String,val productId:String,val outputQuantity:String,val outputUnit:String)
@Serializable data class SaleItemCommand(val productId:String,val quantity:String,val customerType:String)
@Serializable data class SaleCommand(val customerId:String,val soldAt:String,val currency:String,val items:List<SaleItemCommand>)
@Serializable data class FinancialRecord(val id:String,val subjectId:String,val name:String,val amount:String,val status:String)
@Serializable data class PaymentEnvelope(val settlementId:String,val providerId:String,val providerName:String,val weekStart:String,val weekEnd:String,val liters:String,val basePrice:String,val grossAmount:String,val qualityAdjustment:String,val netAmount:String,val totalPaid:String,val pendingBalance:String,val currency:String,val status:String)

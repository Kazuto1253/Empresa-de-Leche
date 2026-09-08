package pe.gob.huata.ecolactea.core.application.finance

import pe.gob.huata.ecolactea.core.network.FinancialRecord
import pe.gob.huata.ecolactea.core.network.PaymentEnvelope

interface WeeklyCollectionClosureQuery { suspend fun closures():List<FinancialRecord> }
interface WeeklyCollectionClosurePort
interface MilkPriceResolver { suspend fun resolve(date:String,criterion:String="BASE"):String? }
interface MilkPriceQuery
interface SettlementQuery
interface SettlementPort
interface ProviderSettlementQuery { suspend fun forProvider(providerId:String):List<FinancialRecord> }
interface PaymentEnvelopeQuery { suspend fun envelope(settlementId:String):PaymentEnvelope }
interface PaymentQuery
interface PaymentPort
interface ProductionYieldQuery
interface FinancialReportQuery
interface ProductionReportQuery
interface QualityAdjustmentPort
interface QualityAdjustmentQuery
interface ProviderQualitySummaryQuery
interface DocumentRendererPort<T> { suspend fun render(document:T):ByteArray }

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
}

group = "pe.gob.huata.ecolactea"
version = "1.0.0"
application {
    mainClass = "pe.gob.huata.ecolactea.server.ApplicationKt"
}

dependencies {
    api(project(":core"))
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.serverCallLogging)
    implementation(libs.ktor.serializationKotlinxJson)
    implementation(libs.kotlinx.serializationJson)
    implementation(libs.hikari)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)
    implementation(libs.mysql.connector)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

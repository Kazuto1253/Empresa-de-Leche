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
    implementation(libs.ktor.serverCors)
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

tasks.named<ProcessResources>("processResources") {
    from(rootProject.file("database/migrations")) {
        include("V*.sql")
        into("db/migration")
    }
}

val integrationTest by sourceSets.creating
configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())
dependencies {
    add(integrationTest.implementationConfigurationName, sourceSets.main.get().output)
    add(integrationTest.implementationConfigurationName, project(":app:shared"))
    add(integrationTest.implementationConfigurationName, "io.ktor:ktor-client-content-negotiation:${libs.versions.ktor.get()}")
}
tasks.register<Test>("mysqlIntegrationTest") {
    description = "Runs RF-34 through the shared HTTP repository and real MySQL; requires external DB configuration."
    group = "verification"
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    outputs.upToDateWhen { false }
    mustRunAfter(tasks.test)
}

tasks.register<JavaExec>("seedDevData") {
    group = "development"
    description = "Explicitly and idempotently populates the configured database with DEV-only data."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("pe.gob.huata.ecolactea.server.seed.DevDataSeederKt")
}

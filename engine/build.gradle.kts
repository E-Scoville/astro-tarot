plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

group = "com.astrotarot"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("com.astrotarot.engine.MainKt")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
}

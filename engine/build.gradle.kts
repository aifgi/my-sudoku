plugins {
    kotlin("jvm") version "2.3.21"
    jacoco
}

kotlin {
    jvmToolchain((findProperty("javaVersion") as String?)?.toInt() ?: 25)
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.4")
    testImplementation(libs.coroutines.core)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}

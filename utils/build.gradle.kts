plugins {
    jacoco
}

dependencies {
    implementation("net.java.dev.jna:jna:5.18.1")
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}
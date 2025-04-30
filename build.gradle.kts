plugins {
    id("java")
}

group = "org.spasitel"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "1g"
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "org.spasitel.Main"
        )
    }
}

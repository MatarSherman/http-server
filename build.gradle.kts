plugins {
    id("java")
    application
    jacoco
    id("com.diffplug.spotless") version "7.2.1"
}

group = "matar.project"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

spotless {
    java {
        googleJavaFormat()

        importOrder()
        removeUnusedImports()

        endWithNewline()

        target("src/**/*.java") // Apply to all .java files in src
    }
    format("misc") { // For other file types like Markdown
        target("*.md")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}



application {
    mainClass.set("matar.project.Main")
    applicationName = "http-server"
}
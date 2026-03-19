plugins {
    java
}

group = "com.cloudcheap"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("fr.xephi:authme:5.6.0-SNAPSHOT") // AuthMe Reloaded
    compileOnly("org.jetbrains:annotations:24.0.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17)) // Java 17 cho Paper 1.20.4+
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

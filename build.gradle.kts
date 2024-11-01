plugins {
    id("java")
}

group = "org.gmarques"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("com.1stleg:jnativehook:2.0.2")
    implementation("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("org.reflections:reflections:0.10.2")
    implementation("ai.picovoice:porcupine-java:3.0.3")
    implementation("javazoom:jlayer:1.0.1")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("commons-io:commons-io:2.11.0")
    implementation("dev.langchain4j:langchain4j:0.35.0")
    implementation("net.java.dev.jna:jna-platform:5.12.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
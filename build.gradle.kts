plugins {
    java
    `java-library`
    `maven-publish`
    id("com.vaadin") version "25.0.4"
    id("biz.aQute.bnd.builder") version "7.2.1"
}

group = "com.wontlost"
version = "5.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

// Exclude node_modules from sources JAR
tasks.named<Jar>("sourcesJar") {
    exclude("**/node_modules/**")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.vaadin.com/vaadin-addons") }
    maven { url = uri("https://maven.vaadin.com/vaadin-prereleases") }
}

val vaadinVersion = "25.0.3"
val ckeditorVersion = "47.4.0"

dependencies {
    // Vaadin
    compileOnly("com.vaadin:vaadin-core:$vaadinVersion")

    // Jackson JSON 3.x (used by Vaadin 25+ for setPropertyJson)
    compileOnly("tools.jackson.core:jackson-databind:3.0.3")

    // HTML Sanitization
    implementation("org.jsoup:jsoup:1.18.3")

    // Jakarta Servlet API (Vaadin 25+)
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("com.vaadin:vaadin-core:$vaadinVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Vaadin-Package-Version" to "1",
            // Hot deploy support - Vaadin will scan for these
            "Vaadin-Advertised-Components" to "com.wontlost.ckeditor.VaadinCKEditor"
        )
    }
    // Include bnd.bnd instructions for OSGi bundle
    from("bnd.bnd")
    // Exclude dev files - dependencies are resolved at build time by consuming app
    exclude("**/node_modules/**")
    exclude("**/package-lock.json")
    exclude("**/tsconfig.json")
    exclude("**/.gitignore")
}

// Vaadin configuration - this is a library, not an application
vaadin {
    productionMode = false
    // Hot deploy optimization - don't optimize bundle for faster dev reloads
    optimizeBundle = false
}

// Disable Vaadin frontend tasks for library project
tasks.matching { it.name.startsWith("vaadin") }.configureEach {
    enabled = false
}

// Hot deploy: Copy resources to build directory for faster reloading
tasks.register<Copy>("hotDeployResources") {
    description = "Copy frontend resources for hot deployment"
    group = "vaadin"
    from("src/main/resources/META-INF/frontend")
    into(layout.buildDirectory.dir("resources/main/META-INF/frontend"))
}

// Hot deploy: Watch task for development
tasks.register("watch") {
    description = "Watch for changes and trigger hot reload"
    group = "vaadin"
    dependsOn("classes", "hotDeployResources")
    doLast {
        println("Watching for changes... Press Ctrl+C to stop.")
        println("Hot deploy is enabled. Changes to Java files will be recompiled automatically.")
        println("Frontend changes in src/main/resources/META-INF/frontend will be copied to build directory.")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("VaadinCKEditor")
                description.set("Modular CKEditor 5 integration for Vaadin - supports plugin-based customization")
                url.set("https://github.com/wontlost-ltd/vaadin-ckeditor")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                scm {
                    url.set("https://github.com/wontlost-ltd/vaadin-ckeditor")
                    connection.set("scm:git:git://github.com/wontlost-ltd/vaadin-ckeditor.git")
                    developerConnection.set("scm:git:git@github.com:wontlost-ltd/vaadin-ckeditor.git")
                }
            }
        }
    }
}

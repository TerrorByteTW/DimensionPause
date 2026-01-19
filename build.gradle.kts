import com.ctc.wstx.shaded.msv_core.datatype.xsd.datetime.TimeZone
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.internal.impldep.org.joda.time.tz.UTCProvider
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
    alias(libs.plugins.lombok);
}

project.group = "org.reprogle"
project.version = "2.0.0"
project.description = "Allows you to pause dimensions to prevent players from entering them"

val isReleaseBuild = project.hasProperty("releaseBuild")
val forceBuildId = project.hasProperty("forceBuildId")

if (!isReleaseBuild || forceBuildId) {
    val timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
    val newVersion = "${project.version}-SNAPSHOT-${timestamp}"
    project.version = newVersion
    println("Auto build ID enabled → version set to $newVersion")
} else {
    println("Release build → using version ${project.version}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    mavenCentral()
}

dependencies {
    compileOnly(libs.paper.api)
//    compileOnly(libs.folia.api)
    compileOnly(libs.boosted.yaml)
    implementation(libs.bstats)
    compileOnly(libs.guice)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    outputs.upToDateWhen { false }
    from(sourceSets.main.get().resources.srcDirs) {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "version" to project.version.toString(),
            )
        )
    }
}

tasks.shadowJar {
    archiveClassifier.set("") // Replace the normal JAR
    mergeServiceFiles()
    exclude("META-INF/*.MF")

    relocate("org.bstats", "org.reprogle.dimensionpause.libs")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

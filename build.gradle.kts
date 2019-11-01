import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0"
    kotlin("jvm") version "1.3.41"
}

group = "com.proximyst"
version = "0.1.1"

repositories{
    maven {
        name = "SpigotMC"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }

    maven {
        name = "Sonatype OSS"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")

        content {
            includeGroup("net.md-5")
        }
    }

    maven {
        name = "PaperMC Repo"
        url = uri("https://papermc.io/repo/repository/maven-snapshots/")

        content {
            includeGroup("com.destroystokyo.paper")
        }
    }
    
    maven {
        name = "jitpack.io"
        url = uri("https://jitpack.io/")

        content {
            includeGroup("com.github.MrPowerGamerBR")
        }
    }

    jcenter()
    mavenCentral()
    mavenLocal() // i hate you, ruan
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
//    compileOnly("com.destroystokyo.paper:paper-api:1.14.4-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.14-SNAPSHOT")
    compile("com.github.MrPowerGamerBR:TemmieWebhook:59de40c3b6")
    compileOnly("litebans:api:0.+")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.apply {
        jvmTarget = "1.8"
        javaParameters = true
    }
}

val shadowJar = tasks["shadowJar"] as ShadowJar
val relocateTask = tasks.create<ConfigureShadowRelocation>("relocateShadowJar") {
    target = shadowJar
    prefix = "com.proximyst.${project.name}.dependencies"
}
shadowJar.dependsOn(relocateTask)

tasks.processResources.configure {
    from("src/main/resources") {
        include("plugin.yml")
        include("bungee.yml")
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "VERSION" to project.version.toString()
            )
        )
    }
}
plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = project.property("pluginGroup") as String
version = project.property("pluginVersion") as String

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:${project.property("commonsLang3Version")}")
}

// 配置 IntelliJ Platform Plugin
intellij {
    version.set(project.property("platformVersion") as String)
    type.set(project.property("platformType") as String)
    plugins.set(listOf(project.property("platformPlugins") as String))
}

tasks {
    // JVM 兼容性配置
    withType<JavaCompile> {
        val javaVersion = project.property("javaVersion") as String
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set(project.property("pluginSinceBuild") as String)
        untilBuild.set(project.property("pluginUntilBuild") as String)
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

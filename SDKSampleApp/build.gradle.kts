import java.net.URI

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()

        val (mavenUrl, mavenToken) = buildSdkMavenRepo()
        maven {
            url = URI.create(mavenUrl)
            credentials {
                username = ""
                password = mavenToken
            }
        }
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

/**
 * Gradle will try to download Fourthline SDK from Github maven by default. It's possible to
 * redirect it to Azure maven by setting Gradle properties like:
 * 1) mavenRepoAlias=azure
 *    azureToken=**YOUR SECRET AZURE TOKEN**
 * 2) mavenRepoAlias=github
 *    githubToken=**YOUR SECRET GITHUB TOKEN**
 */
fun buildSdkMavenRepo(): Pair<String, String> {
    val azureUrl =
        "https://pkgs.dev.azure.com/fourthline/MobileSDK/_packaging/android-fourthline-sdk-internal/maven/v1"
    val githubUrl = "https://maven.pkg.github.com/Fourthline-com/FourthlineSDK-Android"
    return when (findProperty("mavenRepoAlias")?.toString()
        ?.toLowerCase()) {
        null, // Github by default
        "github" -> githubUrl to property("githubToken").toString()
        "azure" -> azureUrl to property("azureToken").toString()
        else -> throw GradleException("mavenRepoAlias may take only following values: `azure` and `github`")
    }
}
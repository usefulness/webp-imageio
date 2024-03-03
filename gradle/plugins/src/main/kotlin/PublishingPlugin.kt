import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaTask

class PublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("maven-publish")
        if (findConfig("SIGNING_PASSWORD").isNotEmpty()) {
            pluginManager.apply("signing")
        }

        extensions.configure<JavaPluginExtension> {
            withSourcesJar()
            withJavadocJar()
        }

        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            pluginManager.apply("org.jetbrains.dokka")

            tasks.withType(DokkaTask::class.java).configureEach { dokkaTask ->
                dokkaTask.notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/1217")
            }
            tasks.named("javadocJar", Jar::class.java) { javadocJar ->
                javadocJar.from(tasks.named("dokkaJavadoc"))
            }
            tasks.named("processResources", ProcessResources::class.java) { processResources ->
                processResources.from(rootProject.file("NOTICE"))
                processResources.from(rootProject.file("LICENSE"))
            }
        }

        extensions.configure<PublishingExtension> {
            with(repositories) {
                maven { maven ->
                    maven.name = "github"
                    maven.setUrl("https://maven.pkg.github.com/usefulness/webp-imageio")
                    with(maven.credentials) {
                        username = "usefulness"
                        password = findConfig("GITHUB_TOKEN")
                    }
                }
                maven { maven ->
                    maven.name = "mavenCentral"
                    maven.setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                    maven.mavenContent { it.releasesOnly() }
                    with(maven.credentials) {
                        username = findConfig("OSSRH_USERNAME")
                        password = findConfig("OSSRH_PASSWORD")
                    }
                }
                maven { maven ->
                    maven.name = "mavenCentralSnapshot"
                    maven.setUrl("https://oss.sonatype.org/content/repositories/snapshots")
                    maven.mavenContent { it.snapshotsOnly() }
                    with(maven.credentials) {
                        username = findConfig("OSSRH_USERNAME")
                        password = findConfig("OSSRH_PASSWORD")
                    }
                }
            }
            with(publications) {
                register("mavenJava", MavenPublication::class.java) { publication ->
                    publication.from(components.getByName("java"))
                    publication.pom { pom ->
                        pom.name.set("${project.group}:${project.name}")
                        pom.description.set("Java ImageIO WebP support")
                        pom.url.set("https://github.com/usefulness/webp-imageio")
                        pom.licenses { licenses ->
                            licenses.license { license ->
                                license.name.set("Apache-2.0")
                                license.url.set("https://github.com/usefulness/webp-imageio/blob/master/LICENSE")
                            }
                        }
                        pom.developers { developers ->
                            developers.developer { developer ->
                                developer.id.set("mateuszkwiecinski")
                                developer.name.set("Mateusz Kwiecinski")
                                developer.email.set("36954793+mateuszkwiecinski@users.noreply.github.com")
                            }
                        }
                        pom.scm { scm ->
                            scm.connection.set("scm:git:github.com/usefulness/webp-imageio.git")
                            scm.developerConnection.set("scm:git:ssh://github.com/usefulness/webp-imageio.git")
                            scm.url.set("https://github.com/usefulness/webp-imageio/tree/master")
                        }
                    }
                }
            }
        }
        pluginManager.withPlugin("signing") {
            with(extensions.extraProperties) {
                set("signing.keyId", findConfig("SIGNING_KEY_ID"))
                set("signing.password", findConfig("SIGNING_PASSWORD"))
                set("signing.secretKeyRingFile", findConfig("SIGNING_SECRET_KEY_RING_FILE"))
            }

            extensions.configure<SigningExtension>("signing") { signing ->
                signing.sign(extensions.getByType(PublishingExtension::class.java).publications)
            }
        }
    }

    private inline fun <reified T> ExtensionContainer.configure(crossinline receiver: T.() -> Unit) {
        configure(T::class.java) { receiver(it) }
    }
}

private fun Project.findConfig(key: String): String {
    return findProperty(key)?.toString() ?: System.getenv(key) ?: ""
}

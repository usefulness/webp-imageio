import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.dokka.gradle.DokkaTask

class PublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.vanniktech.maven.publish")
        pluginManager.apply("org.jetbrains.dokka")

        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            tasks.named("processResources", ProcessResources::class.java) { processResources ->
                processResources.from(rootProject.file("NOTICE"))
                processResources.from(rootProject.file("LICENSE"))
            }
        }

        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            coordinates(group.toString(), name, version.toString())

            signAllPublications()

            configureBasedOnAppliedPlugins()

            pom { pom ->
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
            }
        }
    }

    private inline fun <reified T : Any> ExtensionContainer.configure(crossinline receiver: T.() -> Unit) {
        configure(T::class.java) { receiver(it) }
    }
}

private fun Project.findConfig(key: String): String {
    return findProperty(key)?.toString() ?: System.getenv(key) ?: ""
}

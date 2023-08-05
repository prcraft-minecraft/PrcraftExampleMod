package xyz.wagyourtail.unimined.minecraft.patch.prcraft

import io.github.gaming32.prcraftinstaller.PrcraftInstaller
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.create
import xyz.wagyourtail.unimined.api.mapping.MappingNamespaceTree
import xyz.wagyourtail.unimined.api.minecraft.EnvType
import xyz.wagyourtail.unimined.api.runs.RunConfig
import xyz.wagyourtail.unimined.internal.minecraft.MinecraftProvider
import xyz.wagyourtail.unimined.internal.minecraft.patch.AbstractMinecraftTransformer
import xyz.wagyourtail.unimined.internal.minecraft.patch.MinecraftJar
import xyz.wagyourtail.unimined.internal.minecraft.resolver.Library
import xyz.wagyourtail.unimined.util.FinalizeOnRead
import xyz.wagyourtail.unimined.util.SemVerUtils
import xyz.wagyourtail.unimined.util.readZipInputStreamFor
import xyz.wagyourtail.unimined.util.withSourceSet
import java.nio.file.Files
import kotlin.io.path.deleteIfExists

class PrcraftMinecraftTransformer(project: Project, provider: MinecraftProvider) : AbstractMinecraftTransformer(project, provider, "prcraft") {

    val prcraft = project.configurations.maybeCreate(providerName.withSourceSet(provider.sourceSet))

    val prcraftRuntime = project.configurations.maybeCreate("prcraftRuntime".withSourceSet(provider.sourceSet)).also {
        project.configurations.getByName("runtimeOnly".withSourceSet(provider.sourceSet)).extendsFrom(it)
    }

    var serverOnly by FinalizeOnRead(false)

    init {
        provider.side = EnvType.CLIENT
    }

    // they're all minified and shaded
    override fun libraryFilter(library: Library): Boolean {
        return false
    }

    @JvmOverloads
    fun loader(dep: Any, action: Dependency.() -> Unit  = {}) {
        prcraft.dependencies.add((
            if (dep is String && !dep.contains(":")) {
                val tail = if (SemVerUtils.matches(dep, ">=0.4.600")) ":slim" else ""
                project.dependencies.create("io.github.gaming32:prcraft:$dep$tail@zip")
            } else {
                project.dependencies.create(dep)
            }
        ).apply(action))
    }

    override val prodNamespace: MappingNamespaceTree.Namespace by lazy {
        provider.mappings.getNamespace("customMCP")
    }

    override fun beforeMappingsResolve() {
        provider.mappings {
            postProcess("customMCP", {
                retroMCP()
                mapping(prcraft.dependencies.first()) {
                    // force retroMCP to load first
                    dependsOn("mcp")
                    // allow this to write on top of retroMCP
                    allowDuplicateOutputs()
                    mapNamespace("named", "mcp")
                    outputs("mcp", true) { listOf("official") }
                }
            }) {
                // rename mcp to customMCP
                mapNamespace("mcp", "customMCP")
                outputs("customMCP", true) { listOf("official") }
            }
        }

        super.beforeMappingsResolve()
    }

    private fun getPatchPath() = prcraft.resolve().first { it.extension == "zip" }.toPath()

    override fun transform(minecraft: MinecraftJar): MinecraftJar {
        if (minecraft.envType != EnvType.CLIENT) {
            throw IllegalArgumentException(
                "Please don't specify a side directly. Please use serverOnly in the customPatcher block."
            )
        }
        val output = MinecraftJar(
            minecraft,
            patches = minecraft.patches + "prcraft-${prcraft.dependencies.first().version}",
            mappingNamespace = provider.mappings.getNamespace("customMCP"),
            envType = if (serverOnly) EnvType.SERVER else EnvType.CLIENT
        )
        try {
            PrcraftInstaller.runInstaller(
                Files.newByteChannel(getPatchPath()),
                prcraft.dependencies.first().version,
                minecraft.path, output.path
            )
        } catch (e: Exception) {
            output.path.deleteIfExists()
            throw e
        }
        return super.transform(output)
    }

    override fun applyClientRunTransform(config: RunConfig) {
        config.mainClass = "net.minecraft.modding.impl.ModdedLaunch"
        config.args.clear()
        config.args.addAll(listOf(
            "--username", "Dev",
            "--accessToken", "0",
            "--gameDir", config.workingDir.absolutePath,
            "--disableUpdate"
        ))
        config.jvmArgs += "-Dprcraft.sideEnvironment=client"

        super.applyClientRunTransform(config)
    }

    override fun applyServerRunTransform(config: RunConfig) {
        config.mainClass = "net.minecraft.modding.impl.ModdedLaunch"
        config.args.clear()
        config.args.addAll(listOf(
            "--gameDir", config.workingDir.absolutePath
        ))
        config.jvmArgs += "-Dprcraft.sideEnvironment=dedicated_server"

        super.applyServerRunTransform(config)
    }

    override fun applyExtraLaunches() {
        project.logger.info("[Unimined/Minecraft] server config")
        provider.runs.addTarget(provider.provideVanillaRunServerTask("server", project.file("run/server")))
        provider.runs.configFirst("server", (provider.mcPatcher as AbstractMinecraftTransformer)::applyServerRunTransform)
    }

    override fun apply() {
        if (serverOnly) {
            val prcraftDep = prcraft.dependencies.first()
            prcraft.dependencies.clear()
            prcraft.dependencies.add(project.dependencies.create(
                prcraftDep.group!!, prcraftDep.name, prcraftDep.version,
                classifier = if (SemVerUtils.matches(prcraftDep.version!!, ">=0.4.600")) {
                    "server-slim"
                } else {
                    "server"
                },
                ext = "zip"
            ))
        }
        getPatchPath().readZipInputStreamFor("depslist.txt", false) {
            it.bufferedReader(Charsets.UTF_8).forEachLine { dep ->
                if ('\t' in dep) {
                    when (val classpath = dep.substringBefore('\t')) {
                        "all" -> provider.minecraftLibraries
                        "runtime" -> prcraftRuntime
                        else -> throw IllegalArgumentException("Unknown depslist.txt classpath: $classpath")
                    }.dependencies.add(project.dependencies.create(dep.substringAfter('\t')))
                } else {
                    provider.minecraftLibraries.dependencies.add(project.dependencies.create(dep))
                }
            }
        }
        super.apply()
    }

}

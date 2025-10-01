package com.github.themartdev.intellijgleam.ide.lsp

import com.intellij.openapi.components.*
import kotlin.io.path.Path
import kotlin.io.path.isExecutable

@Service(Service.Level.APP)
@State(name = "GleamToolchainSettings", storages = [Storage("gleamToolchainSettings.xml")])
class GleamServiceSettings() :
    SimplePersistentStateComponent<GleamToolchainSettings>(GleamToolchainSettings()) {

    var lspMode
        get() = state.lspMode
        set(value) {
            state.lspMode = value
        }

    var gleamPath
        get() = state.gleamPath ?: ""
        set(value) {
            state.gleamPath = value.ifBlank { getPath("gleam") }
        }

    var erlangPath
        get() = state.erlangPath ?: ""
        set(value) {
            state.erlangPath = value
        }

    companion object {
        fun getInstance(): GleamServiceSettings = service()

        fun getPath(executableName: String): String? {
            val pathVar = System.getenv("PATH")
            val allDirs = pathVar.split(":", ";").map { Path(it).resolve(executableName) }
            val executablePath = allDirs.firstOrNull { it.isExecutable() }
            return executablePath?.toString()
        }

    }
}

class GleamToolchainSettings : BaseState() {
    var lspMode by enum(GleamLspMode.ENABLED)
    var gleamPath by string(GleamServiceSettings.getPath("gleam") ?: "")
    var erlangPath by string("")
}

enum class GleamLspMode {
    ENABLED,
    DISABLED
}

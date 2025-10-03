package com.github.themartdev.intellijgleam.ide.lsp

import com.github.themartdev.intellijgleam.ide.common.GleamExecutableFinder
import com.intellij.openapi.components.*

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
            state.gleamPath = value.ifBlank { getPath() }
        }

    var erlangPath
        get() = state.erlangPath ?: ""
        set(value) {
            state.erlangPath = value
        }

    companion object {
        fun getInstance(): GleamServiceSettings = service()

        fun getPath(): String? {
            val detectedGleamPaths = GleamExecutableFinder.findGleamInstalls()
            return detectedGleamPaths.map { it.path }.firstOrNull()
        }

    }
}

class GleamToolchainSettings : BaseState() {
    var lspMode by enum(GleamLspMode.ENABLED)
    var gleamPath by string(GleamServiceSettings.getPath() ?: "")
    var erlangPath by string("")
}

enum class GleamLspMode {
    ENABLED,
    DISABLED
}

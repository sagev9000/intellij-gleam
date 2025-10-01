package com.github.themartdev.intellijgleam.ide.lsp

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

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
            state.gleamPath = value
        }

    var erlangPath
        get() = state.erlangPath ?: ""
        set(value) {
            state.erlangPath = value
        }

    companion object {
        fun getInstance(): GleamServiceSettings = service()
    }
}

class GleamToolchainSettings : BaseState() {
    var lspMode by enum(GleamLspMode.ENABLED)
    var gleamPath by string("")
    var erlangPath by string("")
}

enum class GleamLspMode {
    ENABLED,
    DISABLED
}

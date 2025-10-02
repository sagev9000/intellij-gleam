package com.github.themartdev.intellijgleam.ide.wizard

class GleamProjectAssets(
    val gleamCommands: List<Array<String>>,
    val templates: List<Template>,
    val target: String,
) {
    companion object {
        fun assetDirs() = listOf("src")

        fun assetProps(name: String, target: String) = mapOf(
            "gleamProjectName" to name,
            "gleamProjectTarget" to target,
        )
    }
}
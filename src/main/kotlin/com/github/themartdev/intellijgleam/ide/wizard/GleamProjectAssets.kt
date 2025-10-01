package com.github.themartdev.intellijgleam.ide.wizard

class GleamProjectAssets(
    val gleamCommands: List<Array<String>>,
    val templates: List<Pair<String, String>>,
    val target: String,
) {
    companion object {
        fun assetDirs() = listOf("src")

        fun assetProps(name: String, target: String) =
            arrayOf(
                Pair("gleamProjectName", name),
                Pair("gleamProjectTarget", target),
            )
    }
}
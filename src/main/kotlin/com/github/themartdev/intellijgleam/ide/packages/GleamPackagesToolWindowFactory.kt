package com.github.themartdev.intellijgleam.ide.packages

import com.github.themartdev.intellijgleam.GleamBundle
import com.github.themartdev.intellijgleam.ide.common.GleamProjectUtils
import com.github.themartdev.intellijgleam.ide.wizard.GleamCommandRunner
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.util.net.IdeHttpClientHelpers
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.ListCellRenderer
import javax.swing.Timer

class GleamPackagesToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        val windowContent = GleamPackagesToolWindowContent(toolWindow)
        val content = ContentFactory.getInstance().createContent(
            windowContent.contentPanel,
            GleamBundle.message("gleam.tool.package.window.title"),
            false
        )
        toolWindow.contentManager.addContent(content)
    }

    private class GleamPackageRenderer : ListCellRenderer<GleamPackage> {
        override fun getListCellRendererComponent(
            list: JList<out GleamPackage?>?,
            value: GleamPackage?,
            index: Int,
            isSelected: Boolean,
            hasFocus: Boolean
        ): Component? {
            // Rider::Nuget uses rows of
            // [Icon] Name (• InstalledVersion)? <long separator> LatestVersion
            return Box(BoxLayout.X_AXIS).apply {
                // TODO? Icon
                add(JLabel(value?.name))
                // TODO: Installed version
                // add(JLabel("•"))
                add(JSeparator().apply {
                    this.foreground = Color(0, 0, 0, 0)
                })
                add(JLabel(value?.latestStableVersion))
            }
        }
    }

    private class GleamPackagesToolWindowContent(
        val toolWindow: ToolWindow,
        val contentPanel: JPanel = JPanel(),
        val searchBox: JBTextField = JBTextField(),
    ) {
        init {
            val listModel = DefaultListModel<GleamPackage>()
            val packageList: JBList<GleamPackage> = JBList<GleamPackage>(listModel)
            contentPanel.apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(JPanel().apply {
                    layout = BorderLayout()
                    add(Box(BoxLayout.X_AXIS).apply {
                        add(JLabel(GleamBundle.message("gleam.tool.package.search.bar.label")))
                        add(searchBox)
                    }, BorderLayout.NORTH)
                    add(JBScrollPane(packageList), BorderLayout.CENTER)
                })
                // add(JPanel())
            }
            packageList.apply {
                cellRenderer = GleamPackageRenderer()
                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        if (e?.clickCount != 2) {
                            return
                        }
                        GleamCommandRunner.runCommand("add", packageList.selectedValue.name)
                    }
                })
            }

            fun updateResultList() {
                val results = search(searchBox.text)
                listModel.clear()
                listModel.addAll(results)
            }
            searchBox.addKeyListener(object : KeyAdapter() {
                val timer = Timer(1000, { evt -> updateResultList() }).apply { isRepeats = false }
                override fun keyReleased(e: KeyEvent?) {
                    if (timer.isRunning) {
                        timer.restart()
                    } else {
                        timer.start()
                    }
                }
            })
        }

        private inline fun <reified T> get(url: String): T {
            buildClient(url).use {
                it.execute(HttpGet(url)).use {
                    if (it.statusLine.statusCode >= 300) {
                        error(it.statusLine.statusCode)
                    }
                    val body = String(it.entity.content.readAllBytes())
                    val json = Json { ignoreUnknownKeys = true }
                    val decoded = json.decodeFromString<T>(body)
                    return decoded
                }
            }
        }

        private fun search(searchText: String): List<GleamPackage> {
            val searchText = URLEncoder.encode(searchText, StandardCharsets.UTF_8)
            val baseUrl = "https://hex.pm/api/packages"
            val searchUrl = "${baseUrl}/?sort=downloads&search=${searchText}"
            val searchResult: MutableList<GleamPackage> = get(searchUrl)
            try {
                val exactUrl = "${baseUrl}/${searchText}"
                val exactMatch: GleamPackage = get(exactUrl)
                searchResult.remove(exactMatch)
                searchResult.add(0, exactMatch)
            } catch (ignore: Exception) {
                //
            }
            return searchResult
        }

        private fun buildClient(url: String): CloseableHttpClient {
            return HttpClientBuilder.create()
                .setDefaultRequestConfig(getRequestConfig(url))
                .build()
        }

        private fun getRequestConfig(url: String): RequestConfig {
            val builder = RequestConfig.custom()
                .setConnectTimeout(3000)
                .setSocketTimeout(3000)
                .setCookieSpec(CookieSpecs.STANDARD)
            IdeHttpClientHelpers.ApacheHttpClient4.setProxyForUrlIfEnabled(builder, url);
            return builder.build();
        }
    }
}

@Serializable
data class RawHexResponseMeta(
    val links: Map<String, String>,
    val description: String,
    val licenses: List<String>,
    val maintainers: List<String>,
)

@Serializable
data class RawHexRelease(
    val version: String,
    val url: String,
    @SerialName("has_docs") val hasDocs: Boolean,
    @SerialName("inserted_at") val insertedAt: String,
)

@Serializable
data class RawHexDownloads(
    val all: Int,
    val recent: Int,
    val week: Int,
)

@Serializable
data class GleamPackage(
    val meta: RawHexResponseMeta,
    val name: String,
    val url: String,
    @SerialName("updated_at") val updatedAt: String,
    val repository: String,
    val releases: List<RawHexRelease>,
    val downloads: RawHexDownloads,
    @SerialName("latest_version") val latestVersion: String,
    val requirements: Map<String, String> = mapOf(),
    val configs: Map<String, String>,
    @SerialName("docs_html_url") val docsHtmlUrl: String?,
    @SerialName("html_url") val htmlUrl: String?,
    @SerialName("latest_stable_version") val latestStableVersion: String?,
)


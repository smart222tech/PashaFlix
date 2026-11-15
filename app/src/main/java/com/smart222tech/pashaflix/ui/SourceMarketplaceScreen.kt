package com.smart222tech.pashaflix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smart222tech.pashaflix.source.model.SourceExtension
import com.smart222tech.pashaflix.source.model.SourceRepository
import com.smart222tech.pashaflix.source.repository.ExtensionRepositoryClient
import com.smart222tech.pashaflix.source.repository.InstalledRepositoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SourceMarketplaceScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val client = remember { ExtensionRepositoryClient() }
    val store = remember { InstalledRepositoryStore(context) }
    val repositories = remember { mutableStateListOf<SourceRepository>() }
    var repositoryUrl by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Загружаю официальный каталог…") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loading = true
        try {
            val bundled = withContext(Dispatchers.IO) {
                context.assets.open("extensions/pashaflix-official-repo.json")
                    .bufferedReader().use { it.readText() }
            }
            repositories.add(client.parseDocument(bundled, "PashaFlix Official"))
            for (url in store.getAll()) {
                runCatching { client.load(url) }.onSuccess { repositories.add(it) }
            }
            message = "Каталоги загружены"
        } catch (error: Exception) {
            message = error.localizedMessage ?: "Не удалось загрузить каталоги"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFF0D1117)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack) { Text("‹ Назад") }
            Text("Extension Marketplace", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
        Text("JSON-репозитории и Stremio Add-ons", color = Color(0xFF9AA9B8))
        OutlinedTextField(
            value = repositoryUrl,
            onValueChange = { repositoryUrl = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("HTTPS Repository URL или Stremio manifest URL") },
            placeholder = { Text("https://example.org/manifest.json") }
        )
        Button(
            onClick = {
                val url = repositoryUrl.trim()
                if (url.isNotBlank() && !loading) {
                    scope.launch {
                        loading = true
                        message = "Проверяю репозиторий…"
                        try {
                            val loaded = client.load(url)
                            require(loaded.extensions.isNotEmpty()) {
                                "В репозитории не найдено поддерживаемых расширений"
                            }
                            store.add(url)
                            repositories.removeAll { existing ->
                                existing.name == loaded.name && existing.website == loaded.website
                            }
                            repositories.add(loaded)
                            repositoryUrl = ""
                            message = "Добавлено: ${loaded.name}"
                        } catch (error: Exception) {
                            message = error.localizedMessage ?: "Ошибка подключения"
                        } finally {
                            loading = false
                        }
                    }
                }
            },
            enabled = repositoryUrl.isNotBlank() && !loading
        ) { Text("＋ Добавить источник") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (loading) CircularProgressIndicator()
            Text(message, color = Color(0xFF9AA9B8), modifier = Modifier.padding(top = 8.dp))
        }
        Text("УСТАНОВЛЕННЫЕ РАСШИРЕНИЯ", color = Color(0xFF28A9E8), style = MaterialTheme.typography.labelLarge)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(repositories.flatMap { it.extensions }, key = { it.id }) { extension ->
                ExtensionCard(extension)
            }
        }
    }
}

@Composable
private fun ExtensionCard(extension: SourceExtension) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18212B))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(extension.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(extension.description, color = Color(0xFF9AA9B8), style = MaterialTheme.typography.bodySmall)
            Text(
                "${extension.language}  •  ${extension.engine.type}  •  ${extension.version}",
                color = Color(0xFF28A9E8),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

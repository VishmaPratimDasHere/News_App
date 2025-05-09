package com.vishma_app_dev.news_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.vishma_app_dev.news_app.data_sources.ViewModel1
import com.vishma_app_dev.news_app.favourites.FavNewsEntity
import com.vishma_app_dev.news_app.network.Article
import com.vishma_app_dev.news_app.network.Source
import com.vishma_app_dev.news_app.ui.theme.News_AppTheme

// added by akash for ML translator
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            News_AppTheme {
                NewsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onTap: () -> Unit = {},
    showSearch: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            if (showSearch) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = { Text("Search…") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Search Icon")
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                Text("News App")
            }
        },
        navigationIcon = {
            IconButton(onClick = onTap) {
                Icon(Icons.Filled.Home, contentDescription = "Home")
            }
        },
        actions = {
            IconButton(onClick = onToggleSearch) {
                Icon(Icons.Filled.Search, contentDescription = "Toggle Search")
            }
        }
    )
}

@Composable
fun NewsApp() {
    val context = LocalContext.current
    val vm: ViewModel1 = viewModel { ViewModel1(context) }
    var showBookmarks by rememberSaveable { mutableStateOf(false) }

    if (showBookmarks) {
        BookmarkScreen(viewModel = vm, onBack = { showBookmarks = false })
    } else {
        HomeScreen(viewModel = vm, onBookmarksClicked = { showBookmarks = true })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ViewModel1,
    onBookmarksClicked: () -> Unit
) {
    val categories = listOf(
        "business", "entertainment", "general",
        "health", "science", "sports", "technology"
    )

    var selectedCategory by rememberSaveable { mutableStateOf(categories[0]) }
    var page by rememberSaveable { mutableStateOf(1) }
    var dialogArticle by remember { mutableStateOf<Article?>(null) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCategory, page, query) {
        if (query.isNotBlank()) {
            viewModel.search(query)
            page = 1
        } else {
            viewModel.getNewsByCategory(selectedCategory, page)
        }
    }

    val newsItems by viewModel.news.collectAsState()

    Scaffold(
        topBar = {
            HomeTopBar(
                onTap = { selectedCategory = categories[0] },
                showSearch = showSearch,
                query = query,
                onQueryChange = {
                    query = it
                    page = 1
                    viewModel.search(it)
                },
                onToggleSearch = {
                    if (showSearch && query.isNotBlank()) {
                        query = ""
                        page = 1
                        viewModel.getNewsByCategory(selectedCategory, page)
                    }
                    showSearch = !showSearch
                }
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(84.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Button(
                    onClick = { if (page > 1) page-- },
                    enabled = page > 1
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Previous")
                    Spacer(Modifier.width(4.dp))
                    Text("Prev")
                }
                Text("Page $page")
                Button(onClick = { page++ }) {
                    Text("Next")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .offset(y = (-40).dp)
                    .height(40.dp),
                text = { Text("Bookmarks") },
                icon = {
                    Icon(Icons.Filled.FavoriteBorder, contentDescription = "Bookmarks")
                },
                onClick = onBookmarksClicked
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                edgePadding = 8.dp
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = category == selectedCategory,
                        onClick = {
                            selectedCategory = category
                            page = 1
                            query = ""
                        },
                        text = { Text(category) }
                    )
                }
            }

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = {
                    isRefreshing = true
                    if (query.isNotBlank()) viewModel.search(query)
                    else viewModel.getNewsByCategory(selectedCategory, page)
                    isRefreshing = false
                }
            ) {
                LazyColumn(Modifier.weight(1f)) {
                    items(newsItems) { item ->
                        MiniNewsItem(
                            newsItem = item,
                            viewModel = viewModel,
                            onClick = { dialogArticle = item }
                        )
                    }
                }
            }
        }

        dialogArticle?.let { art ->
            AlertDialog(
                onDismissRequest = { dialogArticle = null },
                confirmButton = {
                    TextButton(onClick = { dialogArticle = null }) {
                        Text("Close")
                    }
                },
                text = {
                    NewsDetailContent(art, onClose = {})
                }
            )
        }
    }
}

@Composable
fun MiniNewsItem(
    newsItem: Article,
    viewModel: ViewModel1,
    onClick: () -> Unit = {}
) {
    val isFavorite by viewModel
        .isFavorite(newsItem.url.orEmpty())
        .collectAsState(initial = false)

    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = newsItem.urlToImage,
                contentDescription = newsItem.title,
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = newsItem.title.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = newsItem.source.name.orEmpty(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = {
                if (isFavorite) viewModel.delete(newsItem.toFavEntity())
                else viewModel.insert(newsItem.toFavEntity())
            }) {
                Icon(
                    imageVector =
                    if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription =
                    if (isFavorite) "Unfavorite" else "Favorite"
                )
            }
        }
    }
}

private fun Article.toFavEntity() = FavNewsEntity(
    source      = this.source.name,
    urlToImage  = this.urlToImage,
    url         = this.url.orEmpty(),
    title       = this.title,
    author      = listOf(this.author.orEmpty()),
    description = this.description
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    viewModel: ViewModel1,
    onBack: () -> Unit
) {
    val favs by viewModel.getAllFavorites().collectAsState(initial = emptyList())
    var isReading by remember { mutableStateOf<FavNewsEntity?>(null) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bookmarks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (favs.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No bookmarks yet")
            }
        } else {
            if (isReading!=null){
                BasicAlertDialog(
                    onDismissRequest = {
                        isReading=null
                    },
                ) {
                    Card {
                        NewsDetailContent(
                            Article(
                                source= Source(isReading?.source ?: ""),
                                author = (isReading?.author ?: "").toString(),
                                title = isReading?.title ?: "",
                                url = isReading?.url ?: "",
                                urlToImage = isReading?.urlToImage ?: "",
                                description = isReading?.description ?: ""
                            )
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                isReading=null
                            }
                        ) {
                            Text("close")
                        }
                    }
                }
            }
            LazyColumn(Modifier.padding(padding)) {
                items(favs) { fav ->
                    FavNewsItem(fav = fav, viewModel = viewModel, onClick={
                        isReading=fav
                    })
                }
            }
        }
    }
}

@Composable
fun FavNewsItem(
    fav: FavNewsEntity,
    viewModel: ViewModel1,
    onClick: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onClick()
            },
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = fav.urlToImage,
                contentDescription = fav.title,
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = fav.title.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = fav.source.orEmpty(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = { viewModel.delete(fav) }) {
                Icon(Icons.Filled.Favorite, contentDescription = "Remove bookmark")
            }
        }
    }
}

@Composable
fun NewsDetailContent(
    article: Article,
    onClose: () -> Unit ={}
) {
    val context = LocalContext.current

    // TTS (so your Read-Aloud still works)
    val tts = remember {
        TextToSpeech(context) { /* no-op */ }
            .apply { setLanguage(Locale.getDefault()) }
    }

    // ML Kit translator setup
    val translator = remember {
        val opts = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.HINDI)
            .build()
        Translation.getClient(opts)
    }

    var showingHindi by rememberSaveable { mutableStateOf(false) }
    var translatedDescription by rememberSaveable { mutableStateOf("") }
    var isTranslating by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // — Your existing image
        AsyncImage(
            model = article.urlToImage,
            contentDescription = article.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(8.dp))

        // — Your existing title
        Text(
            text = article.title.orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        // — Description swaps between English & Hindi
        Text(
            text = if (showingHindi && translatedDescription.isNotBlank())
                translatedDescription
            else
                article.description.orEmpty(),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))

        // — Read-Aloud button (unchanged)
        Button(onClick = {
            val toSpeak = "${article.title}. ${article.description.orEmpty()}"
            tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        }) {
            Icon(Icons.Filled.AddCircle, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Read Aloud")
        }

        Spacer(Modifier.width(8.dp))

        // — Translate / Show English toggle
        Button(
            onClick = {
                if (!showingHindi) {
                    isTranslating = true
                    scope.launch(Dispatchers.IO) {
                        try {
                            translator.downloadModelIfNeeded().await()
                            translatedDescription = translator
                                .translate(article.description.orEmpty())
                                .await()
                            isTranslating = false
                            showingHindi = true
                        } catch (e: Exception){
                            Log.d("Translation Error","Trsndlstion failed", e)
                        }
                    }
                } else {
                    showingHindi = false
                }
            },
            enabled = !isTranslating
        ) {
            Text(
                when {
                    showingHindi  -> "Show English"
                    isTranslating -> "Translating…"
                    else          -> "Translate"
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // — For more info link
        Text(
            "For more info: click here",
            color = Color.Blue,
            modifier = Modifier.clickable {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        // — Close button (unchanged)
        TextButton(onClick = onClose) {
            Text("Close")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    News_AppTheme {
        NewsApp()
    }
}

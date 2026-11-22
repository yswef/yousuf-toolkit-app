package feature.library

import core.design.rememberUndoHost
import core.design.SwipeActionRow
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.FormDialog
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onBack: () -> Unit, onPdf: () -> Unit = {}) {
    val store = LocalStore.current
    val undo = rememberUndoHost()
    var adding by remember { mutableStateOf(false) }
    val books = store.books

    ScreenScaffold(
        title = stringResource(R.string.feature_library),
        onBack = onBack,
        actions = {
            TextButton(onClick = onPdf) { Text(stringResource(R.string.feature_pdf)) }
            IconButton(onClick = { adding = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (books.isEmpty()) {
                item {
                    Image(
                        painter = painterResource(R.drawable.empty_library),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(24.dp))
                    )
                    Text(
                        stringResource(R.string.library_none),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
            items(books, key = { it.id }) { book ->
                val remove = { store.deleteBook(book.id); undo.deleted { store.restoreBook(book) } }
                SwipeActionRow(Modifier.animateItem(), onSwipeEnd = remove) {
                    BookRow(book = book, onMore = { store.readMore(book.id, 10) }, onDelete = remove)
                }
            }
        }
    }

    if (adding) {
        BookDialog(
            onDismiss = { adding = false },
            onAdd = { title, pages -> store.addBook(title, pages) }
        )
    }
}

@Composable
private fun BookRow(book: Book, onMore: () -> Unit, onDelete: () -> Unit) {
    val done = book.page >= book.pages
    val ratio = if (book.pages > 0) (book.page.toFloat() / book.pages).coerceIn(0f, 1f) else 0f

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(book.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.library_progress, book.page, book.pages),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (done) {
                    Text(
                        stringResource(R.string.library_done),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth()
            )
            if (!done) {
                TextButton(onClick = onMore) {
                    Text(stringResource(R.string.library_more))
                }
            }
        }
    }
}

@Composable
private fun BookDialog(onDismiss: () -> Unit, onAdd: (String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }
    val parsed = pages.toIntOrNull()

    FormDialog(
        title = stringResource(R.string.library_new),
        confirm = stringResource(R.string.add),
        onDismiss = onDismiss,
        onConfirm = {
            parsed?.let { onAdd(title, it) }
            onDismiss()
        },
        valid = title.isNotBlank() && parsed != null && parsed > 0
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.library_book)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = pages,
            onValueChange = { pages = it },
            label = { Text(stringResource(R.string.library_pages)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

package com.kynzai.kinotes_kotlin_compose

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Add
import androidx.compose.material3.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kynzai.kinotes_kotlin_compose.ui.theme.KiNotesKotlinComposeTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KiNotesKotlinComposeTheme(dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                NotesApp()
            }
        }
    }
}

private val formatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())

private data class Note(
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesApp() {
    var titleInput by rememberSaveable { mutableStateOf("") }
    var contentInput by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable {
        mutableStateOf(
            listOf(
                Note(
                    title = "Добро пожаловать!",
                    content = "Добавляйте свои заметки и держите их под рукой.",
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = {
                            if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
                                notes = listOf(Note(titleInput.trim(), contentInput.trim())) + notes
                                titleInput = ""
                                contentInput = ""
                            }
                        },
                        enabled = titleInput.isNotBlank() && contentInput.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.add_note)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NoteEditor(
                title = titleInput,
                content = contentInput,
                onTitleChange = { titleInput = it },
                onContentChange = { contentInput = it },
                onAddNote = {
                    if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
                        notes = listOf(Note(titleInput.trim(), contentInput.trim())) + notes
                        titleInput = ""
                        contentInput = ""
                    }
                }
            )

            NoteList(
                notes = notes,
                onRemoveNote = { note -> notes = notes.filterNot { it.timestamp == note.timestamp } },
                contentPadding = PaddingValues(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun NoteEditor(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onAddNote: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(text = stringResource(id = R.string.note_title)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text(text = stringResource(id = R.string.note_body)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Button(
            onClick = onAddNote,
            enabled = title.isNotBlank() && content.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.create_note))
        }
    }
}

@Composable
private fun NoteList(
    notes: List<Note>,
    onRemoveNote: (Note) -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = contentPadding
    ) {
        items(notes, key = { it.timestamp }) { note ->
            NoteCard(note = note, onRemove = { onRemoveNote(note) })
        }
    }
}

@Composable
private fun NoteCard(note: Note, onRemove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatter.format(Instant.ofEpochMilli(note.timestamp)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete_note)
                )
            }
        }
    }
}


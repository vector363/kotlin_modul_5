package com.example.modul_5

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modul_5.ui.theme.Modul_5Theme
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Modul_5Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiaryApp()
                }
            }
        }
    }
}

@Composable
fun DiaryApp(){
    val navController = rememberNavController()

    val viewModel: DiaryViewModel = viewModel(
        factory = DiaryViewModelFactory(LocalContext.current.applicationContext as Application)
    )

    NavHost(
        navController = navController,
        startDestination = "diary"
    ) {
        composable("diary") {
            DiaryScreen(
                viewModel = viewModel,
                onAddClick = {
                    navController.navigate("new_entry")
                },
                onEntryClick = { fileName ->
                    navController.navigate("entry/$fileName")
                }
            )
        }

        composable("new_entry") {
            AddEntryScreen(
                onSave = { text ->
                    viewModel.addEntry(text)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("entry/{fileName}") { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            EntryDetailScreen(
                viewModel = viewModel,
                fileName = fileName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel,
    onAddClick: () -> Unit,
    onEntryClick: (String) -> Unit
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заметки") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить запись",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 25.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = entries,
                key = { it.fileName }
            ) { entry ->
                DiaryItem(
                    entry = entry,
                    onClick = { onEntryClick(entry.fileName) },
                    onDelete = {
                        viewModel.deleteEntry(entry.fileName)
                    }
                )
            }
        }
    }
}


@Composable
fun DiaryItem(
    entry: DiaryEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit
){
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatDate(entry.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = entry.content.take(40) + if (entry.content.length > 40) "..." else "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    onSave: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var text by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактор записей") },

                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },

                actions = {
                    TextButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                onSave(text)
                                onNavigateBack()
                            }
                        },
                        enabled = text.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Сохранить",
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Сохранить")
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Напишите что-нибудь...") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                shape = MaterialTheme.shapes.medium
            )

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    viewModel: DiaryViewModel,
    fileName: String,
    onNavigateBack: () -> Unit
) {
    // Находим запись по fileName
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val entry = entries.find { it.fileName == fileName }

    var text by remember { mutableStateOf(entry?.content ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    if (entry == null) {
        // Если запись не найдена, возвращаемся
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Редактирование" else "Просмотр записи")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        // Кнопка сохранения при редактировании
                        TextButton(
                            onClick = {
                                if (text.isNotBlank() && text != entry.content) {
                                    viewModel.updateEntry(fileName, text)
                                }
                                isEditing = false
                            },
                            enabled = text.isNotBlank() && text != entry.content
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Сохранить",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Сохранить")
                        }
                    } else {
                        // Кнопка редактирования в режиме просмотра
                        TextButton(
                            onClick = { isEditing = true }
                        ) {
                            Text("Редактировать")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Дата создания
            Text(
                text = "Создано: ${formatDate(entry.timestamp)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isEditing) {
                // Режим редактирования
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text("Введите текст...") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            } else {
                // Режим просмотра
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = entry.content,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return format.format(date)
}

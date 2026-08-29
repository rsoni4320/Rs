package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.productivityTab.collectAsState()

    val memories by viewModel.memories.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val shortcuts by viewModel.shortcuts.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val tabTitles = listOf("MEMORIES", "NOTES", "TASKS", "REMINDERS", "SHORTCUTS")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
            .statusBarsPadding()
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PRODUCTIVITY & MEMORY",
                    color = ElectricCyan,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "NEURAL DATABASE & REPOSITORIES",
                    color = SecondaryText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("add_item_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = DeepBackground, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD ${tabTitles[currentTab].dropLast(1)}", color = DeepBackground, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Selector Row
        ScrollableTabRow(
            selectedTabIndex = currentTab,
            containerColor = SurfaceCard,
            contentColor = ElectricCyan,
            edgePadding = 0.dp,
            divider = {},
            indicator = {},
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(0.5.dp, CyanBorder, RoundedCornerShape(10.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = currentTab == index
                Box(
                    modifier = Modifier
                        .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { viewModel.setProductivityTab(index) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) ElectricCyan else SecondaryText,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter ${tabTitles[currentTab].lowercase()}...", fontSize = 11.sp, color = DimText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = ElectricCyan, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SecondaryText, modifier = Modifier.size(14.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = CyanBorder,
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (currentTab) {
                0 -> MemoriesTab(
                    memories = memories.filter { it.content.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) },
                    onDelete = { viewModel.deleteMemory(it) },
                    onClearAll = { viewModel.clearMemories() }
                )
                1 -> NotesTab(
                    notes = notes.filter { it.title.contains(searchQuery, ignoreCase = true) || it.content.contains(searchQuery, ignoreCase = true) },
                    onDelete = { viewModel.deleteNote(it) }
                )
                2 -> TasksTab(
                    tasks = tasks.filter { it.title.contains(searchQuery, ignoreCase = true) },
                    onToggle = { viewModel.toggleTask(it) },
                    onDelete = { viewModel.deleteTask(it) }
                )
                3 -> RemindersTab(
                    reminders = reminders.filter { it.title.contains(searchQuery, ignoreCase = true) },
                    onToggle = { viewModel.toggleReminder(it) },
                    onDelete = { viewModel.deleteReminder(it) }
                )
                4 -> ShortcutsTab(
                    shortcuts = shortcuts.filter { it.name.contains(searchQuery, ignoreCase = true) },
                    onLaunch = { shortcut ->
                        val pkgIntent = context.packageManager.getLaunchIntentForPackage(shortcut.packageName)
                        if (pkgIntent != null) {
                            pkgIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(pkgIntent)
                        } else if (shortcut.webUrl.isNotBlank()) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(shortcut.webUrl)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(webIntent)
                        }
                    },
                    onDelete = { viewModel.deleteShortcut(it) }
                )
            }
        }
    }

    // Modal Add Dialog
    if (showAddDialog) {
        AddProductivityItemDialog(
            tabIndex = currentTab,
            onDismiss = { showAddDialog = false },
            onAddMemory = { cat, content ->
                viewModel.addMemory(cat, content)
                showAddDialog = false
            },
            onAddNote = { title, content ->
                viewModel.addNote(title, content)
                showAddDialog = false
            },
            onAddTask = { title, priority ->
                viewModel.addTask(title, priority)
                showAddDialog = false
            },
            onAddReminder = { title, time ->
                viewModel.addReminder(title, time)
                showAddDialog = false
            },
            onAddShortcut = { name, pkg, url, cat ->
                viewModel.addShortcut(name, pkg, url, cat)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MemoriesTab(
    memories: List<MemoryEntity>,
    onDelete: (MemoryEntity) -> Unit,
    onClearAll: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (memories.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                    Text("No stored neural memories found.", color = SecondaryText, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("USER CONTEXT & PREFERENCES (${memories.size})", color = PurpleAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(
                        text = "CLEAR ALL",
                        color = ErrorRed,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { onClearAll() }
                    )
                }
            }
        }

        items(memories) { memory ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(0.5.dp, PurpleAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .background(PurpleAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(memory.category, color = PurpleAccent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(memory.content, color = PrimaryText, fontSize = 13.sp)
                }
                IconButton(onClick = { onDelete(memory) }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun NotesTab(
    notes: List<NoteEntity>,
    onDelete: (NoteEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (notes.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                    Text("No notes found. Say 'Jarvis create note...' to record one.", color = SecondaryText, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        items(notes) { note ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(0.5.dp, CyanBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(note.title, color = ElectricCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(note.content, color = PrimaryText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(dateFormat.format(Date(note.timestamp)), color = SecondaryText, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                IconButton(onClick = { onDelete(note) }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun TasksTab(
    tasks: List<TaskEntity>,
    onToggle: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (tasks.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                    Text("No tasks active. Say 'Jarvis create a task...' to schedule one.", color = SecondaryText, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        items(tasks) { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (task.isCompleted) SurfaceCard.copy(alpha = 0.5f) else SurfaceCard)
                    .border(
                        0.5.dp,
                        if (task.isCompleted) DimText else ElectricCyan.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onToggle(task) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggle(task) },
                        colors = CheckboxDefaults.colors(checkedColor = SuccessGreen, uncheckedColor = ElectricCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = task.title,
                            color = if (task.isCompleted) SecondaryText else PrimaryText,
                            fontSize = 13.sp,
                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium
                        )
                        Text(
                            text = "Priority: ${task.priority}",
                            color = if (task.priority == "High") WarningAmber else SecondaryText,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                IconButton(onClick = { onDelete(task) }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun RemindersTab(
    reminders: List<ReminderEntity>,
    onToggle: (ReminderEntity) -> Unit,
    onDelete: (ReminderEntity) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (reminders.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                    Text("No scheduled reminders. Say 'Jarvis create a reminder...'", color = SecondaryText, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        items(reminders) { reminder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(0.5.dp, if (reminder.isCompleted) DimText else TechBlue, RoundedCornerShape(10.dp))
                    .clickable { onToggle(reminder) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.Alarm,
                        contentDescription = "Status",
                        tint = if (reminder.isCompleted) SuccessGreen else ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(reminder.title, color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Scheduled: ${reminder.scheduledTime}", color = SecondaryText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                IconButton(onClick = { onDelete(reminder) }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ShortcutsTab(
    shortcuts: List<AppShortcutEntity>,
    onLaunch: (AppShortcutEntity) -> Unit,
    onDelete: (AppShortcutEntity) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(
                text = "CONFIGURED SHORTCUTS & DEEP LINKS",
                color = ElectricCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        items(shortcuts) { shortcut ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(0.5.dp, CyanBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).clickable { onLaunch(shortcut) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TechBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = "Open", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(shortcut.name, color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(shortcut.webUrl, color = SecondaryText, fontSize = 10.sp, maxLines = 1)
                    }
                }

                Row {
                    IconButton(onClick = { onLaunch(shortcut) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Launch", tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { onDelete(shortcut) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddProductivityItemDialog(
    tabIndex: Int,
    onDismiss: () -> Unit,
    onAddMemory: (String, String) -> Unit,
    onAddNote: (String, String) -> Unit,
    onAddTask: (String, String) -> Unit,
    onAddReminder: (String, String) -> Unit,
    onAddShortcut: (String, String, String, String) -> Unit
) {
    var field1 by remember { mutableStateOf("") }
    var field2 by remember { mutableStateOf("") }
    var field3 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCardElevated,
        title = {
            Text(
                text = when (tabIndex) {
                    0 -> "RECORD MEMORY"
                    1 -> "CREATE NOTE"
                    2 -> "CREATE TASK"
                    3 -> "SCHEDULE REMINDER"
                    else -> "ADD SHORTCUT"
                },
                color = ElectricCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (tabIndex) {
                    0 -> {
                        OutlinedTextField(
                            value = field1,
                            onValueChange = { field1 = it },
                            label = { Text("Category (e.g. Preference, Fact)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = field2,
                            onValueChange = { field2 = it },
                            label = { Text("Memory Fact / Context") }
                        )
                    }
                    1 -> {
                        OutlinedTextField(
                            value = field1,
                            onValueChange = { field1 = it },
                            label = { Text("Note Title") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = field2,
                            onValueChange = { field2 = it },
                            label = { Text("Note Content") }
                        )
                    }
                    2 -> {
                        OutlinedTextField(
                            value = field1,
                            onValueChange = { field1 = it },
                            label = { Text("Task Description") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = field2,
                            onValueChange = { field2 = it },
                            label = { Text("Priority (High / Normal / Low)") },
                            singleLine = true
                        )
                    }
                    3 -> {
                        OutlinedTextField(
                            value = field1,
                            onValueChange = { field1 = it },
                            label = { Text("Reminder Title") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = field2,
                            onValueChange = { field2 = it },
                            label = { Text("Scheduled Time (e.g. 5:00 PM)") },
                            singleLine = true
                        )
                    }
                    4 -> {
                        OutlinedTextField(
                            value = field1,
                            onValueChange = { field1 = it },
                            label = { Text("Shortcut Name (e.g. GitHub)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = field2,
                            onValueChange = { field2 = it },
                            label = { Text("Web URL (e.g. https://github.com)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = field3,
                            onValueChange = { field3 = it },
                            label = { Text("Android Package (Optional)") },
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (tabIndex) {
                        0 -> if (field2.isNotBlank()) onAddMemory(field1.ifBlank { "General" }, field2)
                        1 -> if (field1.isNotBlank()) onAddNote(field1, field2)
                        2 -> if (field1.isNotBlank()) onAddTask(field1, field2.ifBlank { "Normal" })
                        3 -> if (field1.isNotBlank()) onAddReminder(field1, field2.ifBlank { "Today" })
                        4 -> if (field1.isNotBlank() && field2.isNotBlank()) onAddShortcut(field1, field3, field2, "custom")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("SAVE", color = DeepBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = SecondaryText)
            }
        }
    )
}

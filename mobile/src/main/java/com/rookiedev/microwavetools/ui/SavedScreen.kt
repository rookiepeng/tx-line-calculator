package com.rookiedev.microwavetools.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rookiedev.microwavetools.R
import java.text.DateFormat
import java.util.Date

/** Lists saved designs; tapping one opens it in its calculator. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(store: DesignStore, onOpenDrawer: (() -> Unit)?, onLoad: (SavedDesign) -> Unit) {
    var designs by remember { mutableStateOf(store.all()) }
    var renaming by remember { mutableStateOf<SavedDesign?>(null) }
    var deleting by remember { mutableStateOf<SavedDesign?>(null) }
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.saved_title)) },
                navigationIcon = {
                    if (onOpenDrawer != null) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(painterResource(R.drawable.ic_menu), stringResource(R.string.navigation_drawer_open))
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (designs.isEmpty()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(painterResource(R.drawable.ic_bookmark), contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                Text(
                    stringResource(R.string.saved_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        } else {
            LazyColumn(
                Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            ) {
                items(designs, key = { it.id }) { d ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLoad(d) },
                    ) {
                        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(d.line.navIcon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                            ) {
                                Text(d.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(d.line.title) + " · " + dateFormat.format(Date(d.savedAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { renaming = d }) {
                                Icon(painterResource(R.drawable.ic_edit), stringResource(R.string.saved_rename))
                            }
                            IconButton(onClick = { deleting = d }) {
                                Icon(painterResource(R.drawable.ic_delete), stringResource(R.string.saved_delete))
                            }
                        }
                    }
                }
            }
        }
    }

    renaming?.let { d ->
        NameDialog(
            title = stringResource(R.string.saved_rename),
            initial = d.name,
            onConfirm = { store.rename(d.id, it); designs = store.all(); renaming = null },
            onDismiss = { renaming = null },
        )
    }
    deleting?.let { d ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.saved_delete_title)) },
            text = { Text(stringResource(R.string.saved_delete_message, d.name)) },
            confirmButton = {
                TextButton(onClick = { store.delete(d.id); designs = store.all(); deleting = null }) {
                    Text(stringResource(R.string.saved_delete))
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

/** Asks for a design name; the confirm button is disabled while the name is blank. */
@Composable
fun NameDialog(title: String, initial: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.saved_name)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.saved_ok))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

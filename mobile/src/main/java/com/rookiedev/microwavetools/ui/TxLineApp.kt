package com.rookiedev.microwavetools.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.Constants
import kotlinx.coroutines.launch

private const val FEEDBACK_EMAIL = "rookie.dev@gmail.com"

/** App shell: a navigation drawer listing the line types (permanent on wide screens) and the calculator. */
@Composable
fun TxLineApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE) }
    // Earlier versions stored the drawer position and first-run flag as strings; keep that format.
    val firstRun = remember {
        (prefs.getString(Constants.PREFS_ISFIRSTRUN, "true") == "true").also {
            if (it) prefs.edit { putString(Constants.PREFS_ISFIRSTRUN, "false") }
        }
    }
    var current by rememberSaveable {
        val saved = prefs.getString(Constants.PREFS_POSITION, "0")?.toIntOrNull() ?: 0
        mutableStateOf(LineType.entries.getOrElse(saved) { LineType.MLIN })
    }
    val select = { type: LineType ->
        current = type
        prefs.edit { putString(Constants.PREFS_POSITION, type.ordinal.toString()) }
    }

    val vm: CalculatorViewModel = viewModel(
        key = current.name,
        factory = viewModelFactory { initializer { CalculatorViewModel(current, prefs) } },
    )

    val windowSize = LocalWindowInfo.current.containerSize
    val (windowWidth, windowHeight) = with(LocalDensity.current) { windowSize.width.toDp() to windowSize.height.toDp() }
    // Tablets get a permanent drawer; phones (including landscape) use a modal one.
    val wide = windowWidth >= 840.dp && windowHeight >= 600.dp
    val shortScreen = windowHeight < 480.dp
    if (wide) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(Modifier.width(300.dp)) {
                    DrawerContent(current, onSelect = select, onFeedback = { sendFeedback(context) })
                }
            },
        ) {
            key(current) { CalculatorScreen(vm, onOpenDrawer = null, compactTopBar = shortScreen) }
        }
    } else {
        val drawerState = rememberDrawerState(if (firstRun) DrawerValue.Open else DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerState = drawerState) {
                    DrawerContent(
                        current,
                        onSelect = { select(it); scope.launch { drawerState.close() } },
                        onFeedback = { scope.launch { drawerState.close() }; sendFeedback(context) },
                    )
                }
            },
        ) {
            key(current) { CalculatorScreen(vm, onOpenDrawer = { scope.launch { drawerState.open() } }, compactTopBar = shortScreen) }
        }
    }
}

@Composable
private fun DrawerContent(current: LineType, onSelect: (LineType) -> Unit, onFeedback: () -> Unit) {
    Column(
        Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.ic_launcher_background)),
            )
            Spacer(Modifier.width(16.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
        }
        for (type in LineType.entries) {
            NavigationDrawerItem(
                label = { Text(stringResource(type.title)) },
                icon = { Icon(painterResource(type.navIcon), contentDescription = null) },
                selected = type == current,
                onClick = { onSelect(type) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp, horizontal = 16.dp))
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_help)) },
            icon = { Icon(painterResource(R.drawable.ic_help), contentDescription = null) },
            selected = false,
            onClick = onFeedback,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
    }
}

private fun sendFeedback(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO, "mailto:$FEEDBACK_EMAIL".toUri())
        .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.noEmail, Toast.LENGTH_SHORT).show()
    }
}

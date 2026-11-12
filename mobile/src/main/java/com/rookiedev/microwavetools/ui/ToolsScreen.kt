package com.rookiedev.microwavetools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.RfMath

/**
 * One quantity in a group of linked fields: [toBase] parses the user's number into the group's
 * shared base value (null if invalid), [fromBase] shows the base in this field's terms.
 */
private class Linked(val label: Int, val suffix: String?, val toBase: (Double) -> Double?, val fromBase: (Double) -> Double)

/** Mismatch, power and quarter-wave converters. [onDesignMicrostrip] sends a Z0 to microstrip synthesis. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(onOpenDrawer: (() -> Unit)?, onDesignMicrostrip: (Double) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tools_title)) },
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
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ToolCard(stringResource(R.string.tools_mismatch), stringResource(R.string.tools_mismatch_hint)) {
                // Base value: |Γ|.
                LinkedFields(
                    key = "mismatch",
                    initialBase = RfMath.gammaFromVswr(1.5),
                    specs = listOf(
                        Linked(R.string.tools_vswr, null, { RfMath.gammaFromVswr(it) }, { RfMath.vswrFromGamma(it) }),
                        Linked(R.string.tools_gamma, null, { RfMath.gammaValid(it) }, { it }),
                        Linked(R.string.tools_return_loss, "dB", { RfMath.gammaFromReturnLoss(it) }, { RfMath.returnLossFromGamma(it) }),
                    ),
                ) { gamma ->
                    if (gamma != null) {
                        Readout(stringResource(R.string.tools_mismatch_loss), "${resultNumber(RfMath.mismatchLoss(gamma))} dB")
                        Readout(stringResource(R.string.tools_reflected), "${resultNumber(100 * gamma * gamma)} %")
                    }
                }
            }
            ToolCard(stringResource(R.string.tools_power), stringResource(R.string.tools_power_hint)) {
                // Base value: watts.
                LinkedFields(
                    key = "power",
                    initialBase = RfMath.wattsFromDbm(0.0),
                    specs = listOf(
                        Linked(R.string.tools_dbm, "dBm", { RfMath.wattsFromDbm(it) }, { RfMath.dbmFromWatts(it) }),
                        Linked(R.string.tools_mw, "mW", { (it / 1000).takeIf { w -> w > 0 } }, { it * 1000 }),
                        Linked(R.string.tools_vrms, "V", { RfMath.wattsFromVrms(it).takeIf { w -> w > 0 } }, { RfMath.vrmsFromWatts(it) }),
                    ),
                ) {}
            }
            ToolCard(stringResource(R.string.tools_qw), stringResource(R.string.tools_qw_hint)) {
                var z0Text by rememberSaveable { mutableStateOf("50") }
                var zlText by rememberSaveable { mutableStateOf("100") }
                val z0 = z0Text.toDoubleOrNull()?.takeIf { it > 0 }
                val zl = zlText.toDoubleOrNull()?.takeIf { it > 0 }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.tools_qw_z0), z0Text, { z0Text = it }, z0 == null, "Ω", Modifier.weight(1f))
                    NumberField(stringResource(R.string.tools_qw_zl), zlText, { zlText = it }, zl == null, "Ω", Modifier.weight(1f))
                }
                if (z0 != null && zl != null) {
                    val z1 = RfMath.quarterWave(z0, zl)
                    Readout(stringResource(R.string.tools_qw_z1), "${resultNumber(z1)} Ω")
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        FilledTonalButton(onClick = { onDesignMicrostrip(z1) }) {
                            Text(stringResource(R.string.tools_qw_design))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCard(title: String, hint: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            content()
        }
    }
}

/**
 * Fields that all show one shared base value; typing in any of them updates the others.
 * The field being typed in keeps the user's exact text; the others show the converted value.
 */
@Composable
private fun LinkedFields(
    key: String,
    initialBase: Double?,
    specs: List<Linked>,
    extra: @Composable (Double?) -> Unit,
) {
    var base by rememberSaveable(key) { mutableStateOf(initialBase) }
    var editing by rememberSaveable(key) { mutableIntStateOf(-1) }
    var raw by rememberSaveable(key) { mutableStateOf("") }
    specs.forEachIndexed { i, spec ->
        val text = if (i == editing) raw else base?.let { toolNumber(spec.fromBase(it)) } ?: ""
        NumberField(
            label = stringResource(spec.label),
            text = text,
            onChange = { t ->
                editing = i
                raw = t
                base = t.toDoubleOrNull()?.let(spec.toBase)
            },
            isError = i == editing && raw.isNotBlank() && base == null,
            suffix = spec.suffix,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    extra(base)
}

/** Like [resultNumber] but tolerating infinity (a perfect match has infinite return loss). */
private fun toolNumber(v: Double): String = when {
    v.isInfinite() -> "∞"
    v.isNaN() -> ""
    else -> resultNumber(v)
}

@Composable
private fun NumberField(
    label: String,
    text: String,
    onChange: (String) -> Unit,
    isError: Boolean,
    suffix: String?,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}

@Composable
private fun Readout(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

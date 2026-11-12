package com.rookiedev.microwavetools.ui

import android.content.SharedPreferences
import androidx.core.content.edit
import com.rookiedev.microwavetools.R
import org.json.JSONArray
import org.json.JSONObject

/**
 * A saved calculator state. Field keys are [FieldId.prefName], so a design keeps loading after
 * fields are reordered; fields the line no longer has are ignored on load.
 */
data class SavedDesign(
    val id: String,
    val name: String,
    val line: LineType,
    val fields: Map<String, Pair<String, String>>,
    val target: FieldId?,
    val useZ0k: Boolean,
    val savedAt: Long,
)

internal fun SavedDesign.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("line", line.name)
    put("fields", JSONObject().apply {
        for ((key, value) in fields) put(key, JSONObject().put("text", value.first).put("unit", value.second))
    })
    target?.let { put("target", it.name) }
    put("useZ0k", useZ0k)
    put("savedAt", savedAt)
}

/** Parses a design, or null if it is malformed or names a line type this version doesn't have. */
internal fun designFromJson(o: JSONObject): SavedDesign? = runCatching {
    val line = LineType.valueOf(o.getString("line"))
    val f = o.getJSONObject("fields")
    SavedDesign(
        id = o.getString("id"),
        name = o.getString("name"),
        line = line,
        fields = f.keys().asSequence().associateWith { k ->
            f.getJSONObject(k).let { it.getString("text") to it.optString("unit", "") }
        },
        target = o.optString("target", "").takeIf { it.isNotEmpty() }?.let { runCatching { FieldId.valueOf(it) }.getOrNull() },
        useZ0k = o.optBoolean("useZ0k", true),
        savedAt = o.optLong("savedAt", 0L),
    )
}.getOrNull()

internal fun encodeDesigns(designs: List<SavedDesign>): String = JSONArray(designs.map { it.toJson() }).toString()

internal fun decodeDesigns(json: String?): List<SavedDesign> {
    if (json.isNullOrBlank()) return emptyList()
    val arr = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
    return (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let(::designFromJson) }
}

/** Saved designs, newest first, in their own SharedPreferences file. */
class DesignStore(private val prefs: SharedPreferences) {
    private val key = "designs"

    fun all(): List<SavedDesign> = decodeDesigns(prefs.getString(key, null)).sortedByDescending { it.savedAt }

    private fun write(list: List<SavedDesign>) = prefs.edit { putString(key, encodeDesigns(list)) }

    fun add(design: SavedDesign) = write(all() + design)

    fun rename(id: String, name: String) = write(all().map { if (it.id == id) it.copy(name = name) else it })

    fun delete(id: String) = write(all().filterNot { it.id == id })
}

/**
 * A plain-text summary of a calculator screen for sharing: every field with its unit, then the
 * results of the last calculation. [str] resolves string resources (a Context in the app).
 */
fun designSummary(line: LineType, state: CalculatorState, str: (Int) -> String): String = buildString {
    appendLine(str(line.title))
    fun section(title: Int, ids: List<FieldId>) {
        appendLine()
        appendLine(str(title))
        for (id in ids) {
            val f = state.field(id)
            if (f.text.isBlank()) continue
            val unit = f.unit.takeIf { it.isNotEmpty() } ?: id.unitKind.suffix.orEmpty()
            appendLine("  ${str(line.labelOf(id))}: ${f.text} $unit".trimEnd())
        }
    }
    section(R.string.Dim, line.dimensions)
    section(R.string.Par, line.parameters)
    section(R.string.materials_title, MATERIAL_FIELDS)
    if (state.extras.isNotEmpty()) {
        appendLine()
        appendLine(str(R.string.results_title))
        for ((out, value) in state.extras.entries.sortedBy { it.key.ordinal }) {
            appendLine("  ${str(out.label)}: ${formatOutput(out, value, state)}")
        }
    }
}.trimEnd()

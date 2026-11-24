package com.cabinetpro.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local storage equivalent of web/src/utils/storage.ts
 * Keys match web version: cabinet-calculator-current-v1 / drafts-v1
 */
class CabinetStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cabinetpro", Context.MODE_PRIVATE)

    companion object {
        const val CURRENT_KEY = "cabinet-calculator-current-v1"
        const val DRAFTS_KEY = "cabinet-calculator-drafts-v1"
    }

    fun readCurrent(): CabinetForm {
        val raw = prefs.getString(CURRENT_KEY, null) ?: return CabinetForm()
        return try { formFromJson(JSONObject(raw)) } catch (_: Exception) { CabinetForm() }
    }

    fun saveCurrent(form: CabinetForm): Boolean =
        prefs.edit().putString(CURRENT_KEY, formToJson(form).toString()).commit()

    fun readDrafts(): List<Draft> {
        val raw = prefs.getString(DRAFTS_KEY, "[]") ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = o.optString("id").ifBlank { return@mapNotNull null }
                val formJson = o.optJSONObject("form") ?: return@mapNotNull null
                Draft(
                    id = id,
                    name = o.optString("name", "پروژه بدون نام"),
                    savedAt = o.optString("savedAt", ""),
                    form = formFromJson(formJson)
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    fun saveDrafts(drafts: List<Draft>): Boolean {
        val arr = JSONArray()
        drafts.forEach { d ->
            arr.put(JSONObject().apply {
                put("id", d.id); put("name", d.name); put("savedAt", d.savedAt)
                put("form", formToJson(d.form))
            })
        }
        return prefs.edit().putString(DRAFTS_KEY, arr.toString()).commit()
    }

    // --- JSON mapping (mirrors web CabinetForm shape) ---
    private fun formToJson(f: CabinetForm): JSONObject = JSONObject().apply {
        put("name", f.name); put("date", f.date); put("basePrice", f.basePrice)
        put("coefficients", JSONObject().apply {
            SectionKey.values().forEach { put(it.name.lowercase(), f.coefficients[it] ?: 0.0) }
        })
        put("lengths", JSONObject().apply {
            SectionKey.values().forEach { put(it.name.lowercase(), f.lengths[it] ?: 0.0) }
        })
        put("deductions", JSONObject().apply {
            DeductionKey.values().forEach { put(it.name.lowercase(), f.deductions[it] ?: 0.0) }
        })
        put("countertopLength", f.countertopLength); put("countertopRate", f.countertopRate)
        put("accessories", JSONArray().apply {
            f.accessories.forEach { a ->
                put(JSONObject().apply {
                    put("id", a.id); put("name", a.name); put("quantity", a.quantity); put("unitPrice", a.unitPrice)
                })
            }
        })
        put("installationMode", f.installationMode.name.lowercase())
        put("installationValue", f.installationValue)
        put("transportCost", f.transportCost)
        put("discountMode", f.discountMode.name.lowercase())
        put("discountValue", f.discountValue)
        put("taxPercent", f.taxPercent)
        put("doubleTierEnabled", f.doubleTierEnabled)
        put("doubleTier", JSONObject().apply {
            put("tier1Length", f.doubleTier.tier1Length)
            put("tier1Price", f.doubleTier.tier1Price)
            put("tier2Length", f.doubleTier.tier2Length)
            put("tier2Price", f.doubleTier.tier2Price)
        })
        put("coffeeBarEnabled", f.coffeeBarEnabled)
        put("coffeeBar", JSONObject().apply {
            put("basePrice", f.coffeeBar.basePrice)
            put("length", f.coffeeBar.length)
            put("pricePerMeter", f.coffeeBar.pricePerMeter)
            put("countertopLength", f.coffeeBar.countertopLength)
            put("countertopRate", f.coffeeBar.countertopRate)
        })
        put("wardrobeEnabled", f.wardrobeEnabled)
        put("wardrobe", JSONObject().apply {
            put("modelId", f.wardrobe.modelId)
            put("width", f.wardrobe.width)
            put("unitPrice", f.wardrobe.unitPrice)
            put("drawers", f.wardrobe.drawers)
            put("drawerPrice", f.wardrobe.drawerPrice)
            put("shelves", f.wardrobe.shelves)
            put("shelfPrice", f.wardrobe.shelfPrice)
        })
    }

    private fun formFromJson(o: JSONObject): CabinetForm {
        val defaults = CabinetForm()
        fun mapOfSection(obj: JSONObject?, def: Map<SectionKey, Double>): Map<SectionKey, Double> {
            if (obj == null) return def
            return SectionKey.values().associateWith { obj.optDouble(it.name.lowercase(), def[it] ?: 0.0) }
        }
        fun mapOfDeduction(obj: JSONObject?, def: Map<DeductionKey, Double>): Map<DeductionKey, Double> {
            if (obj == null) return def
            return DeductionKey.values().associateWith { obj.optDouble(it.name.lowercase(), def[it] ?: 0.0) }
        }
        val accArr = o.optJSONArray("accessories")
        val accessories = if (accArr != null) (0 until accArr.length()).mapNotNull { i ->
            val a = accArr.optJSONObject(i) ?: return@mapNotNull null
            Accessory(
                id = a.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                name = a.optString("name", ""),
                quantity = a.optDouble("quantity", 0.0),
                unitPrice = a.optDouble("unitPrice", 0.0)
            )
        } else emptyList()

        val dtObj = o.optJSONObject("doubleTier")
        val doubleTier = DoubleTierData(
            tier1Length = dtObj?.optDouble("tier1Length", 0.0) ?: 0.0,
            tier1Price = dtObj?.optDouble("tier1Price", 0.0) ?: 0.0,
            tier2Length = dtObj?.optDouble("tier2Length", 0.0) ?: 0.0,
            tier2Price = dtObj?.optDouble("tier2Price", 0.0) ?: 0.0
        )
        val cbObj = o.optJSONObject("coffeeBar")
        val coffeeBar = CoffeeBarData(
            basePrice = cbObj?.optDouble("basePrice", 0.0) ?: 0.0,
            length = cbObj?.optDouble("length", 0.0) ?: 0.0,
            pricePerMeter = cbObj?.optDouble("pricePerMeter", 0.0) ?: 0.0,
            countertopLength = cbObj?.optDouble("countertopLength", 0.0) ?: 0.0,
            countertopRate = cbObj?.optDouble("countertopRate", 0.0) ?: 0.0
        )
        val wdObj = o.optJSONObject("wardrobe")
        val wardrobe = WardrobeData(
            modelId = wdObj?.optString("modelId", WARDROBE_MODELS.first().id) ?: WARDROBE_MODELS.first().id,
            width = wdObj?.optDouble("width", 0.0) ?: 0.0,
            unitPrice = wdObj?.optDouble("unitPrice", 0.0) ?: 0.0,
            drawers = wdObj?.optInt("drawers", 0) ?: 0,
            drawerPrice = wdObj?.optDouble("drawerPrice", 0.0) ?: 0.0,
            shelves = wdObj?.optInt("shelves", 0) ?: 0,
            shelfPrice = wdObj?.optDouble("shelfPrice", 0.0) ?: 0.0
        )

        return CabinetForm(
            name = o.optString("name", ""),
            date = o.optString("date", todayISO()),
            basePrice = o.optDouble("basePrice", defaults.basePrice),
            coefficients = mapOfSection(o.optJSONObject("coefficients"), defaults.coefficients),
            lengths = mapOfSection(o.optJSONObject("lengths"), defaults.lengths),
            deductions = mapOfDeduction(o.optJSONObject("deductions"), defaults.deductions),
            countertopLength = o.optDouble("countertopLength", 0.0),
            countertopRate = o.optDouble("countertopRate", 0.0),
            accessories = accessories,
            installationMode = if (o.optString("installationMode") == "percent") CostMode.PERCENT else CostMode.FIXED,
            installationValue = o.optDouble("installationValue", 0.0),
            transportCost = o.optDouble("transportCost", 0.0),
            discountMode = if (o.optString("discountMode") == "percent") CostMode.PERCENT else CostMode.FIXED,
            discountValue = o.optDouble("discountValue", 0.0),
            taxPercent = o.optDouble("taxPercent", 0.0),
            doubleTierEnabled = o.optBoolean("doubleTierEnabled", false),
            doubleTier = doubleTier,
            coffeeBarEnabled = o.optBoolean("coffeeBarEnabled", false),
            coffeeBar = coffeeBar,
            wardrobeEnabled = o.optBoolean("wardrobeEnabled", false),
            wardrobe = wardrobe
        )
    }
}

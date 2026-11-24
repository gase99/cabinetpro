package com.cabinetpro.app

import com.cabinetpro.app.data.CabinetForm
import com.cabinetpro.app.data.SectionKey
import com.cabinetpro.app.data.calculateCabinetPrice
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorTest {
    @Test
    fun acceptanceSample_ground3_wall3_tall1_base6500000() {
        val form = CabinetForm() // defaults: base 6.5M, ground 3, wall 3, tall 1
        val r = calculateCabinetPrice(form)
        assertEquals(3.0, r.effectiveMeters[SectionKey.GROUND]!!, 1e-9)
        assertEquals(1.8, r.effectiveMeters[SectionKey.WALL]!!, 1e-9)
        assertEquals(2.0, r.effectiveMeters[SectionKey.TALL]!!, 1e-9)
        assertEquals(6.8, r.effectiveTotal, 1e-9)
        assertEquals(44_200_000.0, r.cabinetCost, 1e-6)
        assertEquals(44_200_000.0, r.total, 1e-6)
    }

    @Test
    fun deductionsOnlyFromGround() {
        val form = CabinetForm().copy(
            deductions = mapOf(
                com.cabinetpro.app.data.DeductionKey.FRIDGE to 1.0,
                com.cabinetpro.app.data.DeductionKey.STOVE to 0.5,
                com.cabinetpro.app.data.DeductionKey.WASHER to 0.0,
                com.cabinetpro.app.data.DeductionKey.OTHER to 0.0
            )
        )
        val r = calculateCabinetPrice(form)
        assertEquals(1.5, r.groundNet, 1e-9) // 3 - 1.5
        assertEquals(1.5, r.effectiveMeters[SectionKey.GROUND]!!, 1e-9)
    }

    @Test
    fun doubleTierModule_formula() {
        val form = CabinetForm().copy(
            doubleTierEnabled = true,
            doubleTier = com.cabinetpro.app.data.DoubleTierData(
                tier1Length = 2.0, tier1Price = 8_000_000.0,
                tier2Length = 1.5, tier2Price = 6_000_000.0
            )
        )
        val r = calculateCabinetPrice(form)
        assertEquals(25_000_000.0, r.doubleTierCost, 1e-6) // 2*8M + 1.5*6M
        assertEquals(25_000_000.0, r.moduleCost, 1e-6)
    }

    @Test
    fun coffeeBarModule_formula() {
        val form = CabinetForm().copy(
            coffeeBarEnabled = true,
            coffeeBar = com.cabinetpro.app.data.CoffeeBarData(
                basePrice = 5_000_000.0,
                length = 2.0, pricePerMeter = 12_000_000.0,
                countertopLength = 2.0, countertopRate = 4_000_000.0
            )
        )
        val r = calculateCabinetPrice(form)
        assertEquals(37_000_000.0, r.coffeeBarCost, 1e-6) // 5M + 2*12M + 2*4M
    }

    @Test
    fun wardrobeModule_modelCoefficient() {
        val form = CabinetForm().copy(
            wardrobeEnabled = true,
            wardrobe = com.cabinetpro.app.data.WardrobeData(
                modelId = "komod_se_dar_malan", // ضریب ۱٫۱۵
                width = 2.0, unitPrice = 9_000_000.0,
                drawers = 2, drawerPrice = 1_600_000.0,
                shelves = 3, shelfPrice = 250_000.0
            )
        )
        val r = calculateCabinetPrice(form)
        assertEquals(20_700_000.0, r.wardrobeDetail.main, 1e-6) // 2 * 9M * 1.15
        assertEquals(3_950_000.0, r.wardrobeDetail.accessories, 1e-6) // 2*1.6M + 3*0.25M
        assertEquals(24_650_000.0, r.wardrobeCost, 1e-6)
        assertEquals("کمد سه درب ملانی", r.wardrobeModelName)
    }

    @Test
    fun inactiveModulesDoNotAffectTotal() {
        val r = calculateCabinetPrice(CabinetForm())
        assertEquals(0.0, r.moduleCost, 1e-9)
        assertEquals(44_200_000.0, r.total, 1e-6)
    }
}

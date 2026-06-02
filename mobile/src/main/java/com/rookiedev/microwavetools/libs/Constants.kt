package com.rookiedev.microwavetools.libs

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.rookiedev.microwavetools.R
import java.util.Objects
import kotlin.math.pow
import kotlin.math.sqrt

object Constants {
    // Shared Preference values
    const val SHARED_PREFS_NAME: String = "com.rookiedev.microwavetools_preferences"
    const val PREFS_POSITION: String = "POSITION"
    const val PREFS_ISFIRSTRUN: String = "ISFIRSTRUN"

    const val DecimalLength: Int = 3

    // constant values
    const val LIGHTSPEED: Double = 2.99792458e8 // free space speed of light, m/s
    val FREESPACE_MU0: Double = 4.0 * Math.PI * 1.0e-7 // free space permitivitty, H/m
    val FREESPACE_E0: Double = 1.0 / (LIGHTSPEED * LIGHTSPEED * FREESPACE_MU0) // free space

    // permitivitty, F/m
    val FREESPACEZ0: Double = FREESPACE_MU0 * LIGHTSPEED // free space impedance, Ohms
    val Pi: Double = Math.PI
    const val MINI_LIMIT: Double = 1e-10 // 0.1 nm

    // flags
    const val Synthesize_Width: Int = 0
    const val Synthesize_Height: Int = 1
    const val Synthesize_Er: Int = 2
    const val Synthesize_Length: Int = 3
    const val Synthesize_Gap: Int = 4
    const val Synthesize_CoreRadius: Int = 5
    const val Synthesize_CoreOffset: Int = 6
    val Synthesize_SubRadius: Int = Synthesize_Height
    const val LengthUnit_mil: String = "mil"
    const val LengthUnit_mm: String = "mm"
    const val LengthUnit_cm: String = "cm"
    const val LengthUnit_m: String = "m"
    const val FreqUnit_MHz: String = "MHz"
    const val FreqUnit_GHz: String = "GHz"
    const val FreqUnit_Hz: String = "Hz"
    const val ImpedanceUnit_Ohm: String = "Ω"
    const val PhaseUnit_Degree: String = "Deg"

    const val PositionMlin: Int = 0
    const val PositionCmlin: Int = 1
    const val PositionSlin: Int = 2
    const val PositionCslin: Int = 3
    const val PositionCpw: Int = 4
    const val PositionGcpw: Int = 5
    const val PositionCoax: Int = 6

    const val IS_AD_FREE: String = "IS_AD_FREE"

    const val MLIN_W: String = "MLIN_W"
    const val MLIN_W_UNIT: String = "MLIN_W_UNIT"
    const val MLIN_L: String = "MLIN_L"
    const val MLIN_L_UNIT: String = "MLIN_L_UNIT"
    const val MLIN_Z0: String = "MLIN_Z0"
    const val MLIN_Z0_UNIT: String = "MLIN_Z0_UNIT"
    const val MLIN_PHS: String = "MLIN_PHS"
    const val MLIN_PHS_UNIT: String = "MLIN_PHS_UNIT"
    const val MLIN_FREQ: String = "MLIN_FREQ"
    const val MLIN_FREQ_UNIT: String = "MLIN_FREQ_UNIT"
    const val MLIN_ER: String = "MLIN_ER"
    const val MLIN_H: String = "MLIN_H"
    const val MLIN_H_UNIT: String = "MLIN_H_UNIT"
    const val MLIN_T: String = "MLIN_T"
    const val MLIN_T_UNIT: String = "MLIN_T_UNIT"
    const val MLIN_TARGET: String = "MLIN_TARGET"

    const val CMLIN_W: String = "CMLIN_W"
    const val CMLIN_W_UNIT: String = "CMLIN_W_UNIT"
    const val CMLIN_S: String = "CMLIN_S"
    const val CMLIN_S_UNIT: String = "CMLIN_S_UNIT"
    const val CMLIN_L: String = "CMLIN_L"
    const val CMLIN_L_UNIT: String = "CMLIN_L_UNIT"
    const val CMLIN_Z0: String = "CMLIN_Z0"
    const val CMLIN_Z0_UNIT: String = "CMLIN_Z0_UNIT"
    const val CMLIN_K: String = "CMLIN_K"
    const val CMLIN_Z0O: String = "CMLIN_Z0O"
    const val CMLIN_Z0O_UNIT: String = "CMLIN_Z0O_UNIT"
    const val CMLIN_Z0E: String = "CMLIN_Z0E"
    const val CMLIN_Z0E_UNIT: String = "CMLIN_Z0E_UNIT"
    const val CMLIN_PHS: String = "CMLIN_PHS"
    const val CMLIN_PHS_UNIT: String = "CMLIN_PHS_UNIT"
    const val CMLIN_FREQ: String = "CMLIN_FREQ"
    const val CMLIN_FREQ_UNIT: String = "CMLIN_FREQ_UNIT"
    const val CMLIN_ER: String = "CMLIN_ER"
    const val CMLIN_H: String = "CMLIN_H"
    const val CMLIN_H_UNIT: String = "CMLIN_H_UNIT"
    const val CMLIN_T: String = "CMLIN_T"
    const val CMLIN_T_UNIT: String = "CMLIN_T_UNIT"
    const val CMLIN_USEZ0K: String = "CMLIN_USEZ0K"

    const val SLIN_W: String = "SLIN_W"
    const val SLIN_W_UNIT: String = "SLIN_W_UNIT"
    const val SLIN_L: String = "SLIN_L"
    const val SLIN_L_UNIT: String = "SLIN_L_UNIT"
    const val SLIN_Z0: String = "SLIN_Z0"
    const val SLIN_Z0_UNIT: String = "SLIN_Z0_UNIT"
    const val SLIN_PHS: String = "SLIN_PHS"
    const val SLIN_PHS_UNIT: String = "SLIN_PHS_UNIT"
    const val SLIN_FREQ: String = "SLIN_FREQ"
    const val SLIN_FREQ_UNIT: String = "SLIN_FREQ_UNIT"
    const val SLIN_ER: String = "SLIN_ER"
    const val SLIN_H: String = "SLIN_H"
    const val SLIN_H_UNIT: String = "SLIN_H_UNIT"
    const val SLIN_T: String = "SLIN_T"
    const val SLIN_T_UNIT: String = "SLIN_T_UNIT"
    const val SLIN_TARGET: String = "SLIN_TARGET"

    const val CSLIN_W: String = "CSLIN_W"
    const val CSLIN_W_UNIT: String = "CSLIN_W_UNIT"
    const val CSLIN_S: String = "CSLIN_S"
    const val CSLIN_S_UNIT: String = "CSLIN_S_UNIT"
    const val CSLIN_L: String = "CSLIN_L"
    const val CSLIN_L_UNIT: String = "CSLIN_L_UNIT"
    const val CSLIN_Z0: String = "CSLIN_Z0"
    const val CSLIN_Z0_UNIT: String = "CSLIN_Z0_UNIT"
    const val CSLIN_K: String = "CSLIN_K"
    const val CSLIN_Z0O: String = "CSLIN_Z0O"
    const val CSLIN_Z0O_UNIT: String = "CSLIN_Z0O_UNIT"
    const val CSLIN_Z0E: String = "CSLIN_Z0E"
    const val CSLIN_Z0E_UNIT: String = "CSLIN_Z0E_UNIT"
    const val CSLIN_PHS: String = "CSLIN_PHS"
    const val CSLIN_PHS_UNIT: String = "CSLIN_PHS_UNIT"
    const val CSLIN_FREQ: String = "CSLIN_FREQ"
    const val CSLIN_FREQ_UNIT: String = "CSLIN_FREQ_UNIT"
    const val CSLIN_ER: String = "CSLIN_ER"
    const val CSLIN_H: String = "CSLIN_H"
    const val CSLIN_H_UNIT: String = "CSLIN_H_UNIT"
    const val CSLIN_T: String = "CSLIN_T"
    const val CSLIN_T_UNIT: String = "CSLIN_T_UNIT"
    const val CSLIN_USEZ0K: String = "CSLIN_USEZ0K"

    const val CPW_W: String = "CPW_W"
    const val CPW_W_UNIT: String = "CPW_W_UNIT"
    const val CPW_S: String = "CPW_S"
    const val CPW_S_UNIT: String = "CPW_S_UNIT"
    const val CPW_L: String = "CPW_L"
    const val CPW_L_UNIT: String = "CPW_L_UNIT"
    const val CPW_Z0: String = "CPW_Z0"
    const val CPW_Z0_UNIT: String = "CPW_Z0_UNIT"
    const val CPW_PHS: String = "CPW_PHS"
    const val CPW_PHS_UNIT: String = "CPW_PHS_UNIT"
    const val CPW_FREQ: String = "CPW_FREQ"
    const val CPW_FREQ_UNIT: String = "CPW_FREQ_UNIT"
    const val CPW_ER: String = "CPW_ER"
    const val CPW_H: String = "CPW_H"
    const val CPW_H_UNIT: String = "CPW_H_UNIT"
    const val CPW_T: String = "CPW_T"
    const val CPW_T_UNIT: String = "CPW_T_UNIT"
    const val CPW_TARGET: String = "CPW_TARGET"

    const val PARAMS_CPW: String = "PARAMS_CPW"
    const val GROUNDED_CPW: String = "GROUNDED_CPW"
    const val UNGROUNDED_CPW: String = "UNGROUNDED_CPW"

    const val GCPW_W: String = "GCPW_W"
    const val GCPW_W_UNIT: String = "GCPW_W_UNIT"
    const val GCPW_S: String = "GCPW_S"
    const val GCPW_S_UNIT: String = "GCPW_S_UNIT"
    const val GCPW_L: String = "GCPW_L"
    const val GCPW_L_UNIT: String = "GCPW_L_UNIT"
    const val GCPW_Z0: String = "GCPW_Z0"
    const val GCPW_Z0_UNIT: String = "GCPW_Z0_UNIT"
    const val GCPW_PHS: String = "GCPW_PHS"
    const val GCPW_PHS_UNIT: String = "GCPW_PHS_UNIT"
    const val GCPW_FREQ: String = "GCPW_FREQ"
    const val GCPW_FREQ_UNIT: String = "GCPW_FREQ_UNIT"
    const val GCPW_ER: String = "GCPW_ER"
    const val GCPW_H: String = "GCPW_H"
    const val GCPW_H_UNIT: String = "GCPW_H_UNIT"
    const val GCPW_T: String = "GCPW_T"
    const val GCPW_T_UNIT: String = "GCPW_T_UNIT"
    const val GCPW_TARGET: String = "GCPW_TARGET"

    const val COAX_A: String = "COAX_A"
    const val COAX_A_UNIT: String = "COAX_A_UNIT"
    const val COAX_B: String = "COAX_B"
    const val COAX_B_UNIT: String = "COAX_B_UNIT"
    const val COAX_C: String = "COAX_C"
    const val COAX_C_UNIT: String = "COAX_C_UNIT"
    const val COAX_ER: String = "COAX_ER"
    const val COAX_L: String = "COAX_L"
    const val COAX_L_UNIT: String = "COAX_L_UNIT"
    const val COAX_Z0: String = "COAX_Z0"
    const val COAX_Z0_UNIT: String = "COAX_Z0_UNIT"
    const val COAX_PHS: String = "COAX_PHS"
    const val COAX_PHS_UNIT: String = "COAX_PHS_UNIT"
    const val COAX_FREQ: String = "COAX_FREQ"
    const val COAX_FREQ_UNIT: String = "COAX_FREQ_UNIT"
    const val COAX_TARGET: String = "COAX_TARGET"

    /**
     * Array adapters for dimension units spinner
     * 
     * @param mContext context
     * @return ArrayAdapter for dimension units
     */
    fun adapterDimensionUnits(mContext: Context): ArrayAdapter<CharSequence?> {
        val adapterLength = ArrayAdapter.createFromResource(
            mContext, R.array.list_units_Length,
            R.layout.dropdown_menu_popup_item
        )
        adapterLength.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapterLength
    }

    /**
     * Array adapters for impedance units spinner
     * 
     * @param mContext context
     * @return ArrayAdapter for impedance units
     */
    fun adapterImpedanceUnits(mContext: Context): ArrayAdapter<CharSequence?> {
        val adapterImpedance = ArrayAdapter.createFromResource(
            mContext,
            R.array.list_units_Impedance, R.layout.dropdown_menu_popup_item
        )
        adapterImpedance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapterImpedance
    }

    /**
     * Array adapters for phase units spinner
     * 
     * @param mContext context
     * @return ArrayAdapter for phase units
     */
    fun adapterPhaseUnits(mContext: Context): ArrayAdapter<CharSequence?> {
        val adapterPhase = ArrayAdapter.createFromResource(
            mContext, R.array.list_units_Phase,
            R.layout.dropdown_menu_popup_item
        )
        adapterPhase.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapterPhase
    }

    /**
     * Array adapters for frequency units spinner
     * 
     * @param mContext context
     * @return ArrayAdapter for frequency units
     */
    fun adapterFrequencyUnits(mContext: Context): ArrayAdapter<CharSequence?> {
        val adapterFreq = ArrayAdapter.createFromResource(
            mContext, R.array.list_units_Frequency,
            R.layout.dropdown_menu_popup_item
        )
        adapterFreq.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapterFreq
    }

    /**
     * Validates the unit against the adapter's items
     * 
     * @param adapter ArrayAdapter containing units
     * @param unit    unit to validate
     * @return validated unit
     */
    fun validateUnit(adapter: ArrayAdapter<CharSequence?>, unit: String): String {
        val len = adapter.getCount()
        var idx = 0

        idx = 0
        while (idx < len) {
            if (unit == Objects.requireNonNull<CharSequence?>(adapter.getItem(idx)).toString()) {
                return unit
            }
            idx++
        }

        return Objects.requireNonNull<CharSequence?>(adapter.getItem(0)).toString()
    }


    /**
     * Converts value to meters based on the length unit
     * 
     * @param value      value to convert
     * @param lengthUnit unit of the value
     * @return value in meters
     */
    fun value2meter(value: Double, lengthUnit: String): Double {
        var l = 0.0
        when (lengthUnit) {
            "mil" -> l = value / 39370.0787402
            "mm" -> l = value / 1000
            "cm" -> l = value / 100
            "m" -> l = value
        }
        return l
    }

    /**
     * Converts value to Hertz based on the frequency unit
     * 
     * @param value    value to convert
     * @param freqUnit unit of the value
     * @return value in Hertz
     */
    fun value2Hz(value: Double, freqUnit: String): Double {
        var f = 0.0
        when (freqUnit) {
            "Hz" -> f = value
            "MHz" -> f = value * 1e6
            "GHz" -> f = value * 1e9
        }
        return f
    }

    /**
     * Converts value from meters to other units
     * 
     * @param value      value in meters
     * @param lengthUnit target unit
     * @return value in target unit
     */
    fun meter2others(value: Double, lengthUnit: String): Double {
        var l = 0.0
        when (lengthUnit) {
            "mil" -> l = value * 1000 * 39.37007874
            "mm" -> l = value * 1000
            "cm" -> l = value * 100
            "m" -> l = value
        }
        return l
    }

    const val ANALYZE: Int = 0
    const val SYNTHESIZE: Int = 1

    /**
     * Refreshes the animation for a given view
     * 
     * @param mContext context
     * @param view     view to animate
     * @param flag     flag to determine animation type
     */
    fun refreshAnimation(mContext: Context, view: ImageView, flag: Int) {
        val cx: Int
        val cy: Int
        if (flag == ANALYZE) {
            cx = view.getRight()
            cy = view.getTop()
        } else {
            cx = view.getLeft()
            cy = view.getBottom()
        }
        val finalRadius =
            sqrt(view.getWidth().toDouble().pow(2.0) + view.getHeight().toDouble().pow(2.0)).toInt()

        val animCircularReveal =
            ViewAnimationUtils.createCircularReveal(view, cx, cy, 0f, finalRadius.toFloat())
        view.setVisibility(View.VISIBLE)
        animCircularReveal.setDuration(
            mContext.getResources().getInteger(android.R.integer.config_mediumAnimTime).toLong()
        )
        animCircularReveal.setInterpolator(AccelerateDecelerateInterpolator())
        animCircularReveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.setVisibility(View.INVISIBLE)
            }
        })

        val animFadeOut = ObjectAnimator.ofFloat<View?>(view, View.ALPHA, 1f, 0f)
        animFadeOut.setDuration(
            mContext.getResources().getInteger(android.R.integer.config_mediumAnimTime).toLong()
        )
        animFadeOut.setInterpolator(AccelerateInterpolator())

        /*
         * ValueAnimator animFadeOut= ValueAnimator.ofFloat(1f, 0.2f);
         * animFadeOut.setDuration(mContext.getResources().getInteger(android.R.integer.
         * config_mediumAnimTime)); animFadeOut.setInterpolator(new
         * DecelerateInterpolator()); animFadeOut.addUpdateListener(new
         * ValueAnimator.AnimatorUpdateListener() {
         *
         * @Override public void onAnimationUpdate(ValueAnimator animation) {
         * view.setAlpha((Float) animation.getAnimatedValue()); } });
         */
        animCircularReveal.start()
        animFadeOut.start()
    }

    enum class ERROR(private val errorCode: Int) {
        NO_ERROR(0), DIMENSION_MINIMAL_LIMIT(1), ER_MINIMAL_LIMIT(2), COULD_NOT_BRACKET_SOLUTION(3), MAX_ITERATIONS(
            4
        ),
        WIDTH_MINIMAL_LIMIT(5), HEIGHT_MINIMAL_LIMIT(6), SPACE_MINIMAL_LIMIT(7), K_OUT_OF_RANGE(8), Z0E_Z0O_MISTAKE(
            9
        ),
        SUBSTRATE_TOO_LARGE(10), OFFSET_TOO_LARGE(11)
    }

    enum class WARNING(private val warningCode: Int) {
        NO_WARNING(0), WIDTH2HEIGHT_OUT_OF_RANGE(1), SPACE2HEIGHT_OUT_OF_RANGE(2), DIELECTRIC_OUT_OF_RANGE(
            3
        ),
        FREQUENCY2HEIGHT_OUT_OF_RANGE(4)
    }
}

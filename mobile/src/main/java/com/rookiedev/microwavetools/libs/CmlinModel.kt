package com.rookiedev.microwavetools.libs

import com.rookiedev.microwavetools.libs.Constants.ERROR
import com.rookiedev.microwavetools.libs.Constants.WARNING

/**
 * This class represents a CmlinModel which includes properties and methods
 * to manipulate and retrieve information about the substrate and metal models.
 */
class CmlinModel {
    private val substrate: SubstrateModel = SubstrateModel()
    private val metal: MetalModel = MetalModel()

    /**
     * Gets the metal space.
     * 
     * @return the metal space
     */
    var metalSpace: Double = 0.0
        private set
    /**
     * Gets the impedance.
     * 
     * @return the impedance
     */
    /**
     * Sets the impedance.
     * 
     * @param impedance the impedance value
     */
    var impedance: Double = 0.0
    /**
     * Gets the phase.
     * 
     * @return the phase
     */
    /**
     * Sets the phase.
     * 
     * @param phase the phase value
     */
    var phase: Double = 0.0 // ohms, degree
    /**
     * Gets the even mode impedance.
     * 
     * @return the even mode impedance
     */
    /**
     * Sets the even mode impedance.
     * 
     * @param impedanceEven the even mode impedance value
     */
    var impedanceEven: Double = 0.0
    /**
     * Gets the odd mode impedance.
     * 
     * @return the odd mode impedance
     */
    /**
     * Sets the odd mode impedance.
     * 
     * @param impedanceOdd the odd mode impedance value
     */
    var impedanceOdd: Double = 0.0
    /**
     * Gets the coupling factor.
     * 
     * @return the coupling factor
     */
    /**
     * Sets the coupling factor.
     * 
     * @param couplingFactor the coupling factor value
     */
    var couplingFactor: Double = 0.0

    /**
     * Gets the frequency.
     * 
     * @return the frequency
     */
    var frequency: Double = 0.0 // Hz
        private set
    /**
     * Gets the error code.
     * 
     * @return the error code
     */
    /**
     * Sets the error code.
     * 
     * @param errorCode the error code
     */
    var errorCode: ERROR? = null
    /**
     * Gets the warning code.
     * 
     * @return the warning code
     */
    /**
     * Sets the warning code.
     * 
     * @param warningCode the warning code
     */
    var warningCode: WARNING? = null

    /**
     * Sets the synthesize parameter based on the provided flag.
     * 
     * @param para the parameter value
     * @param flag the flag indicating which parameter to set
     */
    fun setSynthesizeParameter(para: Double, flag: Int) {
        when (flag) {
            Constants.Synthesize_Width -> metal.metalWidth = para
            Constants.Synthesize_Height -> substrate.subHeight = para
            Constants.Synthesize_Er -> substrate.subEpsilon = para
            Constants.Synthesize_Length -> metal.metalLength = para
            else -> {}
        }
    }

    /**
     * Sets the metal width.
     * 
     * @param width the width value
     * @param unit the unit of the width
     */
    fun setMetalWidth(width: Double, unit: String) {
        metal.metalWidth = Constants.value2meter(width, unit)
    }

    val metalWidth: Double
        /**
         * Gets the metal width.
         * 
         * @return the metal width
         */
        get() = metal.metalWidth

    /**
     * Sets the metal length.
     * 
     * @param length the length value
     * @param unit the unit of the length
     */
    fun setMetalLength(length: Double, unit: String) {
        metal.metalLength = Constants.value2meter(length, unit)
    }

    val metalLength: Double
        /**
         * Gets the metal length.
         * 
         * @return the metal length
         */
        get() = metal.metalLength

    /**
     * Sets the metal thickness.
     * 
     * @param thick the thickness value
     * @param unit the unit of the thickness
     */
    fun setMetalThick(thick: Double, unit: String) {
        metal.metalThick = Constants.value2meter(thick, unit)
    }

    val metalThick: Double
        /**
         * Gets the metal thickness.
         * 
         * @return the metal thickness
         */
        get() = metal.metalThick

    /**
     * Sets the frequency.
     * 
     * @param frequency the frequency value
     * @param unit the unit of the frequency
     */
    fun setFrequency(frequency: Double, unit: String) {
        this.frequency = Constants.value2Hz(frequency, unit)
    }

    var subEpsilon: Double
        /**
         * Gets the substrate epsilon.
         * 
         * @return the substrate epsilon
         */
        get() = substrate.subEpsilon
        /**
         * Sets the substrate epsilon.
         * 
         * @param subEpsilon the substrate epsilon value
         */
        set(subEpsilon) {
            substrate.subEpsilon = subEpsilon
        }

    val subHeight: Double
        /**
         * Gets the substrate height.
         * 
         * @return the substrate height
         */
        get() = substrate.subHeight

    /**
     * Sets the substrate height.
     * 
     * @param subHeight the substrate height value
     * @param unit the unit of the height
     */
    fun setSubHeight(subHeight: Double, unit: String) {
        substrate.subHeight = Constants.value2meter(subHeight, unit)
    }

    /**
     * Sets the metal space.
     * 
     * @param metalSpace the metal space value
     * @param unit the unit of the space
     */
    fun setMetalSpace(metalSpace: Double, unit: String) {
        this.metalSpace = Constants.value2meter(metalSpace, unit)
    }

}

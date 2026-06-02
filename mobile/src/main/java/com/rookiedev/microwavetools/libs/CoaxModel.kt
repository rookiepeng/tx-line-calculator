package com.rookiedev.microwavetools.libs

import com.rookiedev.microwavetools.libs.Constants.ERROR

class CoaxModel {
    private val substrate: SubstrateModel
    /**
     * Gets the impedance.
     * 
     * @return the impedance in ohms
     */
    /**
     * Sets the impedance.
     * 
     * @param impedance the impedance in ohms
     */
    var impedance: Double = 0.0
    /**
     * Gets the phase.
     * 
     * @return the phase in degrees
     */
    /**
     * Sets the phase.
     * 
     * @param phase the phase in degrees
     */
    var phase: Double = 0.0 // ohms, degree

    /**
     * Gets the frequency.
     * 
     * @return the frequency in Hz
     */
    var frequency: Double = 0.0 // Hz
        private set

    /**
     * Gets the core radius.
     * 
     * @return the core radius
     */
    var coreRadius: Double = 0.0
        private set

    /**
     * Gets the core offset.
     * 
     * @return the core offset
     */
    var coreOffset: Double = 0.0
        private set

    /**
     * Gets the metal length.
     * 
     * @return the metal length
     */
    var metalLength: Double = 0.0
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
     * Constructor for CoaxModel.
     */
    init {
        substrate = SubstrateModel()
    }

    /**
     * Sets the synthesis parameter based on the provided flag.
     * 
     * @param para the parameter value
     * @param flag the flag indicating which parameter to set
     */
    fun setSynthesizeParameter(para: Double, flag: Int) {
        when (flag) {
            Constants.Synthesize_Height -> substrate.subHeight = para
            Constants.Synthesize_Er -> substrate.subEpsilon = para
            Constants.Synthesize_CoreRadius -> coreRadius = para
            Constants.Synthesize_CoreOffset -> coreOffset = para
            else -> {}
        }
    }

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
         * @param subEpsilon the substrate epsilon
         */
        set(subEpsilon) {
            substrate.subEpsilon = subEpsilon
        }

    val subRadius: Double
        /**
         * Gets the substrate radius.
         * 
         * @return the substrate radius
         */
        get() = substrate.subHeight

    /**
     * Sets the substrate radius.
     * 
     * @param subHeight the substrate height
     * @param unit the unit of the height
     */
    fun setSubRadius(subHeight: Double, unit: String) {
        substrate.subHeight = Constants.value2meter(subHeight, unit)
    }

    /**
     * Sets the core radius.
     * 
     * @param coreRadius the core radius
     * @param unit the unit of the radius
     */
    fun setCoreRadius(coreRadius: Double, unit: String) {
        this.coreRadius = Constants.value2meter(coreRadius, unit)
    }

    /**
     * Sets the core offset.
     * 
     * @param coreOffset the core offset
     * @param unit the unit of the offset
     */
    fun setCoreOffset(coreOffset: Double, unit: String) {
        this.coreOffset = Constants.value2meter(coreOffset, unit)
    }

    /**
     * Sets the metal length.
     * 
     * @param metalLength the metal length
     * @param unit the unit of the length
     */
    fun setMetalLength(metalLength: Double, unit: String) {
        this.metalLength = Constants.value2meter(metalLength, unit)
    }
}

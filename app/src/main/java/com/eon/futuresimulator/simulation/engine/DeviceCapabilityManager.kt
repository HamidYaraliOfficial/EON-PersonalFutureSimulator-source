package com.eon.futuresimulator.simulation.engine

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class SimulationBudget(val recommendedIterations: Int, val maxIterations: Int, val reason: String)

/**
 * Resource Manager + Device Capability Manager — caps Monte Carlo iteration count and
 * worker concurrency to what the device can comfortably handle, and scales down further
 * under Battery Saver / thermal throttling so EON stays usable on modest hardware.
 */
@Singleton
class DeviceCapabilityManager @Inject constructor(@ApplicationContext private val context: Context) {

    fun currentBudget(userOverrideCap: Int = 0): SimulationBudget {
        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val isPowerSaveMode = (context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager)?.isPowerSaveMode ?: false
        val isCharging = batteryManager?.isCharging ?: true
        val thermalThrottled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            (pm?.currentThermalStatus ?: 0) >= android.os.PowerManager.THERMAL_STATUS_MODERATE
        } else false

        var max = (cores * 250).coerceIn(200, 2000)
        var reason = "Base cap from $cores CPU core(s)."

        if (isPowerSaveMode) { max = (max / 4).coerceAtLeast(50); reason = "Reduced: Battery Saver is on." }
        else if (thermalThrottled) { max = (max / 2).coerceAtLeast(100); reason = "Reduced: device is running warm." }
        else if (!isCharging) { max = (max * 0.75).toInt().coerceAtLeast(100); reason = "Slightly reduced: running on battery." }

        val effectiveMax = if (userOverrideCap in 1..max) userOverrideCap else max
        return SimulationBudget(recommendedIterations = (effectiveMax / 2).coerceAtLeast(50), maxIterations = effectiveMax, reason = reason)
    }
}

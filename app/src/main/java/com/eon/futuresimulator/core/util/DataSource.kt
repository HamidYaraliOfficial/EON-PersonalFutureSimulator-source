package com.eon.futuresimulator.core.util

/**
 * Provenance tag stored on every entity so EON (and the user, via the Data Source
 * Inspector) always knows whether a value was typed in by a human, derived by an
 * engine, produced by a simulation, synced from the OS calendar, or set by the system.
 */
enum class DataSource {
    MANUAL,
    CALENDAR_SYNC,
    DERIVED,
    SIMULATION,
    SYSTEM,
}

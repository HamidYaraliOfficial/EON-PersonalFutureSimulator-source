package com.eon.futuresimulator.core.util

import java.util.UUID

/** Every EON entity is identified by a stable UUID so Export/Import and Sync stay conflict-free. */
object IdGenerator {
    fun newId(): String = UUID.randomUUID().toString()
}

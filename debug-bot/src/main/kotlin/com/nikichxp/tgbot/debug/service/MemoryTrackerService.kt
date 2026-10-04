package com.nikichxp.tgbot.debug.service

import com.nikichxp.tgbot.debug.dto.MemoryStatus
import org.springframework.stereotype.Service

@Service
class MemoryTrackerService {

    fun getMemoryStatus(): MemoryStatus {
        val runtime = Runtime.getRuntime()
        return MemoryStatus(
            used = runtime.totalMemory() - runtime.freeMemory(),
            available = runtime.freeMemory(),
            allocated = runtime.totalMemory()
        )
    }

}

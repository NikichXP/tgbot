package com.nikichxp.tgbot.core.util

import org.springframework.stereotype.Service

@Service
class MemoryTrackerService {

    fun getMemoryStatus() = MemoryStatus()

    data class MemoryStatus(
        val used: String,
        val available: String,
        val allocated: String
    ) {

        constructor(used: Long, available: Long, allocated: Long) : this(
            formatMemorySize(used),
            formatMemorySize(available),
            formatMemorySize(allocated)
        )

        constructor() : this(
            Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
            Runtime.getRuntime().freeMemory(),
            Runtime.getRuntime().totalMemory()
        )

        fun prettyPrint() = "Memory stats: use = $used, available = $available, reserve = $allocated"
    }

    companion object {
        fun formatMemorySize(size: Long): String {
            val unit = 1024
            if (size < unit) return "$size B"
            val exp = (Math.log(size.toDouble()) / Math.log(unit.toDouble())).toInt()
            val pre = "KMGTPE"[exp - 1]
            return String.format("%.1f %sB", size / Math.pow(unit.toDouble(), exp.toDouble()), pre)
        }
    }

}


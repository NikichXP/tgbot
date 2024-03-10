package com.nikichxp.tgbot.util

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class MemoryTrackerService {

//    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    fun printMemStats() {
        println(MemoryStatus().prettyPrint())
    }

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
            val pre = "KMGTPE"[exp - 1] + "i"
            return String.format("%.1f %sB", size / Math.pow(unit.toDouble(), exp.toDouble()), pre)
        }
    }

}


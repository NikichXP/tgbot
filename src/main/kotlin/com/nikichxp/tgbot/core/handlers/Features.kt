package com.nikichxp.tgbot.core.handlers

// TODO Why I made this not enum? Maybe it's time to migrate?
object Features {

    const val CHILD_TRACKER = "childTracker"
    const val DEBUG = "debug"
    const val SANTA = "santa"
    const val DEMO = "demo"
    const val KARMA = "karma"
    const val SUMMARY = "summary"
    const val STATS = "stats"
    const val SHITPOSTING = "shitposting"
    const val TOOLBOX = "toolbox"
    const val OKX = "okx"

    // keep in sync with the constants above: this is what the dashboard offers to toggle
    val ALL = listOf(CHILD_TRACKER, DEBUG, SANTA, DEMO, KARMA, SUMMARY, STATS, SHITPOSTING, TOOLBOX, OKX)

}
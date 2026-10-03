package com.nikichxp.tgbot.core.util

interface IAppStorage {
    fun saveData(data: AppData): AppData
    fun saveData(key: String, value: String): AppData
    fun getData(key: String): AppData?
    fun getOrPut(key: String, defaultValue: String): String
}

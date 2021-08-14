package com.nikichxp.tgbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TGBotApplication

fun main(args: Array<String>) {
    println("----------------- env")
    System.getenv().forEach { (k, _) -> println(k) }
    println("------------ props")
    System.getProperties().forEach { (k, _) -> println(k) }
    println("------- end this")
    runApplication<TGBotApplication>(*args)
}

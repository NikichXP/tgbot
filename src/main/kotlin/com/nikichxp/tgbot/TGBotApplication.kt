package com.nikichxp.tgbot

import org.bson.Document
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TGBotApplication

fun main(args: Array<String>) {
    runApplication<TGBotApplication>(*args)
}

/*
TODO Значит какой у нас план
    есть куча различных сценариев пользования
    пока что все заточено под один
    схема какая - есть Update
    он процессится в MessageClassifier
    выходит MessageInteractionResult который уже чисто под реакции подвязан
    и дальше пошла история
    но что нам нужно сделать?
    1. глобальный контекст с апдейтом пришедшим нам
    2. вынос игры рейтинга из кор-функционала бота
  +++ 3. на изменение рейтинга слать сообщение (блокер для п.1)
 */
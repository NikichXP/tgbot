package com.nikichxp.tgbot.people.error

class PeopleListNotFoundException(name: String) : RuntimeException("List '$name' not found")
class PeopleListAlreadyExistsException(name: String) : RuntimeException("List '$name' already exists")

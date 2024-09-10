package com.nikichxp.tgbot.core.util

class ChatCommandParser private constructor() {

    var vars = mutableMapOf<String, String>()
    private var argName: String? = null
    private var nextStage: (suspend ChatCommandParser.() -> Unit)? = null
    private var layerState = LayerState.END
    private val paths = mutableMapOf<String, suspend ChatCommandParser.() -> Unit>()

    fun path(pathName: String, function: suspend ChatCommandParser.() -> Unit) {
        if (layerState == LayerState.PARAM) {
            throw IllegalStateException()
        }
        paths[pathName] = function
        layerState = LayerState.PATH
    }

    fun paths(vararg possiblePaths: String, function: ChatCommandParser.() -> Unit) {
        for (path in possiblePaths) {
            path(path, function)
        }
    }

    fun asArg(argName: String, function: suspend ChatCommandParser.() -> Unit) {
        if (layerState == LayerState.PATH) {
            throw IllegalStateException()
        }
        this.argName = argName
        this.nextStage = function
        layerState = LayerState.PARAM
    }

    private suspend fun proceed(tokens: List<String>): Boolean {
        val whatNext: suspend ChatCommandParser.() -> Unit = when (layerState) {
            LayerState.END -> {
                return true
            }
            LayerState.PATH -> {
                val token = tokens.first()
                paths[token] ?: return false
            }
            LayerState.PARAM -> {
                vars[argName!!] = tokens.first()
                nextStage ?: return false
            }
        }
        val layer = ChatCommandParser()
        layer.vars = this.vars
        whatNext(layer)
        return layer.proceed(tokens.drop(1))
    }

    private enum class LayerState {
        END, PATH, PARAM
    }

    companion object {
        suspend fun analyze(command: String, function: ChatCommandParser.() -> Unit) = analyze(command.split(" "), function)
        suspend fun analyze(tokens: List<String>, function: suspend ChatCommandParser.() -> Unit): Boolean {
            val layer = ChatCommandParser()
            function(layer)
            return layer.proceed(tokens)
        }
    }
}
package com.nikichxp.tgbot.util

class ChatCommandParser private constructor() {

    var vars = mutableMapOf<String, String>()
    var argName: String? = null
    var nextStage: (ChatCommandParser.() -> Unit)? = null
    private var layerState = LayerState.END
    private val paths = mutableMapOf<String, ChatCommandParser.() -> Unit>()

    fun path(pathName: String, function: ChatCommandParser.() -> Unit) {
        if (layerState == LayerState.PARAM) {
            throw IllegalStateException()
        }
        paths[pathName] = function
        layerState = LayerState.PATH
    }

    fun asArg(argName: String, function: ChatCommandParser.() -> Unit) {
        if (layerState == LayerState.PATH) {
            throw IllegalStateException()
        }
        this.argName = argName
        this.nextStage = function
        layerState = LayerState.PARAM
    }

    private fun proceed(tokens: List<String>): Boolean {
        val whatNext: ChatCommandParser.() -> Unit = when (layerState) {
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
        fun analyze(command: String, function: ChatCommandParser.() -> Unit) = analyze(command.split(" "), function)
        fun analyze(tokens: List<String>, function: ChatCommandParser.() -> Unit): Boolean {
            val layer = ChatCommandParser()
            function(layer)
            return layer.proceed(tokens)
        }
    }
}
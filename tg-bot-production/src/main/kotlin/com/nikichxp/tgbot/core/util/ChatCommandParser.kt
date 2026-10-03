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

    private suspend fun proceed(tokens: List<String>): List<String>? {
        return when (layerState) {
            LayerState.END -> tokens
            LayerState.PATH -> proceedPaths(tokens)
            LayerState.PARAM -> {
                if (tokens.isEmpty()) return null
                vars[argName!!] = tokens.first()
                val whatNext = nextStage ?: return null
                val layer = ChatCommandParser()
                layer.vars = this.vars
                whatNext(layer)
                layer.proceed(tokens.drop(1))
            }
        }
    }

    private suspend fun proceedPaths(tokens: List<String>): List<String>? {
        val remainingPaths = paths.toMutableMap()
        var current = tokens
        var matchedAny = false
        while (true) {
            val idx = current.indexOfFirst { remainingPaths.containsKey(it) }
            if (idx == -1) break
            val function = remainingPaths.remove(current[idx])!!
            val before = current.subList(0, idx)
            val after = current.subList(idx + 1, current.size)
            val layer = ChatCommandParser()
            layer.vars = this.vars
            function(layer)
            val remainderAfter = layer.proceed(after) ?: return null
            matchedAny = true
            current = before + remainderAfter
        }
        return if (matchedAny) current else null
    }

    private enum class LayerState {
        END, PATH, PARAM
    }

    companion object {
        suspend fun analyze(command: String, function: ChatCommandParser.() -> Unit) = analyze(command.split(" "), function)
        suspend fun analyze(tokens: List<String>, function: suspend ChatCommandParser.() -> Unit): Boolean {
            val layer = ChatCommandParser()
            function(layer)
            return layer.proceed(tokens) != null
        }
    }
}
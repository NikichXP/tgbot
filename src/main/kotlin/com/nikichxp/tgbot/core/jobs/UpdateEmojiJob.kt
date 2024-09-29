package com.nikichxp.tgbot.core.jobs

import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.karmabot.handlers.StickerReaction
import com.nikichxp.tgbot.karmabot.service.EmojiService
import com.nikichxp.tgbot.karmabot.service.UserService
import com.nikichxp.tgbot.karmabot.service.actions.LikedMessageService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll

/*
 TODO Это надо перенести в исполняемый скрипт
        технически это сопутствующий скрипт, надо держать его где-то в другом месте как часть supportive lib
 */
//@Component
class UpdateEmojiJob(
    private val mongoTemplate: MongoTemplate,
    private val emojiService: EmojiService,
    private val userService: UserService,
    private val tgOperations: TgOperations
) {

//    @PostConstruct
    fun fixEmojiRating() {
        val reactions = mongoTemplate.findAll<StickerReaction>()
        val emojiMap = emojiService.listEmojis().toMap()

        emojiMap.forEach { emoji, power ->
            val messages = reactions
                .filter { it.emoji == emoji }
                .filter { it.from != it.to }
                .map { reaction ->
                    val actor = userService.getUserInfo(reaction.from) ?: throw Exception()
                    val diff = LikedMessageService.calculateKarmaDiff(actor.rating, power)
                    var toName = ""
                    var resRating = 0.0
                    userService.modifyUser(reaction.to) {
                        it.rating = LikedMessageService.roundF(it.rating + diff)
                        toName = it.username ?: it.id.toString()
                        resRating = it.rating
                    }
                    mongoTemplate.remove(reaction)
                    "From ${actor.username ?: actor.id.toString()} (${actor.rating}) to $toName ($resRating), diff = $diff"
                }
            if (messages.isNotEmpty()) {
//                tgOperations.sendMessage(
//                    -1001361600905L,
//                    "Emoji $emoji changed data:\n${messages.joinToString(separator = "\n")}",
//                    TODO()
//                )
            }
        }

    }

}
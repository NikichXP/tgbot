package com.nikichxp.tgbot.childcarebot.logic

import com.nikichxp.tgbot.childcarebot.ChildInfo
import com.nikichxp.tgbot.core.entity.UserId
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class ChildInfoRepo(
    private val mongoTemplate: MongoTemplate
) {

    @Cacheable("childInfo")
    fun findChildByParent(parentId: UserId): ChildInfo? {
        return mongoTemplate.findOne(Query.query(Criteria.where(ChildInfo::parents.name).`is`(parentId)))
    }

    @Cacheable("childInfo")
    fun findChildById(childId: Long): ChildInfo? {
        return mongoTemplate.findById(childId)
    }

    fun updateById(childId: Long, update: (ChildInfo) -> Unit) {
        val child = findChildById(childId) ?: return
        update(child)
        mongoTemplate.save(child)
    }

}
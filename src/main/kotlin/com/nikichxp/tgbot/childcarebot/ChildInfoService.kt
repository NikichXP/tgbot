package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.core.entity.UserId
import jakarta.annotation.PostConstruct
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.findOne
import org.springframework.stereotype.Service

@Service
class ChildInfoService(
    private val mongoTemplate: MongoTemplate
) {

    @Cacheable("childInfo")
    fun findChildByParent(parentId: UserId): ChildInfo? {
        return mongoTemplate.findOne(Query.query(Criteria.where(ChildInfo::parents.name).`is`(parentId)))
    }

}

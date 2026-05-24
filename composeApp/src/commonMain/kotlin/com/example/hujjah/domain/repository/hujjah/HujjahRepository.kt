package com.example.hujjah.domain.repository.hujjah

import com.example.hujjah.domain.model.islamic.IslamicReference
import com.example.hujjah.domain.model.islamic.TopicOption
import kotlinx.coroutines.flow.Flow

interface HujjahRepository {
    fun getTopics(): Flow<List<TopicOption>>
    fun getReferencesByTopic(topicId: String): Flow<List<IslamicReference>>
    fun getReferenceById(referenceId: String): Flow<IslamicReference?>
}

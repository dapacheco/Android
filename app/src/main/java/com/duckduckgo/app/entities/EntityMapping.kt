/*
 * Copyright (c) 2018 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.entities

import android.net.Uri
import com.duckduckgo.app.entities.db.EntityListEntity
import com.duckduckgo.app.global.UriString
import com.duckduckgo.app.global.baseHost
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntityMapping @Inject constructor() {

    private var entities: List<EntityListEntity> = emptyList()
    private var entitiesMap: MutableMap<String, MutableList<EntityListEntity>> = mutableMapOf()

    fun updateEntities(entities: List<EntityListEntity>) {
        Timber.d("updateEntities")
        this.entities = entities

        updateEntriesMap()
    }

    private fun updateEntriesMap() {
        entitiesMap = mutableMapOf()
        entities.forEach { entity ->
            val topLevelDomain = getTopLevelDomain(entity.domainName)

            if (entitiesMap.containsKey(topLevelDomain)) {
                entitiesMap[topLevelDomain]?.add(entity)
            } else {
                entitiesMap[topLevelDomain] = mutableListOf(entity)
            }
        }
    }

    fun entityForUrl(url: String): EntityListEntity? {
        val childUri = Uri.parse(url) ?: return null
        val baseHost = childUri.baseHost ?: return null
        val topLevelDomain = getTopLevelDomain(baseHost)
        return entitiesMap[topLevelDomain]?.find { UriString.sameOrSubdomain(childUri, it.domainName) }
    }

    private fun getTopLevelDomain(url: String): String {
        return url.split(".").last()
    }
}
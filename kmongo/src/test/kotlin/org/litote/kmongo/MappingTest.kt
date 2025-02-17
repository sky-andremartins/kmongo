/*
 * Copyright (C) 2016/2021 Litote
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litote.kmongo

import com.mongodb.client.model.Projections
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.MongoOperator.date
import org.litote.kmongo.MongoOperator.set
import java.time.Instant
import kotlin.test.assertEquals

/**
 *
 */
class MappingTest {

    data class Project(val reports: List<ProjectReport>)
    data class ProjectReport(@BsonId val key: String, val points: List<ProjectReportPoint>)
    data class ProjectReportPoint(@BsonId val key: String, val published: Instant)

    data class ReportProjection(val report: ProjectReport)
    data class PointProjection(val point: ProjectReportPoint)

    @Test
    fun testFindAndUpdate() {
        val now = Instant.now()
        assertEquals(
            """{"$set": {"reports.${'$'}[report].points.${'$'}[point].published": {"$date": "$now"}}}""",
            setValue(
                (Project::reports.filteredPosOp("report") / ProjectReport::points)
                    .filteredPosOp("point") / ProjectReportPoint::published,
                now
            ).json
        )

        assertEquals(
            listOf("""{"report._id": "a"}""", """{"point._id": "a"}"""),
            listOf(
                ReportProjection::report / ProjectReport::key eq "a",
                PointProjection::point / ProjectReportPoint::key eq "a",
            ).map { it.json }
        )


    }

    class User(val improvementPlan:ImprovementPlan)
    class ImprovementPlan(val viewersList: List<ViewerImprovementPlan>)
    class ViewerImprovementPlan(val jauthId:ObjectId)

    @Test
    fun `complex query`() {
        val bson = User::improvementPlan.div(ImprovementPlan::viewersList) from(Projections.computed("\$not", Projections.computed("\$elemMatch", ViewerImprovementPlan::jauthId eq ObjectId("5e4e90f54b69960012f6e01d"))))
        println(bson.json)
    }
}
package com.example.akashiconline.data

import androidx.room.Embedded
import androidx.room.Relation

data class DayDetail(
    @Embedded val day: DayEntity,
    @Relation(parentColumn = "id", entityColumn = "dayId")
    val steps: List<StepEntity>,
)

data class WeekDetail(
    @Embedded val week: WeekEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "weekId",
        entity = DayEntity::class,
    )
    val days: List<DayDetail>,
)

data class ProgramDetail(
    @Embedded val program: ProgramEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "programId",
        entity = WeekEntity::class,
    )
    val weeks: List<WeekDetail>,
)

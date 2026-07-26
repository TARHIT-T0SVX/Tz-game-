package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1,
    val gold: Int = 5000,
    val elixir: Int = 4000,
    val darkElixir: Int = 500,
    val gems: Int = 250,
    val townHallLevel: Int = 1,
    val selectedHeroId: String = "barbarian_king",
    val unlockedStagesCount: Int = 1
)

@Entity(tableName = "hero_data")
data class HeroEntity(
    @PrimaryKey val heroId: String,
    val name: String,
    val level: Int = 1,
    val maxHp: Int,
    val attackPower: Int,
    val attackSpeed: Float, // attacks per sec
    val speed: Float,
    val abilityLevel: Int = 1,
    val isUnlocked: Boolean = true,
    val unlockCostGold: Int = 0
)

@Entity(tableName = "stage_progress")
data class StageProgressEntity(
    @PrimaryKey val stageId: Int,
    val stageName: String,
    val starsEarned: Int = 0,
    val isUnlocked: Boolean = false,
    val highScore: Int = 0
)

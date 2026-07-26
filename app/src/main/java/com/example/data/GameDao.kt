package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getPlayerProfile(): Flow<PlayerProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerProfile(profile: PlayerProfile)

    @Query("SELECT * FROM hero_data")
    fun getAllHeroes(): Flow<List<HeroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeroes(heroes: List<HeroEntity>)

    @Query("UPDATE hero_data SET level = level + 1, maxHp = maxHp + 120, attackPower = attackPower + 25 WHERE heroId = :heroId")
    suspend fun upgradeHero(heroId: String)

    @Query("SELECT * FROM stage_progress ORDER BY stageId ASC")
    fun getAllStages(): Flow<List<StageProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStages(stages: List<StageProgressEntity>)

    @Query("UPDATE stage_progress SET starsEarned = :stars WHERE stageId = :stageId AND starsEarned < :stars")
    suspend fun updateStageStars(stageId: Int, stars: Int)
}

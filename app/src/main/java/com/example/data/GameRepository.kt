package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val dao: GameDao) {

    val playerProfile: Flow<PlayerProfile?> = dao.getPlayerProfile()
    val heroes: Flow<List<HeroEntity>> = dao.getAllHeroes()
    val stages: Flow<List<StageProgressEntity>> = dao.getAllStages()

    suspend fun checkAndSeedDatabase() {
        val currentProfile = dao.getPlayerProfile().firstOrNull()
        if (currentProfile == null) {
            dao.savePlayerProfile(PlayerProfile())
            
            // Seed Heroes
            val defaultHeroes = listOf(
                HeroEntity(
                    heroId = "barbarian_king",
                    name = "Barbarian King",
                    level = 1,
                    maxHp = 1200,
                    attackPower = 110,
                    attackSpeed = 1.2f,
                    speed = 4.2f,
                    abilityLevel = 1,
                    isUnlocked = true,
                    unlockCostGold = 0
                ),
                HeroEntity(
                    heroId = "archer_queen",
                    name = "Archer Queen",
                    level = 1,
                    maxHp = 780,
                    attackPower = 165,
                    attackSpeed = 1.6f,
                    speed = 4.8f,
                    abilityLevel = 1,
                    isUnlocked = true,
                    unlockCostGold = 0
                ),
                HeroEntity(
                    heroId = "pekka",
                    name = "P.E.K.K.A",
                    level = 1,
                    maxHp = 1800,
                    attackPower = 280,
                    attackSpeed = 0.8f,
                    speed = 3.5f,
                    abilityLevel = 1,
                    isUnlocked = true,
                    unlockCostGold = 0
                ),
                HeroEntity(
                    heroId = "grand_warden",
                    name = "Grand Warden",
                    level = 1,
                    maxHp = 950,
                    attackPower = 140,
                    attackSpeed = 1.4f,
                    speed = 4.5f,
                    abilityLevel = 1,
                    isUnlocked = true,
                    unlockCostGold = 0
                )
            )
            dao.insertHeroes(defaultHeroes)

            // Seed Stages
            val defaultStages = listOf(
                StageProgressEntity(1, "Goblin Outpost", starsEarned = 0, isUnlocked = true),
                StageProgressEntity(2, "Skeleton Crypt", starsEarned = 0, isUnlocked = true),
                StageProgressEntity(3, "Elixir Ravine", starsEarned = 0, isUnlocked = true),
                StageProgressEntity(4, "Inferno Dragon Peak", starsEarned = 0, isUnlocked = true),
                StageProgressEntity(5, "Golem Citadel", starsEarned = 0, isUnlocked = true)
            )
            dao.insertStages(defaultStages)
        }
    }

    suspend fun updateResources(goldDelta: Int, elixirDelta: Int, gemsDelta: Int = 0) {
        val current = dao.getPlayerProfile().firstOrNull() ?: PlayerProfile()
        val updated = current.copy(
            gold = (current.gold + goldDelta).coerceAtLeast(0),
            elixir = (current.elixir + elixirDelta).coerceAtLeast(0),
            gems = (current.gems + gemsDelta).coerceAtLeast(0)
        )
        dao.savePlayerProfile(updated)
    }

    suspend fun setSelectedHero(heroId: String) {
        val current = dao.getPlayerProfile().firstOrNull() ?: PlayerProfile()
        dao.savePlayerProfile(current.copy(selectedHeroId = heroId))
    }

    suspend fun upgradeHero(heroId: String, costGold: Int, costElixir: Int): Boolean {
        val current = dao.getPlayerProfile().firstOrNull() ?: return false
        if (current.gold >= costGold && current.elixir >= costElixir) {
            updateResources(-costGold, -costElixir)
            dao.upgradeHero(heroId)
            return true
        }
        return false
    }

    suspend fun recordStageCompletion(stageId: Int, stars: Int, goldReward: Int, elixirReward: Int) {
        dao.updateStageStars(stageId, stars)
        updateResources(goldReward, elixirReward, if (stars == 3) 25 else 10)
    }

    suspend fun upgradeTownHall(): Boolean {
        val current = dao.getPlayerProfile().firstOrNull() ?: return false
        val costGold = current.townHallLevel * 2500
        val costElixir = current.townHallLevel * 2500
        if (current.gold >= costGold && current.elixir >= costElixir) {
            updateResources(-costGold, -costElixir)
            dao.savePlayerProfile(current.copy(townHallLevel = current.townHallLevel + 1))
            return true
        }
        return false
    }
}

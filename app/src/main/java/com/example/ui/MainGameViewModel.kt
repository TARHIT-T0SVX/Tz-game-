package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.game.engine.GameEngine
import com.example.game.engine.StageConfig
import com.example.game.engine.WaveConfig
import com.example.game.engine.CharacterType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class GameScreen {
    VILLAGE,
    CAMPAIGN_MAP,
    HERO_ALTAR,
    BATTLE
}

class MainGameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GameDatabase.getDatabase(application)
    private val repository = GameRepository(db.gameDao())

    val playerProfile: StateFlow<PlayerProfile?> = repository.playerProfile.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val heroes: StateFlow<List<HeroEntity>> = repository.heroes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val stages: StateFlow<List<StageProgressEntity>> = repository.stages.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val gameEngine = GameEngine()

    private val _activeScreen = MutableStateFlow(GameScreen.VILLAGE)
    val activeScreen: StateFlow<GameScreen> = _activeScreen.asStateFlow()

    private val _selectedStageConfig = MutableStateFlow<StageConfig?>(null)
    val selectedStageConfig: StateFlow<StageConfig?> = _selectedStageConfig.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
    }

    fun navigateTo(screen: GameScreen) {
        _activeScreen.value = screen
    }

    fun selectHero(heroId: String) {
        viewModelScope.launch {
            repository.setSelectedHero(heroId)
        }
    }

    fun upgradeHero(heroId: String, costGold: Int, costElixir: Int) {
        viewModelScope.launch {
            repository.upgradeHero(heroId, costGold, costElixir)
        }
    }

    fun upgradeTownHall() {
        viewModelScope.launch {
            repository.upgradeTownHall()
        }
    }

    fun collectVillageResources() {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val goldGain = 200 * profile.townHallLevel
            val elixirGain = 200 * profile.townHallLevel
            repository.updateResources(goldGain, elixirGain)
        }
    }

    fun prepareAndStartBattle(stageId: Int) {
        viewModelScope.launch {
            val allHeroes = heroes.firstOrNull() ?: emptyList()
            val profile = playerProfile.value ?: return@launch
            val activeHero = allHeroes.find { it.heroId == profile.selectedHeroId } ?: allHeroes.firstOrNull() ?: return@launch

            val stageConfig = getStageConfigForId(stageId)
            _selectedStageConfig.value = stageConfig

            gameEngine.startBattle(activeHero, stageConfig)
            _activeScreen.value = GameScreen.BATTLE
        }
    }

    fun finishBattle(starsEarned: Int, goldEarned: Int, elixirEarned: Int) {
        viewModelScope.launch {
            val stageConfig = _selectedStageConfig.value ?: return@launch
            if (starsEarned > 0) {
                repository.recordStageCompletion(stageConfig.stageId, starsEarned, goldEarned, elixirEarned)
            }
        }
    }

    private fun getStageConfigForId(stageId: Int): StageConfig {
        return when (stageId) {
            1 -> StageConfig(
                stageId = 1,
                name = "Goblin Outpost",
                waves = listOf(
                    WaveConfig(1, goblins = 5, skeletons = 2),
                    WaveConfig(2, goblins = 8, wizards = 2),
                    WaveConfig(3, goblins = 10, bossType = CharacterType.GOBLIN)
                ),
                stageBonusGold = 600,
                stageBonusElixir = 500
            )
            2 -> StageConfig(
                stageId = 2,
                name = "Skeleton Crypt",
                waves = listOf(
                    WaveConfig(1, skeletons = 6, wizards = 2),
                    WaveConfig(2, skeletons = 10, goblins = 5),
                    WaveConfig(3, skeletons = 12, bossType = CharacterType.SKELETON)
                ),
                stageBonusGold = 900,
                stageBonusElixir = 800
            )
            3 -> StageConfig(
                stageId = 3,
                name = "Elixir Ravine",
                waves = listOf(
                    WaveConfig(1, wizards = 4, goblins = 6),
                    WaveConfig(2, wizards = 6, skeletons = 8),
                    WaveConfig(3, wizards = 8, bossType = CharacterType.GOLEM_BOSS)
                ),
                stageBonusGold = 1400,
                stageBonusElixir = 1200
            )
            4 -> StageConfig(
                stageId = 4,
                name = "Inferno Dragon Peak",
                waves = listOf(
                    WaveConfig(1, wizards = 5, skeletons = 10),
                    WaveConfig(2, wizards = 8, goblins = 10),
                    WaveConfig(3, wizards = 6, bossType = CharacterType.INFERNO_DRAGON_BOSS)
                ),
                stageBonusGold = 2200,
                stageBonusElixir = 2000
            )
            else -> StageConfig(
                stageId = 5,
                name = "Golem Citadel",
                waves = listOf(
                    WaveConfig(1, skeletons = 12, wizards = 6),
                    WaveConfig(2, goblins = 15, wizards = 8),
                    WaveConfig(3, skeletons = 10, bossType = CharacterType.GOLEM_BOSS)
                ),
                stageBonusGold = 3500,
                stageBonusElixir = 3000
            )
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GameScreen
import com.example.ui.MainGameViewModel
import com.example.ui.components.TopNavBar
import com.example.ui.screens.BattleScreen
import com.example.ui.screens.CampaignMapScreen
import com.example.ui.screens.HeroAltarScreen
import com.example.ui.screens.VillageScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val gameViewModel: MainGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val profile by gameViewModel.playerProfile.collectAsStateWithLifecycle()
                val heroes by gameViewModel.heroes.collectAsStateWithLifecycle()
                val stages by gameViewModel.stages.collectAsStateWithLifecycle()
                val activeScreen by gameViewModel.activeScreen.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (activeScreen != GameScreen.BATTLE) {
                            TopNavBar(profile = profile)
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (activeScreen) {
                            GameScreen.VILLAGE -> VillageScreen(
                                viewModel = gameViewModel,
                                profile = profile,
                                heroes = heroes
                            )
                            GameScreen.HERO_ALTAR -> HeroAltarScreen(
                                viewModel = gameViewModel,
                                profile = profile,
                                heroes = heroes
                            )
                            GameScreen.CAMPAIGN_MAP -> CampaignMapScreen(
                                viewModel = gameViewModel,
                                stages = stages
                            )
                            GameScreen.BATTLE -> BattleScreen(
                                viewModel = gameViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

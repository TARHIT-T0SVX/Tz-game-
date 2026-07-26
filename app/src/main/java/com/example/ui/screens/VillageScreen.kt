package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.HeroEntity
import com.example.data.PlayerProfile
import com.example.ui.GameScreen
import com.example.ui.MainGameViewModel
import com.example.ui.theme.*

@Composable
fun VillageScreen(
    viewModel: MainGameViewModel,
    profile: PlayerProfile?,
    heroes: List<HeroEntity>
) {
    val scrollState = rememberScrollState()
    val selectedHero = heroes.find { it.heroId == profile?.selectedHeroId } ?: heroes.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1B18))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Splash Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, ClashGold, RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_clash_hero_banner_1785085177791),
                contentDescription = "Clash Village Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "CLASH VILLAGE HUB",
                    color = ClashGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Active Commander: ${selectedHero?.name ?: "Barbarian King"}",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }

        // ATTACK BUTTON (Primary Call to Action)
        Button(
            onClick = { viewModel.navigateTo(GameScreen.CAMPAIGN_MAP) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .border(2.dp, ClashGoldLight, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ClashGold
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsKabaddi,
                    contentDescription = "Attack",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "ATTACK / BATTLE",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Text(
            text = "VILLAGE BUILDINGS & ALTARS",
            color = Color.LightGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Building Grid Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BuildingCard(
                title = "Town Hall Lvl ${profile?.townHallLevel ?: 1}",
                subtitle = "Upgrade Base",
                icon = Icons.Default.Shield,
                color = ClashGold,
                onClick = { viewModel.upgradeTownHall() },
                actionLabel = "Upgrade (${(profile?.townHallLevel ?: 1) * 2500} G/E)",
                modifier = Modifier.weight(1f)
            )
            BuildingCard(
                title = "Hero Altar",
                subtitle = "Select & Upgrade",
                icon = Icons.Default.Person,
                color = ClashElixirPurple,
                onClick = { viewModel.navigateTo(GameScreen.HERO_ALTAR) },
                actionLabel = "Open Altar",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BuildingCard(
                title = "Gold Mine",
                subtitle = "Collect Gold",
                icon = Icons.Default.MonetizationOn,
                color = ClashGold,
                onClick = { viewModel.collectVillageResources() },
                actionLabel = "Collect +${200 * (profile?.townHallLevel ?: 1)} G",
                modifier = Modifier.weight(1f)
            )
            BuildingCard(
                title = "Elixir Collector",
                subtitle = "Collect Elixir",
                icon = Icons.Default.WaterDrop,
                color = ClashElixirPurple,
                onClick = { viewModel.collectVillageResources() },
                actionLabel = "Collect +${200 * (profile?.townHallLevel ?: 1)} E",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BuildingCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    actionLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = ClashCardBg)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionLabel,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun HeroAltarScreen(
    viewModel: MainGameViewModel,
    profile: PlayerProfile?,
    heroes: List<HeroEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1B18))
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(GameScreen.VILLAGE) },
                modifier = Modifier
                    .background(Color(0xFF2A241F), RoundedCornerShape(12.dp))
                    .border(1.dp, ClashGold, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ClashGold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "HERO ALTAR",
                    color = ClashGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Select & Upgrade Your Clash Heroes",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        // Hero List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(heroes) { hero ->
                val isSelected = profile?.selectedHeroId == hero.heroId
                HeroCardItem(
                    hero = hero,
                    isSelected = isSelected,
                    profile = profile,
                    onSelect = { viewModel.selectHero(hero.heroId) },
                    onUpgrade = {
                        val costG = hero.level * 800
                        val costE = hero.level * 800
                        viewModel.upgradeHero(hero.heroId, costG, costE)
                    }
                )
            }
        }
    }
}

@Composable
private fun HeroCardItem(
    hero: HeroEntity,
    isSelected: Boolean,
    profile: PlayerProfile?,
    onSelect: () -> Unit,
    onUpgrade: () -> Unit
) {
    val imageRes = when (hero.heroId) {
        "archer_queen" -> R.drawable.img_archer_queen_1785085209293
        "pekka" -> R.drawable.img_pekka_1785085222381
        else -> R.drawable.img_barbarian_king_1785085195895
    }

    val skillDescription = when (hero.heroId) {
        "archer_queen" -> "Royal Cloak: Turns invisible & fires rapid elixir arrow volley"
        "pekka" -> "Overcharge: High-voltage electric shockwave area attack"
        "grand_warden" -> "Eternal Tome: Grants damage immunity aura to hero and allies"
        else -> "Iron Fist: Shockwave slam, heals 35% HP & summons 3 Raging Barbarians"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) ClashGold else Color(0xFF3F3730),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF322A23) else ClashCardBg
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hero Image Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, ClashGold, RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = hero.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = hero.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(ClashGold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Lvl ${hero.level}",
                                color = ClashGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Stats Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${hero.maxHp}", color = Color.LightGray, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = ClashGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${hero.attackPower} ATK", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = skillDescription,
                color = ClashElixirPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Select Button
                Button(
                    onClick = onSelect,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF15803D) else Color(0xFF3F3730)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (isSelected) "SELECTED" else "SELECT HERO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Upgrade Button
                val upgradeCost = hero.level * 800
                val canUpgrade = (profile?.gold ?: 0) >= upgradeCost && (profile?.elixir ?: 0) >= upgradeCost
                Button(
                    onClick = onUpgrade,
                    enabled = canUpgrade,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClashGold
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "UPGRADE (${upgradeCost}G)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

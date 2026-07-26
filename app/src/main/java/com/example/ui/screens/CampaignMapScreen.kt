package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StageProgressEntity
import com.example.ui.GameScreen
import com.example.ui.MainGameViewModel
import com.example.ui.theme.*

@Composable
fun CampaignMapScreen(
    viewModel: MainGameViewModel,
    stages: List<StageProgressEntity>
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
                    text = "GOBLIN CAMPAIGN MAP",
                    color = ClashGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Conquer Goblin Fortresses & Epic Bosses",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        // Campaign Stage Items
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(stages) { stage ->
                StageCardItem(
                    stage = stage,
                    onAttack = { viewModel.prepareAndStartBattle(stage.stageId) }
                )
            }
        }
    }
}

@Composable
private fun StageCardItem(
    stage: StageProgressEntity,
    onAttack: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (stage.isUnlocked) ClashGold.copy(alpha = 0.8f) else Color.DarkGray,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (stage.isUnlocked) ClashCardBg else Color(0xFF181512)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "STAGE ${stage.stageId}: ${stage.stageName}",
                        color = if (stage.isUnlocked) Color.White else Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Stars Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isStarred = index < stage.starsEarned
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isStarred) ClashGold else Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Rewards text
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = ClashGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "+${stage.stageId * 600}G", color = Color.LightGray, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = ClashElixirPurple, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "+${stage.stageId * 500}E", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }

            if (stage.isUnlocked) {
                Button(
                    onClick = onAttack,
                    colors = ButtonDefaults.buttonColors(containerColor = ClashGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ATTACK",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(Color.DarkGray, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

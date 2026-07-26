package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.engine.*
import com.example.ui.GameScreen
import com.example.ui.MainGameViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.isActive
import kotlin.math.*

@Composable
fun BattleScreen(viewModel: MainGameViewModel) {
    val engine = viewModel.gameEngine

    val battleStatus by engine.battleStatus.collectAsStateWithLifecycle()
    val hero by engine.hero.collectAsStateWithLifecycle()
    val minions by engine.minions.collectAsStateWithLifecycle()
    val enemies by engine.enemies.collectAsStateWithLifecycle()
    val projectiles by engine.projectiles.collectAsStateWithLifecycle()
    val spells by engine.spells.collectAsStateWithLifecycle()
    val damageNumbers by engine.damageNumbers.collectAsStateWithLifecycle()
    val particles by engine.particles.collectAsStateWithLifecycle()

    val currentWave by engine.currentWave.collectAsStateWithLifecycle()
    val totalWaves by engine.totalWaves.collectAsStateWithLifecycle()
    val lootGold by engine.lootGold.collectAsStateWithLifecycle()
    val lootElixir by engine.lootElixir.collectAsStateWithLifecycle()

    val rageCount by engine.rageSpellCount.collectAsStateWithLifecycle()
    val healCount by engine.healSpellCount.collectAsStateWithLifecycle()
    val freezeCount by engine.freezeSpellCount.collectAsStateWithLifecycle()
    val lightningCount by engine.lightningSpellCount.collectAsStateWithLifecycle()

    var joystickDx by remember { mutableStateOf(0f) }
    var joystickDy by remember { mutableStateOf(0f) }

    var selectedSpellToPlace by remember { mutableStateOf<SpellType?>(null) }

    // 120 FPS Frame Loop
    LaunchedEffect(battleStatus) {
        var lastFrameTimeNanos = System.nanoTime()
        while (isActive && battleStatus == BattleStatus.RUNNING) {
            withFrameNanos { frameNanos ->
                val deltaMs = ((frameNanos - lastFrameTimeNanos) / 1_000_000f).coerceIn(1f, 33f)
                lastFrameTimeNanos = frameNanos
                engine.updateFrame(deltaMs, joystickDx, joystickDy)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // 1. ISOMETRIC 3D ARENA CANVAS
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedSpellToPlace) {
                    detectTapGestures { offset ->
                        // Convert canvas pixels back to arena coords
                        val arenaX = (offset.x / size.width) * GameEngine.ARENA_WIDTH
                        val arenaY = (offset.y / size.height) * GameEngine.ARENA_HEIGHT

                        if (selectedSpellToPlace != null) {
                            engine.dropSpell(selectedSpellToPlace!!, Position(arenaX, arenaY))
                            selectedSpellToPlace = null
                        } else {
                            engine.handleCanvasTap(Position(arenaX, arenaY))
                        }
                    }
                }
        ) {
            drawIsometricArenaGrid(size)

            // Draw Active Spells Ground Rings
            spells.forEach { spell ->
                drawSpellEffectRing(spell, size)
            }

            // Draw Minions
            minions.forEach { minion ->
                if (minion.hp > 0) {
                    drawIsometricCharacter(minion, size)
                }
            }

            // Draw Hero Player Character
            hero?.let { player ->
                if (player.hp > 0) {
                    drawIsometricCharacter(player, size)
                }
            }

            // Draw Enemies & Bosses
            enemies.forEach { enemy ->
                if (enemy.hp > 0) {
                    drawIsometricCharacter(enemy, size)
                }
            }

            // Draw Projectiles
            projectiles.forEach { proj ->
                drawIsometricProjectile(proj, size)
            }

            // Draw Particles
            particles.forEach { part ->
                drawParticle(part, size)
            }

            // Draw Damage Numbers
            damageNumbers.forEach { dmg ->
                drawDamageText(dmg, size)
            }
        }

        // 2. TOP HUD (Wave, Boss Health, Loot, Exit)
        TopBattleHUD(
            currentWave = currentWave,
            totalWaves = totalWaves,
            boss = enemies.find { it.isBoss && it.hp > 0 },
            lootGold = lootGold,
            lootElixir = lootElixir,
            onExit = { viewModel.navigateTo(GameScreen.CAMPAIGN_MAP) }
        )

        // 3. SPELL SELECTION BAR (Top-Right / Center-Right)
        SpellBarOverlay(
            rageCount = rageCount,
            healCount = healCount,
            freezeCount = freezeCount,
            lightningCount = lightningCount,
            selectedSpell = selectedSpellToPlace,
            onSelectSpell = { spell ->
                selectedSpellToPlace = if (selectedSpellToPlace == spell) null else spell
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 12.dp)
        )

        if (selectedSpellToPlace != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 130.dp)
                    .background(ClashGold, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "TAP ANYWHERE ON ARENA TO DROP ${selectedSpellToPlace!!.name} SPELL",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }

        // 4. BOTTOM GAME CONTROLS (Joystick & Hero Abilities)
        BottomBattleControls(
            hero = hero,
            onJoystickMove = { dx, dy ->
                joystickDx = dx
                joystickDy = dy
            },
            onSpecialAbility = { engine.castSpecialAbility() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        )

        // 5. VICTORY / DEFEAT OVERLAY
        AnimatedVisibility(
            visible = battleStatus == BattleStatus.VICTORY || battleStatus == BattleStatus.DEFEAT,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val isVictory = battleStatus == BattleStatus.VICTORY
            BattleResultDialog(
                isVictory = isVictory,
                lootGold = lootGold,
                lootElixir = lootElixir,
                onContinue = {
                    viewModel.finishBattle(
                        starsEarned = if (isVictory) 3 else 0,
                        goldEarned = lootGold,
                        elixirEarned = lootElixir
                    )
                    viewModel.navigateTo(GameScreen.CAMPAIGN_MAP)
                }
            )
        }
    }
}

@Composable
private fun TopBattleHUD(
    currentWave: Int,
    totalWaves: Int,
    boss: GameCharacter?,
    lootGold: Int,
    lootElixir: Int,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wave Pill
            Box(
                modifier = Modifier
                    .background(Color(0xFF2A241F), RoundedCornerShape(12.dp))
                    .border(1.dp, ClashGold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "WAVE $currentWave / $totalWaves",
                    color = ClashGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            // Loot
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF1B1815), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = ClashGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+$lootGold", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF1B1815), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = ClashElixirPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+$lootElixir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Exit Button
            IconButton(
                onClick = onExit,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF2A241F), CircleShape)
                    .border(1.dp, Color.Gray, CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // BOSS HEALTH BAR (If Boss is present)
        boss?.let { b ->
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                    .border(1.dp, ClashGold, RoundedCornerShape(10.dp))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = b.name.uppercase(),
                    color = ClashGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { b.hpPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color.Red,
                    trackColor = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun SpellBarOverlay(
    rageCount: Int,
    healCount: Int,
    freezeCount: Int,
    lightningCount: Int,
    selectedSpell: SpellType?,
    onSelectSpell: (SpellType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .border(1.dp, ClashGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpellIconButton(SpellType.RAGE, rageCount, selectedSpell == SpellType.RAGE, ClashElixirPurple, Icons.Default.Bolt) { onSelectSpell(SpellType.RAGE) }
        SpellIconButton(SpellType.HEALING, healCount, selectedSpell == SpellType.HEALING, Color.Green, Icons.Default.Favorite) { onSelectSpell(SpellType.HEALING) }
        SpellIconButton(SpellType.FREEZE, freezeCount, selectedSpell == SpellType.FREEZE, Color.Cyan, Icons.Default.AcUnit) { onSelectSpell(SpellType.FREEZE) }
        SpellIconButton(SpellType.LIGHTNING, lightningCount, selectedSpell == SpellType.LIGHTNING, ClashGold, Icons.Default.FlashOn) { onSelectSpell(SpellType.LIGHTNING) }
    }
}

@Composable
private fun SpellIconButton(
    type: SpellType,
    count: Int,
    isSelected: Boolean,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color else Color(0xFF2A241F))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else color.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Color.Black else color, modifier = Modifier.size(24.dp))
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.Black, CircleShape)
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(text = "$count", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BottomBattleControls(
    hero: GameCharacter?,
    onJoystickMove: (Float, Float) -> Unit,
    onSpecialAbility: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Virtual Joystick Controller
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .border(2.dp, ClashGold.copy(alpha = 0.7f), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { onJoystickMove(0f, 0f) },
                        onDragCancel = { onJoystickMove(0f, 0f) },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val maxRadius = 60f
                            val dx = (dragAmount.x / maxRadius).coerceIn(-1f, 1f)
                            val dy = (dragAmount.y / maxRadius).coerceIn(-1f, 1f)
                            onJoystickMove(dx, dy)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ClashGold)
                    .border(2.dp, Color.White, CircleShape)
            )
        }

        // Special Ability Button
        val cooldownPct = if ((hero?.skillMaxCooldownMs ?: 1f) > 0) {
            (hero?.skillCooldownRemainingMs ?: 0f) / (hero?.skillMaxCooldownMs ?: 10000f)
        } else 0f

        val canCast = hero != null && hero.skillCooldownRemainingMs <= 0

        Button(
            onClick = onSpecialAbility,
            enabled = canCast,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(3.dp, if (canCast) ClashGold else Color.Gray, CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canCast) ClashElixirPurple else Color.DarkGray
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Special Skill",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                if (cooldownPct > 0) {
                    CircularProgressIndicator(
                        progress = { cooldownPct },
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Red,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}

@Composable
private fun BattleResultDialog(
    isVictory: Boolean,
    lootGold: Int,
    lootElixir: Int,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, if (isVictory) ClashGold else Color.Red, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = ClashCardBg)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isVictory) "VICTORY!" else "DEFEAT",
                    color = if (isVictory) ClashGold else Color.Red,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isVictory) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ClashGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "LOOT EARNED:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = ClashGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "+$lootGold", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = ClashElixirPurple)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "+$lootElixir", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "Your hero was defeated! Upgrade your hero at the Altar and try again.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVictory) ClashGold else Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CONTINUE",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// DrawScope Isometric Rendering Extensions
private fun DrawScope.drawIsometricArenaGrid(size: Size) {
    // Ground Grass Gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF15803D), Color(0xFF166534))
        ),
        size = size
    )

    // Draw Isometric Tile Lines
    val gridCols = 10
    val gridRows = 14
    val cellW = size.width / gridCols
    val cellH = size.height / gridRows

    for (i in 0..gridCols) {
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(i * cellW, 0f),
            end = Offset(i * cellW, size.height),
            strokeWidth = 1f
        )
    }
    for (j in 0..gridRows) {
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(0f, j * cellH),
            end = Offset(size.width, j * cellH),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawIsometricCharacter(char: GameCharacter, canvasSize: Size) {
    val px = (char.pos.x / GameEngine.ARENA_WIDTH) * canvasSize.width
    val py = (char.pos.y / GameEngine.ARENA_HEIGHT) * canvasSize.height

    // 1. Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.35f),
        topLeft = Offset(px - 22f, py - 6f),
        size = Size(44f, 14f)
    )

    // 2. Character Body 3D Color
    val baseColor = when (char.type) {
        CharacterType.BARBARIAN_KING -> ClashGold
        CharacterType.ARCHER_QUEEN -> ClashElixirPurple
        CharacterType.PEKKA -> Color(0xFF3B82F6)
        CharacterType.GRAND_WARDEN -> Color(0xFFF59E0B)
        CharacterType.GOBLIN -> Color(0xFF22C55E)
        CharacterType.SKELETON -> Color.LightGray
        CharacterType.WIZARD -> Color(0xFFEF4444)
        CharacterType.INFERNO_DRAGON_BOSS -> Color(0xFFDC2626)
        CharacterType.GOLEM_BOSS -> Color(0xFF78716C)
    }

    val bodyRadius = if (char.isBoss) 36f else if (char.isHero) 24f else 16f

    // Draw Body
    drawCircle(
        color = if (char.isFrozen) Color.Cyan else baseColor,
        radius = bodyRadius,
        center = Offset(px, py - bodyRadius / 2f)
    )

    // Draw Facing Direction Nose/Crown Accent
    val noseX = px + cos(char.facingAngleRad) * (bodyRadius * 0.8f)
    val noseY = (py - bodyRadius / 2f) + sin(char.facingAngleRad) * (bodyRadius * 0.8f)
    drawCircle(color = Color.White, radius = 4f, center = Offset(noseX, noseY))

    // Raged / Shielded Aura
    if (char.isRaged) {
        drawCircle(color = Color.Yellow.copy(alpha = 0.4f), radius = bodyRadius * 1.4f, center = Offset(px, py - bodyRadius / 2f))
    }
    if (char.isShielded) {
        drawCircle(color = ClashGold.copy(alpha = 0.6f), radius = bodyRadius * 1.5f, center = Offset(px, py - bodyRadius / 2f), style = Stroke(width = 4f))
    }

    // Health Bar
    val barW = bodyRadius * 2.2f
    val barH = 6f
    val barX = px - barW / 2f
    val barY = py - bodyRadius * 1.8f

    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(barX, barY),
        size = Size(barW, barH),
        cornerRadius = CornerRadius(3f)
    )
    drawRoundRect(
        color = if (char.isHero) Color.Green else Color.Red,
        topLeft = Offset(barX, barY),
        size = Size(barW * char.hpPercent, barH),
        cornerRadius = CornerRadius(3f)
    )
}

private fun DrawScope.drawIsometricProjectile(proj: Projectile, canvasSize: Size) {
    val px = (proj.currentPos.x / GameEngine.ARENA_WIDTH) * canvasSize.width
    val py = (proj.currentPos.y / GameEngine.ARENA_HEIGHT) * canvasSize.height

    drawCircle(
        color = Color(proj.colorHex),
        radius = proj.radius,
        center = Offset(px, py)
    )
}

private fun DrawScope.drawSpellEffectRing(spell: SpellEffect, canvasSize: Size) {
    val px = (spell.pos.x / GameEngine.ARENA_WIDTH) * canvasSize.width
    val py = (spell.pos.y / GameEngine.ARENA_HEIGHT) * canvasSize.height
    val r = spell.radius

    val color = when (spell.type) {
        SpellType.RAGE -> ClashElixirPurple
        SpellType.HEALING -> Color.Green
        SpellType.FREEZE -> Color.Cyan
        SpellType.LIGHTNING -> ClashGold
    }

    drawCircle(
        color = color.copy(alpha = 0.25f),
        radius = r,
        center = Offset(px, py)
    )
    drawCircle(
        color = color,
        radius = r,
        center = Offset(px, py),
        style = Stroke(width = 3f)
    )
}

private fun DrawScope.drawParticle(p: Particle, canvasSize: Size) {
    val px = (p.x / GameEngine.ARENA_WIDTH) * canvasSize.width
    val py = (p.y / GameEngine.ARENA_HEIGHT) * canvasSize.height
    drawCircle(
        color = p.color.copy(alpha = (p.lifeMs / p.maxLifeMs).coerceIn(0f, 1f)),
        radius = p.radius,
        center = Offset(px, py)
    )
}

private fun DrawScope.drawDamageText(d: DamageNumber, canvasSize: Size) {
    val px = (d.x / GameEngine.ARENA_WIDTH) * canvasSize.width
    val py = (d.y / GameEngine.ARENA_HEIGHT) * canvasSize.height

    drawCircle(
        color = d.color.copy(alpha = (d.lifeMs / 1000f).coerceIn(0f, 1f)),
        radius = if (d.isCritical) 12f else 6f,
        center = Offset(px, py)
    )
}

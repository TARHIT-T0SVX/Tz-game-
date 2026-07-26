package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.data.HeroEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

class GameEngine {

    companion object {
        const val ARENA_WIDTH = 1000f
        const val ARENA_HEIGHT = 1400f
    }

    // Game state reactive containers
    private val _hero = MutableStateFlow<GameCharacter?>(null)
    val hero: StateFlow<GameCharacter?> = _hero.asStateFlow()

    private val _minions = MutableStateFlow<List<GameCharacter>>(emptyList())
    val minions: StateFlow<List<GameCharacter>> = _minions.asStateFlow()

    private val _enemies = MutableStateFlow<List<GameCharacter>>(emptyList())
    val enemies: StateFlow<List<GameCharacter>> = _enemies.asStateFlow()

    private val _projectiles = MutableStateFlow<List<Projectile>>(emptyList())
    val projectiles: StateFlow<List<Projectile>> = _projectiles.asStateFlow()

    private val _spells = MutableStateFlow<List<SpellEffect>>(emptyList())
    val spells: StateFlow<List<SpellEffect>> = _spells.asStateFlow()

    private val _damageNumbers = MutableStateFlow<List<DamageNumber>>(emptyList())
    val damageNumbers: StateFlow<List<DamageNumber>> = _damageNumbers.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    private val _battleStatus = MutableStateFlow(BattleStatus.NOT_STARTED)
    val battleStatus: StateFlow<BattleStatus> = _battleStatus.asStateFlow()

    private val _currentWave = MutableStateFlow(1)
    val currentWave: StateFlow<Int> = _currentWave.asStateFlow()

    private val _totalWaves = MutableStateFlow(3)
    val totalWaves: StateFlow<Int> = _totalWaves.asStateFlow()

    private val _lootGold = MutableStateFlow(0)
    val lootGold: StateFlow<Int> = _lootGold.asStateFlow()

    private val _lootElixir = MutableStateFlow(0)
    val lootElixir: StateFlow<Int> = _lootElixir.asStateFlow()

    private var activeStageConfig: StageConfig? = null
    private var nextEntityId = 1L

    // Spell Charges
    val rageSpellCount = MutableStateFlow(3)
    val healSpellCount = MutableStateFlow(3)
    val freezeSpellCount = MutableStateFlow(2)
    val lightningSpellCount = MutableStateFlow(2)

    fun startBattle(heroEntity: HeroEntity, stageConfig: StageConfig) {
        activeStageConfig = stageConfig
        _totalWaves.value = stageConfig.waves.size
        _currentWave.value = 1
        _lootGold.value = 0
        _lootElixir.value = 0
        _projectiles.value = emptyList()
        _spells.value = emptyList()
        _damageNumbers.value = emptyList()
        _particles.value = emptyList()
        _minions.value = emptyList()

        rageSpellCount.value = 3
        healSpellCount.value = 3
        freezeSpellCount.value = 2
        lightningSpellCount.value = 2

        val charType = when (heroEntity.heroId) {
            "archer_queen" -> CharacterType.ARCHER_QUEEN
            "pekka" -> CharacterType.PEKKA
            "grand_warden" -> CharacterType.GRAND_WARDEN
            else -> CharacterType.BARBARIAN_KING
        }

        val range = when (charType) {
            CharacterType.ARCHER_QUEEN -> 320f
            CharacterType.GRAND_WARDEN -> 380f
            else -> 60f
        }

        val playerHero = GameCharacter(
            id = "hero_player",
            name = heroEntity.name,
            type = charType,
            isHero = true,
            pos = Position(ARENA_WIDTH / 2f, ARENA_HEIGHT - 250f),
            maxHp = heroEntity.maxHp.toFloat(),
            hp = heroEntity.maxHp.toFloat(),
            attackPower = heroEntity.attackPower.toFloat(),
            attackRange = range,
            attackSpeed = heroEntity.attackSpeed,
            baseMoveSpeed = heroEntity.speed * 40f,
            skillMaxCooldownMs = 8000f
        )
        _hero.value = playerHero
        _battleStatus.value = BattleStatus.RUNNING

        spawnWave(1)
    }

    private fun spawnWave(waveNum: Int) {
        val config = activeStageConfig?.waves?.getOrNull(waveNum - 1) ?: return
        val spawnedList = mutableListOf<GameCharacter>()

        var spawnY = 200f
        fun randomX(): Float = Random.nextFloat() * (ARENA_WIDTH - 200f) + 100f

        // Goblins
        repeat(config.goblins) { i ->
            spawnedList.add(
                GameCharacter(
                    id = "enemy_goblin_${waveNum}_$i",
                    name = "Goblin",
                    type = CharacterType.GOBLIN,
                    isHero = false,
                    pos = Position(randomX(), spawnY + Random.nextFloat() * 100f),
                    maxHp = 180f + waveNum * 40f,
                    hp = 180f + waveNum * 40f,
                    attackPower = 35f + waveNum * 10f,
                    attackRange = 45f,
                    attackSpeed = 1.4f,
                    baseMoveSpeed = 190f
                )
            )
        }

        // Skeletons
        repeat(config.skeletons) { i ->
            spawnedList.add(
                GameCharacter(
                    id = "enemy_skeleton_${waveNum}_$i",
                    name = "Skeleton",
                    type = CharacterType.SKELETON,
                    isHero = false,
                    pos = Position(randomX(), spawnY + Random.nextFloat() * 120f),
                    maxHp = 240f + waveNum * 50f,
                    hp = 240f + waveNum * 50f,
                    attackPower = 45f + waveNum * 12f,
                    attackRange = 50f,
                    attackSpeed = 1.2f,
                    baseMoveSpeed = 160f
                )
            )
        }

        // Wizards
        repeat(config.wizards) { i ->
            spawnedList.add(
                GameCharacter(
                    id = "enemy_wizard_${waveNum}_$i",
                    name = "Fire Wizard",
                    type = CharacterType.WIZARD,
                    isHero = false,
                    pos = Position(randomX(), spawnY + Random.nextFloat() * 80f),
                    maxHp = 320f + waveNum * 60f,
                    hp = 320f + waveNum * 60f,
                    attackPower = 70f + waveNum * 15f,
                    attackRange = 300f,
                    attackSpeed = 1.1f,
                    baseMoveSpeed = 130f
                )
            )
        }

        // Boss
        config.bossType?.let { bossType ->
            val (bossName, hp, atk, range) = when (bossType) {
                CharacterType.INFERNO_DRAGON_BOSS -> Quad("Inferno Dragon Boss", 3200f, 120f, 340f)
                CharacterType.GOLEM_BOSS -> Quad("Mountain Golem Boss", 4500f, 180f, 70f)
                else -> Quad("Dark PEKKA Boss", 3800f, 220f, 65f)
            }
            spawnedList.add(
                GameCharacter(
                    id = "boss_${waveNum}",
                    name = bossName,
                    type = bossType,
                    isHero = false,
                    isBoss = true,
                    pos = Position(ARENA_WIDTH / 2f, 220f),
                    maxHp = hp,
                    hp = hp,
                    attackPower = atk,
                    attackRange = range,
                    attackSpeed = 0.9f,
                    baseMoveSpeed = 110f
                )
            )
        }

        _enemies.value = spawnedList
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    // Main 120 FPS frame update call
    fun updateFrame(deltaMs: Float, joystickDx: Float, joystickDy: Float) {
        if (_battleStatus.value != BattleStatus.RUNNING) return

        val player = _hero.value ?: return
        if (player.hp <= 0) {
            player.actionState = ActionState.DEAD
            _battleStatus.value = BattleStatus.DEFEAT
            return
        }

        // 1. Update Spells
        val currentSpells = _spells.value.toMutableList()
        val itSpells = currentSpells.iterator()
        while (itSpells.hasNext()) {
            val spell = itSpells.next()
            spell.durationRemainingMs -= deltaMs
            if (spell.durationRemainingMs <= 0) {
                itSpells.remove()
            } else {
                applySpellEffects(spell, deltaMs)
            }
        }
        _spells.value = currentSpells

        // 2. Update Player Hero Movement & Cooldowns
        if (player.skillCooldownRemainingMs > 0) {
            player.skillCooldownRemainingMs = (player.skillCooldownRemainingMs - deltaMs).coerceAtLeast(0f)
        }
        if (player.attackCooldownMs > 0) {
            player.attackCooldownMs = (player.attackCooldownMs - deltaMs).coerceAtLeast(0f)
        }
        if (player.attackAnimationTimerMs > 0) {
            player.attackAnimationTimerMs = (player.attackAnimationTimerMs - deltaMs).coerceAtLeast(0f)
        }

        // Handle Joystick movement
        if (abs(joystickDx) > 0.05f || abs(joystickDy) > 0.05f) {
            val moveMult = if (player.isRaged) 1.6f else 1.0f
            val moveDist = player.currentMoveSpeed * moveMult * (deltaMs / 1000f)
            val newX = (player.pos.x + joystickDx * moveDist).coerceIn(60f, ARENA_WIDTH - 60f)
            val newY = (player.pos.y + joystickDy * moveDist).coerceIn(120f, ARENA_HEIGHT - 120f)
            player.pos = Position(newX, newY)
            player.facingAngleRad = atan2(joystickDy, joystickDx)
            player.actionState = ActionState.WALKING
            player.targetPos = null
        } else if (player.targetPos != null) {
            // Tap-to-move target pathing
            val tPos = player.targetPos!!
            val dist = player.pos.distanceTo(tPos)
            if (dist > 15f) {
                val angle = player.pos.angleTo(tPos)
                val moveDist = player.currentMoveSpeed * (if (player.isRaged) 1.6f else 1.0f) * (deltaMs / 1000f)
                player.pos.x += cos(angle) * moveDist
                player.pos.y += sin(angle) * moveDist
                player.facingAngleRad = angle
                player.actionState = ActionState.WALKING
            } else {
                player.targetPos = null
                player.actionState = ActionState.IDLE
            }
        } else if (player.attackAnimationTimerMs <= 0) {
            player.actionState = ActionState.IDLE
        }

        // 3. Hero Targeting & Auto Attack
        val aliveEnemies = _enemies.value.filter { it.hp > 0 }
        if (aliveEnemies.isEmpty()) {
            if (_currentWave.value < _totalWaves.value) {
                _currentWave.value += 1
                spawnWave(_currentWave.value)
            } else {
                _battleStatus.value = BattleStatus.VICTORY
                _lootGold.value += activeStageConfig?.stageBonusGold ?: 500
                _lootElixir.value += activeStageConfig?.stageBonusElixir ?: 500
                return
            }
        }

        val nearestEnemy = aliveEnemies.minByOrNull { player.pos.distanceTo(it.pos) }
        if (nearestEnemy != null) {
            val distToEnemy = player.pos.distanceTo(nearestEnemy.pos)
            if (distToEnemy <= player.attackRange) {
                player.facingAngleRad = player.pos.angleTo(nearestEnemy.pos)
                if (player.attackCooldownMs <= 0) {
                    performCharacterAttack(player, nearestEnemy)
                    player.attackCooldownMs = 1000f / (player.attackSpeed * if (player.isRaged) 1.5f else 1.0f)
                    player.attackAnimationTimerMs = 300f
                }
            }
        }

        // 4. Update Minions
        val currentMinions = _minions.value.toMutableList()
        val minionIterator = currentMinions.iterator()
        while (minionIterator.hasNext()) {
            val minion = minionIterator.next()
            if (minion.hp <= 0) {
                minionIterator.remove()
                continue
            }
            if (minion.attackCooldownMs > 0) {
                minion.attackCooldownMs = (minion.attackCooldownMs - deltaMs).coerceAtLeast(0f)
            }
            val target = aliveEnemies.minByOrNull { minion.pos.distanceTo(it.pos) }
            if (target != null) {
                val d = minion.pos.distanceTo(target.pos)
                if (d <= minion.attackRange) {
                    if (minion.attackCooldownMs <= 0) {
                        performCharacterAttack(minion, target)
                        minion.attackCooldownMs = 1000f / minion.attackSpeed
                    }
                } else {
                    val angle = minion.pos.angleTo(target.pos)
                    val mSpeed = minion.baseMoveSpeed * (deltaMs / 1000f)
                    minion.pos.x += cos(angle) * mSpeed
                    minion.pos.y += sin(angle) * mSpeed
                    minion.facingAngleRad = angle
                }
            }
        }
        _minions.value = currentMinions

        // 5. Update Enemies AI & Attacks
        val currentEnemies = _enemies.value.toMutableList()
        currentEnemies.forEach { enemy ->
            if (enemy.hp > 0) {
                if (enemy.freezeDurationMs > 0) {
                    enemy.freezeDurationMs = (enemy.freezeDurationMs - deltaMs).coerceAtLeast(0f)
                    enemy.isFrozen = enemy.freezeDurationMs > 0
                }
                if (enemy.attackCooldownMs > 0) {
                    enemy.attackCooldownMs = (enemy.attackCooldownMs - deltaMs).coerceAtLeast(0f)
                }

                if (!enemy.isFrozen) {
                    // Enemy moves towards hero or nearest minion
                    val targets = mutableListOf(player)
                    targets.addAll(currentMinions.filter { it.hp > 0 })
                    val closestTarget = targets.minByOrNull { enemy.pos.distanceTo(it.pos) }

                    if (closestTarget != null) {
                        val d = enemy.pos.distanceTo(closestTarget.pos)
                        enemy.facingAngleRad = enemy.pos.angleTo(closestTarget.pos)
                        if (d <= enemy.attackRange) {
                            if (enemy.attackCooldownMs <= 0) {
                                performCharacterAttack(enemy, closestTarget)
                                enemy.attackCooldownMs = 1000f / enemy.attackSpeed
                            }
                        } else {
                            val angle = enemy.pos.angleTo(closestTarget.pos)
                            val moveSpeed = enemy.baseMoveSpeed * (deltaMs / 1000f)
                            enemy.pos.x += cos(angle) * moveSpeed
                            enemy.pos.y += sin(angle) * moveSpeed
                            enemy.actionState = ActionState.WALKING
                        }
                    }
                }
            }
        }
        _enemies.value = currentEnemies

        // 6. Update Projectiles
        val currentProjectiles = _projectiles.value.toMutableList()
        val projIt = currentProjectiles.iterator()
        while (projIt.hasNext()) {
            val proj = projIt.next()
            val dx = proj.targetPos.x - proj.currentPos.x
            val dy = proj.targetPos.y - proj.currentPos.y
            val dist = sqrt(dx * dx + dy * dy)
            val moveStep = proj.speed * (deltaMs / 1000f)

            if (dist <= moveStep) {
                // Hit target position!
                projIt.remove()
                handleProjectileImpact(proj)
            } else {
                proj.currentPos.x += (dx / dist) * moveStep
                proj.currentPos.y += (dy / dist) * moveStep
            }
        }
        _projectiles.value = currentProjectiles

        // 7. Update Particles & Floating Damage
        val currentDmg = _damageNumbers.value.toMutableList()
        val dmgIt = currentDmg.iterator()
        while (dmgIt.hasNext()) {
            val d = dmgIt.next()
            d.lifeMs -= deltaMs
            d.y -= 30f * (deltaMs / 1000f)
            if (d.lifeMs <= 0) dmgIt.remove()
        }
        _damageNumbers.value = currentDmg

        val currentParts = _particles.value.toMutableList()
        val partIt = currentParts.iterator()
        while (partIt.hasNext()) {
            val p = partIt.next()
            p.lifeMs -= deltaMs
            p.x += p.vx * (deltaMs / 1000f)
            p.y += p.vy * (deltaMs / 1000f)
            if (p.lifeMs <= 0) partIt.remove()
        }
        _particles.value = currentParts
    }

    private fun performCharacterAttack(attacker: GameCharacter, defender: GameCharacter) {
        attacker.actionState = ActionState.ATTACKING

        val isMelee = attacker.attackRange <= 80f
        if (isMelee) {
            // Melee Slash
            val damage = attacker.attackPower * (if (attacker.isRaged) 1.4f else 1.0f)
            if (!defender.isShielded) {
                applyDamage(defender, damage, attacker.isHero)
            }
            spawnHitParticles(defender.pos.x, defender.pos.y, if (attacker.isHero) Color.Yellow else Color.Red)
        } else {
            // Ranged Projectile
            val projColor = when (attacker.type) {
                CharacterType.ARCHER_QUEEN -> 0xFFEC4899 // Elixir Pink Arrow
                CharacterType.GRAND_WARDEN -> 0xFFF59E0B // Golden Orb
                CharacterType.WIZARD -> 0xFFEF4444 // Fireball
                CharacterType.INFERNO_DRAGON_BOSS -> 0xFFDC2626 // Flame Beam
                else -> 0xFF3B82F6
            }

            val proj = Projectile(
                id = nextEntityId++,
                startPos = Position(attacker.pos.x, attacker.pos.y),
                currentPos = Position(attacker.pos.x, attacker.pos.y),
                targetPos = Position(defender.pos.x, defender.pos.y),
                targetCharacterId = defender.id,
                speed = 750f,
                damage = attacker.attackPower,
                isHeroProjectile = attacker.isHero,
                colorHex = projColor,
                isSplash = attacker.type == CharacterType.WIZARD || attacker.type == CharacterType.INFERNO_DRAGON_BOSS,
                splashRadius = if (attacker.isBoss) 120f else 60f
            )
            val list = _projectiles.value.toMutableList()
            list.add(proj)
            _projectiles.value = list
        }
    }

    private fun handleProjectileImpact(proj: Projectile) {
        spawnHitParticles(proj.targetPos.x, proj.targetPos.y, Color(proj.colorHex))

        if (proj.isSplash) {
            val targets = if (proj.isHeroProjectile) _enemies.value else listOfNotNull(_hero.value)
            targets.forEach { target ->
                if (target.pos.distanceTo(proj.targetPos) <= proj.splashRadius && !target.isShielded) {
                    applyDamage(target, proj.damage, proj.isHeroProjectile)
                }
            }
        } else {
            val allPossible = mutableListOf<GameCharacter>()
            _hero.value?.let { allPossible.add(it) }
            allPossible.addAll(_enemies.value)
            allPossible.addAll(_minions.value)

            val hitChar = allPossible.find { it.id == proj.targetCharacterId } ?: allPossible.minByOrNull { it.pos.distanceTo(proj.targetPos) }
            if (hitChar != null && hitChar.pos.distanceTo(proj.targetPos) <= 100f) {
                if (!hitChar.isShielded) {
                    applyDamage(hitChar, proj.damage, proj.isHeroProjectile)
                }
            }
        }
    }

    private fun applyDamage(target: GameCharacter, damageAmount: Float, isHeroSource: Boolean) {
        val isCrit = Random.nextFloat() < 0.25f
        val finalDmg = if (isCrit) damageAmount * 1.6f else damageAmount

        target.hp = (target.hp - finalDmg).coerceAtLeast(0f)

        // Show floating text
        val text = if (isCrit) "CRIT ${finalDmg.toInt()}" else "-${finalDmg.toInt()}"
        val color = if (target.isHero) Color.Red else if (isCrit) Color(0xFFF59E0B) else Color.White
        addDamageNumber(target.pos.x, target.pos.y - 40f, text, color, isCrit)

        if (target.hp <= 0 && !target.isHero) {
            _lootGold.value += if (target.isBoss) 600 else 40
            _lootElixir.value += if (target.isBoss) 500 else 30
            spawnLootCoins(target.pos.x, target.pos.y)
        }
    }

    private fun applySpellEffects(spell: SpellEffect, deltaMs: Float) {
        val radius = spell.radius
        when (spell.type) {
            SpellType.RAGE -> {
                _hero.value?.let {
                    if (it.pos.distanceTo(spell.pos) <= radius) it.isRaged = true
                }
                _minions.value.forEach {
                    if (it.pos.distanceTo(spell.pos) <= radius) it.isRaged = true
                }
            }
            SpellType.HEALING -> {
                val healAmt = 90f * (deltaMs / 1000f)
                _hero.value?.let {
                    if (it.pos.distanceTo(spell.pos) <= radius) {
                        it.hp = (it.hp + healAmt).coerceAtMost(it.maxHp)
                        addDamageNumber(it.pos.x, it.pos.y - 30f, "+${healAmt.toInt()}", Color.Green, false)
                    }
                }
                _minions.value.forEach {
                    if (it.pos.distanceTo(spell.pos) <= radius) {
                        it.hp = (it.hp + healAmt).coerceAtMost(it.maxHp)
                    }
                }
            }
            SpellType.FREEZE -> {
                _enemies.value.forEach {
                    if (it.pos.distanceTo(spell.pos) <= radius) {
                        it.isFrozen = true
                        it.freezeDurationMs = 3500f
                    }
                }
            }
            SpellType.LIGHTNING -> {
                if (spell.durationRemainingMs >= spell.maxDurationMs - 100f) {
                    // Strike instant lightning damage
                    _enemies.value.filter { it.pos.distanceTo(spell.pos) <= radius }.take(4).forEach {
                        applyDamage(it, 480f, true)
                        spawnHitParticles(it.pos.x, it.pos.y, Color.Cyan)
                    }
                }
            }
        }
    }

    fun castSpecialAbility() {
        val player = _hero.value ?: return
        if (player.skillCooldownRemainingMs > 0 || player.hp <= 0) return

        player.skillCooldownRemainingMs = player.skillMaxCooldownMs
        player.actionState = ActionState.SPECIAL

        when (player.type) {
            CharacterType.BARBARIAN_KING -> {
                // Iron Fist: Shockwave damage, Heal 35%, Spawn 3 Barbarians!
                player.hp = (player.hp + player.maxHp * 0.35f).coerceAtMost(player.maxHp)
                addDamageNumber(player.pos.x, player.pos.y - 50f, "IRON FIST!", Color(0xFFF59E0B), true)

                // Damage surrounding enemies
                _enemies.value.filter { it.pos.distanceTo(player.pos) <= 220f }.forEach {
                    applyDamage(it, 320f, true)
                }

                // Spawn Barbarians
                val newMinions = _minions.value.toMutableList()
                repeat(3) { i ->
                    newMinions.add(
                        GameCharacter(
                            id = "minion_barbarian_${nextEntityId++}",
                            name = "Rage Barbarian",
                            type = CharacterType.BARBARIAN_KING,
                            isHero = false,
                            pos = Position(player.pos.x + Random.nextFloat() * 80f - 40f, player.pos.y + Random.nextFloat() * 80f - 40f),
                            maxHp = 450f,
                            hp = 450f,
                            attackPower = 95f,
                            attackRange = 50f,
                            attackSpeed = 1.3f,
                            baseMoveSpeed = 170f,
                            isRaged = true
                        )
                    )
                }
                _minions.value = newMinions
            }
            CharacterType.ARCHER_QUEEN -> {
                // Royal Cloak: Invisibility + Rapid Volley!
                player.isCloaked = true
                addDamageNumber(player.pos.x, player.pos.y - 50f, "ROYAL CLOAK!", Color(0xFFEC4899), true)

                val targets = _enemies.value.filter { it.hp > 0 }.take(3)
                targets.forEach { target ->
                    performCharacterAttack(player, target)
                    performCharacterAttack(player, target)
                }
            }
            CharacterType.PEKKA -> {
                // Overcharge: High Voltage Shockwave!
                addDamageNumber(player.pos.x, player.pos.y - 50f, "OVERCHARGE!", Color(0xFFA855F7), true)
                player.isRaged = true
                _enemies.value.filter { it.pos.distanceTo(player.pos) <= 300f }.forEach {
                    applyDamage(it, 500f, true)
                    spawnHitParticles(it.pos.x, it.pos.y, Color.Magenta)
                }
            }
            CharacterType.GRAND_WARDEN -> {
                // Eternal Tome: Immunity Shield!
                player.isShielded = true
                addDamageNumber(player.pos.x, player.pos.y - 50f, "ETERNAL TOME!", Color(0xFFF59E0B), true)
                _minions.value.forEach { it.isShielded = true }
            }
            else -> {}
        }
    }

    fun dropSpell(type: SpellType, dropPos: Position) {
        val countState = when (type) {
            SpellType.RAGE -> rageSpellCount
            SpellType.HEALING -> healSpellCount
            SpellType.FREEZE -> freezeSpellCount
            SpellType.LIGHTNING -> lightningSpellCount
        }
        if (countState.value <= 0) return

        countState.value -= 1
        val newSpells = _spells.value.toMutableList()
        newSpells.add(
            SpellEffect(
                id = nextEntityId++,
                type = type,
                pos = dropPos,
                radius = if (type == SpellType.FREEZE) 160f else 140f
            )
        )
        _spells.value = newSpells
    }

    fun handleCanvasTap(tapPos: Position) {
        val player = _hero.value ?: return
        if (player.hp > 0) {
            player.targetPos = tapPos
        }
    }

    private fun addDamageNumber(x: Float, y: Float, text: String, color: Color, isCrit: Boolean) {
        val list = _damageNumbers.value.toMutableList()
        list.add(
            DamageNumber(
                id = nextEntityId++,
                x = x,
                y = y,
                damageText = text,
                color = color,
                isCritical = isCrit
            )
        )
        _damageNumbers.value = list
    }

    private fun spawnHitParticles(x: Float, y: Float, color: Color) {
        val list = _particles.value.toMutableList()
        repeat(8) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 180f + 60f
            list.add(
                Particle(
                    id = nextEntityId++,
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    radius = Random.nextFloat() * 6f + 3f,
                    lifeMs = 400f,
                    maxLifeMs = 400f
                )
            )
        }
        _particles.value = list
    }

    private fun spawnLootCoins(x: Float, y: Float) {
        val list = _particles.value.toMutableList()
        repeat(5) {
            list.add(
                Particle(
                    id = nextEntityId++,
                    x = x + Random.nextFloat() * 30f - 15f,
                    y = y + Random.nextFloat() * 30f - 15f,
                    vx = 0f,
                    vy = -120f,
                    color = Color(0xFFF59E0B),
                    radius = 8f,
                    lifeMs = 600f,
                    maxLifeMs = 600f
                )
            )
        }
        _particles.value = list
    }
}

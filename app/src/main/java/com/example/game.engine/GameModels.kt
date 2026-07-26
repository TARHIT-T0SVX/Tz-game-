package com.example.game.engine

import androidx.compose.ui.graphics.Color
import kotlin.math.atan2

data class Position(var x: Float, var y: Float) {
    fun distanceTo(other: Position): Float {
        val dx = x - other.x
        val dy = y - other.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    fun angleTo(other: Position): Float {
        return atan2(other.y - y, other.x - x)
    }
}

enum class CharacterType {
    BARBARIAN_KING,
    ARCHER_QUEEN,
    PEKKA,
    GRAND_WARDEN,
    GOBLIN,
    SKELETON,
    WIZARD,
    INFERNO_DRAGON_BOSS,
    GOLEM_BOSS
}

enum class SpellType {
    RAGE,
    HEALING,
    FREEZE,
    LIGHTNING
}

enum class ActionState {
    IDLE,
    WALKING,
    ATTACKING,
    SPECIAL,
    DEAD
}

data class SpellEffect(
    val id: Long,
    val type: SpellType,
    val pos: Position,
    val radius: Float = 140f,
    var durationRemainingMs: Float = 4000f,
    val maxDurationMs: Float = 4000f
)

data class GameCharacter(
    val id: String,
    val name: String,
    val type: CharacterType,
    val isHero: Boolean,
    val isBoss: Boolean = false,
    var pos: Position,
    var targetPos: Position? = null,
    val maxHp: Float,
    var hp: Float,
    var attackPower: Float,
    val attackRange: Float, // melee: 40-60, ranged: 250-400
    val attackSpeed: Float, // attacks per sec
    val baseMoveSpeed: Float,
    var currentMoveSpeed: Float = baseMoveSpeed,
    var actionState: ActionState = ActionState.IDLE,
    var facingAngleRad: Float = 0f,
    var attackCooldownMs: Float = 0f,
    var skillCooldownRemainingMs: Float = 0f,
    val skillMaxCooldownMs: Float = 10000f,
    var isRaged: Boolean = false,
    var isCloaked: Boolean = false,
    var isShielded: Boolean = false,
    var isFrozen: Boolean = false,
    var freezeDurationMs: Float = 0f,
    var attackAnimationTimerMs: Float = 0f
) {
    val hpPercent: Float
        get() = (hp / maxHp).coerceIn(0f, 1f)
}

data class Projectile(
    val id: Long,
    val startPos: Position,
    var currentPos: Position,
    val targetPos: Position,
    val targetCharacterId: String?,
    val speed: Float,
    val damage: Float,
    val isHeroProjectile: Boolean,
    val colorHex: Long,
    val radius: Float = 12f,
    val isSplash: Boolean = false,
    val splashRadius: Float = 0f
)

data class DamageNumber(
    val id: Long,
    var x: Float,
    var y: Float,
    val damageText: String,
    val color: Color,
    var lifeMs: Float = 1000f,
    val isCritical: Boolean = false
)

data class Particle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var radius: Float,
    var lifeMs: Float,
    val maxLifeMs: Float
)

enum class BattleStatus {
    NOT_STARTED,
    RUNNING,
    PAUSED,
    VICTORY,
    DEFEAT
}

data class StageConfig(
    val stageId: Int,
    val name: String,
    val waves: List<WaveConfig>,
    val stageBonusGold: Int,
    val stageBonusElixir: Int
)

data class WaveConfig(
    val waveNumber: Int,
    val goblins: Int = 0,
    val skeletons: Int = 0,
    val wizards: Int = 0,
    val bossType: CharacterType? = null
)

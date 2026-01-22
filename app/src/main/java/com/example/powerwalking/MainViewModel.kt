// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.powerwalking.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.random.Random

data class Hat(
    val imageResId: Int,
    val attackBonus: Float, // percentage
    val defenseBonus: Float, // percentage
    val healthBonus: Float // percentage
)

data class MainUiState(
    val totalCoins: Int = 1000,
    val currentSteps: Int = 0,
    val lastClaimedSteps: Int = 0,
    val baseAttack: Int = 100,
    val baseDefense: Int = 100,
    val baseHealth: Int = 500,
    val ownedHats: List<Hat> = emptyList(),
    val equippedHat: Hat? = null,
    val isFighting: Boolean = false,
    val opponent: User? = null,
    val opponentHat: Hat? = null
) {
    val totalAttack: Int
        get() = if (equippedHat != null) (baseAttack * (1 + equippedHat.attackBonus / 100)).toInt() else baseAttack
    val totalDefense: Int
        get() = if (equippedHat != null) (baseDefense * (1 + equippedHat.defenseBonus / 100)).toInt() else baseDefense
    val totalHealth: Int
        get() = if (equippedHat != null) (baseHealth * (1 + equippedHat.healthBonus / 100)).toInt() else baseHealth
}

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val claimableCoins: Int
        get() {
            val steps = _uiState.value.currentSteps
            val lastClaimed = _uiState.value.lastClaimedSteps
            // Calculate coins based on steps up to 8000, minus already claimed steps
            val claimableSteps = min(steps, 8000) - lastClaimed
            return if (claimableSteps > 0) claimableSteps / 100 else 0
        }
        
    fun updateCurrentSteps(steps: Int) {
        _uiState.update { it.copy(currentSteps = steps) }
    }

    fun claimCoins() {
        viewModelScope.launch {
            val coinsToClaim = claimableCoins
            if (coinsToClaim > 0) {
                _uiState.update { currentState ->
                    val stepsToClaimFrom = min(currentState.currentSteps, 8000)
                    currentState.copy(
                        totalCoins = currentState.totalCoins + coinsToClaim,
                        lastClaimedSteps = stepsToClaimFrom
                    )
                }
            }
        }
    }

    // 스탯 업그레이드 함수 (1코인 소모)
    fun upgradeStat(statType: String) {
        if (_uiState.value.totalCoins >= 1) {
            _uiState.update { currentState ->
                when (statType) {
                    "attack" -> currentState.copy(
                        totalCoins = currentState.totalCoins - 1,
                        baseAttack = currentState.baseAttack + 1
                    )
                    "defense" -> currentState.copy(
                        totalCoins = currentState.totalCoins - 1,
                        baseDefense = currentState.baseDefense + 1
                    )
                    "health" -> currentState.copy(
                        totalCoins = currentState.totalCoins - 1,
                        baseHealth = currentState.baseHealth + 10
                    )
                    else -> currentState
                }
            }
        }
    }

    fun drawHat(cost: Int): Hat? {
        if (_uiState.value.totalCoins >= cost) {
            val newHat = generateRandomHat()
            _uiState.update { currentState ->
                currentState.copy(
                    totalCoins = currentState.totalCoins - cost,
                    ownedHats = currentState.ownedHats + newHat
                )
            }
            return newHat
        }
        return null
    }

    fun equipHat(hat: Hat) {
        _uiState.update { it.copy(equippedHat = hat) }
    }

    fun unequipHat() {
        _uiState.update { it.copy(equippedHat = null) }
    }

    fun deleteHat(hat: Hat) {
        _uiState.update { currentState ->
            val newOwnedHats = currentState.ownedHats.toMutableList()
            newOwnedHats.remove(hat)
            
            val newEquippedHat = if (currentState.equippedHat == hat) null else currentState.equippedHat
            
            currentState.copy(
                ownedHats = newOwnedHats,
                equippedHat = newEquippedHat
            )
        }
    }

    fun startFight(opponent: User) {
        val opponentHat = generateRandomHat()
        _uiState.update { 
            it.copy(
                isFighting = true,
                opponent = opponent,
                opponentHat = opponentHat
            )
        }
    }
    
    fun endFight() {
        _uiState.update { 
            it.copy(
                isFighting = false,
                opponent = null,
                opponentHat = null
            )
        }
    }

    fun generateRandomHat(): Hat {
        val hatImages = listOf(
            R.drawable.cap1,
            R.drawable.cap2,
            R.drawable.cap3,
            R.drawable.cap4,
            R.drawable.cap5,
            R.drawable.cap6
        )
        val randomImage = hatImages.random()

        val attackBonus = generateStatBonus(isHealth = false)
        val defenseBonus = generateStatBonus(isHealth = false)
        val healthBonus = generateStatBonus(isHealth = true)

        return Hat(randomImage, attackBonus, defenseBonus, healthBonus)
    }

    private fun generateStatBonus(isHealth: Boolean): Float {
        val randomVal = Random.nextFloat() * 100
        val rawBonus = if (isHealth) {
            // 체력: 5%~10%
            // 5%~7% (80%), 7%~9% (15%), 9%~10% (5%)
            when {
                randomVal < 80 -> Random.nextDouble(5.0, 7.0)
                randomVal < 95 -> Random.nextDouble(7.0, 9.0)
                else -> Random.nextDouble(9.0, 10.0)
            }
        } else {
            // 공격력, 방어력: 1%~4%
            // 1%~2% (80%), 2%~3% (15%), 3%~4% (5%)
            when {
                randomVal < 80 -> Random.nextDouble(1.0, 2.0)
                randomVal < 95 -> Random.nextDouble(2.0, 3.0)
                else -> Random.nextDouble(3.0, 4.0)
            }
        }
        
        // 소수점 둘째 자리에서 반올림하여 첫째 자리까지 유지
        // 예: 1.234 -> 1.2
        return (Math.round(rawBonus * 10.0) / 10.0).toFloat()
    }
}

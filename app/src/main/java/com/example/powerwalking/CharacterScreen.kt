// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CharacterScreen(modifier: Modifier = Modifier, mainViewModel: MainViewModel = viewModel()) {
    var selectedStat by remember { mutableStateOf<String?>("공격력") }
    var selectedScreen by remember { mutableStateOf("모자") }
    var selectedHat by remember { mutableStateOf<Hat?>(null) }
    var showHatDetailDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val uiState by mainViewModel.uiState.collectAsState()

    // 삭제 확인 다이얼로그
    if (showDeleteConfirmDialog && selectedHat != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "삭제 확인", fontFamily = memomentkkukkukk) },
            text = { Text(text = "해당 모자를 삭제하시겠습니까?", fontFamily = memomentkkukkukk) },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.deleteHat(selectedHat!!)
                        showDeleteConfirmDialog = false
                        showHatDetailDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("확인", fontFamily = memomentkkukkukk)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmDialog = false }) {
                    Text("취소", fontFamily = memomentkkukkukk)
                }
            }
        )
    }


    if (showHatDetailDialog && selectedHat != null) {
        val hat = selectedHat!!
        val isEquipped = uiState.equippedHat == hat


        val attackBonusStr = if (hat.attackBonus > 0) "+${String.format("%.1f", hat.attackBonus)}%" else ""
        val defenseBonusStr = if (hat.defenseBonus > 0) "+${String.format("%.1f", hat.defenseBonus)}%" else ""
        val healthBonusStr = if (hat.healthBonus > 0) "+${String.format("%.1f", hat.healthBonus)}%" else ""


        val currentAttack = uiState.totalAttack
        val currentDefense = uiState.totalDefense
        val currentHealth = uiState.totalHealth
        

        val newAttack = (uiState.baseAttack * (1 + hat.attackBonus / 100)).toInt()
        val newDefense = (uiState.baseDefense * (1 + hat.defenseBonus / 100)).toInt()
        val newHealth = (uiState.baseHealth * (1 + hat.healthBonus / 100)).toInt()
        
        val attackDiff = newAttack - currentAttack
        val defenseDiff = newDefense - currentDefense
        val healthDiff = newHealth - currentHealth
        
        val attackDiffStr = if (attackDiff >= 0) "+$attackDiff" else "$attackDiff"
        val defenseDiffStr = if (defenseDiff >= 0) "+$defenseDiff" else "$defenseDiff"
        val healthDiffStr = if (healthDiff >= 0) "+$healthDiff" else "$healthDiff"


        AlertDialog(
            onDismissRequest = { showHatDetailDialog = false },
            title = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "모자 상세 정보",
                        fontFamily = memomentkkukkukk,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Hat",
                            tint = Color.Red
                        )
                    }
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = hat.imageResId),
                        contentDescription = "Selected Hat",
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (attackBonusStr.isNotEmpty()) Text("공격력: $attackBonusStr", fontFamily = memomentkkukkukk)
                    if (defenseBonusStr.isNotEmpty()) Text("방어력: $defenseBonusStr", fontFamily = memomentkkukkukk)
                    if (healthBonusStr.isNotEmpty()) Text("체력: $healthBonusStr", fontFamily = memomentkkukkukk)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("장착 시 내 스탯 변화", fontFamily = memomentkkukkukk, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (hat.attackBonus > 0 || attackDiff != 0) Text("공격력: $currentAttack → $newAttack ($attackDiffStr)",
                        fontFamily = memomentkkukkukk)
                    if (hat.defenseBonus > 0 || defenseDiff != 0) Text("방어력: $currentDefense → $newDefense ($defenseDiffStr)",
                        fontFamily = memomentkkukkukk)
                    if (hat.healthBonus > 0 || healthDiff != 0) Text("체력: $currentHealth → $newHealth ($healthDiffStr)",
                        fontFamily = memomentkkukkukk)
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 장착 버튼
                    Button(
                        onClick = {
                            mainViewModel.equipHat(hat)
                            showHatDetailDialog = false
                        },
                        enabled = !isEquipped,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isEquipped) Color(0xFFFFD700) else Color.Gray,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("장착", fontFamily = memomentkkukkukk)
                    }

                    // 해제 버튼
                    Button(
                        onClick = {
                            mainViewModel.unequipHat()
                            showHatDetailDialog = false
                        },
                        enabled = isEquipped,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEquipped) Color(0xFFFF6347) else Color.Gray, // Tomato color for unequip
                            contentColor = Color.White
                        )
                    ) {
                        Text("해제", fontFamily = memomentkkukkukk)
                    }
                }
            }
        )
    }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.background_char),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // 상단 공통 영역: 성장화면의 공방체 또는 모자화면의 캐릭터
            when (selectedScreen) {
                "성장" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top // 상단 정렬
                    ) {
                        val statItemModifier = Modifier
                            .width(100.dp)
                            .height(140.dp)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = statItemModifier.clickable { selectedStat = "공격력" }
                        ) {
                            Box(
                                modifier = Modifier.size(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.attack_stat),
                                    contentDescription = "공격력",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = if (selectedStat != "공격력") ColorFilter.colorMatrix(ColorMatrix().apply
                                    { setToSaturation(0f) }) else null
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .offset(y = (-8).dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("공격력")
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(24.dp))
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = statItemModifier.clickable { selectedStat = "방어력" }
                        ) {
                            Box(
                                modifier = Modifier.size(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.defense_stat),
                                    contentDescription = "방어력",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = if (selectedStat != "방어력") ColorFilter.colorMatrix(ColorMatrix().apply
                                    { setToSaturation(0f) }) else null
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .offset(y = (-8).dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("방어력")
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(24.dp))
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = statItemModifier.clickable { selectedStat = "체력" }
                        ) {
                            Box(
                                modifier = Modifier.size(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.health_stat),
                                    contentDescription = "체력",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = if (selectedStat != "체력") ColorFilter.colorMatrix(ColorMatrix().apply
                                    { setToSaturation(0f) }) else null
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .offset(y = (-8).dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("체력")
                            }
                        }
                    }
                }
                "모자" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(152.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cat_hat),
                                contentDescription = "캐릭터",
                                modifier = Modifier.size(120.dp)
                            )
                            if (uiState.equippedHat != null) {
                                Image(
                                    painter = painterResource(id = uiState.equippedHat!!.imageResId),
                                    contentDescription = "선택된 모자",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .offset(y = (-50).dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.White.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            if (uiState.equippedHat != null) {
                                Text(
                                    text = "추가 능력치: " +
                                            (if (uiState.equippedHat!!.attackBonus > 0)
                                                "공+${String.format("%.1f", uiState.equippedHat!!.attackBonus)}% " else "") +
                                            (if (uiState.equippedHat!!.defenseBonus > 0)
                                                "방+${String.format("%.1f", uiState.equippedHat!!.defenseBonus)}% " else "") +
                                            (if (uiState.equippedHat!!.healthBonus > 0)
                                                "체+${String.format("%.1f", uiState.equippedHat!!.healthBonus)}% " else ""),
                                    fontFamily = memomentkkukkukk,
                                    fontSize = 14.sp
                                )
                            } else {
                                Text(
                                    text = "모자를 장착해주세요",
                                    fontFamily = memomentkkukkukk,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            when (selectedScreen) {
                "성장" -> {
                    if (selectedStat != null) {
                        val hat = uiState.equippedHat
                        val (currentStat, afterStat) = when(selectedStat) {
                            "공격력" -> {
                                val curr = uiState.totalAttack
                                val nextBase = uiState.baseAttack + 1
                                val next = if (hat != null) (nextBase * (1 + hat.attackBonus / 100)).toInt() else nextBase
                                curr to next
                            }
                            "방어력" -> {
                                val curr = uiState.totalDefense
                                val nextBase = uiState.baseDefense + 1
                                val next = if (hat != null) (nextBase * (1 + hat.defenseBonus / 100)).toInt() else nextBase
                                curr to next
                            }
                            "체력" -> {
                                val curr = uiState.totalHealth
                                val nextBase = uiState.baseHealth + 10
                                val next = if (hat != null) (nextBase * (1 + hat.healthBonus / 100)).toInt() else nextBase
                                curr to next
                            }
                            else -> 0 to 0
                        }
                        
                        GrowthSection(
                            statName = selectedStat!!, 
                            currentStat = currentStat,
                            afterStat = afterStat,
                            onUpgrade = { 
                                val type = when(selectedStat) {
                                    "공격력" -> "attack"
                                    "방어력" -> "defense"
                                    "체력" -> "health"
                                    else -> ""
                                }
                                if (type.isNotEmpty()) {
                                    mainViewModel.upgradeStat(type)
                                }
                            },
                            canUpgrade = uiState.totalCoins >= 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                "모자" -> {
                    HatSection(
                        ownedHats = uiState.ownedHats,
                        equippedHat = uiState.equippedHat,
                        onHatSelected = { hat -> 
                            selectedHat = hat 
                            showHatDetailDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 모자와 성장 박스
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(45.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFC0CB))
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .clickable { selectedScreen = "모자" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("모자")
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(45.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFC0CB))
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .clickable { selectedScreen = "성장" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("성장")
                }
            }
        }
    }
}

@Composable
fun GrowthSection(
    statName: String, 
    currentStat: Int,
    afterStat: Int,
    onUpgrade: () -> Unit,
    canUpgrade: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize()
            .padding(end = 10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.stat_frame),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-20).dp),
            contentScale = ContentScale.FillBounds
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "$statName 성장",
                fontFamily = memomentkkukkukk,
                fontSize = 28.sp, 
                color = Color.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("전", fontSize = 18.sp, fontFamily = memomentkkukkukk)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("$currentStat", fontSize = 24.sp, fontFamily = memomentkkukkukk)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                Text("→", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("후", fontSize = 18.sp, fontFamily = memomentkkukkukk)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("$afterStat", fontSize = 24.sp, fontFamily = memomentkkukkukk)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 강화 버튼
            Button(
                onClick = onUpgrade,
                enabled = canUpgrade,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    disabledContainerColor = Color.Gray,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(150.dp)
                    .height(45.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("강화", fontFamily = memomentkkukkukk, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.coin),
                        contentDescription = "Coin",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("1", fontFamily = memomentkkukkukk, fontSize = 20.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HatSection(
    ownedHats: List<Hat>,
    equippedHat: Hat?,
    onHatSelected: (Hat?) -> Unit,
    modifier: Modifier = Modifier
) {
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize()
            .padding(end = 10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.cap_frame),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-20).dp),
            contentScale = ContentScale.FillBounds
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 50.dp, bottom = 80.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (ownedHats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("보유한 모자가 없습니다.", fontFamily = memomentkkukkukk, color = Color.Gray)
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 3
                ) {
                    ownedHats.forEach { hat ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { onHatSelected(hat) },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = hat.imageResId),
                                contentDescription = "모자",
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(
                                        width = if (equippedHat == hat) 2.dp else 0.dp,
                                        color = if (equippedHat == hat) Color.Red else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ShopScreen(modifier: Modifier = Modifier, mainViewModel: MainViewModel = viewModel()) {
    val uiState by mainViewModel.uiState.collectAsState()
    var showDrawResult by remember { mutableStateOf(false) }
    var drawnHats by remember { mutableStateOf<List<Hat>>(emptyList()) }
    var showProbabilityInfo by remember { mutableStateOf(false) }

    if (showProbabilityInfo) {
        AlertDialog(
            onDismissRequest = { showProbabilityInfo = false },
            title = { Text(text = "확률 정보", fontFamily = memomentkkukkukk) },
            text = {
                Column {
                    Text(text = "공격력, 방어력 추가 스탯", fontFamily = memomentkkukkukk, fontSize = 18.sp)
                    Text(text = "1.0% ~ 2.0% : 80%", fontFamily = memomentkkukkukk)
                    Text(text = "2.0% ~ 3.0% : 15%", fontFamily = memomentkkukkukk)
                    Text(text = "3.0% ~ 4.0% : 5%", fontFamily = memomentkkukkukk)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = "체력 추가 스탯", fontFamily = memomentkkukkukk, fontSize = 18.sp)
                    Text(text = "5.0% ~ 7.0% : 80%", fontFamily = memomentkkukkukk)
                    Text(text = "7.0% ~ 9.0% : 15%", fontFamily = memomentkkukkukk)
                    Text(text = "9.0% ~ 10.0% : 5%", fontFamily = memomentkkukkukk)
                }
            },
            confirmButton = {
                Button(onClick = { showProbabilityInfo = false }) {
                    Text("확인", fontFamily = memomentkkukkukk)
                }
            }
        )
    }

    if (showDrawResult && drawnHats.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDrawResult = false },
            title = { Text(text = if (drawnHats.size == 1) "모자 획득!" else "모자 ${drawnHats.size}개 획득!", fontFamily = memomentkkukkukk) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    drawnHats.forEach { hat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = hat.imageResId),
                                contentDescription = null,
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("공격력 +${String.format("%.1f", hat.attackBonus)}%", fontFamily = memomentkkukkukk, fontSize = 16.sp)
                                Text("방어력 +${String.format("%.1f", hat.defenseBonus)}%", fontFamily = memomentkkukkukk, fontSize = 16.sp)
                                Text("체력 +${String.format("%.1f", hat.healthBonus)}%", fontFamily = memomentkkukkukk, fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDrawResult = false }) {
                    Text("확인", fontFamily = memomentkkukkukk)
                }
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
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
                .padding(bottom = 120.dp), // 하단바 공간 확보
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            
            // 모자 뽑기 섹션
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .border(3.dp, Color.Black, RoundedCornerShape(0.dp))
                    .background(Color.White, RoundedCornerShape(0.dp))
                    .padding(vertical = 20.dp, horizontal = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "모자 뽑기",
                            fontFamily = memomentkkukkukk,
                            fontSize = 32.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { showProbabilityInfo = true },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("확률 정보", fontFamily = memomentkkukkukk, fontSize = 12.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // 선물 상자 이미지 (대체 이미지 사용)
                    Image(
                        painter = painterResource(id = R.drawable.shopbox), // 적절한 선물 상자 이미지가 없어 모자 이미지로 대체
                        contentDescription = "Gift Box",
                        modifier = Modifier.size(150.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DrawButton(count = 1, price = 100) {
                            if (uiState.totalCoins >= 100) {
                                val hat = mainViewModel.drawHat(100)
                                if (hat != null) {
                                    drawnHats = listOf(hat)
                                    showDrawResult = true
                                }
                            }
                        }
                        DrawButton(count = 5, price = 500) {
                            if (uiState.totalCoins >= 500) {
                                val newHats = mutableListOf<Hat>()
                                repeat(5) {
                                    val hat = mainViewModel.drawHat(100)
                                    if (hat != null) newHats.add(hat)
                                }
                                if (newHats.isNotEmpty()) {
                                    drawnHats = newHats
                                    showDrawResult = true
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 하단 섹션: 스탯 초기화 & 코인 구매
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 스탯 초기화
                ShopCard(
                    title = "스탯 초기화",
                    price = "KRW 1,000",
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(60.dp),
                        tint = Color.Black
                    )
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                // 코인 구매
                ShopCard(
                    title = "코인 구매",
                    price = "KRW 2,000",
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                             Image(
                                painter = painterResource(id = R.drawable.coin),
                                contentDescription = "Coin",
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        Text(
                            text = "2000",
                            fontFamily = memomentkkukkukk,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawButton(count: Int, price: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(3.dp, Color.Black, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "${count}회", fontFamily = memomentkkukkukk, fontSize = 24.sp, color = Color.Black)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.coin),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(text = " $price", fontFamily = memomentkkukkukk, fontSize = 20.sp, color = Color.Black)
            }
        }
    }
}

@Composable
fun ShopCard(title: String, price: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
     Box(
        modifier = modifier
            .height(220.dp)
            .border(3.dp, Color.Black, RoundedCornerShape(0.dp))
            .background(Color.White, RoundedCornerShape(0.dp))
            .padding(10.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontFamily = memomentkkukkukk, fontSize = 24.sp, color = Color.Black)
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                content()
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, Color.Black, RoundedCornerShape(10.dp))
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = price, fontFamily = memomentkkukkukk, fontSize = 24.sp, color = Color.Black)
            }
        }
    }
}

// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.powerwalking.data.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FightingScreen(
    onFinished: (Boolean, Int) -> Unit, // 승리 여부, 획득/차감 점수
    myUser: User,
    opponentUser: User,
    equippedHat: Hat? = null,
    opponentHat: Hat? = null
) {
    // 전투 진행 상태
    var myCurrentHealth by remember { mutableStateOf(myUser.health.toFloat()) }
    var opponentCurrentHealth by remember { mutableStateOf(opponentUser.health.toFloat()) }
    var battleFinished by remember { mutableStateOf(false) }
    var isVictory by remember { mutableStateOf(false) }
    var resultPoints by remember { mutableStateOf(0) }
    
    // 데미지 표시
    var myDamageText by remember { mutableStateOf<String?>(null) }
    var opponentDamageText by remember { mutableStateOf<String?>(null) }
    
    // 캐릭터 흔들림 효과
    val myShakeOffset = remember { Animatable(0f) }
    val opponentShakeOffset = remember { Animatable(0f) }
    
    // 최대 체력 (퍼센트 계산용)
    val myMaxHealth = myUser.health.toFloat()
    val opponentMaxHealth = opponentUser.health.toFloat()

    // 전투 로직: 1초마다 데미지 계산
    LaunchedEffect(Unit) {
        while (myCurrentHealth > 0 && opponentCurrentHealth > 0) {
            delay(1000)
            
            // 데미지 계산 공식 (MainViewModel 참조: 공격력 * (100 / (100 + 방어력)))
            // 내 공격 -> 상대
            val damageToOpponent = (myUser.attack * (100.0 / (100.0 + opponentUser.defense))).toInt().coerceAtLeast(1)
            // 상대 공격 -> 나
            val damageToMe = (opponentUser.attack * (100.0 / (100.0 + myUser.defense))).toInt().coerceAtLeast(1)
            
            // 데미지 텍스트 표시 및 흔들림 효과
            myDamageText = "-$damageToMe"
            opponentDamageText = "-$damageToOpponent"
            
            // 체력 감소
            opponentCurrentHealth = (opponentCurrentHealth - damageToOpponent).coerceAtLeast(0f)
            myCurrentHealth = (myCurrentHealth - damageToMe).coerceAtLeast(0f)
            
            // 진동 효과 실행
            launch {
                myShakeOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 300
                        -10f at 50
                        10f at 100
                        -10f at 150
                        10f at 200
                        0f at 300
                    }
                )
            }
            launch {
                opponentShakeOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 300
                        -10f at 50
                        10f at 100
                        -10f at 150
                        10f at 200
                        0f at 300
                    }
                )
            }
            
            // 0.5초 후 텍스트 숨김 (다음 턴 전에 사라지게)
            delay(500)
            myDamageText = null
            opponentDamageText = null
        }
        
        // 전투 종료
        battleFinished = true
        
        if (myCurrentHealth <= 0 && opponentCurrentHealth <= 0) {
            // 무승부 (둘 다 0 이하가 된 경우, 로직상 무승부는 패배 처리 혹은 별도 처리 가능. 여기선 내가 졌다고 가정하거나 남은 체력 비율로 판정 등)
            // 간단하게 내가 먼저 죽었으면 패배, 아니면 승리로 판정. 동시 사망은 패배로 처리
            isVictory = false
            resultPoints = -10 // 패배 시 점수 차감 (예시)
        } else if (myCurrentHealth <= 0) {
            // 패배
            isVictory = false
            resultPoints = -10
        } else {
            // 승리
            isVictory = true
            // 점수 계산: 기본 30 + (상대 점수 - 내 점수)
            val scoreDiff = opponentUser.score - myUser.score
            resultPoints = 30 + scoreDiff
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // 캐릭터 대치 영역
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 내 캐릭터 (왼쪽)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 체력바
                HealthBar(
                    currentHealth = myCurrentHealth,
                    maxHealth = myMaxHealth,
                    name = myUser.name,
                    color = Color.Green
                )
                
                // 체력바와 캐릭터 사이 간격 증가 (위로 올림 효과)
                Spacer(modifier = Modifier.height(50.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.offset(x = myShakeOffset.value.dp)
                ) {
                    // 데미지 텍스트 (캐릭터 머리 위)
                    if (myDamageText != null) {
                        Text(
                            text = myDamageText!!,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-40).dp), // 캐릭터 머리 위로 올림
                             style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.White,
                                    blurRadius = 2f
                                )
                            )
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.arena),
                        contentDescription = "My Character",
                        modifier = Modifier
                            .size(200.dp)
                            .scale(scaleX = -1f, scaleY = 1f) // 좌우반전
                    )
                    
                    if (equippedHat != null) {
                        Image(
                            painter = painterResource(id = equippedHat.imageResId),
                            contentDescription = "Equipped Hat",
                            modifier = Modifier
                                .size(80.dp) // 모자 크기 축소 (100 -> 80)
                                .align(Alignment.TopCenter)
                                .offset(y = 15.dp) // 위치 조정 (더 아래로 내림: 0.dp -> 15.dp)
                                .scale(scaleX = -1f, scaleY = 1f)
                        )
                    }
                }
                
                // 하단 스탯 텍스트 제거됨
            }

            // VS 텍스트
            Text(
                text = "VS",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                fontFamily = memomentkkukkukk,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            // 상대방 캐릭터 (오른쪽)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 체력바
                HealthBar(
                    currentHealth = opponentCurrentHealth,
                    maxHealth = opponentMaxHealth,
                    name = opponentUser.name,
                    color = Color.Red
                )
                
                // 체력바와 캐릭터 사이 간격 증가
                Spacer(modifier = Modifier.height(50.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.offset(x = opponentShakeOffset.value.dp)
                ) {
                    // 데미지 텍스트 (캐릭터 머리 위)
                    if (opponentDamageText != null) {
                        Text(
                            text = opponentDamageText!!,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-40).dp), // 캐릭터 머리 위로 올림
                             style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.White,
                                    blurRadius = 2f
                                )
                            )
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.arena),
                        contentDescription = "Opponent Character",
                        modifier = Modifier.size(200.dp)
                    )
                    
                    if (opponentHat != null) {
                        Image(
                            painter = painterResource(id = opponentHat.imageResId),
                            contentDescription = "Opponent Hat",
                            modifier = Modifier
                                .size(80.dp) // 모자 크기 축소 (100 -> 80)
                                .align(Alignment.TopCenter)
                                .offset(y = 15.dp) // 위치 조정 (더 아래로 내림: 0.dp -> 15.dp)
                        )
                    }
                }
                
                // 하단 스탯 텍스트 제거됨
            }
        }
        
        // 결과 팝업
        if (battleFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .background(if (isVictory) Color(0xFF90EE90) else Color(0xFFFFB6C1))
                        .border(3.dp, Color.Black)
                        .padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 결과 텍스트
                        Text(
                            text = if (isVictory) "승리!" else "패배",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isVictory) Color(0xFF006400) else Color(0xFF8B0000),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        // 결과 메시지
                        if (isVictory) {
                            Text(
                                text = "축하합니다!\n전투에서 승리했습니다!",
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            // 획득한 점수 표시
                            Text(
                                text = if (resultPoints > 0) "+${resultPoints}점 획득!" else "${resultPoints}점 획득!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006400),
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
                        } else {
                            Text(
                                text = "아쉽네요.\n다음에는 승리할 수 있을 거예요!",
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            // 잃은 점수 표시
                            Text(
                                text = "${resultPoints}점",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B0000),
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
                        }
                        
                        // 확인 버튼
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(50.dp)
                                .background(Color(0xFFFFF0F5))
                                .border(2.dp, Color.Black)
                                .clickable { 
                                    onFinished(isVictory, resultPoints)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "확인",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HealthBar(
    currentHealth: Float,
    maxHealth: Float,
    name: String,
    color: Color
) {
    val progress = (currentHealth / maxHealth).coerceIn(0f, 1f)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp),
             style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black,
                    blurRadius = 3f
                )
            )
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(20.dp)
                .background(Color.Gray, RoundedCornerShape(10.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(20.dp)
                    .background(color, RoundedCornerShape(10.dp))
            )
            
            Text(
                text = "${currentHealth.toInt()}/${maxHealth.toInt()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

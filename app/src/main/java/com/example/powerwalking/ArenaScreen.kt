// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.powerwalking.data.AppDatabase
import com.example.powerwalking.data.User
import com.example.powerwalking.R
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun ArenaScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val userDao = remember { database.userDao() }
    val uiState by mainViewModel.uiState.collectAsState()
    
    var remainingTime by remember { mutableStateOf<Long>(calculateTimeUntilNextMonday()) }
    var showPopup by remember { mutableStateOf(false) }
    var showRewardPopup by remember { mutableStateOf(false) }
    
    // FightingScreen에서 결과 확인 후 돌아왔을 때 ArenaScreen에서 팝업을 띄우지 않도록
    // showBattleResult 제거 또는 사용하지 않음.
    // var showBattleResult by remember { mutableStateOf(false) }
    
    var isVictory by remember { mutableStateOf(false) }
    var myScore by remember { mutableStateOf(0) }
    var myUserId by remember { mutableStateOf<Long?>(null) }
    var earnedPoints by remember { mutableStateOf(0) }
    var challengeCount by remember { mutableStateOf(5) } // 도전 횟수
    var showStatsPopup by remember { mutableStateOf(false) }
    var selectedUserStats by remember { mutableStateOf<Triple<String, Int, Triple<Int, Int, Int>>?>(null) } // 이름, 점수, (공격력, 방어력, 체력)
    val coroutineScope = rememberCoroutineScope()
    
    // 비슷한 점수 유저 데이터 (점수 높은 순으로 정렬)
    var similarUsers by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    // 랭킹 데이터 (점수 높은 순으로 정렬)
    var rankingUsers by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    // 도전리스트 갱신 함수
    fun updateChallengeList() {
        coroutineScope.launch(Dispatchers.IO) {
            val minScore = maxOf(0, myScore - 10)
            val maxScore = myScore + 10
            val users = userDao.getSimilarUsers("파워킹유저", minScore, maxScore, 10)
            val newList = users.map { it.name to it.score }.sortedByDescending { it.second }
            withContext(Dispatchers.Main) {
                similarUsers = newList
            }
        }
    }
    
    // 랭킹 갱신 함수
    fun updateRanking() {
        coroutineScope.launch(Dispatchers.IO) {
            val allUsersFlow = userDao.getAllUsersOrderedByScore()
            val allUsers = allUsersFlow.first()
            val newRanking = allUsers.map { it.name to it.score }.sortedByDescending { it.second }
            withContext(Dispatchers.Main) {
                rankingUsers = newRanking
            }
        }
    }
    
    // 전투 화면 표시 로직
    if (uiState.isFighting && uiState.opponent != null) {
        // 내 유저 정보 구성 (DB 점수 + ViewModel 스탯)
        // 현재 myUserId가 로드된 상태여야 함.
        // 스탯은 ViewModel의 현재 스탯(장착 모자 포함)을 사용.
        val myUserForBattle = User(
            id = myUserId ?: 0,
            name = "파워킹유저",
            score = myScore,
            attack = uiState.totalAttack,
            defense = uiState.totalDefense,
            health = uiState.totalHealth
        )
        
        // 상대방 모자 보너스 적용:
        val opponentHat = uiState.opponentHat
        val opponentBase = uiState.opponent!!
        val opponentWithHat = if (opponentHat != null) {
            opponentBase.copy(
                attack = (opponentBase.attack * (1 + opponentHat.attackBonus / 100)).toInt(),
                defense = (opponentBase.defense * (1 + opponentHat.defenseBonus / 100)).toInt(),
                health = (opponentBase.health * (1 + opponentHat.healthBonus / 100)).toInt()
            )
        } else {
            opponentBase
        }
        
        FightingScreen(
            onFinished = { victory, points ->
                isVictory = victory
                earnedPoints = points
                
                // 점수 업데이트 로직
                coroutineScope.launch {
                    val newScore = myScore + points
                    val opponentNewScore = maxOf(0, opponentBase.score - points) // 상대 점수도 변동
                    
                    withContext(Dispatchers.IO) {
                        // 내 점수 업데이트
                        myUserId?.let { userId ->
                            userDao.updateUserScore(userId, newScore)
                        }
                        
                        // 상대 유저 점수 업데이트
                        userDao.updateUserScore(opponentBase.id, opponentNewScore)
                    }
                    
                    withContext(Dispatchers.Main) {
                        // UI 업데이트
                        myScore = newScore
                        challengeCount--
                        
                        // 전투 종료 처리 (FightingScreen 내려감)
                        mainViewModel.endFight()
                        
                        // ArenaScreen으로 돌아온 직후 데이터 갱신
                        updateChallengeList()
                        updateRanking()
                    }
                }
            },
            myUser = myUserForBattle,
            opponentUser = opponentWithHat,
            equippedHat = uiState.equippedHat,
            opponentHat = uiState.opponentHat
        )
        return
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val myUser = userDao.getUserByName("파워킹유저")
            var currentMyScore = 0
            if (myUser == null) {
                // 내 유저가 없으면 생성
                val userId = userDao.insertUser(User(name = "파워킹유저", score = 0))
                withContext(Dispatchers.Main) {
                    myUserId = userId
                    myScore = 0
                }
                currentMyScore = 0
            } else {
                currentMyScore = myUser.score
                
                withContext(Dispatchers.Main) {
                    myUserId = myUser.id
                    myScore = currentMyScore
                }
            }

            userDao.deleteAllSampleUsers()

            val sampleUsers = listOf(
                Triple("달리는고양이", Triple(120, 80, 550), 200),
                Triple("파워워커", Triple(100, 90, 500), 193),
                Triple("스피드킹", Triple(110, 85, 520), 186),
                Triple("걷기마스터", Triple(95, 95, 480), 179),
                Triple("워킹히어로", Triple(105, 88, 510), 172),
                Triple("빠른발걸음", Triple(90, 100, 460), 165),
                Triple("걷기왕", Triple(115, 82, 540), 158),
                Triple("스텝마스터", Triple(85, 105, 440), 151),
                Triple("워킹스타", Triple(125, 75, 560), 144),
                Triple("걷기전사", Triple(80, 110, 420), 137),
                Triple("파워스텝", Triple(130, 70, 580), 130),
                Triple("스피드워커", Triple(75, 115, 400), 123),
                Triple("걷기챔피언", Triple(135, 65, 600), 116),
                Triple("워킹레전드", Triple(70, 120, 450), 109),
                Triple("빠른걸음", Triple(140, 60, 590), 102),
                Triple("스텝킹", Triple(65, 125, 430), 96),
                Triple("걷기신", Triple(145, 55, 570), 89),
                Triple("워킹고수", Triple(60, 130, 410), 82),
                Triple("파워워킹", Triple(150, 50, 580), 75),
                Triple("스피드스텝", Triple(55, 135, 400), 68),
                Triple("걷기달인", Triple(98, 102, 490), 61),
                Triple("워킹프로", Triple(50, 140, 420), 54),
                Triple("빠른발", Triple(103, 97, 470), 47),
                Triple("스텝히어로", Triple(45, 145, 410), 41),
                Triple("걷기천재", Triple(108, 92, 480), 34),
                Triple("워킹마법사", Triple(40, 150, 400), 27),
                Triple("파워걷기", Triple(113, 87, 490), 20),
                Triple("스피드걸음", Triple(88, 107, 450), 13),
                Triple("걷기선수", Triple(118, 77, 530), 6),
                Triple("워킹아이돌", Triple(83, 112, 440), 0)
            )

            sampleUsers.forEach { (name, stats, score) ->
                val (attack, defense, health) = stats
                userDao.insertUser(User(name = name, score = score, attack = attack, defense = defense, health = health))
            }

            val allUsersFlow = userDao.getAllUsersOrderedByScore()
            val allUsers = allUsersFlow.first()
            val newRanking = allUsers.map { it.name to it.score }.sortedByDescending { it.second }
            
            val minScore = maxOf(0, currentMyScore - 10)
            val maxScore = currentMyScore + 10
            val similarUsersList = userDao.getSimilarUsers("파워킹유저", minScore, maxScore, 10)
            val newSimilarUsers = similarUsersList.map { it.name to it.score }.sortedByDescending { it.second }
            
            withContext(Dispatchers.Main) {
                rankingUsers = newRanking
                similarUsers = newSimilarUsers
            }
        }
    }

    LaunchedEffect(myScore) {
        updateChallengeList()
        updateRanking()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            remainingTime = calculateTimeUntilNextMonday()
        }
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
                .padding(16.dp)
                .padding(bottom = 120.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF0F5))
                        .border(2.dp, Color.Black)
                        .padding(vertical = 16.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "시즌 종료 시간: 월요일 00시",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = memomentkkukkukk
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "남은 시간 : ",
                            fontSize = 16.sp,
                            fontFamily = memomentkkukkukk
                        )
                        Text(
                            text = formatRemainingTime(remainingTime),
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = memomentkkukkukk
                        )
                    }
                }

                // 트로피 아이콘 (오른쪽 아래 모서리)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(60.dp)
                        .padding(8.dp)
                        .clickable { showRewardPopup = true },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.trophy),
                        contentDescription = "랭킹 보상",
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 랭킹 박스와 내 정보 박스를 감싸는 Box (팝업 위치 기준)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // 팝업이 표시되지 않을 때만 랭킹 박스와 내 정보 박스 표시
                if (!showPopup) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 중간: 랭킹 박스 (크게)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color(0xFFFFF0F5))
                                .border(2.dp, Color.Black)
                                .padding(horizontal = 32.dp, vertical = 24.dp)
                        ) {
                            // "랭킹" 텍스트 (고정)
                            Text(
                                text = "랭킹",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = memomentkkukkukk,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            // 등수/닉네임/점수 리스트 (스크롤 가능)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Spacer(modifier = Modifier.height(20.dp))
                                rankingUsers.take(30).forEachIndexed { index, (userName, score) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.cat_foot),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable(enabled = !showStatsPopup) {
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        val user = userDao.getUserByName(userName)
                                                        user?.let {
                                                            withContext(Dispatchers.Main) {
                                                                selectedUserStats = Triple(userName, score, Triple(it.attack, it.defense, it.health))
                                                                showStatsPopup = true
                                                            }
                                                        }
                                                    }
                                                }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${index + 1}등",
                                            fontSize = 20.sp,
                                            modifier = Modifier.width(50.dp)
                                        )
                                        Spacer(modifier = Modifier.width(20.dp))
                                        Text(
                                            text = userName,
                                            fontSize = 20.sp,
                                            fontFamily = memomentkkukkukk,
                                            modifier = Modifier
                                                .clickable(enabled = !showStatsPopup) {
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        val user = userDao.getUserByName(userName)
                                                        user?.let {
                                                            withContext(Dispatchers.Main) {
                                                                selectedUserStats = Triple(userName, score, Triple(it.attack, it.defense, it.health))
                                                                showStatsPopup = true
                                                            }
                                                        }
                                                    }
                                                }
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = "${score}점",
                                            fontSize = 20.sp,
                                            fontFamily = memomentkkukkukk
                                        )
                                    }
                                    if (index < rankingUsers.take(30).size - 1) {
                                        Spacer(modifier = Modifier.height(20.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 밑: 내 정보 박스
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color(0xFFFFF0F5))
                                .border(2.dp, Color.Black)
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 프로필 이미지
                            Image(
                                painter = painterResource(id = R.drawable.cat_foot),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(50.dp)
                            )

                            Spacer(modifier = Modifier.width(20.dp))

                            // 현재 등수
                            val myRank = rankingUsers.indexOfFirst { it.first == "파워킹유저" } + 1
                            val displayRank = if (myRank > 0) myRank else rankingUsers.size + 1
                            Text(
                                text = "${displayRank}등",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = memomentkkukkukk
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // 닉네임
                            Text(
                                text = "파워킹유저",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = memomentkkukkukk
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // 점수
                            Text(
                                text = "${myScore}점",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = memomentkkukkukk
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 내 정보 박스 아래: 도전 버튼
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(50.dp)
                                    .background(if (!showStatsPopup) Color(0xFFFFF0F5) else Color.Gray)
                                    .border(2.dp, Color.Black)
                                    .padding(horizontal = 24.dp)
                                    .clickable(enabled = !showStatsPopup) { showPopup = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "도전",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = memomentkkukkukk
                                )
                            }
                        }
                    }
                }

                // 팝업: 랭킹 박스와 내 정보 박스 위에 오버레이
                if (showPopup) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .border(3.dp, Color.Black)
                            .padding(24.dp)
                    ) {
                        // X 버튼 (오른쪽 위)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(40.dp)
                                .background(Color.Red)
                                .border(2.dp, Color.Black)
                                .clickable { showPopup = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "X",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // 유저 리스트
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp)
                        ) {
                            // 제목: 도전리스트
                            Text(
                                text = "도전리스트",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = memomentkkukkukk,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )

                            // 나의 점수
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "나의 점수 : ",
                                    fontSize = 18.sp,
                                    fontFamily = memomentkkukkukk
                                )
                                Text(
                                    text = "$myScore",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = memomentkkukkukk
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 유저 리스트 (점수 높은 순으로 정렬, 5명)
                            similarUsers.take(5).forEachIndexed { index, (userName, score) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.cat_foot),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable(enabled = !showStatsPopup) {
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val user = userDao.getUserByName(userName)
                                                    user?.let {
                                                        withContext(Dispatchers.Main) {
                                                            selectedUserStats = Triple(userName, score, Triple(it.attack, it.defense, it.health))
                                                            showStatsPopup = true
                                                        }
                                                    }
                                                }
                                            }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = userName,
                                        fontSize = 18.sp,
                                        modifier = Modifier
                                            .clickable(enabled = !showStatsPopup) {
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val user = userDao.getUserByName(userName)
                                                    user?.let {
                                                        withContext(Dispatchers.Main) {
                                                            selectedUserStats = Triple(userName, score, Triple(it.attack, it.defense, it.health))
                                                            showStatsPopup = true
                                                        }
                                                    }
                                                }
                                            }
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "${score}점",
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .background(if (challengeCount > 0 && !showStatsPopup) Color(0xFFFFF0F5) else Color.Gray)
                                            .border(2.dp, Color.Black)
                                            .padding(horizontal = 16.dp)
                                            .clickable(enabled = challengeCount > 0 && !showStatsPopup) { 
                                                // 도전 횟수 확인
                                                if (challengeCount <= 0) return@clickable
                                                
                                                // 상대방 정보 로드 및 전투 시작
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val opponent = userDao.getUserByName(userName)
                                                    if (opponent != null) {
                                                        withContext(Dispatchers.Main) {
                                                            mainViewModel.startFight(opponent)
                                                            showPopup = false // 전투 시작 시 팝업 닫기 (선택사항)
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "도전",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (index < 4) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // 남은 도전 횟수
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "남은 도전 횟수",
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "$challengeCount/5",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // 보상 팝업: 랭킹 박스와 내 정보 박스 위에 오버레이
                if (showRewardPopup) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .border(3.dp, Color.Black)
                            .padding(24.dp)
                    ) {
                        // X 버튼 (오른쪽 위)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(40.dp)
                                .background(Color.Red)
                                .border(2.dp, Color.Black)
                                .clickable { showRewardPopup = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "X",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // 보상 리스트
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 16.dp)
                        ) {
                            Text(
                                text = "랭킹 보상",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = memomentkkukkukk,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 1등 보상
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1등",
                                    fontSize = 18.sp,
                                    fontFamily = memomentkkukkukk,
                                    modifier = Modifier.width(90.dp),
                                    textAlign = TextAlign.Left
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.width(100.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.coin),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "골드 100",
                                        fontSize = 18.sp,
                                        fontFamily = memomentkkukkukk,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.Left
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2등 보상
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "2등",
                                    fontSize = 18.sp,
                                    fontFamily = memomentkkukkukk,
                                    modifier = Modifier.width(90.dp),
                                    textAlign = TextAlign.Left
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.width(100.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.coin),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "골드 80",
                                        fontSize = 18.sp,
                                        fontFamily = memomentkkukkukk,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.Left
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 3등 보상
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "3등",
                                    fontSize = 18.sp,
                                    fontFamily = memomentkkukkukk,
                                    modifier = Modifier.width(90.dp),
                                    textAlign = TextAlign.Left
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.width(100.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.coin),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "골드 60",
                                        fontSize = 18.sp,
                                        fontFamily = memomentkkukkukk,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.Left
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 4~10등 보상
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "4~10등",
                                    fontSize = 18.sp,
                                    fontFamily = memomentkkukkukk,
                                    modifier = Modifier.width(90.dp),
                                    textAlign = TextAlign.Left
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.width(100.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.coin),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "골드 40",
                                        fontSize = 18.sp,
                                        fontFamily = memomentkkukkukk,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.Left
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 11~20등 보상
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "11~20등",
                                    fontSize = 18.sp,
                                    fontFamily = memomentkkukkukk,
                                    modifier = Modifier.width(90.dp),
                                    textAlign = TextAlign.Left
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.width(100.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.coin),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "골드 30",
                                        fontSize = 18.sp,
                                        fontFamily = memomentkkukkukk,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.Left
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 21~50등 보상
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "21~50등",
                                    fontSize = 18.sp,
                                    fontFamily = memomentkkukkukk,
                                    modifier = Modifier.width(90.dp),
                                    textAlign = TextAlign.Left
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.width(100.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.coin),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "골드 20",
                                        fontSize = 18.sp,
                                        fontFamily = memomentkkukkukk,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.Left
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 51~100등 보상
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "51~100등",
                                    fontSize = 18.sp,
                                    fontFamily = memomentkkukkukk,
                                    modifier = Modifier.width(90.dp),
                                    textAlign = TextAlign.Left
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.width(100.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.coin),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "골드 10",
                                        fontSize = 18.sp,
                                        fontFamily = memomentkkukkukk,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.Left
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 공방체 팝업
                if (showStatsPopup && selectedUserStats != null) {
                    val (userName, score, stats) = selectedUserStats!!
                    val (attack, defense, health) = stats
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .background(Color.White)
                                .border(2.dp, Color.Black)
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // X 버튼 (오른쪽 위)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .size(32.dp)
                                        .background(Color.Red)
                                        .border(2.dp, Color.Black)
                                        .clickable { showStatsPopup = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "X",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // 유저 이름
                                Text(
                                    text = userName,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                // 점수
                                Text(
                                    text = "점수: ${score}점",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                                
                                // 공격력
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "공격력:",
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "$attack",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // 방어력
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "방어력:",
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "$defense",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // 체력
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "체력:",
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "$health",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // 확인 버튼
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(Color(0xFFFFF0F5))
                                        .border(2.dp, Color.Black)
                                        .clickable { showStatsPopup = false },
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
    }
}

// 다음 월요일 00시까지의 남은 시간 계산
fun calculateTimeUntilNextMonday(): Long {
    val now = Calendar.getInstance()
    val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
    
    val calendar = Calendar.getInstance()
    val daysUntilMonday = if (currentDayOfWeek == Calendar.MONDAY) {
        7
    } else {
        (Calendar.MONDAY - currentDayOfWeek + 7) % 7
    }

    calendar.add(Calendar.DAY_OF_YEAR, daysUntilMonday)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    
    val nowMillis = Calendar.getInstance().timeInMillis
    val nextMonday = calendar.timeInMillis
    
    return nextMonday - nowMillis
}

fun formatRemainingTime(milliseconds: Long): String {
    if (milliseconds <= 0) {
        return "시즌 종료"
    }
    
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    val remainingHours = hours % 24
    val remainingMinutes = minutes % 60
    val remainingSeconds = seconds % 60
    
    return "${days}일 ${remainingHours}시간 ${remainingMinutes}분 ${remainingSeconds}초"
}
// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.powerwalking.service.StepSensorService
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val activityRecognitionGranted = permissions[android.Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[android.Manifest.permission.POST_NOTIFICATIONS] ?: false
        } else {
            true
        }

        if (activityRecognitionGranted && notificationGranted) {
            startStepService()
        } else {
            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-Edge 활성화 및 시스템 바 숨기기 설정
        enableEdgeToEdge()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        
        checkPermissionsAndStartService()

        setContent {
            MainScreen()
        }
    }

    private fun checkPermissionsAndStartService() {
        val permissions = mutableListOf(android.Manifest.permission.ACTIVITY_RECOGNITION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            startStepService()
        } else {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun startStepService() {
        val intent = Intent(this, StepSensorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

val memomentkkukkukk = FontFamily(
    Font(R.font.memomentkkukkukk)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel = viewModel()) {
    val uiState by mainViewModel.uiState.collectAsState()
    var selectedItem by remember { mutableIntStateOf(0) }
    
    // 전투 중인지 확인
    val isFighting = uiState.isFighting

    val context = LocalContext.current
    
    // Broadcast Receiver 등록
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.example.powerwalking.STEP_UPDATE") {
                    val steps = intent.getIntExtra("steps", 0)
                    mainViewModel.updateCurrentSteps(steps)
                }
            }
        }
        val filter = IntentFilter("com.example.powerwalking.STEP_UPDATE")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
       
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    if (!isFighting) {
                        MyTopAppBar(totalCoins = uiState.totalCoins)
                    }
                },
                containerColor = Color.Transparent
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    when (selectedItem) {
                        0 -> HomeScreen(uiState = uiState, mainViewModel = mainViewModel)
                        1 -> CharacterScreen()
                        2 -> ArenaScreen()
                        3 -> ShopScreen()
                    }
                }
            }

            if (!isFighting) {
                MyBottomNavBar(
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(uiState: MainUiState, mainViewModel: MainViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = (-250).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(400.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cloud),
                    contentDescription = "Cloud",
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier.offset(y = 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = NumberFormat.getNumberInstance(Locale.US).format(uiState.currentSteps),
                        fontFamily = memomentkkukkukk,
                        fontSize = 50.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "/8,000",
                        fontFamily = memomentkkukkukk,
                        fontSize = 20.sp,
                        color = Color.DarkGray,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 15.dp, y = 10.dp)
                    )
                }
            }

            val claimableCoins = mainViewModel.claimableCoins
            val buttonEnabled = claimableCoins > 0

            Button(
                onClick = { mainViewModel.claimCoins() },
                enabled = buttonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (buttonEnabled) Color(0xFFFFD700) else Color.Gray,
                    contentColor = Color.Black
                ),
                modifier = Modifier.offset(y = (-130).dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.coin),
                        contentDescription = "Coin",
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "$claimableCoins 받기",
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 22.sp
                    )
                }
            }
        }

        val catImages: List<Int> = listOf(
            R.drawable.cat_running1,
            R.drawable.cat_running2,
            R.drawable.cat_running1,
            R.drawable.cat_running3
        )
        var currentImageIndex by remember { mutableIntStateOf(0) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(300)
                currentImageIndex = (currentImageIndex + 1) % catImages.size
            }
        }

        // 장착된 모자가 있으면 고양이 머리 위에 렌더링
        Box(
            modifier = Modifier.offset(y = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = catImages[currentImageIndex]),
                contentDescription = "Animated Cat"
            )
            
            // 장착된 모자 표시
            uiState.equippedHat?.let { hat ->
                Image(
                    painter = painterResource(id = hat.imageResId),
                    contentDescription = "Equipped Hat",
                    modifier = Modifier
                        .size(100.dp)
                        .offset(y = (-80).dp)
                )
            }
        }

        // Stats Row
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 140.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.attack),
                    contentDescription = "Attack Icon",
                    modifier = Modifier.fillMaxSize()
                )
                Box(contentAlignment = Alignment.Center) {
                    val text = "${uiState.totalAttack}"
                    val fontSize = 40.sp
                    Text(
                        text = text,
                        color = Color.Black,
                        style = TextStyle(
                            fontSize = fontSize,
                            fontFamily = memomentkkukkukk,
                            drawStyle = Stroke(width = 10f)
                        )
                    )
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = fontSize,
                        fontFamily = memomentkkukkukk
                    )
                }
            }
            Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.defense),
                    contentDescription = "Defense Icon",
                    modifier = Modifier.fillMaxSize()
                )
                Box(contentAlignment = Alignment.Center) {
                    // 방어력을 정수형 문자열로 변환
                    val text = "${uiState.totalDefense}"
                    val fontSize = 40.sp
                    Text(
                        text = text,
                        color = Color.Black,
                        style = TextStyle(
                            fontSize = fontSize,
                            fontFamily = memomentkkukkukk,
                            drawStyle = Stroke(width = 10f)
                        )
                    )
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = fontSize,
                        fontFamily = memomentkkukkukk
                    )
                }
            }
            Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.health),
                    contentDescription = "Health Icon",
                    modifier = Modifier.fillMaxSize()
                )
                Box(contentAlignment = Alignment.Center) {
                    val text = "${uiState.totalHealth}"
                    val fontSize = 40.sp
                    Text(
                        text = text,
                        color = Color.Black,
                        style = TextStyle(
                            fontSize = fontSize,
                            fontFamily = memomentkkukkukk,
                            drawStyle = Stroke(width = 10f)
                        )
                    )
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = fontSize,
                        fontFamily = memomentkkukkukk
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainScreen()
}

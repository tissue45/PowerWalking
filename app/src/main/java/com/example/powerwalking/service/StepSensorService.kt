// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.powerwalking.data.AppDatabase
import com.example.powerwalking.data.StepEntity
import com.example.powerwalking.util.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepSensorService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    
    private var previousSensorValue: Int = 0 
    private var todaySteps: Int = 0
    private lateinit var prefs: SharedPreferences
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase
    
    // 키를 변경하여 이전의 잘못된 데이터와 섞이지 않도록 함
    private val PREF_KEY_LAST_SENSOR = "v8_last_sensor_value"

    override fun onCreate() {
        super.onCreate()
        try {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            prefs = getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
            database = AppDatabase.getDatabase(this)
            
            NotificationUtil.createNotificationChannel(this)

            try {
                val date = getTodayDate()
                // 동기 호출
                val stepsFromDb = database.stepDao().getStepsByDateSync(date)?.steps
                todaySteps = stepsFromDb ?: 0
            } catch (e: Exception) {
                Log.e("StepSensorService", "DB Error: ${e.message}")
                todaySteps = 0
            }

            previousSensorValue = prefs.getInt(PREF_KEY_LAST_SENSOR, 0)
            
            val notification = NotificationUtil.createNotification(this, todaySteps)
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
            } else {
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            Log.e("StepSensorService", "Error in onCreate: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            stepCounter?.let {
            sensorManager.unregisterListener(this) 
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            sendStepUpdate(todaySteps)
        } ?: run {
            showToast("이 기기엔 만보기 센서가 없습니다.")
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val currentVal = event.values[0].toInt()
            
            // 1. 기준점 설정 (앱 설치 후 첫 실행 시)
            if (previousSensorValue == 0) {
                previousSensorValue = currentVal
                prefs.edit().putInt(PREF_KEY_LAST_SENSOR, previousSensorValue).apply()
                return 
            }
            
            // 2. 재부팅 감지
            if (currentVal < previousSensorValue) {
                previousSensorValue = 0
                return
            }

            // 3. 변화량(Delta) 계산
            val delta = currentVal - previousSensorValue
            
            if (delta > 0) {
                todaySteps += delta
                previousSensorValue = currentVal
                
                prefs.edit().putInt(PREF_KEY_LAST_SENSOR, previousSensorValue).apply()
                saveStepsToDb(todaySteps)
                
                updateNotification(todaySteps)
                sendStepUpdate(todaySteps)
            }
        }
    }

    private fun saveStepsToDb(steps: Int) {
        serviceScope.launch {
            try {
                val date = getTodayDate()
                val entity = StepEntity(date, steps)
                database.stepDao().insertSteps(entity)
            } catch (e: Exception) {
                Log.e("StepSensorService", "Save Error: ${e.message}")
            }
        }
    }
    
    private fun sendStepUpdate(steps: Int) {
        val intent = Intent("com.example.powerwalking.STEP_UPDATE")
        intent.setPackage(packageName)
        intent.putExtra("steps", steps)
        sendBroadcast(intent)
    }

    private fun updateNotification(steps: Int) {
        val notification = NotificationUtil.createNotification(this, steps)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(1, notification)
    }
    
    private fun showToast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }
}

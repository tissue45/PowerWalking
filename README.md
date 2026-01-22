# 🚶 PowerWalking

걸음 수를 기반으로 한 피트니스 경쟁 게임 앱입니다. 일상 속 걷기를 게임화하여 건강한 생활 습관을 유도합니다.

## 📱 프로젝트 소개

PowerWalking은 안드로이드 기기의 만보기 센서를 활용하여 사용자의 걸음 수를 추적하고, 이를 게임 내 코인으로 전환하여 캐릭터의 스탯을 성장시키는 앱입니다. 걸을수록 강해지는 캐릭터로 다른 플레이어와 전투를 벌이고, 주간 랭킹에서 순위를 경쟁할 수 있습니다.

## ✨ 주요 기능

### 🏠 홈 화면
- 실시간 걸음 수 추적 및 표시
- 걸음 수에 따른 코인 자동 획득 (100걸음당 1코인, 최대 8,000걸음까지)
- 캐릭터 애니메이션
- 현재 스탯 표시 (공격력, 방어력, 체력)

### 👤 캐릭터 관리
- **성장 시스템**: 코인을 사용하여 공격력, 방어력, 체력을 강화
- **모자 시스템**: 
  - 뽑기를 통해 다양한 모자 획득
  - 모자마다 공격력, 방어력, 체력 보너스 제공
  - 모자 장착/해제 및 삭제 기능
  - 랜덤 스탯 보너스 (공격력/방어력: 1~4%, 체력: 5~10%)

### ⚔️ 전투 시스템
- **아레나**: 다른 플레이어와 전투
- **랭킹 시스템**: 주간 시즌제 랭킹 (월요일 00시 리셋)
- **도전 시스템**: 하루 5회 제한
- **전투 결과**: 승리 시 점수 획득, 패배 시 점수 차감
- **실시간 전투**: 1초마다 데미지 계산 및 체력바 업데이트

### 🛒 상점
- **모자 뽑기**: 100코인(1회) 또는 500코인(5회)
- **확률 정보**: 각 스탯 보너스의 확률 표시

### 📊 데이터 관리
- Room Database를 활용한 로컬 데이터 저장
- 걸음 수 일별 기록
- 사용자 정보 및 랭킹 데이터 관리

## 🛠️ 기술 스택

### 언어 및 프레임워크
- **Kotlin** 2.0.21
- **Jetpack Compose** - 선언적 UI 프레임워크
- **Material 3** - 최신 Material Design 컴포넌트

### 아키텍처
- **MVVM (Model-View-ViewModel)** 패턴
- **StateFlow** - 상태 관리
- **Coroutines** - 비동기 처리

### 데이터베이스
- **Room Database** 2.6.1
  - User Entity (사용자 정보, 점수, 스탯)
  - StepEntity (일별 걸음 수 기록)

### 안드로이드 기능
- **Foreground Service** - 백그라운드 걸음 수 추적
- **Sensor API** - 만보기 센서 활용
- **Notification** - 실시간 걸음 수 알림
- **Activity Recognition Permission** - 걸음 수 추적 권한

### 빌드 도구
- **Gradle** (Kotlin DSL)
- **KSP (Kotlin Symbol Processing)** - Room 코드 생성

## 📁 프로젝트 구조

```
app/src/main/java/com/example/powerwalking/
├── MainActivity.kt              # 메인 액티비티
├── MainViewModel.kt             # 메인 뷰모델 및 상태 관리
├── MainUiState.kt               # UI 상태 데이터 클래스
│
├── data/                        # 데이터 레이어
│   ├── AppDatabase.kt           # Room 데이터베이스
│   ├── User.kt                  # 사용자 엔티티
│   ├── UserDao.kt               # 사용자 DAO
│   ├── StepEntity.kt            # 걸음 수 엔티티
│   └── StepDao.kt               # 걸음 수 DAO
│
├── service/                     # 서비스 레이어
│   └── StepSensorService.kt    # 걸음 수 추적 서비스
│
├── util/                        # 유틸리티
│   └── NotificationUtil.kt     # 알림 유틸리티
│
└── 화면 컴포저블
    ├── HomeScreen.kt            # 홈 화면
    ├── CharacterScreen.kt       # 캐릭터 관리 화면
    ├── ArenaScreen.kt           # 전투/랭킹 화면
    ├── ShopScreen.kt            # 상점 화면
    ├── FightingScreen.kt        # 전투 화면
    ├── MyTopAppBar.kt           # 상단 앱바
    └── MyBottomNavBar.kt        # 하단 네비게이션 바
```

## 🚀 설치 및 실행

### 요구사항
- Android Studio Hedgehog 이상
- Android SDK 24 이상 (최소 지원 버전)
- Android SDK 36 (타겟 버전)
- JDK 11 이상

### 빌드 방법

1. 저장소 클론
```bash
git clone https://github.com/tissue45/PowerWalking.git
cd PowerWalking
```

2. Android Studio에서 프로젝트 열기

3. Gradle 동기화

4. 빌드 및 실행
```bash
# 디버그 빌드
./gradlew assembleDebug

# 릴리즈 빌드
./gradlew assembleRelease
```

또는 Android Studio에서 직접 실행 (Shift + F10)

### 권한 설정
앱 실행 시 다음 권한이 필요합니다:
- **활동 인식 권한** (ACTIVITY_RECOGNITION): 걸음 수 추적
- **알림 권한** (POST_NOTIFICATIONS): 실시간 걸음 수 알림

## 🎮 사용 방법

1. **앱 실행**: 앱을 실행하면 자동으로 걸음 수 추적이 시작됩니다.
2. **코인 획득**: 8,000걸음까지 걸으면 코인을 획득할 수 있습니다.
3. **캐릭터 성장**: 획득한 코인으로 공격력, 방어력, 체력을 강화합니다.
4. **모자 뽑기**: 상점에서 모자를 뽑아 스탯을 더욱 향상시킵니다.
5. **전투**: 아레나에서 다른 플레이어와 전투하여 점수를 획득합니다.
6. **랭킹 경쟁**: 주간 랭킹에서 상위권을 노려보세요!


## 🔧 개발 환경

- **IDE**: Android Studio
- **언어**: Kotlin
- **최소 SDK**: 24 (Android 7.0)
- **타겟 SDK**: 36 (Android 14)
- **컴파일 SDK**: 36

## 📝 주요 구현 사항

### 백그라운드 서비스
- `StepSensorService`를 통해 앱이 종료되어도 걸음 수를 지속적으로 추적
- Foreground Service로 실행되어 시스템에 의해 종료되지 않음
- 재부팅 감지 및 센서 값 초기화 처리

### 전투 시스템
- 실시간 데미지 계산: `공격력 * (100 / (100 + 방어력))`
- 1초마다 턴 진행
- 승리/패배에 따른 점수 변동
- 모자 보너스 적용

### 데이터 영속성
- Room Database를 통한 로컬 저장
- 일별 걸음 수 기록
- 사용자 정보 및 랭킹 데이터 관리

## 🤝 기여하기

이슈나 풀 리퀘스트는 언제든 환영합니다!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 👤 개발자

개발자 정보를 추가해주세요.

## 🙏 감사의 말

- Jetpack Compose 팀
- Android 개발 커뮤니티

---

**건강한 생활 습관을 위한 첫 걸음, PowerWalking과 함께하세요!** 🚶‍♂️✨

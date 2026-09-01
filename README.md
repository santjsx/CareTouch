# CareTouch

**CareTouch** is an Apple-grade, Voice-First Android Communication Launcher designed specifically for non-literate and elderly Telugu-speaking family members. It completely removes the complexity of standard smartphone launchers, replacing it with an intuitive, zero-emoji, voice-assisted interface.

---

## ✨ Key Features

- **Voice-First Interaction**: Spoken Telugu voice prompts for time, date, battery status, signal strength, and contact actions powered by Android TTS.
- **Visual Contact Grid**: 4-column horizontal photo card grid with high-contrast avatars, clean minimal labels, and physics-based bounce micro-interactions.
- **Graphic Status Gauges**: 
  - Dynamic fluid battery gauge with charging animations.
  - 4-bar stepped signal tower reflecting live real-time telephony modem levels.
  - Internet connectivity indicator with captive portal / throughput detection.
- **PIN-Protected Admin Console**:
  - Secure 4-digit PIN authentication with brute-force rate limiting (30s cooldown).
  - Add/Edit family contacts with custom photos, local persistent downsampling, and Telugu pronunciation overrides.
  - Emergency SOS configuration with auto-reassignment safeguards.
  - Voice speed tuner with preset speeds (0.85x for elders).
  - Full system diagnostics and interactive voice testing.
- **Bulletproof Architecture**:
  - Fallback modal for offline/WhatsApp routing.
  - 1200ms debounce protection against accidental double taps.
  - Automatic restart on launch and back-gesture suppression.

---

## 🛠️ Built With

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose & Material 3
- **Architecture**: MVVM with unidirectional StateFlow
- **Voice Engine**: Android TextToSpeech (`te_IN` with fallback synthesis)
- **Telecom**: Android TelephonyManager & TelecomManager APIs

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- Android SDK 34+
- Java 17+

### Build & Run
```bash
# Clone the repository
git clone https://github.com/santjsx/CareTouch.git

# Navigate to project directory
cd CareTouch

# Build debug APK
./gradlew assembleDebug

# Install to connected device or emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License
This project is open-source under the [MIT License](LICENSE).

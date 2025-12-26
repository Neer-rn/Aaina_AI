# Quick Start Guide

## 🚀 Get Running in 3 Steps

### 1. Open the Project
```bash
# Open Android Studio
# File → Open → Navigate to: /home/neer-rn/AndroidStudioProjects/AainaAI
```

### 2. Sync Dependencies
- Wait for Gradle sync to complete (check bottom status bar)
- If prompted, update Gradle or Android Gradle Plugin

### 3. Run the App
- Connect an Android device (API 26+) or start an emulator
- Click the green Run button ▶️ or press `Shift + F10`

---

## 📱 App Flow

```
┌──────────────────┐
│  Onboarding      │  Welcome screen with verification steps
│  Screen          │  • Tap "Start Verification"
└────────┬─────────┘
         ↓
┌──────────────────┐
│  Citizenship     │  Document scanning
│  Scan Screen     │  • Position card in landscape frame
│                  │  • Tap camera button to capture
└────────┬─────────┘
         ↓
┌──────────────────┐
│  Active Liveness │  Face verification (THE KEY SCREEN)
│  Screen          │  • Turn head LEFT (wait for ✓)
│                  │  • Turn head RIGHT (wait for ✓)
│                  │  • SMILE (wait for ✓)
└────────┬─────────┘
         ↓
┌──────────────────┐
│  Success         │  Verification complete!
│  Screen          │  (Mock - Phase 2 will add API)
└──────────────────┘
```

---

## 🎮 Testing Tips

### Citizenship Scan Screen
- Use any card (doesn't need to be real citizenship)
- Ensure good lighting
- Fill the golden frame with the card
- Tap the big camera button

### Liveness Screen (Most Important!)
- **Position face close**: Fill the oval frame
- **Wait for green border**: Means face is detected
- **Turn LEFT sharply**: Not just a slight tilt - really turn your head
- **Hold for 1 second**: System requires 3 consecutive frames
- **Turn RIGHT sharply**: Same as left
- **Big genuine SMILE**: Not a smirk - show teeth!

**Pro tip**: Each action needs to be held for ~1 second. You'll feel a vibration when it succeeds!

---

## 🔧 Troubleshooting

### "No face detected"
- Move closer to camera
- Ensure good lighting
- Face the camera directly

### "Camera permission denied"
- Go to Settings → Apps → Nagarikta KYC → Permissions → Enable Camera

### Liveness step won't complete
- Make the head turn **more exaggerated** (30+ degrees)
- Hold the position for 1+ second
- Ensure face fills at least 30% of the oval frame

### Build errors
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

---

## 📂 Key Files to Explore

| File | Purpose |
|------|---------|
| `LivenessScreen.kt` | The polished liveness UI |
| `LivenessViewModel.kt` | State machine logic (LEFT→RIGHT→SMILE) |
| `FaceDetector.kt` | ML Kit wrapper for face analysis |
| `CameraManager.kt` | CameraX wrapper |
| `Theme.kt` | Custom color scheme |

---

## 🎨 Customization

### Change Colors
Edit `ui/theme/Color.kt`:
```kotlin
val DeepEmerald = Color(0xFF004D40)  // Change to your primary
val OfficialGold = Color(0xFFD4AF37)  // Change to your secondary
```

### Change Liveness Thresholds
Edit `LivenessViewModel.kt`:
```kotlin
// Line ~68 - Make head turns easier/harder
is LivenessChallenge.TurnLeft -> {
    result.isHeadTurnedLeft(threshold = -25f)  // Change -25f to -15f for easier
}
```

### Add More Liveness Steps
1. Add new challenge to `LivenessState.kt`
2. Update `getNextChallenge()` in `LivenessViewModel.kt`
3. Add detection logic in `processFaceAnalysis()`

---

## ⚡ Performance Notes

- **CameraX**: Uses STRATEGY_KEEP_ONLY_LATEST to prevent frame backlog
- **ML Kit**: PERFORMANCE_MODE_FAST for real-time analysis
- **Memory**: Images are stored in cache (auto-cleaned by Android)
- **Battery**: Camera and ML Kit are resource-intensive - this is expected

---

## 🔐 Security Notes (For Production)

- [ ] Enable ProGuard/R8 obfuscation
- [ ] Add SSL pinning for API calls (Phase 2)
- [ ] Encrypt captured images before storage
- [ ] Implement screenshot blocking
- [ ] Add root detection
- [ ] Use secure key storage (Android Keystore)

---

## 📞 Need Help?

Check the main `README.md` for detailed architecture documentation.

**Happy Testing! 🎉**

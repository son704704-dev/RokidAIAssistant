# Rokid AI Assistant v1.1.0

Release date: 2026-08-08

## English

### Highlights

- Added the experimental on-device Local Gemma provider foundation and manual vision capability overrides for custom OpenAI-compatible endpoints.
- Hardened Bluetooth reconnection, camera capture, photo transfer, recording pause/resume, and phone-to-glasses event delivery.
- Improved cancellation, timeout, resource cleanup, and error handling across AI, STT, TTS, and streaming services.
- Protected credentials and logs, added safe log-file access, and preserved user data with an explicit Room database migration.
- Improved provider/model compatibility, encoded-audio transcription, localized errors, and documentation language fallback.
- Published three signed APK variants with signature verification and SHA-256 checksums.

### Validation

- 861 unit tests pass across the app, common, glasses-app, and phone-app modules.
- Android lint passes with no unbaselined errors.
- Signed release APKs build successfully for all three application modules.

### Assets

- `rokid-ai-assistant-v1.1.0.apk` — integrated/legacy application.
- `rokid-phone-v1.1.0.apk` — modular phone application.
- `rokid-glasses-v1.1.0.apk` — Rokid glasses application.
- `SHA256SUMS.txt` — checksums for all APK assets.

## 中文（繁體）

### 更新重點

- 新增實驗性的裝置端 Local Gemma 服務基礎，以及自訂 OpenAI 相容端點的手動視覺能力設定。
- 強化藍牙自動重連、相機拍攝、照片傳輸、錄音暫停／繼續，以及手機與眼鏡間的事件傳遞。
- 改善 AI、STT、TTS 與串流服務的取消、逾時、資源釋放及錯誤處理。
- 加強憑證與日誌保護、安全的日誌檔案存取，並透過明確的 Room 資料庫遷移保留使用者資料。
- 改善服務商／模型相容性、編碼音訊轉錄、在地化錯誤訊息，以及文件語言備援行為。
- 發布三個已簽署 APK 版本，並提供 APK 簽章驗證與 SHA-256 校驗碼。

### 驗證結果

- app、common、glasses-app 與 phone-app 共 861 個單元測試全部通過。
- Android lint 通過，沒有未列入基準的錯誤。
- 三個應用程式模組皆可成功建立已簽署的 release APK。

### 下載檔案

- `rokid-ai-assistant-v1.1.0.apk` — 整合版／舊版應用程式。
- `rokid-phone-v1.1.0.apk` — 模組化手機端應用程式。
- `rokid-glasses-v1.1.0.apk` — Rokid 眼鏡端應用程式。
- `SHA256SUMS.txt` — 所有 APK 的校驗碼。

## Compatibility / 相容性

- Minimum Android version / 最低 Android 版本：Android 9 (API 28)
- Release signing certificate SHA-256: `7C:A3:A3:F7:BA:C7:48:3C:0D:16:BB:9E:1E:BD:B6:57:F0:94:CE:77:94:83:DD:BC:7F:E3:64:32:46:01:E3:0B`

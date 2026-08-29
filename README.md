# J.A.R.V.I.S. (Just A Rather Very Intelligent System)

An intelligent voice-activated Android assistant featuring a futuristic Holographic HUD, interactive speech synthesis, natural language command recognition, multi-model AI provider integration (OpenRouter, Google Gemini, OpenAI, DeepSeek), offline note & task memory, and privacy shield telemetry.

---

## 🛠️ Building the Debug APK

### Local Debug Build

To compile and assemble the Debug APK locally on your machine:

1. Ensure you have **JDK 17** (or newer) and the **Android SDK** installed.
2. Grant execution permission to the Gradle Wrapper (Linux/macOS):
   ```bash
   chmod +x ./gradlew
   ```
3. Run the Gradle build task:
   ```bash
   ./gradlew assembleDebug
   ```
4. Upon successful compilation, the generated Debug APK is located at:
   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🚀 GitHub Actions Automated Builds

The repository includes an automated CI/CD workflow (`.github/workflows/build-apk.yml`) to automatically build and verify the Debug APK on every update.

### Automatic Builds on Push
- Any push to the `main` or `master` branch automatically triggers the build workflow.

### Manual Workflow Trigger
1. Go to the **Actions** tab in your GitHub repository.
2. Select **Build and Publish Android Debug APK** from the workflows list.
3. Click **Run workflow**, choose the branch, and optionally check **Publish generated APK to GitHub Releases**.

---

## 📥 Downloading the Generated APK

### 1. From GitHub Actions Artifacts
1. Go to the **Actions** tab of your repository on GitHub.
2. Click on the most recent completed workflow run.
3. Scroll down to the **Artifacts** section at the bottom of the summary page.
4. Click on **JARVIS-Debug-APK** to download the ZIP file containing the signed Debug APK.

### 2. From GitHub Releases
When a version tag is pushed or a manual release workflow is run, the APK will be directly available under the **Releases** tab on GitHub:
1. Navigate to **Releases** on your GitHub repository page.
2. Expand the **Assets** section of the latest release.
3. Download `app-debug.apk`.

---

## 🏷️ Creating a GitHub Release

To trigger an automated release and attach the Debug APK:

1. Create a version tag locally:
   ```bash
   git tag v1.0.0
   ```
2. Push the tag to your GitHub repository:
   ```bash
   git push origin v1.0.0
   ```
3. GitHub Actions will automatically detect the `v*` tag, build the Debug APK, create the release `JARVIS v1.0.0`, generate changelogs, and attach the APK asset.

---

## 🔒 Security & API Configurations

- **No Hardcoded Secrets**: All AI provider API keys (OpenRouter, Gemini, OpenAI, DeepSeek) are configured securely on-device by the user via the in-app **API Center** or through build-time `.env` injection.
- **Privacy Mode**: One-tap privacy shield immediately mutes acoustic sensors and isolates external telemetry.

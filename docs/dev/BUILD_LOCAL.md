# بناء التطبيق محلياً من الصفر

هذا الدليل يبني `app-qa.apk` على جهازك مباشرة. بعد أول بناء لن تحتاج تنزيل APK من GitHub كل مرة: تعدّل، تبني، وتثبّت على الجوال بأمر واحد.

## المتطلبات

| المطلوب | النسخة | ملاحظة |
|---|---|---|
| JDK | 17 | Gradle 9.3.1 و AGP 9.1.1 |
| Android SDK Platform | 36 | `compileSdk = 36` |
| Android Build-Tools | 36.0.0 | `aapt2` و`d8` |
| Android Platform-Tools | أحدث إصدار | يوفر `adb` |
| Git | أي إصدار حديث | لسحب المشروع |
| مساحة القرص | 8–10 GB | Gradle + SDK + الكاش |
| الذاكرة | 8 GB مستحسنة | البناء يعمل بـ 4 GB لكن أبطأ |

المشروع يستخدم Gradle Wrapper، فلا تحتاج تثبيت Gradle يدوياً. أول بناء ينزّل Gradle والاعتماديات (حوالي 1–2 GB)، بعده البناء التزايدي أسرع بكثير.

## الطريقة الأسهل: Android Studio

1. نزّل Android Studio من <https://developer.android.com/studio> وثبّته بالإعدادات الافتراضية.
2. من شاشة الترحيب افتح **More Actions → SDK Manager**.
3. في تبويب **SDK Platforms** علّم `Android 16 (API 36)`.
4. في تبويب **SDK Tools** علّم:
   - `Android SDK Build-Tools 36`
   - `Android SDK Platform-Tools`
   - `Android SDK Command-line Tools (latest)`
5. اضغط **Apply**. سيثبّت كل شيء ويقبل التراخيص.
6. افتح مجلد المشروع: **Open** ثم اختر مجلد `yousuf-toolkit-app`.
7. انتظر `Gradle Sync`. إن ظهرت رسالة JDK، اختر **Gradle JDK → 17** من `Settings → Build, Execution, Deployment → Build Tools → Gradle`.
8. من القائمة اختر `app` ثم اضغط ▶︎ **Run**. سيثبت نسخة debug على الجوال أو المحاكي.

نسخة debug لها معرّف منفصل `com.yousuf.toolkit.debug`، فلا تستبدل نسخة الاختبار العادية. لتثبيت نسخة الاختبار بنفس المعرّف استخدم سطر الأوامر بالأسفل.

## الطريقة الثانية: سطر الأوامر

### Windows 11

افتح **PowerShell** كمدير.

1. ثبّت JDK 17 و Git:
   ```powershell
   winget install --id EclipseAdoptium.Temurin.17.JDK -e
   winget install --id Git.Git -e
   ```
   أغلق PowerShell وافتحه من جديد، ثم تأكد:
   ```powershell
   java -version
   git --version
   ```
2. نزّل **Command-line tools only** من <https://developer.android.com/studio#command-line-tools-only> (ملف `commandlinetools-win-*.zip`).
3. فك الضغط إلى مجلد SDK:
   ```powershell
   $sdk = "$env:LOCALAPPDATA\Android\Sdk"
   New-Item -ItemType Directory -Force "$sdk\cmdline-tools" | Out-Null
   Expand-Archive "$HOME\Downloads\commandlinetools-win-*.zip" -DestinationPath "$env:TEMP\cmdline-tools"
   Move-Item "$env:TEMP\cmdline-tools\cmdline-tools" "$sdk\cmdline-tools\latest"
   ```
   المهم أن يكون المسار النهائي:
   ```
   C:\Users\<اسمك>\AppData\Local\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat
   ```
4. اضبط المتغيرات واضبط PATH لهذه الجلسة:
   ```powershell
   [Environment]::SetEnvironmentVariable("ANDROID_HOME", $sdk, "User")
   [Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17*", "User")
   $env:ANDROID_HOME = $sdk
   $env:Path += ";$sdk\cmdline-tools\latest\bin;$sdk\platform-tools"
   ```
   إن كان مسار JDK مختلفاً، اقرأه من `Get-ChildItem "C:\Program Files\Eclipse Adoptium"`.
5. اقبل التراخيص وثبّت الحزم:
   ```powershell
   sdkmanager --licenses
   sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"
   ```
   وافق على كل ترخيص بكتابة `y`.
6. اسحب المشروع:
   ```powershell
   cd $HOME\Projects
   git clone https://github.com/yswef/yousuf-toolkit-app.git
   cd yousuf-toolkit-app
   ```
7. أنشئ `local.properties`:
   ```powershell
   "sdk.dir=$($env:ANDROID_HOME -replace '\\','\\')" | Set-Content -Encoding ASCII local.properties
   ```
   أو اترك `ANDROID_HOME` مضبوطاً وسيكتشفه Gradle.
8. ابنِ:
   ```powershell
   .\gradlew.bat :app:assembleQa
   ```
   الناتج:
   ```
   app\build\outputs\apk\qa\app-qa.apk
   ```
9. ثبّت على الجوال الموصول:
   ```powershell
   adb devices
   adb install -r app\build\outputs\apk\qa\app-qa.apk
   ```
   أو اجعل Gradle يبني ويثبّت بأمر واحد:
   ```powershell
   .\gradlew.bat :app:installQa
   ```

### macOS

1. ثبّت JDK 17 و Git:
   ```bash
   brew install --cask temurin@17
   brew install git
   java -version
   ```
2. نزّل Command-line tools من صفحة أندرويد وفك الضغط:
   ```bash
   mkdir -p "$HOME/Library/Android/sdk/cmdline-tools"
   unzip "$HOME/Downloads/commandlinetools-mac-*.zip" -d /tmp/android-cmdline
   mv /tmp/android-cmdline/cmdline-tools "$HOME/Library/Android/sdk/cmdline-tools/latest"
   ```
3. أضف إلى `~/.zshrc`:
   ```bash
   export ANDROID_HOME="$HOME/Library/Android/sdk"
   export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ```
   ثم:
   ```bash
   source ~/.zshrc
   ```
   ملاحظة: على Apple Silicon نزّل حزمة `mac` وليس `mac_arm64` إن لم توجد؛ أدوات سطر الأوامر تعمل عبر Rosetta عند الحاجة.
4. ثبّت الحزم واقبل التراخيص:
   ```bash
   sdkmanager --licenses
   sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"
   ```
5. اسحب وابنِ:
   ```bash
   cd ~/Projects
   git clone https://github.com/yswef/yousuf-toolkit-app.git
   cd yousuf-toolkit-app
   echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
   ./gradlew :app:assembleQa
   adb install -r app/build/outputs/apk/qa/app-qa.apk
   ```

### Linux (Ubuntu/Debian)

1. ثبّت الحزم:
   ```bash
   sudo apt update
   sudo apt install -y openjdk-17-jdk git unzip wget android-tools-adb
   java -version
   ```
2. نزّل Command-line tools وفك الضغط:
   ```bash
   mkdir -p "$HOME/Android/Sdk/cmdline-tools"
   unzip "$HOME/Downloads/commandlinetools-linux-*.zip" -d /tmp/android-cmdline
   mv /tmp/android-cmdline/cmdline-tools "$HOME/Android/Sdk/cmdline-tools/latest"
   ```
3. أضف إلى `~/.bashrc`:
   ```bash
   export ANDROID_HOME="$HOME/Android/Sdk"
   export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
   ```
   ثم:
   ```bash
   source ~/.bashrc
   ```
4. ثبّت الحزم:
   ```bash
   sdkmanager --licenses
   sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"
   ```
5. اسحب وابنِ:
   ```bash
   git clone https://github.com/yswef/yousuf-toolkit-app.git
   cd yousuf-toolkit-app
   echo "sdk.dir=$HOME/Android/Sdk" > local.properties
   ./gradlew :app:assembleQa
   adb install -r app/build/outputs/apk/qa/app-qa.apk
   ```
6. إن لم يتعرّف adb على الجوال، أضف قواعد udev:
   ```bash
   sudo apt install android-sdk-platform-tools-common
   ```
   ثم أعد توصيل الجوال ووافق على بصمة USB في الجوال.

## أوامر البناء المفيدة

| الأمر | الناتج | متى تستخدمه |
|---|---|---|
| `./gradlew :app:assembleDebug` | `app-debug.apk` | تجربة سريعة، معرّف منفصل `.debug` |
| `./gradlew :app:assembleQa` | `app-qa.apk` | نفس معرّف نسخة الاختبار وتوقيعها |
| `./gradlew :app:installQa` | تثبيت مباشر | الجوال موصول بـ USB أو Wi-Fi |
| `./gradlew :app:assembleRelease` | `app-release.apk` | نسخة موقعة للإصدار |
| `./gradlew :app:assembleTester` | نفس QA | توافق مع مهمة GitHub Actions |
| `./gradlew clean` | حذف مخرجات البناء | عند أخطاء كاش غريبة |

على Windows استبدل `./gradlew` بـ `.\gradlew.bat`.

`assembleQa` يستخدم مفتاح الاختبار الموجود في `ci/test-signing.p12`، وهو نفس مفتاح نسخ GitHub. لذلك تثبيت `app-qa.apk` المحلي فوق نسخة الاختبار المنزّلة يحافظ على بياناتك.

## بناء نسخة release موقعة محلياً

أنشئ keystore مرة واحدة:

```bash
keytool -genkeypair -v -keystore ~/yousuf-release.jks -alias yousuf \
  -keyalg RSA -keysize 4096 -validity 10000
```

ثم:

```bash
export KEYSTORE_PATH="$HOME/yousuf-release.jks"
export KEYSTORE_PASSWORD="كلمة السر"
export KEY_ALIAS="yousuf"
export KEY_PASSWORD="كلمة السر"
./gradlew :app:assembleRelease
```

احتفظ بالملف وكلماته خارج Git. بدونه يستخدم المشروع مفتاح الاختبار، وهذا غير صالح للنشر في المتجر.

## التثبيت اللاسلكي بدون كابل

على أندرويد 11 أو أحدث:

1. في الجوال: **الإعدادات → خيارات المطور → التصحيح اللاسلكي**.
2. اضغط **إقران الجهاز باستخدام رمز الإقران**.
3. على الكمبيوتر:
   ```bash
   adb pair 192.168.1.50:37123
   # أدخل رمز الإقران الظاهر في الجوال
   adb connect 192.168.1.50:5555
   adb devices
   ```
4. بعدها `adb install -r ...` يعمل لاسلكياً. المنفذ في `pair` يختلف عن منفذ `connect`؛ خذهما من شاشة الجوال.

## تيرمكس (Termux) — الحقيقة باختصار

بناء تطبيق أندرويد كامل داخل Termux على نفس الجوال **غير مدعوم عملياً**:

- Android Gradle Plugin يحتاج `aapt2` و`d8` المبنيين لمعمارية x86_64، وليست متوفرة لـ arm64/aarch64 في حزم SDK الرسمية.
- حتى مع `proot-distro` وتثبيت Ubuntu، ستبقى المعمارية arm64 ويفشل `aapt2` غالباً.
- لا توجد نسخة رسمية من Android Studio لأندرويد.

لذلك استخدم Termux كـ **عميل تثبيت وتحميل**، والبناء على اللابتوب:

```bash
pkg update
pkg install android-tools curl
termux-setup-storage
```

تثبيت نسخة جاهزة:

```bash
curl -L -o app-qa.apk https://github.com/yswef/yousuf-toolkit-app/releases/download/v0.10.3-test.1/app-qa.apk
adb pair 127.0.0.1:37123    # رمز الإقران من التصحيح اللاسلكي
adb connect 127.0.0.1:5555
adb install -r app-qa.apk
```

بديل عملي للجوال فقط: افتح المشروع في **GitHub Codespaces**، شغّل `./gradlew :app:assembleQa`، ثم نزّل الـ APK من شجرة الملفات وثبّته. البناء يحدث على خادم Linux كامل، وليس على الجوال.

## بناء APK وتقديمه للجوال عبر الشبكة

بدل تنزيله من GitHub كل مرة، بعد البناء المحلي:

```bash
cd app/build/outputs/apk/qa
python3 -m http.server 8000
```

ثم افتح على الجوال `http://IP-اللابتوب:8000/app-qa.apk`. يجب أن يكون الجوال واللابتوب على نفس الشبكة.

## حل المشاكل الشائعة

| المشكلة | الحل |
|---|---|
| `SDK location not found` | أنشئ `local.properties` فيه `sdk.dir=...` أو اضبط `ANDROID_HOME` |
| `Unsupported class file major version` | استخدم JDK 17 وليس 21 أو 24: `java -version` |
| `Failed to install ... INSTALL_FAILED_UPDATE_INCOMPATIBLE` | النسخة الحالية بتوقيع مختلف. احذفها مرة واحدة: `adb uninstall com.yousuf.toolkit` |
| `aapt2 ... not found` | ثبّت `build-tools;36.0.0` عبر `sdkmanager` |
| البناء يستهلك ذاكرة كبيرة | في `gradle.properties` اجعل `-Xmx3072m` إن كان لديك 8 GB |
| `adb: no devices` | فعّل خيارات المطور وتصحيح USB، ووافق على البصمة |
| أول بناء بطيء جداً | طبيعي: تنزيل Gradle والاعتماديات. البناء الثاني أسرع |
| `Could not resolve ...` | تحقق من الإنترنت ثم `./gradlew --refresh-dependencies` |

## فحص سريع بعد التثبيت

```bash
adb shell dumpsys package com.yousuf.toolkit | grep versionName
adb logcat -s AndroidRuntime | grep -i yousuf
```

الأولى تعرض النسخة المثبتة، والثانية تعرض أي انهيار أثناء التشغيل.

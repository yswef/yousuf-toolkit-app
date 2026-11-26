<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# التخصيص والترخيص

- `Skin.kt` يعرّف `Accent`: violet وgold وteal وrose بألوان فاتحة وداكنة.
- `AppTheme` يستقبل `accent` وينسخ المخطط الأساسي بـ`primary` و`inversePrimary`.
- `SkinScreen` يحفظ اللون والرمز، و`MainActivity` يقرأهما عند الرسم.
- `LicenseScreen` يرسل الكود إلى `Api.activateLicense` ويحفظ `premium` و`licenseCode`.
- `DevmodeScreen` يعرض الإصدار والخادم والحساب والتقدم مع نسخ النص.

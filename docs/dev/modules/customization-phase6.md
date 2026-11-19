# التخصيص والترخيص

- `Skin.kt` يعرّف `Accent`: violet وgold وteal وrose بألوان فاتحة وداكنة.
- `AppTheme` يستقبل `accent` وينسخ المخطط الأساسي بـ`primary` و`inversePrimary`.
- `SkinScreen` يحفظ اللون والرمز، و`MainActivity` يقرأهما عند الرسم.
- `LicenseScreen` يرسل الكود إلى `Api.activateLicense` ويحفظ `premium` و`licenseCode`.
- `DevmodeScreen` يعرض الإصدار والخادم والحساب والتقدم مع نسخ النص.

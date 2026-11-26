<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# الرسائل والفحص الذاتي

- `SmsReader` يقرأ صندوق الرسائل بعد صلاحية `READ_SMS`، ويستخرج المبلغ والرصيد والمرسل.
- يقبل صيغاً عربية مثل `اضيف 1500 ري` و`رص:1500`، ويكوّن مفتاحاً فريداً لكل رسالة.
- `Store.importSms` يضيف الحركات مرة واحدة ويحفظ المفاتيح في `smsSeen`.
- `TestsScreen` فحوص محلية: الإعدادات، التخزين، الحساب، الخادم، الصلاحيات، الترخيص.

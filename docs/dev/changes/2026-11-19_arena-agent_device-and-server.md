<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# تغيير: أدوات الجهاز + الخادم والمجتمع

- الموديل: arena-agent
- الميزة: المرحلة 4 (أدوات الجهاز) والمرحلة 5 (الخادم والمجتمع)
- التاريخ: 2026-11-19
- الفرع: arena/a5f4b6ac-yousuf-toolkit-app من dev

## ما تغيّر

- صلاحيات الشبكة والاستخدام في البيان، وخدمة إمكانية الوصول للحاجب، وخدمة التقاط الإشعارات.
- `Store` صار يحمل المحافظ والإشعارات والخزنة والحاجب والنطاقات وحساب الخادم، مع حفظها في JSON نفسه.
- الخزنة تشفّر محلياً بـAES-GCM عبر Android Keystore.
- صفحة الجهاز بستة تبويبات: الاستخدام، الحاجب، المواقع، التنبيهات، الخزنة، المزامنة.
- محافظ كاملة الإضافة والإيداع والسحب، منظف لذاكرة التطبيق، سجل إشعارات، صفحة تحديثات تقرأ GitHub Releases.
- عميل HTTP `core/net/Api.kt` ونماذج المنتدى والمتجر والغارات والخزنة.
- منتدى حقيقي، متجر يخصم من رصيد الخادم، غارات بتحقق دوري، ومزامنة للمذكرات والحركات والكتب والمحافظ.
- خادم PHP 8.1 تحت `server/public/api` مع مخطط MySQL في `server/migrations/001_init.sql`.

## إعداد مطلوب من المالك

- `config/environments.json` ← `apiBaseUrl` (فارغ الآن، والتطبيق يبقى محلياً بدونه).
- `server/config/config.php` من المثال: `db` و`base_url` و`allowed_origins`.
- `license_secret` لم يُستخدم بعد، وضعه المالك عند تفعيل الترخيص.
- عناصر المتجر ومواسم الغارات تُضاف من SQL؛ لا بيانات تجريبية مبطّنة.

## التحقق

لا بناء محلي (لا JDK/Android SDK). يُتحقق عبر GitHub Actions بعد الوسم، ثم يدوياً على جهاز وفق `docs/site/dev/tests-data.js`.

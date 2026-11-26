<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# أدوات الجهاز

## الصفحة

`DeviceScreen` في `feature/device/DeviceScreen.kt` وتعرض ستة تبويبات. يمكن فتحها على تبويب محدد:

- `BlockerScreen` يفتح تبويب الحاجب.
- `FirewallScreen` يفتح تبويب النطاقات.
- `VaultScreen` يفتح تبويب الخزنة.

## الاستخدام

`core/device/AppUsage.kt` يقرأ `UsageStatsManager` لليوم الحالي بعد منح صلاحية الوصول من إعدادات النظام. لا تُرسل البيانات تلقائياً.

## الحاجب

- `Store.blockedApps` و`Store.blockStart/blockEnd` و`Store.blockerOn`.
- `BlockingAccessibilityService` يعيد التطبيق المحجوب للشاشة الرئيسية خلال النافذة.
- الخدمة معلنة في البيان مع `res/xml/blocker_service.xml`، ويجب تشغيلها من إعدادات إمكانية الوصول.

## النطاقات

قائمة محلية في `Store.blockedDomains`. لا يوجد تنفيذ VPN بعد؛ التطبيق الفعلي يحتاج `VpnService` وقراراً من المالك.

## الإشعارات

`NotificationCollectorService` يقرأ العنوان والنص عند وصول إشعار ويحفظ آخر 200 في `Store.notifications`. لا يلتقط إشعارات التطبيق نفسه ولا الإشعارات الدائمة.

## الخزنة

`VaultCrypto` يستخدم مفتاح AES-GCM في Android Keystore. `Store.addVault` يشفّر JSON صغيراً (عنوان ونص) ويحفظه. فك التشفير يفشل بأمان إذا تغيّر مفتاح الجهاز.

## المزامنة

`core/net/Api.kt` يتصل بخادم المستخدم. `Store.serverUrl` و`authToken` يُحفظان محلياً. بدون خادم مهيأ تعمل كل الميزات المحلية كما هي.

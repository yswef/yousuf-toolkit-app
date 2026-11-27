<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# Y- Dimensions

تطبيق أندرويد للمهام والمال والعادات والأدوات، مع خادم PHP ولوحة تحكم. الدخول أونلاين أولاً، وبعض البيانات تُحفظ محلياً.

## الملخص

- الإصدار الحالي: `0.19.0` / `code 23`.
- من لوحة التحكم: تعيين مشرفين ومطورين، إدارة حتى 3 حسابات، و18 تبويباً مع مراجعة خطط التعلم.
- في التطبيق: مذكرات غنية، استيراد خطط التعلم، مزامنة، قفل، صوت مدفوع، وموافقة حقوق قبل الدخول.
- أوسمة ملاحظات `0.19.0`: من `v0.19.0-note.01` إلى `v0.19.0-note.10`؛ و`v0.18.0-test.1` يبقى إصدار APK التجريبي المنشور.
- سياسة الإصدار: كل وسم `v*` يبني APK عبر `release.yml` ويُرفق تلقائياً معه `docs/mother-panel/y-dimensions-mother-panel.zip` كأصل في الإصدار.
- التوقيع: إن لم تُضبط أسرار `KEYSTORE_B64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` في المستودع، يُوقّع APK بمفتاح الاختبار المضمّن `ci/test-signing.p12` مع تحذير في سجل CI؛ توقيع الإنتاج يحتاج الأسرار الأربعة.

## البناء

اتبع `docs/dev/BUILD_LOCAL.md`. البناء والنشر على GitHub Releases عند دفع tag فقط.

```
git tag -a v0.19.0-note.10 -m "v0.19.0-note.10"
git push origin v0.19.0-note.10
```

## الهيكل

```
app/       تطبيق أندرويد
server/    خادم PHP + اللوحة الأساسية + ترحيلات SQL
config/    بيئات التشغيل وبيانات التطبيق
docs/plan/ ملخص الخطة وصفحة الاقتراحات والملاحظات
docs/      القواعد، البناء، الأصول، التشخيص، لوحة الأم، توثيق المستخدم
.github/   خط البناء
```

## قواعد مختصرة

اقرأ `docs/dev/RULES.md` و`handover_state.md`. قاعدة واحدة موحّدة: `users` واحدة لكل التطبيقات، ولا جدول مستخدمين خاص بتطبيق.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->

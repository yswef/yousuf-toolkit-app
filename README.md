<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# Y- Dimensions

تطبيق أندرويد للمهام والمال والعادات والأدوات، مع خادم PHP ولوحة تحكم. الدخول أونلاين أولاً، وبعض البيانات تُحفظ محلياً.

## الملخص

- الإصدار الحالي: `0.18.0` / `code 22`.
- من لوحة التحكم: تعيين مشرفين ومطورين، إدارة حتى 3 حسابات، و17 تبويباً.
- في التطبيق: تبديل حتى 3 حسابات، وضع مطور بأربعة أقسام و62 اختباراً يدوياً، وتشخيص عتاد مجهّل الهوية.
- آخر إصدار منشور: `v0.18.0-test.1` مع `app-qa.apk`.

## البناء

اتبع `docs/dev/BUILD_LOCAL.md`. البناء والنشر على GitHub Releases عند دفع tag فقط.

```
git tag -a v0.18.0-test.1 -m "v0.18.0-test.1"
git push origin v0.18.0-test.1
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

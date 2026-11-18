# تعديل

- الموديل: Claude Sonnet 5.5
- الميزة: fix launch crash
- التاريخ: 2026-11-18
- الملفات المتأثرة: app/src/main/kotlin/MainActivity.kt، app/src/main/AndroidManifest.xml

## ما تم

التطبيق كان يتوقف فور الفتح. السبب: MainActivity بلا package (الحزمة الافتراضية)، والـ manifest يكتب الاسم بلا نقطة فيُحلّ إلى com.yousuf.toolkit.MainActivity فلا يجده النظام (ClassNotFoundException).

الإصلاح: إضافة `package com.yousuf.toolkit` إلى MainActivity.kt، وتغيير الاسم في الـ manifest إلى `.MainActivity`.

لم يُفحص على جهاز: البناء والتشغيل يحتاجان tag اختبار جديد.

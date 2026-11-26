# النشر على هوستنجر

النطاق: `https://yousef.romoozbasmah.com`

1. من hPanel فعّل PHP 8.1 أو أحدث، وأنشئ قاعدة MySQL ومستخدماً لها.
2. ارفع محتويات `server/public/` **كاملة** إلى `public_html/` حتى تصبح `public_html/api/auth.php` و`public_html/admin/index.php` و`public_html/panel/index.php`. المجلد الواحد يحتوي الـ API واللوحة الأساسية واللوحة التابعة؛ **اللوحة الأساسية هي `panel/`** (واجهة + أوامر + تطبيقات + كتب + صورة التطبيق). لا ترفع `boards/y-dimensions/` وحدها وتنتظر أن تعمل الحقول: تلك قوالب إعلانية تحلّها لوحة الأم فقط.
3. ارفع `server/src/` و`server/config/` و`server/migrations/` إلى مجلد النطاق خارج `public_html`:
   `domains/yousef.romoozbasmah.com/src` و`config` و`migrations`.
4. انسخ `config/config.example.php` إلى `config/config.php` واملأ `db` و`base_url` و`license_secret`.
5. استورد `migrations/001_init.sql` من phpMyAdmin.
6. فعّل SSL المجاني، ثم افتح `https://yousef.romoozbasmah.com/` وتحقق من `ok=true`، وافتح `https://yousef.romoozbasmah.com/panel/` للوحة الأساسية، و`.../admin/` للوحة التفصيلية (نفس `admin_token`).
7. أضف عناصر `items` ومواسم `raid_seasons` من phpMyAdmin.
8. لتوليد كود ترخيص: `php tools/code.php <user_id>` بعد وضع `license_secret` في `config.php`.

لا ترفع `config.php` إلى git. التطبيق يقرأ العنوان من `config/environments.json` وهو مضبوط على هذا النطاق؛ ويمكن ضبطه من شاشة الحساب ← «عنوان API للخادم»، أو استعمال `https://النطاق/panel` لأن `panel/api/*.php` تحوّل الطلبات إلى `api/*.php`.

قبل الرفع، بعد أي تعديل على `config/app.json` أو `Books.kt` أو الإصدار: `python3 server/tools/build_panel_data.py` يعيد توليد بيانات اللوحة الأساسية (24 كتاباً، بيانات التطبيق، الأيقونتان). شرح اللوحة: `public/panel/README.md`.

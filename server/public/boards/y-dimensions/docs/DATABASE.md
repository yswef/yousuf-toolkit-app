<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions (بداية الملف) -->
# قاعدة البيانات الموحّدة — Y- Dimensions

قاعدة واحدة موحّدة تديرها لوحة الأم لكل تطبيقات المطوّر يوسف الحمزي. هذه اللوحة لا تنشئ ولا تعدّل ولا تكرّر أي جدول، ولا تتصل مباشرة بالقاعدة: كل شيء عبر `mother_panel.db.shared` باستعلامات محضّرة ومقيّدة بـ `app_id = :app_id` و`app_id = y-dimensions`. الطوابع الزمنية `BIGINT` بثواني Unix UTC. الكتابات غير قابلة للتكرار عبر `sync_log.request_id`. لا تعارضات ولا تضاربات.

## جدول المستخدمون الموحّد
| العنصر | القيمة |
|---|---|
| الاسم | `users` |
| الحالة | موحّد لكل تطبيقات المطوّر، موجود مسبقاً في لوحة الأم |
| الإنشاء من هذه اللوحة | لا |
| جدول مستخدمين لكل تطبيق | لا |
| المتغيّرات المطلوبة لهذا التطبيق فقط | `id` INT UNSIGNED، `app_id` VARCHAR(64)، `email` VARCHAR(190)، `password_hash` VARCHAR(255)، `created_at` BIGINT |
| الفصل | `auto_propagate_to_other_apps = false`؛ حساب جديد في كل تطبيق؛ المفتاح الفريد `(app_id, email)`؛ نفس البريد مسموح به في أكثر من تطبيق |

> من يسجّل في تطبيق ليس له حساب في التطبيقات الأخرى تلقائياً؛ ينشئ حساباً جديداً في كل تطبيق، لكن بنفس البريد الإلكتروني المسموح به.

## الجداول الموجودة وأنواع المتغيّرات
كل الجداول الـ24 المذكورة في بيانات التطبيق، مع أنواع المتغيّرات التي **يحتاجها ويستخدمها هذا التطبيق فقط**؛ الجداول غير المستخدمة تُذكر بلا متغيّرات.

| الجدول | يستخدمه هذا التطبيق | المتغيّرات وأنواعها | الاستخدام |
|---|---|---|---|
| `admins` | نعم | `id` INT UNSIGNED, `email` VARCHAR(190), `role` VARCHAR(40), `app_id` VARCHAR(64) | التحقق من صلاحية مدير هذه اللوحة عبر لوحة الأم |
| `app_files` | نعم | `id` BIGINT UNSIGNED, `app_id` VARCHAR(64), `path` VARCHAR(255), `order_no` INT UNSIGNED, `sha256` CHAR(64) | حفظ مسارات ملفات اللوحة بالترتيب الصحيح من MAP.json |
| `app_keys` | نعم | `id` BIGINT UNSIGNED, `app_id` VARCHAR(64), `key_name` VARCHAR(80), `key_hash` CHAR(64) | المفاتيح والرموز مملوكة للوحة الأم وحدها؛ اللوحة لا تخزّن أي قيمة سرية |
| `apps` | نعم | `id` INT UNSIGNED, `app_id` VARCHAR(64), `name` VARCHAR(120), `type` VARCHAR(40), `developer` VARCHAR(120), `icon_path` VARCHAR(255), `url` VARCHAR(255), `version` VARCHAR(20), `active` TINYINT(1) | بطاقة التطبيق في لوحة الأم وقسم «التطبيقات الأخرى» |
| `audit_log` | نعم | `id` BIGINT UNSIGNED, `app_id` VARCHAR(64), `action` VARCHAR(60), `actor` VARCHAR(120), `detail` TEXT, `created_at` BIGINT | تسجيل إنشاء/إلغاء/تفعيل التراخيص وإجراءات اللوحة |
| `board_comments` | نعم | `id` BIGINT UNSIGNED, `app_id` VARCHAR(64), `user_id` INT UNSIGNED, `post_id` BIGINT UNSIGNED, `body` TEXT, `rating` TINYINT UNSIGNED, `created_at` BIGINT | تعليقات وتقييمات العملاء في المدونة الموحّدة |
| `board_files` | نعم | `id` BIGINT UNSIGNED, `board_id` VARCHAR(64), `path` VARCHAR(255), `order_no` INT UNSIGNED, `sha256` CHAR(64) | سجل الملفات المثبّتة لمراجعة ترتيب التثبيت والتحقق |
| `board_licenses` | نعم | `id` BIGINT UNSIGNED, `app_id` VARCHAR(64), `license_key_hash` CHAR(64), `status` VARCHAR(20), `user_id` INT UNSIGNED, `created_at` BIGINT, `activated_at` BIGINT, `revoked_at` BIGINT | المصدر الوحيد لتراخيص التطبيقات المدفوعة: إنشاء وإلغاء وتفعيل |
| `board_stats` | نعم | `board_id` VARCHAR(64), `app_id` VARCHAR(64), `users_count` INT UNSIGNED, `downloads_count` INT UNSIGNED, `licenses_count` INT UNSIGNED, `ratings_count` INT UNSIGNED, `rating_avg` DECIMAL(3,2), `visits_count` INT UNSIGNED, `updated_at` BIGINT | إحصائيات الرئيسية وبطاقات «لوحات الإدارة» لكل تطبيق على حدة |
| `boards` | نعم | `id` INT UNSIGNED, `board_id` VARCHAR(64), `app_id` VARCHAR(64), `name` VARCHAR(120), `icon_path` VARCHAR(255), `active` TINYINT(1) | قائمة اللوحات في قسم «لوحات الإدارة» |
| `cvs` | لا | — | موجود في قاعدة لوحة الأم ولا يحتاجه ولا يستخدمه Y- Dimensions |
| `docs` | نعم | `id` BIGINT UNSIGNED, `app_id` VARCHAR(64), `path` VARCHAR(255), `title` VARCHAR(160), `body` TEXT, `updated_at` BIGINT | توثيق هذا التطبيق المعروض في اللوحة |
| `downloads` | نعم | `id` BIGINT UNSIGNED, `app_id` VARCHAR(64), `user_id` INT UNSIGNED, `version` VARCHAR(20), `platform` VARCHAR(40), `created_at` BIGINT | إحصائية التنزيلات في الرئيسية |
| `items` | لا | — | موجود في قاعدة لوحة الأم ولا يحتاجه ولا يستخدمه Y- Dimensions |
| `messages` | لا | — | موجود في قاعدة لوحة الأم ولا يحتاجه ولا يستخدمه Y- Dimensions |
| `pages` | لا | — | موجود في قاعدة لوحة الأم ولا يحتاجه ولا يستخدمه Y- Dimensions |
| `posts` | نعم | `id` BIGINT UNSIGNED, `app_id` VARCHAR(64), `title` VARCHAR(160), `body` TEXT, `author` VARCHAR(120), `created_at` BIGINT | مقالات المدونة الموحّدة لآراء وتقييمات العملاء عن التطبيقات |
| `projects` | لا | — | موجود في قاعدة لوحة الأم ولا يحتاجه ولا يستخدمه Y- Dimensions |
| `rate_limits` | لا | — | تفرضه لوحة الأم على كل اللوحات؛ لا يقرأه هذا التطبيق مباشرة |
| `settings` | نعم | `app_id` VARCHAR(64), `setting_key` VARCHAR(80), `setting_value` TEXT, `updated_at` BIGINT | إعدادات هذه اللوحة تُقرأ وتُحفظ عبر لوحة الأم فقط |
| `site_pages` | لا | — | موجود في قاعدة لوحة الأم ولا يحتاجه ولا يستخدمه Y- Dimensions |
| `sites` | لا | — | موجود في قاعدة لوحة الأم ولا يحتاجه ولا يستخدمه Y- Dimensions |
| `sync_log` | نعم | `id` BIGINT UNSIGNED, `app_id` VARCHAR(64), `board_id` VARCHAR(64), `action` VARCHAR(60), `request_id` CHAR(64), `status` VARCHAR(20), `created_at` BIGINT | سجل المزامنة والتحقق ومنع تكرار الكتابات |
| `visits` | نعم | `id` BIGINT UNSIGNED, `app_id` VARCHAR(64), `user_id` INT UNSIGNED, `created_at` BIGINT | إحصائية الزيارات في الرئيسية |

## المدونة الموحّدة
مدونة واحدة في اللوحة تجمع `posts` و`board_comments` (ومتوسط التقييم من `board_stats`)، محتواها آراء وتقييمات العملاء عن التطبيقات. لا مدونة مستقلة لكل تطبيق، ولا جدول تعليقات منفصل لهذه اللوحة.

## التراخيص
`board_licenses` هو المصدر الوحيد، و`app_keys` تحمل المفاتيح في لوحة الأم وحدها. الإنشاء والإلغاء والتفعيل عبر `mother_panel.licenses.*` مع `audit_log` و`sync_log`. لا توليد ولا سرّ محلي.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions (نهاية الملف) -->

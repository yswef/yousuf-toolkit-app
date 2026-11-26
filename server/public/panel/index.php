<?php

declare(strict_types=1);

/*
 * Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
 * اللوحة الأساسية لتطبيق Y- Dimensions (موبايل).
 * مجلد واحد يحتوي: الواجهة + نقاط API + الأيقونة + بيانات التعبئة التلقائية + تنفيذ الأوامر.
 * ارفع محتويات server/public/ كاملة إلى public_html/، ثم server/src وserver/config وserver/migrations خارجه.
 * الجلسة مشتركة مع public/admin/ (نفس admin_token)، أو سجّل الدخول من هنا مباشرة.
 */

$panelDir = __DIR__;
$serverRoot = rtrim((string) (getenv('YD_SERVER_ROOT') ?: dirname(__DIR__, 2)), '/\\');
$settings = is_file($panelDir . '/settings.php') ? (array) require $panelDir . '/settings.php' : [];
if (!empty($settings['server_root'])) {
    $serverRoot = rtrim((string) $settings['server_root'], '/\\');
}
$configFile = $serverRoot . '/config/config.php';
$config = is_file($configFile) ? (array) require $configFile : [];
$adminToken = (string) ($config['admin_token'] ?? '');
$licenseSecret = (string) ($config['license_secret'] ?? '');

date_default_timezone_set('UTC');
$previewFrame = getenv('YD_PANEL_PREVIEW') === '1';
if (!$previewFrame) header('X-Frame-Options: DENY');
header('X-Content-Type-Options: nosniff');
header('Referrer-Policy: no-referrer');
$csp = "default-src 'self'; style-src 'unsafe-inline'; script-src 'unsafe-inline'; img-src 'self' data:";
if ($previewFrame) $csp .= "; frame-ancestors *";
header('Content-Security-Policy: ' . $csp);
header('Cache-Control: no-store');

$https = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') || (($_SERVER['HTTP_X_FORWARDED_PROTO'] ?? '') === 'https');
session_name('yd_admin');
session_set_cookie_params(['lifetime' => 0, 'path' => '/', 'secure' => $https, 'httponly' => true, 'samesite' => 'Strict']);
session_start();

function h($v): string { return htmlspecialchars((string) $v, ENT_QUOTES, 'UTF-8'); }
function num($v): string { return number_format((int) $v); }
function ago($ts): string {
    $ts = (int) $ts;
    if ($ts <= 0) return '—';
    return date('Y-m-d H:i', $ts);
}

$self = (string) strtok((string) ($_SERVER['REQUEST_URI'] ?? 'index.php'), '?');
$ip = (string) ($_SERVER['REMOTE_ADDR'] ?? '');

// ---------- login / accounts (max 3) ----------
$loginError = '';
$readOnly = ($adminToken === '' || strlen($adminToken) < 12);
$maxAccounts = 3;
if (!isset($_SESSION['accounts']) || !is_array($_SESSION['accounts'])) $_SESSION['accounts'] = [];

function panel_admin_label(string $token): ?string {
    global $config;
    $c = $config['db'] ?? [];
    if (($c['host'] ?? '') === '' || ($c['name'] ?? '') === '') return null;
    try {
        $pdo = new PDO(
            'mysql:host=' . $c['host'] . ';dbname=' . $c['name'] . ';charset=utf8mb4',
            (string) ($c['user'] ?? ''), (string) ($c['pass'] ?? ''),
            [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC]
        );
        $s = $pdo->prepare('SELECT label FROM panel_admins WHERE token_hash = ? AND active = 1 LIMIT 1');
        $s->execute([hash('sha256', $token)]);
        $row = $s->fetch();
        return $row ? (string) $row['label'] : null;
    } catch (Throwable $e) {
        return null;
    }
}

if (($_GET['logout'] ?? '') === '1') {
    $active = (string) ($_SESSION['active'] ?? '');
    $_SESSION['accounts'] = array_values(array_filter(
        $_SESSION['accounts'],
        function ($a) use ($active) { return (string) ($a['id'] ?? '') !== $active; }
    ));
    if ($_SESSION['accounts']) {
        $_SESSION['active'] = (string) $_SESSION['accounts'][0]['id'];
        $_SESSION['at'] = time();
    } else {
        $_SESSION = [];
        session_destroy();
    }
    header('Location: ' . $self);
    exit;
}
if (($_GET['use'] ?? '') !== '' && !empty($_SESSION['ok'])) {
    $want = (string) $_GET['use'];
    foreach ($_SESSION['accounts'] as $a) {
        if ((string) ($a['id'] ?? '') === $want) {
            $_SESSION['active'] = $want;
            $_SESSION['at'] = time();
            break;
        }
    }
    header('Location: ' . $self);
    exit;
}
$forceLogin = (($_GET['add'] ?? '') === '1');
if ($readOnly && empty($_SESSION['ok'])) {
    // config.php غير مضبوط: اعرض اللوحة بالبيانات المحلية بلا دخول، وامنع تنفيذ الأوامر.
    $_SESSION['ok'] = true;
    $_SESSION['csrf'] = bin2hex(random_bytes(16));
    $_SESSION['at'] = time();
    $_SESSION['accounts'] = [['id' => 'preview', 'label' => 'معاينة للقراءة فقط', 'hash' => '']];
    $_SESSION['active'] = 'preview';
}
if (empty($_SESSION['ok']) || $forceLogin) {
    if ($adminToken === '' || strlen($adminToken) < 12) {
        $loginError = 'config/config.php غير موجود أو admin_token غير مضبوط.';
    } elseif (($_SERVER['REQUEST_METHOD'] ?? '') === 'POST' && !isset($_POST['cmd']) && !isset($_POST['req_index'])) {
        $lock = sys_get_temp_dir() . '/yd_panel_' . md5($ip) . '.json';
        $st = is_file($lock) ? (json_decode((string) file_get_contents($lock), true) ?: []) : [];
        $fails = (int) ($st['n'] ?? 0);
        $until = (int) ($st['until'] ?? 0);
        if ($until > time()) {
            $loginError = 'محاولات كثيرة. انتظر ' . ($until - time()) . ' ثانية.';
        } else {
            $code = (string) ($_POST['code'] ?? '');
            $givenLabel = trim((string) ($_POST['label'] ?? ''));
            $label = null;
            if ($adminToken !== '' && hash_equals($adminToken, $code)) {
                $label = $givenLabel !== '' ? $givenLabel : 'المشرف الأساسي';
            } else {
                $found = panel_admin_label($code);
                if ($found !== null) $label = $found;
            }
            if ($label === null) {
                $fails++;
                @file_put_contents($lock, json_encode(['n' => $fails, 'until' => $fails >= 5 ? time() + 60 : 0]));
                usleep(300000);
                $loginError = 'الرمز غير صحيح.';
            } else {
                @unlink($lock);
                $id = substr(hash('sha256', $code), 0, 12);
                $entry = ['id' => $id, 'label' => mb_substr($label, 0, 80), 'hash' => hash('sha256', $code)];
                $accounts = $_SESSION['accounts'];
                $replaced = false;
                foreach ($accounts as $i => $a) {
                    if ((string) ($a['id'] ?? '') === $id) { $accounts[$i] = $entry; $replaced = true; break; }
                }
                if (!$replaced) {
                    if (count($accounts) >= $maxAccounts) {
                        $loginError = 'الحد الأقصى ثلاثة حسابات. احذف حساباً من تبويب «المشرفون والمطورون» أولاً.';
                    } else {
                        $accounts[] = $entry;
                    }
                }
                if ($loginError === '') {
                    $_SESSION['accounts'] = array_values($accounts);
                    $_SESSION['active'] = $id;
                    if (empty($_SESSION['ok'])) session_regenerate_id(true);
                    $_SESSION['ok'] = true;
                    if (empty($_SESSION['csrf'])) $_SESSION['csrf'] = bin2hex(random_bytes(16));
                    $_SESSION['at'] = time();
                    header('Location: ' . $self);
                    exit;
                }
            }
        }
    }
    if ($forceLogin || empty($_SESSION['ok'])) {
        ?><!doctype html><html lang="ar" dir="rtl"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>اللوحة الأساسية — Y- Dimensions</title>
<style>:root{--bg:#12111a;--fg:#f0edfa;--card:#1d1b29;--pri:#8b6cf5;--bd:#3a3650}body{margin:0;min-height:100vh;display:grid;place-items:center;background:var(--bg);color:var(--fg);font-family:system-ui,"Segoe UI",Tahoma,sans-serif}form{background:var(--card);border:1px solid var(--bd);border-radius:20px;padding:28px;width:min(92vw,380px)}img{width:64px;height:64px;border-radius:16px}h1{font-size:19px;margin:12px 0 6px}p{font-size:13px;opacity:.7;margin:0 0 16px}input,button{width:100%;box-sizing:border-box;padding:12px;border-radius:12px;border:1px solid var(--bd);font:inherit;background:transparent;color:inherit;margin-bottom:10px}button{background:var(--pri);color:#fff;border:0;cursor:pointer;font-weight:700}.e{color:#ff8a94;font-size:13px;margin-bottom:8px}.acc{font-size:12px;opacity:.7;margin-bottom:10px}</style></head><body>
<form method="post"><img src="assets/icon.svg" alt="أيقونة التطبيق"><h1>اللوحة الأساسية — Y- Dimensions</h1><p>دخول بحساب مشرف · الحد الأقصى 3 حسابات محفوظة</p>
<div class="acc">الحسابات المحفوظة: <?= count($_SESSION['accounts']) ?> / <?= $maxAccounts ?> — <?php foreach ($_SESSION['accounts'] as $a) echo h($a['label']) . ' · '; ?></div>
<?php if ($loginError !== '') echo '<div class="e">' . h($loginError) . '</div>'; ?>
<input name="label" placeholder="اسم الحساب (اختياري)" autocomplete="off">
<input type="password" name="code" placeholder="رمز الدخول" autocomplete="current-password" autofocus><button>دخول</button>
<a href="<?= h($self) ?>" style="display:block;text-align:center;color:var(--pri);font-size:13px;text-decoration:none">إلغاء والعودة</a></form></body></html><?php
        exit;
    }
}
if (time() - (int) ($_SESSION['at'] ?? 0) > 8 * 3600) {
    $_SESSION = [];
    session_destroy();
    header('Location: ' . $self);
    exit;
}
$csrf = (string) ($_SESSION['csrf'] ?? '');

// ---------- database ----------
$pdo = null;
$dbError = '';
try {
    $c = $config['db'] ?? [];
    if (($c['host'] ?? '') === '' || ($c['name'] ?? '') === '') {
        throw new RuntimeException('بيانات الاتصال ناقصة في config.php');
    }
    $pdo = new PDO(
        'mysql:host=' . $c['host'] . ';dbname=' . $c['name'] . ';charset=utf8mb4',
        (string) ($c['user'] ?? ''), (string) ($c['pass'] ?? ''),
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC, PDO::ATTR_EMULATE_PREPARES => false]
    );
} catch (Throwable $e) {
    $dbError = $e->getMessage();
}
function q(string $sql, array $p = []): array {
    global $pdo;
    if ($pdo === null) return [];
    try { $s = $pdo->prepare($sql); $s->execute($p); return $s->fetchAll(); } catch (Throwable $e) { return []; }
}
function one(string $sql, array $p = []) {
    global $pdo;
    if ($pdo === null) return 0;
    try { $s = $pdo->prepare($sql); $s->execute($p); return $s->fetchColumn(); } catch (Throwable $e) { return 0; }
}
function run(string $sql, array $p = []): int {
    global $pdo;
    if ($pdo === null) return 0;
    try { $s = $pdo->prepare($sql); $s->execute($p); return $s->rowCount(); } catch (Throwable $e) { return 0; }
}
function logit(string $action, string $detail = ''): void {
    global $ip;
    run('INSERT INTO admin_log (action, detail, ip, created_at) VALUES (?,?,?,?)', [$action, mb_substr($detail, 0, 400), $ip, time()]);
}

// ---------- auto-filled data sources (no {{placeholders}}) ----------
function readJson(string $path): array {
    if (!is_file($path)) return [];
    $data = json_decode((string) file_get_contents($path), true);
    return is_array($data) ? $data : [];
}
$app = readJson($panelDir . '/data/app.json');
$env = readJson($panelDir . '/data/environments.json');
$catalog = readJson($panelDir . '/data/books.json');
$appInfo = $app['app'] ?? [];
$sisters = $app['sister_apps'] ?? [];
$bookSections = $catalog['sections'] ?? [];
$apiBase = rtrim((string) ($env['apiBaseUrl'] ?? ''), '/');
$publicRoot = dirname(__DIR__);
$apiDir = $publicRoot . '/api';

$endpoints = [
    ['auth.php?action=register', 'POST', 'إنشاء حساب جديد للتطبيق', ['name', 'email', 'password']],
    ['auth.php?action=login', 'POST', 'تسجيل الدخول وإصدار توكن', ['email', 'password']],
    ['auth.php?action=logout', 'POST', 'إنهاء الجلسة', ['Authorization']],
    ['auth.php?action=forgot', 'POST', 'استعادة كلمة المرور', ['email']],
    ['sync.php', 'POST', 'مزامنة بيانات الجهاز', ['Authorization']],
    ['forum.php?action=threads', 'GET', 'مواضيع المنتدى', ['Authorization']],
    ['forum.php?action=new_post', 'POST', 'رد جديد', ['Authorization']],
    ['shop.php?action=list', 'GET', 'عناصر المتجر', ['Authorization']],
    ['shop.php?action=buy', 'POST', 'شراء عنصر', ['Authorization']],
    ['raids.php?action=info', 'GET', 'موسم الحملة', ['Authorization']],
    ['raids.php?action=checkin', 'POST', 'تسجيل حضور', ['Authorization']],
    ['vault.php', 'GET|POST|DELETE', 'الخزنة المشفّرة', ['Authorization']],
    ['license.php', 'POST', 'تفعيل رمز ترخيص', ['Authorization', 'code']],
    ['announcements.php', 'GET', 'الإعلانات النشطة', []],
    ['devices.php?action=list', 'GET', 'الأجهزة المسجّلة', ['Authorization']],
    ['devices.php?action=revoke', 'POST', 'تسجيل خروج جهاز عن بُعد', ['Authorization']],
    ['devices.php?action=ping', 'POST', 'تحديث آخر ظهور', ['Authorization']],
    ['content.php?action=manifest', 'GET', 'بيان التحديثات الفرعية (بيانات فقط)', []],
    ['content.php?action=file', 'GET', 'تنزيل ملف بيانات متغيّر', ['name']],
    ['requirements.php?action=list', 'GET', 'متطلبات التطبيقات ومصادر الأصول', []],
    ['requirements.php?action=mark', 'POST', 'التأشير على بند تم توفيره', ['X-Admin-Token']],
    ['devmode.php?action=status', 'GET', 'صلاحية المطور وكتالوج الاختبارات', ['Authorization']],
    ['devmode.php?action=tests', 'GET', 'اختبارات وضع المطور المفعّلة', ['Authorization']],
    ['diagnostics.php?action=match', 'GET|POST', 'حل معروف لتوقيع مشكلة', ['Authorization']],
    ['diagnostics.php?action=report', 'POST', 'بلاغ تشخيص مجهّل الهوية', ['Authorization']],
    ['diagnostics.php?action=list', 'GET', 'بلاغات التشخيص للوحة', ['X-Admin-Token']],
];
$tableNames = ['users', 'tokens', 'profiles', 'journal', 'ledger', 'books', 'wallets', 'threads', 'posts',
               'items', 'purchases', 'raid_seasons', 'raid_members', 'licenses', 'vault_items', 'announcements', 'admin_log'];

$commands = [
    'health.check' => 'فحص config.php وقاعدة البيانات وكل ملفات API',
    'stats.refresh' => 'إعادة حساب كل الإحصائيات من الجداول',
    'api.selftest' => 'التحقق من وجود نقاط API داخل المجلد',
    'apps.reload' => 'إعادة قراءة بيانات التطبيق من data/app.json',
    'books.list' => 'عرض كل الكتب السحرية من data/books.json',
    'tokens.purge' => 'حذف جلسات أقدم من 90 يوماً',
    'user.logout' => 'إنهاء جلسات مستخدم (المعامل: user_id)',
    'license.create' => 'إنشاء رمز ترخيص (المعامل: user_id)',
    'license.revoke' => 'إلغاء ترخيص مستخدم (المعامل: user_id)',
    'logs.tail' => 'آخر 20 إجراءً من سجل اللوحة',
    'requirements.list' => 'عرض نواقص التطبيقات مرتّبة (الناقص أولاً)',
    'content.manifest' => 'عرض حزم التحديثات الفرعية وبصماتها',
    'devices.list' => 'عرض الأجهزة المسجّلة وآخر ظهور',
    'releases.list' => 'عرض الإصدارات المسجّلة',
    'devmode.tests' => 'عدد اختبارات وضع المطور المفعّلة',
    'diagnostics.reports' => 'آخر بلاغات تشخيص العتاد',
    'accounts.max' => 'عرض الحد الأقصى للحسابات (3)',
];

function endpointFile(string $target): string {
    $file = explode('?', $target)[0];
    return $file;
}
function tableCount(string $name): int {
    return (int) one('SELECT COUNT(*) FROM `' . $name . '`');
}
function stats(): array {
    $now = time();
    return [
        'المستخدمون' => tableCount('users'),
        'جدد اليوم' => (int) one('SELECT COUNT(*) FROM users WHERE created_at > ?', [$now - 86400]),
        'جلسات نشطة' => (int) one('SELECT COUNT(*) FROM tokens WHERE last_used > ?', [$now - 86400]),
        'تراخيص' => tableCount('licenses'),
        'عناصر المتجر' => tableCount('items'),
        'مشتريات' => tableCount('purchases'),
        'مواضيع' => tableCount('threads'),
        'ردود وآراء' => tableCount('posts'),
        'كتب قيد القراءة' => tableCount('books'),
        'محافظ' => tableCount('wallets'),
        'خزنة' => tableCount('vault_items'),
        'إعلانات' => tableCount('announcements'),
        'سجل الأوامر' => tableCount('admin_log'),
    ];
}
function runCommand(string $cmd, string $arg): string {
    global $commands, $pdo, $dbError, $endpoints, $apiDir, $appInfo, $catalog, $licenseSecret, $configFile, $serverRoot, $adminToken, $apiBase, $sisters, $panelDir;
    if (!isset($commands[$cmd])) return 'أمر غير مسموح. استخدم القائمة فقط؛ لا يُنفَّذ SQL أو أوامر shell حرة.';
    $out = [];
    switch ($cmd) {
        case 'health.check':
            $out[] = 'config.php: ' . (is_file($configFile) ? 'موجود' : 'مفقود');
            $out[] = 'admin_token: ' . ($adminToken !== '' ? 'مضبوط' : 'غير مضبوط');
            $out[] = 'license_secret: ' . ($licenseSecret !== '' ? 'مضبوط' : 'غير مضبوط');
            $out[] = 'قاعدة البيانات: ' . ($pdo === null ? 'غير متصلة (' . $dbError . ')' : 'متصلة');
            $out[] = 'PHP: ' . PHP_VERSION;
            $out[] = 'جذر الخادم: ' . $serverRoot;
            break;
        case 'stats.refresh':
            foreach (stats() as $k => $v) $out[] = $k . ': ' . num($v);
            break;
        case 'api.selftest':
            foreach ($endpoints as $e) {
                $f = endpointFile($e[0]);
                $out[] = ($e[0]) . ' => ' . (is_file($apiDir . '/' . $f) ? 'موجود' : 'مفقود');
            }
            break;
        case 'apps.reload':
            $out[] = 'الاسم: ' . ($appInfo['name'] ?? '—');
            $out[] = 'الإصدار: ' . ($appInfo['version'] ?? '—') . ' (code ' . ($appInfo['version_code'] ?? 0) . ')';
            $out[] = 'المطوّر: ' . ($appInfo['developer_name'] ?? '—') . ' — ' . ($appInfo['developer_email'] ?? '—');
            $out[] = 'عنوان API: ' . $apiBase;
            $out[] = 'تطبيقات أخرى: ' . count($sisters);
            break;
        case 'books.list':
            foreach (($catalog['sections'] ?? []) as $s) {
                $titles = [];
                foreach (($s['books'] ?? []) as $b) $titles[] = $b['title'] ?? $b['route'];
                $out[] = ($s['title'] ?? 'قسم') . ': ' . implode('، ', $titles);
            }
            $out[] = 'الإجمالي: ' . ($catalog['count'] ?? 0);
            break;
        case 'tokens.purge':
            $n = run('DELETE FROM tokens WHERE last_used < ?', [time() - 90 * 86400]);
            $out[] = 'حُذف ' . $n . ' رمز جلسة قديم';
            break;
        case 'user.logout':
            $uid = (int) $arg;
            if ($uid <= 0) { $out[] = 'اكتب user_id في الحقل'; break; }
            $out[] = 'أُنهيت ' . run('DELETE FROM tokens WHERE user_id = ?', [$uid]) . ' جلسة للمستخدم #' . $uid;
            break;
        case 'license.create':
            $uid = (int) $arg;
            if ($uid <= 0) { $out[] = 'اكتب user_id في الحقل'; break; }
            if ($licenseSecret === '') { $out[] = 'license_secret غير مضبوط في config.php'; break; }
            $raw = strtoupper(substr(hash_hmac('sha256', (string) $uid, $licenseSecret), 0, 16));
            $out[] = 'رمز الترخيص للمستخدم #' . $uid . ': YD-' . implode('-', str_split($raw, 4));
            break;
        case 'license.revoke':
            $uid = (int) $arg;
            if ($uid <= 0) { $out[] = 'اكتب user_id في الحقل'; break; }
            $out[] = 'أُلغي ' . run('DELETE FROM licenses WHERE user_id = ?', [$uid]) . ' ترخيص للمستخدم #' . $uid;
            break;
        case 'logs.tail':
            foreach (q('SELECT created_at, action, detail FROM admin_log ORDER BY id DESC LIMIT 20') as $r) {
                $out[] = ago($r['created_at']) . ' | ' . $r['action'] . ' | ' . $r['detail'];
            }
            if (!$out) $out[] = 'لا سجلات بعد (جدول admin_log غير موجود أو فارغ).';
            break;
        case 'requirements.list':
            $reqData = readJson($panelDir . '/data/requirements.json');
            $reqRows = $reqData['requirements'] ?? [];
            usort($reqRows, function ($a, $b) {
                return [(int) ($a['provided'] ?? 0), (int) ($a['priority'] ?? 5)]
                    <=> [(int) ($b['provided'] ?? 0), (int) ($b['priority'] ?? 5)];
            });
            foreach ($reqRows as $r) {
                $out[] = (($r['provided'] ?? false) ? '[✓] ' : '[ ] ') . ($r['app'] ?? '') . ' / ' . ($r['item'] ?? '')
                    . ' — المصدر: ' . ($r['source'] ?? '—') . ' — أولوية ' . ($r['priority'] ?? 5);
            }
            $out[] = 'الناقص: ' . count(array_filter($reqRows, function ($r) { return empty($r['provided']); }));
            break;
        case 'content.manifest':
            foreach (['app.json', 'books.json', 'environments.json', 'requirements.json', 'devmode_tests.json', 'releases.json'] as $n) {
                $f = $panelDir . '/data/' . $n;
                $out[] = $n . ' => ' . (is_file($f) ? hash_file('sha256', $f) : 'مفقود');
            }
            $out[] = 'الإصدار: ' . (is_file(dirname(__DIR__) . '/content/VERSION') ? trim((string) file_get_contents(dirname(__DIR__) . '/content/VERSION')) : '1');
            break;
        case 'devices.list':
            foreach (q('SELECT id, label, model, last_seen, revoked FROM devices ORDER BY last_seen DESC LIMIT 50') as $r) {
                $out[] = '#' . $r['id'] . ' ' . $r['label'] . ' / ' . $r['model'] . ' — ' . ago($r['last_seen']) . ($r['revoked'] ? ' (ملغي)' : '');
            }
            if (!$out) $out[] = 'لا أجهزة مسجّلة (جدول devices غير مستورد أو فارغ).';
            break;
        case 'releases.list':
            foreach (q('SELECT version, version_code, active, published_at FROM releases ORDER BY id DESC LIMIT 50') as $r) {
                $out[] = $r['version'] . ' (code ' . $r['version_code'] . ') ' . ago($r['published_at']) . ($r['active'] ? '' : ' (موقف)');
            }
            if (!$out) $out[] = 'لا إصدارات مسجّلة (جدول releases غير مستورد أو فارغ).';
            break;
        case 'devmode.tests':
            $t = readJson($panelDir . '/data/devmode_tests.json')['tests'] ?? [];
            $on = count(array_filter($t, function ($x) { return !empty($x['enabled']); }));
            $out[] = 'الاختبارات: ' . count($t) . ' | المفعّل: ' . $on . ' | المعطّل: ' . (count($t) - $on);
            $out[] = 'الحد الأقصى للحسابات: 3';
            break;
        case 'diagnostics.reports':
            foreach (q('SELECT issue_signature, device_model, created_at FROM diagnostic_reports ORDER BY id DESC LIMIT 20') as $r) {
                $out[] = ago($r['created_at']) . ' | ' . $r['issue_signature'] . ' | ' . $r['device_model'];
            }
            if (!$out) $out[] = 'لا بلاغات تشخيص (استورد 004_devmode.sql أو لا بلاغات بعد).';
            break;
        case 'accounts.max':
            $out[] = 'الحد الأقصى للحسابات المحفوظة: 3';
            $out[] = 'حسابات هذه الجلسة: ' . count($_SESSION['accounts'] ?? []);
            break;
    }
    return implode("\n", $out);
}

// ---------- actions ----------
$flash = '';
$commandOutput = '';
$generatedAdminToken = '';
$panelTestsPath = $panelDir . '/data/devmode_tests.json';
$devActions = ['dev_grant', 'dev_revoke', 'test_toggle', 'admin_add', 'admin_drop'];
if (($_SERVER['REQUEST_METHOD'] ?? '') === 'POST' && in_array((string) ($_POST['dev_action'] ?? ''), $devActions, true)) {
    if (!hash_equals($csrf, (string) ($_POST['csrf'] ?? ''))) { http_response_code(403); exit('CSRF'); }
    $devAction = (string) $_POST['dev_action'];
    if ($readOnly) {
        $flash = 'وضع القراءة فقط: اضبط config.php و admin_token واستورد 004_devmode.sql أولاً.';
    } else {
        if ($devAction === 'dev_grant') {
            $uid = (int) ($_POST['user_id'] ?? 0);
            $role = (string) ($_POST['role'] ?? 'developer');
            if (!in_array($role, ['developer', 'admin'], true)) $role = 'developer';
            if ($uid > 0) {
                run('INSERT INTO developer_accounts (app_id, user_id, role, granted_by, granted_at, active)
                     VALUES (?, ?, ?, ?, ?, 1)
                     ON DUPLICATE KEY UPDATE role = VALUES(role), granted_by = VALUES(granted_by),
                                             granted_at = VALUES(granted_at), active = 1',
                    ['y-dimensions', $uid, $role, 'panel', time()]);
                logit('developer.grant', 'user=' . $uid . ' role=' . $role);
                $flash = 'تم تعيين المستخدم #' . $uid . ' ' . ($role === 'admin' ? 'مشرفاً' : 'مطوراً') . '.';
            }
        } elseif ($devAction === 'dev_revoke') {
            $uid = (int) ($_POST['user_id'] ?? 0);
            if ($uid > 0) {
                run('UPDATE developer_accounts SET active = 0 WHERE app_id = ? AND user_id = ?', ['y-dimensions', $uid]);
                logit('developer.revoke', 'user=' . $uid);
                $flash = 'أُلغيت صلاحية المطور للمستخدم #' . $uid . '.';
            }
        } elseif ($devAction === 'test_toggle') {
            $testsData = readJson($panelTestsPath);
            $idx = (int) ($_POST['test_index'] ?? -1);
            if (isset($testsData['tests'][$idx])) {
                $testsData['tests'][$idx]['enabled'] = empty($testsData['tests'][$idx]['enabled']);
                file_put_contents($panelTestsPath,
                    json_encode($testsData, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT) . "\n", LOCK_EX);
                logit('devmode.test_toggle', 'index=' . $idx);
                $flash = 'تم تحديث حالة الاختبار.';
            }
        } elseif ($devAction === 'admin_add') {
            $label = trim((string) ($_POST['admin_label'] ?? ''));
            if ($label === '') $label = 'مشرف ' . date('Y/m/d H:i');
            $count = (int) one('SELECT COUNT(*) FROM panel_admins WHERE active = 1');
            if ($count >= 3) {
                $flash = 'الحد الأقصى ثلاثة حسابات مشرفين. عطّل واحداً أولاً.';
            } else {
                $generatedAdminToken = bin2hex(random_bytes(24));
                run('INSERT INTO panel_admins (label, token_hash, role, created_at, active) VALUES (?, ?, ?, ?, 1)',
                    [mb_substr($label, 0, 80), hash('sha256', $generatedAdminToken), 'admin', time()]);
                logit('panel.admin_add', 'label=' . $label);
                $flash = 'أُنشئ حساب مشرف جديد. انسخ رمزه الآن (يُعرض مرة واحدة).';
            }
        } elseif ($devAction === 'admin_drop') {
            $id = (int) ($_POST['admin_id'] ?? 0);
            if ($id > 0) {
                run('UPDATE panel_admins SET active = 0 WHERE id = ?', [$id]);
                logit('panel.admin_drop', 'id=' . $id);
                $flash = 'عُطّل حساب المشرف.';
            }
        }
    }
}
if (($_SERVER['REQUEST_METHOD'] ?? '') === 'POST' && isset($_POST['req_index'])) {
    if (!hash_equals($csrf, (string) ($_POST['csrf'] ?? ''))) {
        http_response_code(403);
        exit('CSRF');
    }
    if ($readOnly) {
        $flash = 'وضع القراءة فقط: لا يمكن التأشير على المتطلبات قبل ضبط config.php.';
    } else {
        $reqPath = $panelDir . '/data/requirements.json';
        $reqData = readJson($reqPath);
        $idx = (int) $_POST['req_index'];
        if (isset($reqData['requirements'][$idx])) {
            $reqData['requirements'][$idx]['provided'] = empty($reqData['requirements'][$idx]['provided']);
            file_put_contents($reqPath, json_encode($reqData, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT) . "\n", LOCK_EX);
            logit('requirements.mark', 'index=' . $idx);
            $flash = 'تم تحديث حالة المتطلب.';
        }
    }
}
if (($_SERVER['REQUEST_METHOD'] ?? '') === 'POST' && isset($_POST['cmd'])) {
    if (!hash_equals($csrf, (string) ($_POST['csrf'] ?? ''))) {
        http_response_code(403);
        exit('CSRF');
    }
    $cmd = (string) ($_POST['cmd'] ?? '');
    $arg = trim((string) ($_POST['arg'] ?? ''));
    if ($readOnly) {
        $flash = 'وضع القراءة فقط: اضبط config.php وadmin_token لتفعيل تنفيذ الأوامر.';
    } else {
        $commandOutput = runCommand($cmd, $arg);
        logit('panel.command', $cmd . ' arg=' . $arg);
        $flash = 'نُفّذ: ' . ($commands[$cmd] ?? $cmd);
    }
}

// ---------- views ----------
$tabs = [
    'home' => 'الرئيسية', 'apps' => 'التطبيقات', 'books' => 'الكتب', 'tools' => 'الأدوات والجداول',
    'users' => 'المستخدمون', 'licenses' => 'التراخيص', 'opinions' => 'الآراء والمنتدى',
    'commands' => 'تنفيذ الأوامر', 'fields' => 'الحقول التلقائية', 'api' => 'الـ API',
    'requirements' => 'متطلبات التطبيقات', 'content' => 'التحديثات الفرعية',
    'devices' => 'الأجهزة المسجّلة', 'releases' => 'الإصدارات',
    'developers' => 'المشرفون والمطورون', 'devtests' => 'اختبارات المطور',
    'diagnostics' => 'بلاغات التشخيص',
];
$page = (string) ($_GET['p'] ?? 'home');
if (!isset($tabs[$page])) $page = 'home';

function card(string $title, string $body): string {
    return '<section class="card"><h2>' . h($title) . '</h2>' . $body . '</section>';
}
function statc(string $label, $value): string {
    return '<div class="st"><b>' . h(num($value)) . '</b><span>' . h($label) . '</span></div>';
}
function table(array $headers, array $rows): string {
    if (!$rows) return '<p class="m">لا بيانات بعد.</p>';
    $o = '<div class="scroll"><table><tr>';
    foreach ($headers as $hd) $o .= '<th>' . h($hd) . '</th>';
    $o .= '</tr>';
    foreach ($rows as $r) {
        $o .= '<tr>';
        foreach ($r as $c) {
            $isRaw = is_string($c) && (str_starts_with($c, '<form') || str_starts_with($c, '<button'));
            $o .= '<td>' . ($isRaw ? $c : h($c)) . '</td>';
        }
        $o .= '</tr>';
    }
    return $o . '</table></div>';
}

ob_start();
if ($page === 'home') {
    echo '<section class="hero"><img src="assets/icon.svg" alt="أيقونة Y- Dimensions"><div><h1>' . h($appInfo['name'] ?? 'Y- Dimensions')
        . '</h1><p>' . h($appInfo['description'] ?? '') . '</p><p class="m">الإصدار ' . h($appInfo['version'] ?? '—')
        . ' · ' . h($appInfo['type'] ?? 'mobile') . ' · ' . h($appInfo['developer_name'] ?? 'يوسف الحمزي') . '</p></div></section>';
    if ($dbError !== '') {
        echo '<div class="warn">قاعدة البيانات غير متصلة: ' . h($dbError) . ' — البيانات أدناه من الملفات المحلية، وستُملأ الأرقام تلقائياً بعد ضبط config.php.</div>';
    }
    echo '<div class="grid">';
    foreach (stats() as $k => $v) echo statc($k, $v);
    echo '</div>';
    echo card('مصادر التعبئة التلقائية', '<ul><li><code>data/app.json</code> → الاسم، الإصدار، المطوّر، البريد، التطبيقات الأخرى، إعدادات اللعبة.</li>'
        . '<li><code>data/environments.json</code> → عنوان API.</li><li><code>data/books.json</code> → ' . h($catalog['count'] ?? 0) . ' كتاباً سحرياً في ' . count($bookSections) . ' أقسام (مستخرجة من كود التطبيق).</li>'
        . '<li>قاعدة البيانات → كل الأرقام والجداول أعلاه، تُقرأ عند كل فتح للصفحة.</li></ul>'
        . '<p class="m">لا توجد قيم <code>{{...}}</code> في هذه اللوحة؛ كل حقل يُقرأ ويُعرض من مصدره مباشرة.</p>');
} elseif ($page === 'apps') {
    echo '<section class="hero"><img src="assets/icon.svg" alt="أيقونة التطبيق"><div><h1>' . h($appInfo['name'] ?? 'Y- Dimensions') . '</h1>'
        . '<p class="m">بطاقة التطبيق الأساسية — تُقرأ تلقائياً من <code>data/app.json</code></p></div></section>';
    $rows = [
        ['المعرّف', $appInfo['id'] ?? 'y-dimensions'], ['الاسم', $appInfo['name'] ?? 'Y- Dimensions'],
        ['النوع', $appInfo['type'] ?? 'mobile'], ['الإصدار', $appInfo['version'] ?? '—'],
        ['versionCode', $appInfo['version_code'] ?? 0], ['المطوّر', $appInfo['developer_name'] ?? '—'],
        ['البريد', $appInfo['developer_email'] ?? '—'], ['عنوان API', $apiBase],
        ['الأيقونة', $appInfo['icon'] ?? 'assets/icon.svg'],
        ['الوسوم', implode('، ', $appInfo['tags'] ?? [])],
    ];
    echo card('بيانات التطبيق', table(['الحقل', 'القيمة'], $rows));
    echo card('الصور', '<img src="assets/icon.svg" class="ico" alt="SVG"><img src="assets/icon.png" class="ico" alt="PNG">'
        . '<p class="m">ملفان داخل المجلد: <code>assets/icon.svg</code> و<code>assets/icon.png</code> (أيقونة launcher الحقيقية).</p>');
    $sis = [];
    foreach ($sisters as $s) {
        $sis[] = [$s['name'] ?? '—', $s['url'] ?? '—', $s['icon'] ?? '—'];
    }
    echo card('التطبيقات الأخرى (' . count($sis) . ')', table(['التطبيق', 'الرابط', 'الأيقونة'], $sis)
        . (count($sis) === 0 ? '<p class="m">الحقل <code>sister_apps</code> فارغ في app.json. أضف بطاقات التطبيقات هناك لتظهر هنا تلقائياً بلا تعديل كود.</p>' : ''));
    echo card('اللوحة التابعة', '<p>مجلد <code>boards/y-dimensions/</code> داخل نفس المجلد هو لوحة إعلانية تابعة للوحة الأم (MAP.json + 13 فحصاً).</p>'
        . '<p class="m">هذه اللوحة الأساسية هي التي تحتوي الكود والـ API وتنفّذ الأوامر.</p>');
} elseif ($page === 'books') {
    echo card('الكتب السحرية (' . ($catalog['count'] ?? 0) . ')', '<p class="m">مستخرجة تلقائياً من <code>app/src/main/kotlin/feature/home/Books.kt</code> مع الترجمة العربية من موارد التطبيق؛ لا إدخال يدوي.</p>');
    foreach ($bookSections as $s) {
        $o = '';
        foreach (($s['books'] ?? []) as $b) {
            $o .= '<div class="book"><i style="background:linear-gradient(135deg,' . h($b['from'] ?? '#6c4cf1') . ',' . h($b['to'] ?? '#231a4a') . ')"></i>'
                . '<div><b>' . h($b['title'] ?? $b['route']) . '</b><br><small>' . h($b['route']) . '</small></div></div>';
        }
        echo card($s['title'] ?? 'قسم', '<div class="books">' . $o . '</div>');
    }
    echo card('كتب المستخدمين في قاعدة البيانات', table(['المستخدم', 'العنوان', 'الصفحة', 'من'], array_map(function ($r) {
        return [$r['user_id'], $r['title'], $r['page'] . '/' . $r['pages'], ago($r['created_at'] ?? 0)];
    }, q('SELECT b.user_id, b.title, b.page, b.pages, u.created_at FROM books b LEFT JOIN users u ON u.id = b.user_id ORDER BY b.id DESC LIMIT 50'))));
} elseif ($page === 'tools') {
    $eps = [];
    foreach ($endpoints as $e) {
        $f = endpointFile($e[0]);
        $eps[] = [$e[0], $e[1], $e[2], implode('، ', $e[3]), is_file($apiDir . '/' . $f) ? 'موجود' : 'مفقود'];
    }
    echo card('نقاط API داخل هذا المجلد', table(['المسار', 'الطريقة', 'الوظيفة', 'المتطلبات', 'الملف'], $eps));
    $tr = [];
    foreach ($tableNames as $t) $tr[] = [$t, tableCount($t)];
    echo card('جداول قاعدة البيانات (قاعدة واحدة موحّدة، بلا جداول جديدة)', table(['الجدول', 'الصفوف'], $tr));
} elseif ($page === 'users') {
    echo card('المستخدمون (آخر 100)', table(['#', 'الاسم', 'البريد', 'النقاط', 'المستوى', 'التسجيل'], array_map(function ($r) {
        return [$r['id'], $r['name'], $r['email'], $r['points'] ?? 0, $r['level'] ?? 1, ago($r['created_at'])];
    }, q('SELECT id, name, email, points, level, created_at FROM users ORDER BY id DESC LIMIT 100'))));
} elseif ($page === 'licenses') {
    echo card('التراخيص', table(['المستخدم', 'بصمة الرمز', 'التفعيل'], array_map(function ($r) {
        return [$r['user_id'], substr((string) $r['code_hash'], 0, 16) . '…', ago($r['activated_at'])];
    }, q('SELECT user_id, code_hash, activated_at FROM licenses ORDER BY activated_at DESC LIMIT 100'))));
} elseif ($page === 'opinions') {
    echo card('مواضيع المنتدى / آراء العملاء', table(['#', 'العنوان', 'الكاتب', 'التاريخ'], array_map(function ($r) {
        return [$r['id'], $r['title'], $r['user_id'], ago($r['created_at'])];
    }, q('SELECT id, title, user_id, created_at FROM threads ORDER BY id DESC LIMIT 100'))));
    echo card('الردود', table(['#', 'الموضوع', 'النص', 'التاريخ'], array_map(function ($r) {
        return [$r['id'], $r['thread_id'], mb_substr((string) $r['body'], 0, 80), ago($r['created_at'])];
    }, q('SELECT id, thread_id, body, created_at FROM posts ORDER BY id DESC LIMIT 100'))));
} elseif ($page === 'commands') {
    $opts = '';
    foreach ($commands as $k => $v) $opts .= '<option value="' . h($k) . '">' . h($k) . ' — ' . h($v) . '</option>';
    echo card('تنفيذ الأوامر', '<form method="post" class="cmd"><input type="hidden" name="csrf" value="' . h($csrf) . '">'
        . '<label>الأمر</label><select name="cmd">' . $opts . '</select>'
        . '<label>المعامل الاختياري (غالباً user_id)</label><input name="arg" placeholder="مثال: 12">'
        . '<button>تنفيذ</button></form>'
        . '<p class="m">قائمة مغلقة بأوامر آمنة فقط: لا SQL حر ولا أوامر shell. كل تنفيذ يُسجَّل في <code>admin_log</code>.</p>');
    if ($commandOutput !== '') echo card('النتيجة', '<pre>' . h($commandOutput) . '</pre>');
    echo card('كل الأوامر المتاحة', table(['الأمر', 'الوظيفة'], array_map(function ($k, $v) { return [$k, $v]; }, array_keys($commands), array_values($commands))));
    echo card('آخر الإجراءات', table(['الوقت', 'الأمر', 'التفاصيل'], array_map(function ($r) {
        return [ago($r['created_at']), $r['action'], $r['detail']];
    }, q('SELECT created_at, action, detail FROM admin_log ORDER BY id DESC LIMIT 20'))));
} elseif ($page === 'fields') {
    $fields = [
        ['اسم التطبيق', $appInfo['name'] ?? '—', 'data/app.json → app.name'],
        ['معرّف التطبيق', $appInfo['id'] ?? '—', 'data/app.json → app.id'],
        ['النوع', $appInfo['type'] ?? '—', 'data/app.json → app.type'],
        ['الإصدار', $appInfo['version'] ?? '—', 'data/app.json → app.version'],
        ['versionCode', $appInfo['version_code'] ?? 0, 'data/app.json → app.version_code'],
        ['المطوّر', $appInfo['developer_name'] ?? '—', 'data/app.json → developer.name'],
        ['بريد المطوّر', $appInfo['developer_email'] ?? '—', 'data/app.json → developer.email'],
        ['عنوان API', $apiBase, 'data/environments.json → apiBaseUrl'],
        ['الأيقونة', $appInfo['icon'] ?? '—', 'assets/icon.svg + icon.png'],
        ['التطبيقات الأخرى', count($sisters), 'data/app.json → sister_apps'],
        ['الكتب السحرية', $catalog['count'] ?? 0, 'data/books.json ← Books.kt'],
        ['أقسام الكتب', count($bookSections), 'data/books.json'],
        ['الرُتب', count($app['game']['ranks'] ?? []), 'data/app.json → game.ranks'],
        ['الطاقة القصوى', $app['energy']['max'] ?? 0, 'data/app.json → energy.max'],
        ['نقاط المهمة', $app['points']['task'] ?? 0, 'data/app.json → points.task'],
        ['XP لكل مستوى', $app['levels']['xpPerLevel'] ?? 0, 'data/app.json → levels.xpPerLevel'],
        ['عدد نقاط API', count($endpoints), 'server/public/api/*.php'],
        ['عدد الجداول', count($tableNames), 'server/migrations/001_init.sql + 002_admin.sql'],
        ['config.php', is_file($configFile) ? 'موجود' : 'مفقود', $serverRoot . '/config/config.php'],
        ['admin_token', $adminToken !== '' ? 'مضبوط' : 'غير مضبوط', 'config.php'],
        ['license_secret', $licenseSecret !== '' ? 'مضبوط' : 'غير مضبوط', 'config.php'],
        ['قاعدة البيانات', $pdo === null ? 'غير متصلة' : 'متصلة', 'PDO / MySQL'],
        ['admin_log', tableCount('admin_log'), 'قاعدة البيانات'],
        ['PHP', PHP_VERSION, 'السيرفر'],
    ];
    echo card('من أين انسحبت كل قيمة؟', table(['الحقل', 'القيمة المعروضة', 'المصدر'], $fields));
    echo card('لماذا كانت الحقول فارغة في اللوحة التابعة؟', '<p>ملفات <code>boards/y-dimensions/views/*.html</code> قوالب إعلانية تحوي رموزاً مثل <code>{{mother_panel.stats.home.users_count}}</code>.'
        . ' هذه الرموز يحلّها مُصيّر لوحة الأم فقط؛ المجلد بحد ذاته بلا كود تنفيذي ولا استعلامات، فإذا رُفع إلى استضافة عادية تظهر القيم فارغة أو كنص الرمز نفسه.</p>'
        . '<p>هذه اللوحة الأساسية تحلّ المشكلة: كل قيمة تُقرأ هنا من <code>app.json</code> أو من قاعدة البيانات عند كل طلب، وتُعرض مباشرة.</p>');
} elseif ($page === 'api') {
    echo card('عنوان الـ API', '<p><code>' . h($apiBase) . '</code></p>'
        . '<p class="m">هو نفسه المضبوط في التطبيق (<code>config/environments.json</code>) ويمكن تغييره من شاشة الحساب ← «عنوان API للخادم».</p>'
        . '<p>إذا أردت أن يكون عنوان التطبيق هو مجلد اللوحة نفسها، استخدم <code>' . h($apiBase) . '/panel</code> — ملفات <code>panel/api/*.php</code> تحوّل الطلب إلى <code>api/*.php</code> الحقيقية.</p>');
    echo card('مثال إنشاء حساب', '<pre>POST ' . h($apiBase) . '/api/auth.php?action=register
Content-Type: application/json

{"name":"يوسف","email":"yswefalhmzy@gmail.com","password":"12345678"}

الرد المتوقع: {"token":"...","user":{"id":1,"name":"يوسف","email":"..."}}</pre>');
    echo card('فحص جاهزية الخادم', '<pre>GET ' . h($apiBase) . '/  →  {"ok":true,"name":"y-dimension"}
GET ' . h($apiBase) . '/api/auth.php?action=login  →  {"error":"use POST"} (JSON وليس HTML)</pre>'
        . '<p class="m">إن جاء الرد صفحة HTML، فملفات <code>server/public/api</code> غير مرفوعة أو العنوان يشير إلى موقع PHP.</p>');
    $eps = [];
    foreach ($endpoints as $e) $eps[] = [$e[0], $e[1], $e[2]];
    echo card('كل المسارات', table(['المسار', 'الطريقة', 'الوظيفة'], $eps));
} elseif ($page === 'requirements') {
    $reqData = readJson($panelDir . '/data/requirements.json');
    $reqRows = $reqData['requirements'] ?? [];
    usort($reqRows, function ($a, $b) {
        return [(int) ($a['provided'] ?? 0), (int) ($a['priority'] ?? 5), (string) ($a['app'] ?? '')]
            <=> [(int) ($b['provided'] ?? 0), (int) ($b['priority'] ?? 5), (string) ($b['app'] ?? '')];
    });
    $missing = count(array_filter($reqRows, function ($r) { return empty($r['provided']); }));
    echo card('الحالة', '<p>النواقص: <b>' . $missing . '</b> من ' . count($reqRows) . ' بنداً. الفرز: الناقص أولاً ثم الأولوية.</p>'
        . '<p class="m">هذا التبويب مطلوب في لوحة التحكم الأم؛ التأشير هنا يسجَّل محلياً وفي <code>admin_log</code>.</p>');
    echo '<h2 class="sec">القسم 1 — مصادر الأشياء</h2>';
    foreach (($reqData['asset_sources'] ?? []) as $group) {
        $rows = [];
        foreach (($group['items'] ?? []) as $it) {
            $rows[] = [$it['name'] ?? '—', $it['path'] ?? '—', $it['license'] ?? '—', $it['status'] ?? '—'];
        }
        echo card($group['kind'] ?? 'قسم', table(['الأصل', 'المصدر/المسار', 'الترخيص', 'الحالة'], $rows));
    }
    echo '<h2 class="sec">القسم 2 — متطلبات التطبيقات</h2>';
    $out = [];
    foreach ($reqRows as $r) {
        $original = -1;
        foreach (($reqData['requirements'] ?? []) as $k => $orig) {
            if (($orig['item'] ?? '') === ($r['item'] ?? '') && ($orig['app'] ?? '') === ($r['app'] ?? '')) { $original = $k; break; }
        }
        $state = !empty($r['provided']) ? 'تم توفيره ✓' : 'ناقص';
        $toggle = '<form method="post" class="inl"><input type="hidden" name="csrf" value="' . h($csrf) . '">'
            . '<input type="hidden" name="req_index" value="' . $original . '"><button class="b">تبديل</button></form>';
        $out[] = [$r['app'] ?? '—', $r['category'] ?? '—', $r['item'] ?? '—', $r['source'] ?? '—', $state, (string) ($r['priority'] ?? 5), $r['note'] ?? '', $toggle];
    }
    echo card('كل المتطلبات (مرتّبة)', '<div class="scroll"><table><tr><th>التطبيق</th><th>التصنيف</th><th>المتطلب</th><th>المصدر</th><th>الحالة</th><th>الأولوية</th><th>ملاحظة</th><th></th></tr>'
        . implode('', array_map(function ($r) {
            $o = '<tr>';
            foreach ($r as $i => $c) $o .= '<td>' . ($i === 7 ? $c : h($c)) . '</td>';
            return $o . '</tr>';
        }, $out)) . '</table></div>');
} elseif ($page === 'content') {
    echo card('التحديثات الفرعية (بيانات فقط، بلا APK)', '<p>عند بدء تشغيل التطبيق يُسحب <code>content.php?action=manifest</code>، '
        . 'ويُنزَّل فقط الملف الذي تغيّرت بصمته <code>sha256</code> إلى <code>filesDir/content/</code>. '
        . 'تغيير الكود أو الواجهة يحتاج إصداراً كاملاً (APK).</p>');
    $rows = [];
    foreach (['app.json', 'books.json', 'environments.json', 'requirements.json'] as $n) {
        $f = $panelDir . '/data/' . $n;
        $rows[] = [$n, is_file($f) ? num(filesize($f)) . ' بايت' : 'مفقود', is_file($f) ? substr(hash_file('sha256', $f), 0, 20) . '…' : '—', 'content.php?action=file&name=' . $n];
    }
    echo card('الملفات وبصماتها', table(['الملف', 'الحجم', 'SHA256', 'المسار'], $rows));
    echo card('كيف يستهلكها التطبيق', '<pre>GET /api/content.php?action=manifest
 → {"version":"1","kind":"data-only","files":[{"name":"books.json","sha256":"...","url":"..."}]}

التطبيق يقارن البصمة المحلية، ينزّل المتغيّر فقط، يتحقق منها، ثم يحدّث الواجهة.
عند الحاجة إلى APK جديد: يظهر إشعار «إصدار جديد» مع زر تنزيل وتثبيت داخل التطبيق.</pre>');
} elseif ($page === 'devices') {
    echo card('الأجهزة المسجّلة', table(['#', 'الاسم', 'الطراز', 'آخر ظهور', 'التسجيل', 'الحالة'], array_map(function ($r) {
        return [$r['id'], $r['label'], $r['model'], ago($r['last_seen']), ago($r['created_at']), $r['revoked'] ? 'ملغي' : 'نشط'];
    }, q('SELECT id, label, model, last_seen, created_at, revoked FROM devices ORDER BY last_seen DESC LIMIT 100'))));
    echo card('تسجيل الخروج عن بُعد', '<p>من الملف الشخصي في التطبيق: قائمة الأجهزة + زر «تسجيل الخروج من هذا الجهاز» '
        . 'يستدعي <code>devices.php?action=revoke</code> فيحذف التوكن ويعلّم الجهاز <code>revoked=1</code>.</p>'
        . '<p class="m">الجدول <code>devices</code> جديد غير مكرر من الترحيل <code>003_devices.sql</code>؛ <code>users</code> بقي موحّداً.</p>');
} elseif ($page === 'releases') {
    echo card('الإصدارات', table(['الإصدار', 'versionCode', 'الملف', 'الوصف', 'النشر', 'الحالة'], array_map(function ($r) {
        return [$r['version'], $r['version_code'], $r['apk_path'], $r['notes'], ago($r['published_at']), $r['active'] ? 'مفعّل' : 'موقف'];
    }, q('SELECT version, version_code, apk_path, notes, published_at, active FROM releases ORDER BY id DESC LIMIT 100'))));
    echo card('قاعدة تحديث واحدة', '<p>حزم البيانات → <code>content.php</code> بلا تنزيل APK. '
        . 'تغيير الكود → إصدار جديد في <code>releases</code> مع زر تنزيل وتثبيت داخل التطبيق.</p>'
        . '<p class="m">سبب إعادة طلب الصلاحيات بعد التحديث سابقاً: اختلاف توقيع <code>ci/test-signing.p12</code> عن توقيع الإصدار الرسمي؛ '
        . 'الإصدار 0.17.0 يوحّد التوقيع في <code>build.gradle.kts</code>.</p>');
} elseif ($page === 'developers') {
    echo '<h2 class="sec">القسم 1 — حسابات لوحة التحكم (الحد الأقصى 3)</h2>';
    echo card('إضافة مشرف', '<p>يُنشأ رمز عشوائي ويُعرض مرة واحدة؛ استخدمه في شاشة الدخول أو زر «حساب آخر».</p>'
        . '<form method="post" class="cmd"><input type="hidden" name="csrf" value="' . h($csrf) . '">'
        . '<input type="hidden" name="dev_action" value="admin_add"><label>اسم الحساب</label>'
        . '<input name="admin_label" placeholder="مثال: مطور الواجهة"><button>إنشاء حساب مشرف</button></form>');
    if ($generatedAdminToken !== '') {
        echo card('رمز الحساب الجديد — انسخه الآن', '<pre>' . h($generatedAdminToken) . '</pre><p class="m">لن يُعرض مرة أخرى؛ يُحفظ فقطhash في قاعدة البيانات.</p>');
    }
    echo card('المشرفون', table(['#', 'الاسم', 'الدور', 'التاريخ', ''], array_map(function ($r) use ($csrf) {
        return [$r['id'], $r['label'], $r['role'], ago($r['created_at']),
            '<form method="post" class="inl"><input type="hidden" name="csrf" value="' . h($csrf) . '">'
            . '<input type="hidden" name="dev_action" value="admin_drop"><input type="hidden" name="admin_id" value="' . (int) $r['id'] . '">'
            . '<button class="b">تعطيل</button></form>'];
    }, q('SELECT id, label, role, created_at FROM panel_admins WHERE active = 1 ORDER BY id DESC'))));
    echo card('حسابات هذه الجلسة', '<p>المحفوظة: ' . count($_SESSION['accounts'] ?? []) . ' / 3 · النشط: '
        . h((string) ($_SESSION['active'] ?? '—')) . '</p><p><a href="?add=1">تسجيل الدخول بحساب آخر</a> · <a href="?logout=1">تسجيل الخروج من الحساب النشط</a></p>'
        . '<p class="m">تبديل الحساب من القائمة في أعلى الصفحة، أو بالضغط على اسم الحساب.</p>');

    echo '<h2 class="sec">القسم 2 — تعيين مستخدمي التطبيق كمشرفين/مطورين</h2>';
    echo card('المستخدمون', table(['#', 'الاسم', 'البريد', 'الصلاحية', 'إجراء'], array_map(function ($r) use ($csrf) {
        $role = (string) ($r['role'] ?? '');
        $state = $role !== '' ? ($role === 'admin' ? 'مشرف' : 'مطور') : 'مستخدم عادي';
        $forms = '<form method="post" class="inl"><input type="hidden" name="csrf" value="' . h($csrf) . '">'
            . '<input type="hidden" name="user_id" value="' . (int) $r['id'] . '">'
            . '<input type="hidden" name="dev_action" value="dev_grant">'
            . '<select name="role"><option value="developer">مطور</option><option value="admin">مشرف</option></select>'
            . '<button class="b">تعيين</button></form>';
        if ($role !== '') {
            $forms .= '<form method="post" class="inl"><input type="hidden" name="csrf" value="' . h($csrf) . '">'
                . '<input type="hidden" name="user_id" value="' . (int) $r['id'] . '">'
                . '<input type="hidden" name="dev_action" value="dev_revoke"><button class="b">إلغاء</button></form>';
        }
        return [$r['id'], $r['name'], $r['email'], $state, $forms];
    }, q('SELECT u.id, u.name, u.email, d.role FROM users u
          LEFT JOIN developer_accounts d ON d.app_id = ? AND d.user_id = u.id AND d.active = 1
          ORDER BY u.id DESC LIMIT 100', ['y-dimensions']))));
    echo card('أثر الصلاحية في التطبيق', '<p>المستخدم المعيّن فقط يرى «وضع المطور» بأقسامه الأربعة؛ '
        . 'يستدعي التطبيق <code>devmode.php?action=status</code> بالتوكن قبل فتح الاختبارات.'
        . ' التطبيق لا يمنح الصلاحية من نفسه.</p><p class="m">الجدول <code>developer_accounts</code> جديد غير مكرر من <code>004_devmode.sql</code>؛ '
        . '<code>users</code> بقي موحّداً.</p>');
} elseif ($page === 'devtests') {
    $testsData = readJson($panelTestsPath);
    $tests = $testsData['tests'] ?? [];
    $bySection = [];
    foreach ($tests as $i => $t) $bySection[(string) ($t['section'] ?? 'عام')][] = [$i, $t];
    echo card('كتالوج اختبارات وضع المطور', '<p>' . count($tests) . ' اختباراً يدوياً، لكل ميزة على حدى، '
        . 'مولّد من <code>Books.kt</code> و<code>Routes.kt</code>. التطبيق يعرض زر «اختبار» بجانب بيانات كل ميزة، '
        . 'ويحفظ نسخة قبل كل اختبار للرجوع إليها.</p>'
        . '<p class="m">التعطيل هنا يمنع ظهور الاختبار في التطبيق عبر <code>devmode.php?action=tests</code> وملف التحديثات الفرعية.</p>');
    foreach ($bySection as $section => $items) {
        echo '<h2 class="sec">' . h($section) . '</h2>';
        $rows = [];
        foreach ($items as [$i, $t]) {
            $rows[] = [$t['id'] ?? '—', $t['title'] ?? '—', $t['data_key'] ?? '—', $t['action'] ?? '—',
                !empty($t['enabled']) ? 'مفعّل' : 'معطّل',
                '<form method="post" class="inl"><input type="hidden" name="csrf" value="' . h($csrf) . '">'
                . '<input type="hidden" name="dev_action" value="test_toggle"><input type="hidden" name="test_index" value="' . $i . '">'
                . '<button class="b">تبديل</button></form>'];
        }
        echo card('الاختبارات', table(['المعرّف', 'الميزة', 'بيانات', 'الإجراء', 'الحالة', ''], $rows));
    }
    echo card('أقسام وضع المطور في التطبيق', '<pre>1) اختبارات المميزات كلها — كل ميزة على حدى (يدوي، ليس تلقائياً)
2) الرجوع للوضع قبل الاختبار — نسخة قبل كل اختبار + نسخة بداية الجلسة
3) بيانات التطبيق — الحالة الكاملة للقراءة والنسخ
4) فوارق الإصدارات — بين الإصدار الحالي والسابق (releases.json)</pre>');
} elseif ($page === 'diagnostics') {
    echo card('مساعد تشخيص العتاد والنظام', '<p>التطبيق يجمع قراءات فعلية (البطارية/الشحن، التخزين الداخلي والخارجي، '
        . 'الواي فاي، الشريحة والشبكة، RAM/CPU)، ويولّد تقريراً عربياً وأوامر إصلاح آمنة، '
        . 'ثم يرسل حزمة <strong>مجهّلة بالكامل</strong> إلى <code>diagnostics.php?action=report</code>.</p>'
        . '<p class="m">مطابقة «حل معروف» تتم أولاً عبر <code>action=match</code>؛ لا IMEI ولا رقم هاتف ولا SSID حقيقي ولا موقع.</p>'
        . '<p><a href="../../docs/diagnostics/SYSTEM_PROMPT.md">System Prompt الكامل لمساعد التشخيص</a></p>');
    echo card('آخر البلاغات', table(['#', 'توقيع المشكلة', 'الطراز', 'أندرويد', 'التاريخ'], array_map(function ($r) {
        return [$r['id'], $r['issue_signature'], $r['device_model'], $r['android_version'], ago($r['created_at'])];
    }, q('SELECT id, issue_signature, device_model, android_version, created_at FROM diagnostic_reports ORDER BY id DESC LIMIT 100'))));
    echo card('حلول معروفة', table(['توقيع المشكلة', 'الحل'], array_map(function ($r) {
        return [$r['issue_signature'], $r['solution_ar']];
    }, q('SELECT issue_signature, solution_ar FROM diagnostic_solutions ORDER BY issue_signature'))));
}
$content = ob_get_clean();
?><!doctype html>
<html lang="ar" dir="rtl">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>اللوحة الأساسية — Y- Dimensions</title>
<link rel="icon" href="assets/icon.svg">
<style>
:root{--bg:#12111a;--fg:#f0edfa;--card:#1d1b29;--sv:#262238;--pri:#8b6cf5;--bd:#3a3650;--m:#a9a3c0;--ok:#39c58a;--no:#ff7a88}
@media(prefers-color-scheme:light){:root{--bg:#f7f5ff;--fg:#1b1926;--card:#fff;--sv:#f0ecfa;--pri:#6244d7;--bd:#ddd7ee;--m:#6b6580}}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--fg);font-family:system-ui,"Segoe UI",Tahoma,sans-serif;font-size:14px}
header{display:flex;align-items:center;gap:14px;padding:14px 20px;border-bottom:1px solid var(--bd);background:var(--card)}
header img{width:48px;height:48px;border-radius:14px}header h1{font-size:17px;margin:0}header p{margin:2px 0 0;color:var(--m);font-size:12px}
header .sp{flex:1}header a{color:var(--pri);text-decoration:none;font-size:13px}
nav{display:flex;flex-wrap:wrap;gap:6px;padding:10px 20px;border-bottom:1px solid var(--bd);background:var(--sv)}
nav a{padding:7px 12px;border-radius:10px;color:var(--fg);text-decoration:none;font-size:13px}nav a.on{background:var(--pri);color:#fff}
main{padding:18px 20px 40px;max-width:1180px;margin:0 auto}
.card{background:var(--card);border:1px solid var(--bd);border-radius:16px;padding:16px;margin-bottom:14px}
.card h2{font-size:15px;margin:0 0 12px}.hero{display:flex;align-items:center;gap:16px;background:var(--card);border:1px solid var(--bd);border-radius:18px;padding:18px;margin-bottom:14px}
.hero img{width:76px;height:76px;border-radius:20px}.hero h1{font-size:21px;margin:0 0 4px}.hero p{margin:0;color:var(--m);font-size:13px}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(140px,1fr));gap:10px;margin-bottom:14px}
.st{background:var(--card);border:1px solid var(--bd);border-radius:14px;padding:12px}.st b{display:block;font-size:20px}.st span{color:var(--m);font-size:12px}
.m{color:var(--m);font-size:12px}.warn{background:rgba(255,122,136,.12);border:1px solid var(--no);color:var(--no);border-radius:12px;padding:12px;margin-bottom:14px;font-size:13px}
table{width:100%;border-collapse:collapse;font-size:13px}th,td{text-align:start;padding:8px 10px;border-bottom:1px solid var(--bd);vertical-align:top}th{color:var(--m);font-weight:600}
.scroll{overflow-x:auto}code{background:var(--sv);padding:1px 5px;border-radius:5px;font-size:12px}pre{background:var(--sv);border-radius:10px;padding:12px;overflow:auto;white-space:pre-wrap;font-size:12px;line-height:1.7}
.books{display:grid;grid-template-columns:repeat(auto-fill,minmax(190px,1fr));gap:8px}.book{display:flex;align-items:center;gap:10px;background:var(--sv);border-radius:12px;padding:8px}
.book i{width:34px;height:34px;border-radius:9px;flex:none}.book small{color:var(--m)}
.ico{width:64px;height:64px;border-radius:16px;margin-inline-end:10px}
.sec{font-size:16px;margin:22px 0 10px;padding-bottom:6px;border-bottom:1px solid var(--bd)}
.inl{display:inline}.inl .b{padding:5px 10px}
.home-fab{position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px;flex-wrap:wrap;max-width:60vw}
.home-fab a{background:var(--pri);color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font-size:12px;font-weight:700;box-shadow:0 6px 18px rgba(0,0,0,.25)}
.cmd{display:grid;gap:8px;max-width:520px}.cmd label{font-size:12px;color:var(--m)}
.cmd select,.cmd input{padding:10px;border-radius:10px;border:1px solid var(--bd);background:var(--sv);color:var(--fg);font:inherit}
.cmd button{padding:11px;border:0;border-radius:10px;background:var(--pri);color:#fff;font-weight:700;cursor:pointer}
ul{margin:0;padding-inline-start:18px;line-height:1.9}
</style>
</head>
<body>
<header>
  <img src="assets/icon.svg" alt="أيقونة Y- Dimensions">
  <div><h1>اللوحة الأساسية — <?= h($appInfo['name'] ?? 'Y- Dimensions') ?></h1><p>API · تنفيذ أوامر · تطبيقات · كتب · <?= h($appInfo['version'] ?? '—') ?></p></div>
  <span class="sp"></span>
  <span class="m" style="margin-inline-end:8px">الحسابات <?= count($_SESSION['accounts'] ?? []) ?>/3:</span>
  <?php foreach (($_SESSION['accounts'] ?? []) as $a): ?>
    <a href="?use=<?= h((string) ($a['id'] ?? '')) ?>" style="margin-inline-end:8px;<?= ((string) ($_SESSION['active'] ?? '') === (string) ($a['id'] ?? '')) ? 'font-weight:700;text-decoration:underline' : '' ?>"><?= h((string) ($a['label'] ?? '')) ?></a>
  <?php endforeach; ?>
  <a href="?add=1" style="margin-inline-end:8px">حساب آخر</a><a href="?logout=1">خروج</a>
</header>
<nav><?php foreach ($tabs as $k => $v) echo '<a href="?p=' . h($k) . '"' . ($k === $page ? ' class="on"' : '') . '>' . h($v) . '</a>'; ?></nav>
<div class="home-fab"><a href="../">🏠 الموقع الأساسي</a><a href="./">⚙ لوحة التحكم الأساسية</a></div>
<main>
<?php if ($readOnly) echo '<div class="warn">وضع القراءة فقط: config.php أو admin_token غير مضبوط. البيانات المعروضة منسحبة من الملفات المحلية، وتنفيذ الأوامر معطّل حتى يُضبط الإعداد.</div>'; ?>
<?php if ($flash !== '') echo '<div class="card" style="border-color:var(--ok);color:var(--ok)">' . h($flash) . '</div>'; ?>
<?= $content ?>
</main>
</body>
</html>
<!-- Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension -->

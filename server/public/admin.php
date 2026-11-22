<?php

declare(strict_types=1);

/*
 * Y-Dimension control panel — single file. Upload to public/ next to api/.
 * Login with `admin_token` from config/config.php (the "code").
 * Features: overview stats, speed/health, users, licenses, shop, raid campaigns,
 * announcements, forum moderation, audit log. CSRF + login rate limit + session hardening.
 */

$configFile = dirname(__DIR__) . '/config/config.php';
if (!is_file($configFile)) {
    http_response_code(500);
    exit('config/config.php is missing');
}
$config = require $configFile;
require_once dirname(__DIR__) . '/src/license.php';
date_default_timezone_set('UTC');

$t0 = microtime(true);
header('X-Frame-Options: DENY');
header('X-Content-Type-Options: nosniff');
header('Referrer-Policy: no-referrer');
header("Content-Security-Policy: default-src 'self'; style-src 'unsafe-inline'; script-src 'unsafe-inline'; img-src data:");
header('Cache-Control: no-store');

$https = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') || (($_SERVER['HTTP_X_FORWARDED_PROTO'] ?? '') === 'https');
session_name('yd_admin');
session_set_cookie_params(['lifetime' => 0, 'path' => '/', 'secure' => $https, 'httponly' => true, 'samesite' => 'Strict']);
session_start();

function h($v): string { return htmlspecialchars((string) $v, ENT_QUOTES, 'UTF-8'); }
function ago(int $ts): string {
    if ($ts <= 0) return '—';
    $d = time() - $ts;
    if ($d < 60) return 'الآن';
    if ($d < 3600) return intdiv($d, 60) . ' د';
    if ($d < 86400) return intdiv($d, 3600) . ' س';
    return date('Y-m-d', $ts);
}
function dt(int $ts): string { return $ts > 0 ? date('Y-m-d H:i', $ts) : '—'; }

$adminToken = (string) ($config['admin_token'] ?? '');
$ip = (string) ($_SERVER['REMOTE_ADDR'] ?? '');

// ---------- login ----------
$loginError = '';
if (($_GET['logout'] ?? '') === '1') {
    $_SESSION = [];
    session_destroy();
    header('Location: admin.php');
    exit;
}
if (empty($_SESSION['ok'])) {
    if ($adminToken === '' || strlen($adminToken) < 12) {
        $loginError = 'اضبط admin_token في config.php (12 حرفاً على الأقل).';
    } elseif (($_SERVER['REQUEST_METHOD'] ?? '') === 'POST') {
        $lockFile = sys_get_temp_dir() . '/yd_admin_' . md5($ip) . '.json';
        $st = is_file($lockFile) ? (json_decode((string) file_get_contents($lockFile), true) ?: []) : [];
        $fails = (int) ($st['n'] ?? 0);
        $until = (int) ($st['until'] ?? 0);
        if ($until > time()) {
            $loginError = 'محاولات كثيرة. انتظر ' . ($until - time()) . ' ثانية.';
        } elseif (hash_equals($adminToken, (string) ($_POST['code'] ?? ''))) {
            @unlink($lockFile);
            session_regenerate_id(true);
            $_SESSION['ok'] = true;
            $_SESSION['csrf'] = bin2hex(random_bytes(16));
            $_SESSION['at'] = time();
            header('Location: admin.php');
            exit;
        } else {
            $fails++;
            $until = $fails >= 5 ? time() + min(900, 30 * ($fails - 4)) : 0;
            @file_put_contents($lockFile, json_encode(['n' => $fails, 'until' => $until]));
            usleep(400000);
            $loginError = 'الرمز غير صحيح.';
        }
    }
    if (empty($_SESSION['ok'])) {
        ?><!doctype html><html lang="ar" dir="rtl"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Y-Dimension — دخول</title>
<style>:root{--bg:#fbf9ff;--fg:#1b1a24;--card:#fff;--pri:#6c4cf1;--bd:#d8d3e6}@media(prefers-color-scheme:dark){:root{--bg:#121118;--fg:#ece9f5;--card:#1b1a24;--pri:#b8a6ff;--bd:#3a3748}}
body{margin:0;min-height:100vh;display:grid;place-items:center;background:var(--bg);color:var(--fg);font-family:system-ui,"Segoe UI",Tahoma,sans-serif}
form{background:var(--card);border:1px solid var(--bd);border-radius:20px;padding:28px;width:min(92vw,340px)}h1{margin:0 0 16px;font-size:20px}
input,button{width:100%;box-sizing:border-box;padding:12px;border-radius:12px;border:1px solid var(--bd);font:inherit;background:transparent;color:inherit;margin-bottom:10px}
button{background:var(--pri);color:#fff;border:0;cursor:pointer;font-weight:700}.e{color:#d64550;font-size:13px;margin-bottom:8px}</style></head><body>
<form method="post"><h1>لوحة تحكم Y-Dimension</h1><?php if ($loginError) echo '<div class="e">' . h($loginError) . '</div>'; ?>
<input type="password" name="code" placeholder="رمز الدخول" autocomplete="current-password" autofocus><button>دخول</button></form></body></html><?php
        exit;
    }
}
if (time() - (int) ($_SESSION['at'] ?? 0) > 8 * 3600) {
    $_SESSION = [];
    session_destroy();
    header('Location: admin.php');
    exit;
}
$csrf = (string) $_SESSION['csrf'];

// ---------- db ----------
try {
    $c = $config['db'] ?? [];
    $pdo = new PDO(
        'mysql:host=' . ($c['host'] ?? '') . ';dbname=' . ($c['name'] ?? '') . ';charset=utf8mb4',
        (string) ($c['user'] ?? ''), (string) ($c['pass'] ?? ''),
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC, PDO::ATTR_EMULATE_PREPARES => false]
    );
} catch (Throwable $e) {
    exit('فشل الاتصال بقاعدة البيانات. راجع config.php');
}
foreach (['announcements' => "CREATE TABLE IF NOT EXISTS announcements (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, title VARCHAR(160) NOT NULL, body TEXT NOT NULL, active TINYINT(1) NOT NULL DEFAULT 1, created_at BIGINT NOT NULL, PRIMARY KEY (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
          'admin_log' => "CREATE TABLE IF NOT EXISTS admin_log (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, action VARCHAR(60) NOT NULL, detail VARCHAR(400) NOT NULL DEFAULT '', ip VARCHAR(45) NOT NULL DEFAULT '', created_at BIGINT NOT NULL, PRIMARY KEY (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"] as $ddl) {
    try { $pdo->exec($ddl); } catch (Throwable $e) { /* limited DB rights: panel still works without */ }
}
function q(string $sql, array $p = []): array { global $pdo; $s = $pdo->prepare($sql); $s->execute($p); return $s->fetchAll(); }
function one(string $sql, array $p = []) { global $pdo; $s = $pdo->prepare($sql); $s->execute($p); return $s->fetchColumn(); }
function run(string $sql, array $p = []): int { global $pdo; $s = $pdo->prepare($sql); $s->execute($p); return $s->rowCount(); }
function logit(string $action, string $detail = ''): void {
    global $ip;
    try { run('INSERT INTO admin_log (action, detail, ip, created_at) VALUES (?,?,?,?)', [$action, mb_substr($detail, 0, 400), $ip, time()]); } catch (Throwable $e) {}
}

// ---------- actions ----------
$page = (string) ($_GET['p'] ?? 'overview');
$flash = '';
$newCode = '';
if (($_SERVER['REQUEST_METHOD'] ?? '') === 'POST') {
    if (!hash_equals($csrf, (string) ($_POST['csrf'] ?? ''))) {
        http_response_code(403);
        exit('CSRF');
    }
    $a = (string) ($_POST['a'] ?? '');
    $id = (int) ($_POST['id'] ?? 0);
    try {
        switch ($a) {
            case 'user_points':
                $pts = (int) ($_POST['points'] ?? 0);
                $lvl = max(1, (int) ($_POST['level'] ?? 1));
                run('UPDATE users SET points = ?, level = ? WHERE id = ?', [$pts, $lvl, $id]);
                logit('user_edit', "user=$id points=$pts level=$lvl");
                $flash = 'تم تحديث المستخدم #' . $id;
                break;
            case 'user_delete':
                run('DELETE FROM users WHERE id = ?', [$id]);
                logit('user_delete', "user=$id");
                $flash = 'تم حذف المستخدم وكل بياناته';
                break;
            case 'user_logout':
                run('DELETE FROM tokens WHERE user_id = ?', [$id]);
                logit('user_logout', "user=$id");
                $flash = 'أُنهيت جلسات المستخدم';
                break;
            case 'license_gen':
                $uid = (int) ($_POST['user_id'] ?? 0);
                $secret = (string) ($config['license_secret'] ?? '');
                if ($secret === '') { $flash = 'license_secret غير مضبوط'; break; }
                if ($uid > 0 && (int) one('SELECT COUNT(*) FROM users WHERE id = ?', [$uid]) === 0) { $flash = 'المستخدم غير موجود'; break; }
                $newCode = license_code($uid, $secret);
                logit('license_gen', "user=$uid");
                $flash = 'رمز الترخيص للمستخدم #' . $uid;
                break;
            case 'license_revoke':
                run('DELETE FROM licenses WHERE user_id = ?', [$id]);
                logit('license_revoke', "user=$id");
                $flash = 'أُلغي ترخيص المستخدم #' . $id;
                break;
            case 'item_save':
                $title = trim((string) ($_POST['title'] ?? ''));
                $desc = trim((string) ($_POST['description'] ?? ''));
                $price = max(0, (int) ($_POST['price'] ?? 0));
                $kind = trim((string) ($_POST['kind'] ?? 'cosmetic')) ?: 'cosmetic';
                $active = isset($_POST['active']) ? 1 : 0;
                if ($title === '') { $flash = 'العنوان مطلوب'; break; }
                if ($id > 0) {
                    run('UPDATE items SET title=?, description=?, price=?, kind=?, active=? WHERE id=?', [$title, $desc, $price, $kind, $active, $id]);
                } else {
                    run('INSERT INTO items (title, description, price, kind, active) VALUES (?,?,?,?,?)', [$title, $desc, $price, $kind, $active]);
                }
                logit('item_save', "id=$id $title");
                $flash = 'حُفظ العنصر';
                break;
            case 'item_delete':
                run('DELETE FROM items WHERE id = ?', [$id]);
                logit('item_delete', "id=$id");
                $flash = 'حُذف العنصر';
                break;
            case 'season_save':
                $goal = max(1, (int) ($_POST['goal'] ?? 1));
                $s = strtotime((string) ($_POST['starts'] ?? '')) ?: time();
                $e = strtotime((string) ($_POST['ends'] ?? '')) ?: ($s + 7 * 86400);
                if ($e <= $s) { $flash = 'النهاية يجب أن تكون بعد البداية'; break; }
                if ($id > 0) {
                    run('UPDATE raid_seasons SET goal=?, starts_at=?, ends_at=? WHERE id=?', [$goal, $s, $e, $id]);
                } else {
                    run('INSERT INTO raid_seasons (goal, starts_at, ends_at) VALUES (?,?,?)', [$goal, $s, $e]);
                }
                logit('season_save', "id=$id goal=$goal");
                $flash = 'حُفظ موسم الحملة';
                break;
            case 'season_end':
                run('UPDATE raid_seasons SET ends_at = ? WHERE id = ?', [time(), $id]);
                logit('season_end', "id=$id");
                $flash = 'أُنهي الموسم الآن';
                break;
            case 'season_delete':
                run('DELETE FROM raid_seasons WHERE id = ?', [$id]);
                logit('season_delete', "id=$id");
                $flash = 'حُذف الموسم';
                break;
            case 'ann_save':
                $title = trim((string) ($_POST['title'] ?? ''));
                $body = trim((string) ($_POST['body'] ?? ''));
                if ($title === '') { $flash = 'العنوان مطلوب'; break; }
                run('INSERT INTO announcements (title, body, active, created_at) VALUES (?,?,1,?)', [$title, $body, time()]);
                logit('ann_save', $title);
                $flash = 'نُشر الإعلان';
                break;
            case 'ann_toggle':
                run('UPDATE announcements SET active = 1 - active WHERE id = ?', [$id]);
                $flash = 'تم';
                break;
            case 'ann_delete':
                run('DELETE FROM announcements WHERE id = ?', [$id]);
                $flash = 'حُذف الإعلان';
                break;
            case 'thread_delete':
                run('DELETE FROM threads WHERE id = ?', [$id]);
                logit('thread_delete', "id=$id");
                $flash = 'حُذف الموضوع وردوده';
                break;
            case 'post_delete':
                run('DELETE FROM posts WHERE id = ?', [$id]);
                logit('post_delete', "id=$id");
                $flash = 'حُذف الرد';
                break;
            case 'purge_tokens':
                $n = run('DELETE FROM tokens WHERE last_used < ?', [time() - 90 * 86400]);
                logit('purge_tokens', "n=$n");
                $flash = "حُذف $n رمز جلسة قديم";
                break;
        }
    } catch (Throwable $e) {
        $flash = 'خطأ: ' . $e->getMessage();
    }
}

// ---------- views ----------
$tabs = ['overview' => 'نظرة عامة', 'speed' => 'السرعة والصحة', 'users' => 'المستخدمون', 'licenses' => 'التراخيص',
         'shop' => 'المتجر', 'raids' => 'الحملات', 'ann' => 'الإعلانات', 'forum' => 'المنتدى', 'log' => 'السجل'];
if (!isset($tabs[$page])) $page = 'overview';

function form_post(string $a, int $id, string $label, string $cls = '', string $confirmMsg = ''): string {
    global $csrf;
    $oc = $confirmMsg !== '' ? ' onsubmit="return confirm(' . h(json_encode($confirmMsg, JSON_UNESCAPED_UNICODE)) . ')"' : '';
    return '<form method="post" class="inl"' . $oc . '><input type="hidden" name="csrf" value="' . h($csrf) . '"><input type="hidden" name="a" value="' . h($a) . '"><input type="hidden" name="id" value="' . $id . '"><button class="b ' . $cls . '">' . h($label) . '</button></form>';
}
function csrf_fields(string $a, int $id = 0): string {
    global $csrf;
    return '<input type="hidden" name="csrf" value="' . h($csrf) . '"><input type="hidden" name="a" value="' . h($a) . '"><input type="hidden" name="id" value="' . $id . '">';
}
function statc(string $label, $value, string $sub = ''): string {
    return '<div class="st"><div class="n">' . h($value) . '</div><div class="l">' . h($label) . '</div>' . ($sub !== '' ? '<div class="s">' . h($sub) . '</div>' : '') . '</div>';
}
function bars(array $rows, string $labelKey, string $valKey): string {
    $max = 1;
    foreach ($rows as $r) $max = max($max, (int) $r[$valKey]);
    $o = '<div class="bars">';
    foreach ($rows as $r) {
        $pct = (int) round(((int) $r[$valKey]) / $max * 100);
        $o .= '<div class="br"><span class="bl">' . h($r[$labelKey]) . '</span><span class="bt"><i style="width:' . $pct . '%"></i></span><span class="bv">' . h($r[$valKey]) . '</span></div>';
    }
    return $o . '</div>';
}

ob_start();
try {
    if ($page === 'overview') {
        $now = time();
        $users = (int) one('SELECT COUNT(*) FROM users');
        $new7 = (int) one('SELECT COUNT(*) FROM users WHERE created_at > ?', [$now - 7 * 86400]);
        $new1 = (int) one('SELECT COUNT(*) FROM users WHERE created_at > ?', [$now - 86400]);
        $active7 = (int) one('SELECT COUNT(DISTINCT user_id) FROM tokens WHERE last_used > ?', [$now - 7 * 86400]);
        $active1 = (int) one('SELECT COUNT(DISTINCT user_id) FROM tokens WHERE last_used > ?', [$now - 86400]);
        $lic = (int) one('SELECT COUNT(*) FROM licenses');
        $pts = (int) one('SELECT COALESCE(SUM(points),0) FROM users');
        $purch = (int) one('SELECT COUNT(*) FROM purchases');
        $spent = (int) one('SELECT COALESCE(SUM(price),0) FROM purchases');
        echo '<div class="grid">' . statc('المستخدمون', $users, "+$new1 اليوم · +$new7 أسبوع") . statc('نشطون (24 س)', $active1, "$active7 خلال 7 أيام")
            . statc('تراخيص مفعّلة', $lic, $users ? round($lic / $users * 100) . '% من المستخدمين' : '') . statc('إجمالي النقاط', number_format($pts))
            . statc('مشتريات المتجر', $purch, number_format($spent) . ' نقطة') . statc('مواضيع المنتدى', (int) one('SELECT COUNT(*) FROM threads'), (int) one('SELECT COUNT(*) FROM posts') . ' رد')
            . statc('عناصر مزامنة', (int) one('SELECT COUNT(*) FROM journal') + (int) one('SELECT COUNT(*) FROM ledger') + (int) one('SELECT COUNT(*) FROM books') + (int) one('SELECT COUNT(*) FROM wallets'), 'يوميات + معاملات + كتب + محافظ')
            . statc('عناصر الخزنة', (int) one('SELECT COUNT(*) FROM vault_items')) . '</div>';
        $reg = q('SELECT FROM_UNIXTIME(created_at, "%m-%d") AS d, COUNT(*) AS c FROM users WHERE created_at > ? GROUP BY d ORDER BY MIN(created_at)', [$now - 14 * 86400]);
        echo '<h2>تسجيلات آخر 14 يوماً</h2>' . ($reg ? bars($reg, 'd', 'c') : '<p class="m">لا بيانات.</p>');
        $top = q('SELECT i.title, COUNT(p.id) AS c FROM purchases p JOIN items i ON i.id = p.item_id GROUP BY i.id, i.title ORDER BY c DESC LIMIT 5');
        echo '<h2>الأكثر شراءً</h2>' . ($top ? bars($top, 'title', 'c') : '<p class="m">لا مشتريات بعد.</p>');
        $teams = q("SELECT IF(team='','—',team) AS team, COUNT(*) AS c FROM profiles GROUP BY team ORDER BY c DESC LIMIT 8");
        echo '<h2>الفرق</h2>' . ($teams ? bars($teams, 'team', 'c') : '<p class="m">لا ملفات شخصية.</p>');
    } elseif ($page === 'speed') {
        $m = [];
        $s = microtime(true); $pdo->query('SELECT 1'); $m['ping'] = (microtime(true) - $s) * 1000;
        foreach (['users', 'tokens', 'journal', 'ledger', 'threads', 'posts', 'vault_items'] as $tb) {
            $s = microtime(true); $cnt = (int) one("SELECT COUNT(*) FROM `$tb`"); $m[$tb] = [(microtime(true) - $s) * 1000, $cnt];
        }
        $s = microtime(true); q('SELECT id,name,points FROM users ORDER BY points DESC LIMIT 20'); $m['top'] = (microtime(true) - $s) * 1000;
        $cls = fn(float $ms) => $ms < 20 ? 'ok' : ($ms < 100 ? 'wn' : 'er');
        echo '<div class="grid">' . statc('زمن الاتصال بالقاعدة', round($m['ping'], 1) . ' ms') . statc('أعلى 20 مستخدماً', round($m['top'], 1) . ' ms')
            . statc('إصدار PHP', PHP_VERSION) . statc('MySQL', (string) one('SELECT VERSION()')) . statc('ذاكرة PHP', round(memory_get_peak_usage(true) / 1048576, 1) . ' MB', 'الحد: ' . ini_get('memory_limit'))
            . statc('OPcache', function_exists('opcache_get_status') && @opcache_get_status(false) ? 'مفعّل' : 'غير مفعّل') . '</div>';
        $free = @disk_free_space(__DIR__); $tot = @disk_total_space(__DIR__);
        if ($free && $tot) echo '<p class="m">القرص: ' . round($free / 1073741824, 2) . ' GB متاح من ' . round($tot / 1073741824, 2) . ' GB</p>';
        echo '<h2>زمن استعلام العدّ لكل جدول</h2><table><tr><th>الجدول</th><th>الصفوف</th><th>الزمن</th></tr>';
        foreach (['users', 'tokens', 'journal', 'ledger', 'threads', 'posts', 'vault_items'] as $tb) {
            echo '<tr><td>' . h($tb) . '</td><td>' . number_format($m[$tb][1]) . '</td><td class="' . $cls($m[$tb][0]) . '">' . round($m[$tb][0], 2) . ' ms</td></tr>';
        }
        echo '</table>';
        $sizes = q('SELECT table_name AS t, ROUND((data_length + index_length)/1024, 1) AS kb FROM information_schema.tables WHERE table_schema = DATABASE() ORDER BY (data_length + index_length) DESC LIMIT 12');
        echo '<h2>حجم الجداول (KB)</h2>' . bars($sizes, 't', 'kb');
        echo '<p class="m">توليد هذه الصفحة: ' . round((microtime(true) - $t0) * 1000) . ' ms. القيم تُقاس لحظة فتح الصفحة؛ للمراقبة المستمرة استخدم UptimeRobot على api/index.php.</p>';
        echo '<form method="post">' . csrf_fields('purge_tokens') . '<button class="b">حذف رموز الجلسات الأقدم من 90 يوماً</button></form>';
    } elseif ($page === 'users') {
        $s = trim((string) ($_GET['s'] ?? ''));
        $like = '%' . addcslashes($s, '%_\\') . '%';
        $rows = q('SELECT u.id, u.name, u.email, u.points, u.xp, u.level, u.created_at, (l.user_id IS NOT NULL) AS lic, (SELECT MAX(last_used) FROM tokens t WHERE t.user_id = u.id) AS seen FROM users u LEFT JOIN licenses l ON l.user_id = u.id WHERE ? = "" OR u.name LIKE ? OR u.email LIKE ? OR u.id = ? ORDER BY u.id DESC LIMIT 100', [$s, $like, $like, (int) $s]);
        echo '<form method="get" class="row"><input type="hidden" name="p" value="users"><input name="s" value="' . h($s) . '" placeholder="بحث بالاسم أو البريد أو الرقم"><button class="b">بحث</button></form>';
        echo '<div class="scroll"><table><tr><th>#</th><th>الاسم</th><th>البريد</th><th>XP</th><th>نقاط / مستوى</th><th>آخر ظهور</th><th>ترخيص</th><th></th></tr>';
        foreach ($rows as $r) {
            echo '<tr><td>' . $r['id'] . '</td><td>' . h($r['name']) . '</td><td dir="ltr">' . h($r['email']) . '</td><td>' . $r['xp'] . '</td><td><form method="post" class="inl">' . csrf_fields('user_points', (int) $r['id'])
                . '<input class="sm" type="number" name="points" value="' . (int) $r['points'] . '"><input class="sm" type="number" name="level" value="' . (int) $r['level'] . '"><button class="b">حفظ</button></form></td><td>' . ago((int) $r['seen']) . '</td><td>' . ($r['lic'] ? '✓' : '—') . '</td><td>'
                . form_post('user_logout', (int) $r['id'], 'إنهاء الجلسات') . form_post('user_delete', (int) $r['id'], 'حذف', 'dg', 'حذف المستخدم #' . $r['id'] . ' وكل بياناته نهائياً؟') . '</td></tr>';
        }
        echo '</table></div>' . (count($rows) === 100 ? '<p class="m">يظهر أول 100 فقط — استعمل البحث.</p>' : '');
    } elseif ($page === 'licenses') {
        if ($newCode !== '') echo '<div class="code" dir="ltr">' . h($newCode) . '</div>';
        echo '<form method="post" class="row">' . csrf_fields('license_gen') . '<input type="number" name="user_id" min="1" placeholder="رقم المستخدم" required><button class="b">توليد رمز الترخيص</button></form>';
        $rows = q('SELECT l.user_id, u.name, u.email, l.activated_at FROM licenses l JOIN users u ON u.id = l.user_id ORDER BY l.activated_at DESC LIMIT 200');
        echo '<h2>التراخيص المفعّلة (' . count($rows) . ')</h2><div class="scroll"><table><tr><th>#</th><th>الاسم</th><th>البريد</th><th>التفعيل</th><th></th></tr>';
        foreach ($rows as $r) echo '<tr><td>' . $r['user_id'] . '</td><td>' . h($r['name']) . '</td><td dir="ltr">' . h($r['email']) . '</td><td>' . dt((int) $r['activated_at']) . '</td><td>' . form_post('license_revoke', (int) $r['user_id'], 'إلغاء', 'dg', 'إلغاء الترخيص؟') . '</td></tr>';
        echo '</table></div>';
    } elseif ($page === 'shop') {
        $edit = (int) ($_GET['e'] ?? 0);
        $cur = $edit ? (q('SELECT * FROM items WHERE id = ?', [$edit])[0] ?? null) : null;
        echo '<h2>' . ($cur ? 'تعديل عنصر #' . $edit : 'عنصر جديد') . '</h2><form method="post" class="card">' . csrf_fields('item_save', $cur ? $edit : 0)
            . '<input name="title" placeholder="العنوان" value="' . h($cur['title'] ?? '') . '" required><input name="description" placeholder="الوصف" value="' . h($cur['description'] ?? '') . '">'
            . '<div class="row"><input type="number" name="price" min="0" placeholder="السعر بالنقاط" value="' . h($cur['price'] ?? '100') . '"><input name="kind" placeholder="النوع (cosmetic…)" value="' . h($cur['kind'] ?? 'cosmetic') . '"></div>'
            . '<label><input type="checkbox" name="active" ' . (!$cur || $cur['active'] ? 'checked' : '') . '> ظاهر في المتجر</label><button class="b">حفظ</button></form>';
        $rows = q('SELECT i.*, (SELECT COUNT(*) FROM purchases p WHERE p.item_id = i.id) AS sold FROM items i ORDER BY i.id DESC');
        echo '<div class="scroll"><table><tr><th>#</th><th>العنوان</th><th>النوع</th><th>السعر</th><th>مبيع</th><th>الحالة</th><th></th></tr>';
        foreach ($rows as $r) echo '<tr><td>' . $r['id'] . '</td><td>' . h($r['title']) . '</td><td>' . h($r['kind']) . '</td><td>' . $r['price'] . '</td><td>' . $r['sold'] . '</td><td>' . ($r['active'] ? 'ظاهر' : 'مخفي') . '</td><td><a class="b" href="?p=shop&e=' . $r['id'] . '">تعديل</a>' . form_post('item_delete', (int) $r['id'], 'حذف', 'dg', 'حذف العنصر ومشترياته؟') . '</td></tr>';
        echo '</table></div>';
        $pur = q('SELECT p.created_at, u.name, i.title, p.price FROM purchases p JOIN users u ON u.id = p.user_id JOIN items i ON i.id = p.item_id ORDER BY p.id DESC LIMIT 15');
        echo '<h2>آخر المشتريات</h2><table><tr><th>الوقت</th><th>المستخدم</th><th>العنصر</th><th>السعر</th></tr>';
        foreach ($pur as $r) echo '<tr><td>' . ago((int) $r['created_at']) . '</td><td>' . h($r['name']) . '</td><td>' . h($r['title']) . '</td><td>' . $r['price'] . '</td></tr>';
        echo '</table>';
    } elseif ($page === 'raids') {
        $edit = (int) ($_GET['e'] ?? 0);
        $cur = $edit ? (q('SELECT * FROM raid_seasons WHERE id = ?', [$edit])[0] ?? null) : null;
        $s = $cur ? (int) $cur['starts_at'] : time(); $e = $cur ? (int) $cur['ends_at'] : time() + 7 * 86400;
        echo '<h2>' . ($cur ? 'تعديل موسم #' . $edit : 'موسم حملة جديد') . '</h2><form method="post" class="card">' . csrf_fields('season_save', $cur ? $edit : 0)
            . '<input type="number" name="goal" min="1" placeholder="الهدف (مجموع التقدم)" value="' . h($cur['goal'] ?? '1000') . '" required>'
            . '<div class="row"><label>البداية <input type="datetime-local" name="starts" value="' . date('Y-m-d\TH:i', $s) . '"></label><label>النهاية <input type="datetime-local" name="ends" value="' . date('Y-m-d\TH:i', $e) . '"></label></div><button class="b">حفظ</button></form>';
        $rows = q('SELECT r.*, (SELECT COUNT(*) FROM raid_members m WHERE m.season_id = r.id) AS members, (SELECT COALESCE(SUM(progress),0) FROM raid_members m WHERE m.season_id = r.id) AS prog FROM raid_seasons r ORDER BY r.id DESC LIMIT 50');
        echo '<div class="scroll"><table><tr><th>#</th><th>الهدف</th><th>التقدم</th><th>المشاركون</th><th>الفترة</th><th></th></tr>';
        foreach ($rows as $r) {
            $live = $r['starts_at'] <= time() && $r['ends_at'] > time();
            echo '<tr><td>' . $r['id'] . ($live ? ' <b class="ok">●</b>' : '') . '</td><td>' . $r['goal'] . '</td><td>' . $r['prog'] . ' (' . ($r['goal'] ? round($r['prog'] / $r['goal'] * 100) : 0) . '%)</td><td>' . $r['members'] . '</td><td>' . dt((int) $r['starts_at']) . ' → ' . dt((int) $r['ends_at']) . '</td><td><a class="b" href="?p=raids&e=' . $r['id'] . '">تعديل</a>'
                . ($live ? form_post('season_end', (int) $r['id'], 'إنهاء الآن') : '') . form_post('season_delete', (int) $r['id'], 'حذف', 'dg', 'حذف الموسم وتقدم المشاركين؟') . '</td></tr>';
        }
        echo '</table></div>';
    } elseif ($page === 'ann') {
        echo '<h2>إعلان جديد</h2><form method="post" class="card">' . csrf_fields('ann_save') . '<input name="title" placeholder="العنوان" required><textarea name="body" rows="3" placeholder="النص"></textarea><button class="b">نشر</button></form>'
            . '<p class="m">يقرؤها التطبيق من <code>api/announcements.php</code> (قراءة عامة، آخر 20 إعلاناً نشطاً).</p>';
        foreach (q('SELECT * FROM announcements ORDER BY id DESC LIMIT 50') as $r) {
            echo '<div class="card"><b>' . h($r['title']) . '</b> <span class="m">' . ago((int) $r['created_at']) . ' · ' . ($r['active'] ? 'نشط' : 'مخفي') . '</span><p>' . nl2br(h($r['body'])) . '</p>'
                . form_post('ann_toggle', (int) $r['id'], $r['active'] ? 'إخفاء' : 'إظهار') . form_post('ann_delete', (int) $r['id'], 'حذف', 'dg', 'حذف الإعلان؟') . '</div>';
        }
    } elseif ($page === 'forum') {
        $th = q('SELECT t.id, t.title, t.body, t.created_at, u.name, (SELECT COUNT(*) FROM posts p WHERE p.thread_id = t.id) AS replies FROM threads t JOIN users u ON u.id = t.user_id ORDER BY t.id DESC LIMIT 30');
        foreach ($th as $t) {
            echo '<div class="card"><b>' . h($t['title']) . '</b> <span class="m">' . h($t['name']) . ' · ' . ago((int) $t['created_at']) . ' · ' . $t['replies'] . ' رد</span><p>' . nl2br(h(mb_substr($t['body'], 0, 300))) . '</p>' . form_post('thread_delete', (int) $t['id'], 'حذف الموضوع', 'dg', 'حذف الموضوع وكل ردوده؟');
            foreach (q('SELECT p.id, p.body, u.name FROM posts p JOIN users u ON u.id = p.user_id WHERE p.thread_id = ? ORDER BY p.id DESC LIMIT 5', [$t['id']]) as $p) {
                echo '<div class="rp"><span class="m">' . h($p['name']) . ':</span> ' . h(mb_substr($p['body'], 0, 160)) . ' ' . form_post('post_delete', (int) $p['id'], 'حذف', 'dg', 'حذف الرد؟') . '</div>';
            }
            echo '</div>';
        }
        if (!$th) echo '<p class="m">لا مواضيع.</p>';
    } elseif ($page === 'log') {
        echo '<table><tr><th>الوقت</th><th>الإجراء</th><th>التفاصيل</th><th>IP</th></tr>';
        foreach (q('SELECT * FROM admin_log ORDER BY id DESC LIMIT 100') as $r) echo '<tr><td>' . dt((int) $r['created_at']) . '</td><td>' . h($r['action']) . '</td><td>' . h($r['detail']) . '</td><td dir="ltr">' . h($r['ip']) . '</td></tr>';
        echo '</table>';
    }
} catch (Throwable $e) {
    echo '<div class="flash er">خطأ في الصفحة: ' . h($e->getMessage()) . '</div>';
}
$body = ob_get_clean();
?><!doctype html><html lang="ar" dir="rtl"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="robots" content="noindex"><title>Y-Dimension — لوحة التحكم</title>
<style>
:root{--bg:#fbf9ff;--fg:#1b1a24;--card:#fff;--sv:#f0ecf8;--pri:#6c4cf1;--on:#fff;--bd:#d8d3e6;--mu:#5e5a6e;--ok:#2e9e6b;--wn:#d98a1e;--er:#d64550}
@media(prefers-color-scheme:dark){:root{--bg:#121118;--fg:#ece9f5;--card:#1b1a24;--sv:#25232f;--pri:#b8a6ff;--on:#1b1a24;--bd:#3a3748;--mu:#b3aec4;--ok:#5cd39b;--wn:#f0b35a;--er:#ff8a92}}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--fg);font:15px/1.6 system-ui,"Segoe UI",Tahoma,sans-serif}
header{display:flex;gap:12px;align-items:center;justify-content:space-between;padding:12px 16px;border-bottom:1px solid var(--bd);background:var(--card);position:sticky;top:0;z-index:2}
header b{font-size:17px}nav{display:flex;gap:6px;overflow-x:auto;padding:10px 16px;border-bottom:1px solid var(--bd)}nav a{white-space:nowrap;padding:7px 14px;border-radius:99px;text-decoration:none;color:var(--fg);background:var(--sv)}nav a.on{background:var(--pri);color:var(--on);font-weight:700}
main{max-width:1000px;margin:0 auto;padding:16px}h2{font-size:17px;margin:22px 0 10px}.m{color:var(--mu);font-size:13px}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(150px,1fr));gap:10px}.st{background:var(--card);border:1px solid var(--bd);border-radius:16px;padding:14px}.st .n{font-size:24px;font-weight:800;color:var(--pri)}.st .l{font-size:13px}.st .s{font-size:12px;color:var(--mu)}
.card{background:var(--card);border:1px solid var(--bd);border-radius:16px;padding:14px;margin-bottom:12px;display:grid;gap:8px}
table{width:100%;border-collapse:collapse;background:var(--card);border-radius:12px;overflow:hidden}th,td{padding:8px 10px;border-bottom:1px solid var(--bd);text-align:start;font-size:13px;vertical-align:middle}th{background:var(--sv)}.scroll{overflow-x:auto}
input,textarea,select{padding:9px 11px;border-radius:10px;border:1px solid var(--bd);background:transparent;color:inherit;font:inherit;max-width:100%}input.sm{width:84px}.row{display:flex;gap:8px;flex-wrap:wrap;align-items:end}.row input{flex:1;min-width:120px}
.b{display:inline-block;padding:7px 13px;border-radius:10px;border:0;background:var(--pri);color:var(--on);cursor:pointer;font:inherit;font-size:13px;font-weight:700;text-decoration:none;margin:2px}.b.dg{background:var(--er);color:#fff}.inl{display:inline}
.bars{display:grid;gap:6px}.br{display:grid;grid-template-columns:110px 1fr 50px;gap:8px;align-items:center;font-size:13px}.bt{background:var(--sv);border-radius:99px;height:10px;overflow:hidden}.bt i{display:block;height:100%;background:var(--pri);border-radius:99px}.bv{text-align:end}.bl{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.flash{padding:10px 14px;border-radius:12px;background:var(--sv);margin-bottom:12px;border-inline-start:4px solid var(--pri)}.ok{color:var(--ok)}.wn{color:var(--wn)}.er{color:var(--er)}
.code{font:700 22px ui-monospace,monospace;letter-spacing:2px;padding:16px;border:2px dashed var(--pri);border-radius:16px;text-align:center;margin-bottom:14px;user-select:all}.rp{padding:6px 10px;border-inline-start:3px solid var(--bd);font-size:13px}
</style></head><body>
<header><b>Y-Dimension · لوحة التحكم</b><a class="b" href="?logout=1">خروج</a></header>
<nav><?php foreach ($tabs as $k => $v) echo '<a href="?p=' . $k . '"' . ($k === $page ? ' class="on"' : '') . '>' . h($v) . '</a>'; ?></nav>
<main><?php if ($flash !== '') echo '<div class="flash">' . h($flash) . '</div>'; echo $body; ?></main></body></html>

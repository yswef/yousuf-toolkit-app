// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
<?php

declare(strict_types=1);

require dirname(__DIR__, 2) . '/src/bootstrap.php';
$learningStore = dirname(__DIR__, 2) . '/src/learning_store.php';
if (!is_file($learningStore)) fail('learning store is unavailable', 500);
require_once $learningStore;

$method = strtoupper((string) ($_SERVER['REQUEST_METHOD'] ?? 'GET'));
$action = (string) ($_GET['action'] ?? '');
$publicRoot = dirname(__DIR__);

if ($action === 'upload' && $method === 'POST') {
    $user = current_user();
    $in = input();
    $planJson = (string) ($in['plan_json'] ?? '');
    $siteHtml = (string) ($in['site_html'] ?? '');
    if (trim($planJson) === '' || trim($siteHtml) === '') {
        fail('learning plan is incomplete', 422);
    }
    $goals = is_array($in['goals'] ?? null) ? array_values(array_map('strval', $in['goals'])) : [];
    $category = substr((string) ($in['category'] ?? ''), 0, 100);
    $courseType = substr((string) (($in['course_type'] ?? '') !== '' ? $in['course_type'] : $category), 0, 100);
    $insert = db()->prepare('INSERT INTO learning_plans (user_id, title, subject, category, course_type, level, mode, minutes, goals_json, plan_json, course_md, roadmap_xml, site_html, status, site_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)');
    $insert->execute([
        (int) $user['id'],
        substr((string) ($in['title'] ?? ''), 0, 255),
        substr((string) ($in['subject'] ?? ''), 0, 255),
        $category,
        $courseType,
        substr((string) ($in['level'] ?? ''), 0, 100),
        (string) ($in['mode'] ?? '') === 'time' ? 'time' : 'daily',
        max(0, (int) ($in['minutes'] ?? 0)),
        (string) json_encode($goals, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
        $planJson,
        (string) ($in['course_md'] ?? ''),
        (string) ($in['roadmap_xml'] ?? ''),
        $siteHtml,
        'pending',
        '',
    ]);
    $id = (int) db()->lastInsertId();
    respond(['ok' => true, 'id' => $id, 'status' => 'pending'], 201);
}

if ($action === 'ready' && $method === 'GET') {
    respond(['ok' => true, 'plans' => learning_published(db())]);
}

if ($action === 'plan' && $method === 'GET') {
    $id = (int) ($_GET['id'] ?? 0);
    $query = db()->prepare("SELECT * FROM learning_plans WHERE id = ? AND status = 'published' LIMIT 1");
    $query->execute([$id]);
    $row = $query->fetch(PDO::FETCH_ASSOC);
    if (!is_array($row)) fail('published plan not found', 404);
    respond(['ok' => true] + $row);
}

if ($action === 'site' && $method === 'GET') {
    $id = (int) ($_GET['id'] ?? 0);
    $query = db()->prepare("SELECT site_html FROM learning_plans WHERE id = ? AND status = 'published' LIMIT 1");
    $query->execute([$id]);
    $row = $query->fetch(PDO::FETCH_ASSOC);
    if (!is_array($row)) fail('published site not found', 404);
    header('Content-Type: text/html; charset=UTF-8');
    echo (string) $row['site_html'];
    exit;
}

fail('unknown learning action', 404);
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com

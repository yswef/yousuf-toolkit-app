// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
<?php

declare(strict_types=1);

function learning_ensure_dir(string $path): void
{
    if (!is_dir($path) && !@mkdir($path, 0755, true) && !is_dir($path)) {
        throw new RuntimeException('could not create learning directory');
    }
}

function learning_write(string $path, string $contents): void
{
    if (@file_put_contents($path, $contents, LOCK_EX) === false) {
        throw new RuntimeException('could not write learning file');
    }
}

function learning_published(PDO $db): array
{
    try {
        $query = $db->prepare('SELECT id, title, subject, category, level, course_type, mode, minutes, site_url FROM learning_plans WHERE status = ? ORDER BY updated_at DESC');
        $query->execute(['published']);
        $rows = $query->fetchAll(PDO::FETCH_ASSOC);
        if (!is_array($rows)) return [];
        return array_values(array_map(static function (array $row): array {
            return [
                'id' => (int) $row['id'],
                'title' => (string) $row['title'],
                'subject' => (string) $row['subject'],
                'category' => (string) $row['category'],
                'level' => (string) $row['level'],
                'course_type' => (string) $row['course_type'],
                'mode' => (string) $row['mode'],
                'minutes' => (int) $row['minutes'],
                'site_url' => (string) $row['site_url'],
            ];
        }, $rows));
    } catch (Throwable $error) {
        return [];
    }
}

function learning_plans_json(PDO $db, string $publicRoot): void
{
    $directory = rtrim($publicRoot, '/\\') . '/learn';
    learning_ensure_dir($directory);
    learning_write($directory . '/plans.json', (string) json_encode([
        'developer' => 'يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com',
        'plans' => learning_published($db),
        'signature' => 'يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com',
    ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT));
}

function learning_site_issues(string $siteHtml): array
{
    $needles = ['©', 'copyright', 'all rights reserved', 'حقوق النشر', 'جميع الحقوق', 'ترخيص', 'license', 'author'];
    $issues = [];
    foreach ($needles as $needle) {
        if (stripos($siteHtml, $needle) !== false) $issues[] = $needle;
    }
    return $issues;
}

function learning_publish_plan(PDO $db, int $id, string $publicRoot): array
{
    $query = $db->prepare('SELECT * FROM learning_plans WHERE id = ? LIMIT 1');
    $query->execute([$id]);
    $row = $query->fetch(PDO::FETCH_ASSOC);
    if (!is_array($row)) {
        throw new RuntimeException('learning plan not found');
    }
    $siteHtml = (string) $row['site_html'];
    if (stripos($siteHtml, '<!doctype html') === false && stripos($siteHtml, '<html') === false) {
        throw new RuntimeException('learning site is incomplete');
    }
    $directory = rtrim($publicRoot, '/\\') . '/learn/' . $id;
    learning_ensure_dir($directory);
    learning_write($directory . '/index.html', $siteHtml);
    learning_write($directory . '/course.md', (string) $row['course_md']);
    learning_write($directory . '/roadmap.xml', (string) $row['roadmap_xml']);
    learning_write($directory . '/plan.json', (string) $row['plan_json']);
    $siteUrl = '/learn/' . $id . '/';
    $update = $db->prepare("UPDATE learning_plans SET status = 'published', site_url = ?, updated_at = NOW() WHERE id = ?");
    $update->execute([$siteUrl, $id]);
    learning_plans_json($db, $publicRoot);
    return ['id' => $id, 'status' => 'published', 'site_url' => $siteUrl];
}

function learning_reject_plan(PDO $db, int $id, string $publicRoot): void
{
    $update = $db->prepare("UPDATE learning_plans SET status = 'rejected', site_url = '', updated_at = NOW() WHERE id = ?");
    $update->execute([$id]);
    @unlink(rtrim($publicRoot, '/\\') . '/learn/' . $id . '/index.html');
    learning_plans_json($db, $publicRoot);
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com

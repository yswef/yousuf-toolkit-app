<?php

declare(strict_types=1);

// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
// تحديثات فرعية: ملفات بيانات فقط، بلا APK. التطبيق يقارن sha256 وينزّل المتغيّر فقط.
require dirname(__DIR__, 2) . '/src/bootstrap.php';

$action = (string) ($_GET['action'] ?? 'manifest');
$dataDir = dirname(__DIR__) . '/panel/data';
$allowed = ['app.json', 'books.json', 'environments.json', 'requirements.json', 'devmode_tests.json', 'releases.json'];
$versionFile = dirname(__DIR__) . '/content/VERSION';

if ($action === 'manifest') {
    $rawVersion = is_file($versionFile) ? trim((string) file_get_contents($versionFile)) : '1';
    $version = '1';
    foreach (preg_split('/\R/', $rawVersion) as $line) {
        $line = trim($line);
        if (preg_match('/^\d+$/', $line) === 1) { $version = $line; break; }
    }
    $files = [];
    foreach ($allowed as $name) {
        $path = $dataDir . '/' . $name;
        if (!is_file($path)) continue;
        $raw = (string) file_get_contents($path);
        $files[] = [
            'name' => $name,
            'sha256' => hash('sha256', $raw),
            'size' => strlen($raw),
            'url' => 'content.php?action=file&name=' . rawurlencode($name),
        ];
    }
    respond(['app_id' => 'y-dimensions', 'version' => $version, 'kind' => 'data-only', 'files' => $files]);
}

if ($action === 'file') {
    $name = (string) ($_GET['name'] ?? '');
    if (!in_array($name, $allowed, true)) fail('unknown file', 404);
    $path = $dataDir . '/' . $name;
    if (!is_file($path)) fail('file missing', 404);
    header('Content-Type: application/json; charset=utf-8');
    header('X-Content-Sha256: ' . hash('sha256', (string) file_get_contents($path)));
    echo (string) file_get_contents($path);
    exit;
}

fail('unknown action', 404);
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension

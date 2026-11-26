<?php

declare(strict_types=1);

// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
require dirname(__DIR__, 2) . '/src/bootstrap.php';

$path = dirname(__DIR__) . '/panel/data/requirements.json';
$action = (string) ($_GET['action'] ?? 'list');

if ($action === 'list') {
    if (!is_file($path)) fail('requirements missing', 404);
    $data = json_decode((string) file_get_contents($path), true);
    if (!is_array($data)) fail('invalid requirements', 500);
    $req = $data['requirements'] ?? [];
    usort($req, function ($a, $b) {
        return [(int) ($a['provided'] ?? 0), (int) ($a['priority'] ?? 5), (string) ($a['app'] ?? '')]
            <=> [(int) ($b['provided'] ?? 0), (int) ($b['priority'] ?? 5), (string) ($b['app'] ?? '')];
    });
    respond(['app_id' => 'y-dimensions', 'requirements' => $req, 'asset_sources' => $data['asset_sources'] ?? []]);
}

if ($action === 'mark') {
    if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') fail('use POST', 405);
    global $config;
    $token = (string) ($_SERVER['HTTP_X_ADMIN_TOKEN'] ?? '');
    $expected = (string) ($config['admin_token'] ?? '');
    if ($expected === '' || !hash_equals($expected, $token)) fail('unauthorized', 401);
    $in = input();
    $index = (int) ($in['index'] ?? -1);
    if (!is_file($path)) fail('requirements missing', 404);
    $data = json_decode((string) file_get_contents($path), true);
    if (!is_array($data) || !isset($data['requirements'][$index])) fail('index out of range', 422);
    $data['requirements'][$index]['provided'] = (bool) ($in['provided'] ?? true);
    file_put_contents($path, json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT) . "\n", LOCK_EX);
    respond(['ok' => true]);
}

fail('unknown action', 404);
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension

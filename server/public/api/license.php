<?php

declare(strict_types=1);

require dirname(__DIR__, 2) . '/src/bootstrap.php';

$user = current_user();
$secret = (string) ($GLOBALS['config']['license_secret'] ?? '');
if ($secret === '') {
    fail('license is not configured', 503);
}

$db = db();

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'GET') {
    $query = $db->prepare('SELECT activated_at FROM licenses WHERE user_id = ? LIMIT 1');
    $query->execute([$user['id']]);
    $row = $query->fetch();
    respond(['active' => (bool) $row, 'activated_at' => $row ? (int) $row['activated_at'] : 0]);
}

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
    fail('method not allowed', 405);
}

$in = input();
$code = strtoupper(trim((string) ($in['code'] ?? '')));
$expected = license_code((int) $user['id'], $secret);
if ($code === '' || !hash_equals($expected, $code)) {
    fail('invalid code', 422);
}

$statement = $db->prepare(
    'INSERT INTO licenses (user_id, code_hash, activated_at) VALUES (?, ?, ?)
     ON DUPLICATE KEY UPDATE code_hash = VALUES(code_hash), activated_at = VALUES(activated_at)'
);
$statement->execute([$user['id'], hash('sha256', $code), time()]);

respond(['ok' => true, 'code' => $code]);

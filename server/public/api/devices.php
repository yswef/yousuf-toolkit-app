<?php

declare(strict_types=1);

// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
require dirname(__DIR__, 2) . '/src/bootstrap.php';

$user = current_user();
$action = (string) ($_GET['action'] ?? 'list');

if ($action === 'list') {
    $rows = db()->prepare(
        'SELECT id, label, model, last_seen, created_at, revoked FROM devices
         WHERE app_id = ? AND user_id = ? AND revoked = 0 ORDER BY last_seen DESC'
    );
    $rows->execute(['y-dimensions', (int) $user['id']]);
    respond(['devices' => $rows->fetchAll()]);
}

if ($action === 'revoke') {
    if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') fail('use POST', 405);
    $in = input();
    $id = (int) ($in['device_id'] ?? 0);
    if ($id <= 0) fail('device_id required', 422);
    $db = db();
    $find = $db->prepare('SELECT token_hash FROM devices WHERE app_id = ? AND user_id = ? AND id = ? LIMIT 1');
    $find->execute(['y-dimensions', (int) $user['id'], $id]);
    $row = $find->fetch();
    if (!$row) fail('device not found', 404);
    $mark = $db->prepare('UPDATE devices SET revoked = 1 WHERE id = ?');
    $mark->execute([$id]);
    $del = $db->prepare('DELETE FROM tokens WHERE token_hash = ?');
    $del->execute([(string) $row['token_hash']]);
    respond(['ok' => true]);
}

if ($action === 'ping') {
    if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') fail('use POST', 405);
    $hash = hash('sha256', (string) bearer());
    $up = db()->prepare('UPDATE devices SET last_seen = ? WHERE app_id = ? AND token_hash = ?');
    $up->execute([time(), 'y-dimensions', $hash]);
    respond(['ok' => true]);
}

fail('unknown action', 404);
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension

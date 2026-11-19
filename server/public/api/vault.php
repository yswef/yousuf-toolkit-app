<?php

declare(strict_types=1);

require dirname(__DIR__, 2) . '/src/bootstrap.php';

$user = current_user();
$db = db();
$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';

if ($method === 'GET') {
    $query = $db->prepare('SELECT client_id, payload, updated_at FROM vault_items WHERE user_id = ?');
    $query->execute([$user['id']]);
    respond(['items' => $query->fetchAll()]);
}

if ($method === 'POST') {
    $in = input();
    $clientId = (int) ($in['client_id'] ?? 0);
    $payload = (string) ($in['payload'] ?? '');
    $max = (int) ($GLOBALS['config']['vault_max_bytes'] ?? 65536);
    if ($clientId <= 0 || $payload === '' || strlen($payload) > $max) {
        fail('invalid payload', 422);
    }
    $statement = $db->prepare(
        'INSERT INTO vault_items (user_id, client_id, payload, updated_at) VALUES (?, ?, ?, ?)
         ON DUPLICATE KEY UPDATE payload = VALUES(payload), updated_at = VALUES(updated_at)'
    );
    $statement->execute([$user['id'], $clientId, $payload, (int) ($in['updated_at'] ?? time())]);
    respond(['ok' => true]);
}

if ($method === 'DELETE') {
    $clientId = (int) ($_GET['client_id'] ?? 0);
    if ($clientId <= 0) {
        fail('client_id required', 422);
    }
    $delete = $db->prepare('DELETE FROM vault_items WHERE user_id = ? AND client_id = ?');
    $delete->execute([$user['id'], $clientId]);
    respond(['ok' => true]);
}

fail('method not allowed', 405);

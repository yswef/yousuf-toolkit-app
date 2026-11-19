<?php

declare(strict_types=1);

require dirname(__DIR__, 2) . '/src/bootstrap.php';

$user = current_user();
$action = (string) ($_GET['action'] ?? 'list');
$db = db();

if ($action === 'list') {
    $query = $db->query('SELECT id, title, description, price, kind FROM items WHERE active = 1 ORDER BY price ASC');
    respond(['items' => $query->fetchAll()]);
}

if ($action !== 'buy') {
    fail('unknown action', 404);
}
if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
    fail('use POST', 405);
}

$in = input();
$itemId = max(0, (int) ($in['item_id'] ?? 0));
if ($itemId === 0) {
    fail('item_id required', 422);
}

$db->beginTransaction();
try {
    $itemQuery = $db->prepare('SELECT id, title, price FROM items WHERE id = ? AND active = 1 FOR UPDATE');
    $itemQuery->execute([$itemId]);
    $item = $itemQuery->fetch();
    if (!$item) {
        $db->rollBack();
        fail('item not found', 404);
    }
    $userQuery = $db->prepare('SELECT points FROM users WHERE id = ? FOR UPDATE');
    $userQuery->execute([$user['id']]);
    $balance = (int) ($userQuery->fetchColumn() ?: 0);
    $price = (int) $item['price'];
    if ($balance < $price) {
        $db->rollBack();
        fail('insufficient balance', 402);
    }
    $update = $db->prepare('UPDATE users SET points = points - ? WHERE id = ?');
    $update->execute([$price, $user['id']]);
    $purchase = $db->prepare('INSERT INTO purchases (user_id, item_id, price, created_at) VALUES (?, ?, ?, ?)');
    $purchase->execute([$user['id'], $itemId, $price, time()]);
    $db->commit();
    respond([
        'ok' => true,
        'balance' => $balance - $price,
        'title' => (string) $item['title'],
    ]);
} catch (Throwable $error) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    fail('purchase failed', 500);
}

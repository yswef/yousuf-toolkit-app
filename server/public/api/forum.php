<?php

declare(strict_types=1);

require dirname(__DIR__, 2) . '/src/bootstrap.php';

$user = current_user();
$action = (string) ($_GET['action'] ?? 'threads');
$db = db();

if ($action === 'threads') {
    $query = $db->prepare(
        'SELECT t.id, t.title, u.name AS author, t.created_at,
                (SELECT COUNT(*) FROM posts p WHERE p.thread_id = t.id) AS post_count
         FROM threads t JOIN users u ON u.id = t.user_id
         ORDER BY t.id DESC LIMIT 100'
    );
    $query->execute();
    respond(['threads' => $query->fetchAll()]);
}

if ($action === 'posts') {
    $threadId = max(0, (int) ($_GET['thread_id'] ?? 0));
    if ($threadId === 0) {
        fail('thread_id required', 422);
    }
    $query = $db->prepare(
        'SELECT p.id, u.name AS author, p.body, p.created_at
         FROM posts p JOIN users u ON u.id = p.user_id
         WHERE p.thread_id = ? ORDER BY p.id ASC LIMIT 500'
    );
    $query->execute([$threadId]);
    respond(['posts' => $query->fetchAll()]);
}

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
    fail('use POST', 405);
}

$in = input();

if ($action === 'new_thread') {
    $title = trim((string) ($in['title'] ?? ''));
    $body = trim((string) ($in['body'] ?? ''));
    if ($title === '' || mb_strlen($title) > 200 || $body === '' || mb_strlen($body) > 5000) {
        fail('invalid thread', 422);
    }
    $insert = $db->prepare('INSERT INTO threads (user_id, title, body, created_at) VALUES (?, ?, ?, ?)');
    $insert->execute([$user['id'], $title, $body, time()]);
    $id = (int) $db->lastInsertId();
    $post = $db->prepare('INSERT INTO posts (thread_id, user_id, body, created_at) VALUES (?, ?, ?, ?)');
    $post->execute([$id, $user['id'], $body, time()]);
    respond(['ok' => true, 'thread_id' => $id], 201);
}

if ($action === 'new_post') {
    $threadId = max(0, (int) ($in['thread_id'] ?? 0));
    $body = trim((string) ($in['body'] ?? ''));
    if ($threadId === 0 || $body === '' || mb_strlen($body) > 5000) {
        fail('invalid post', 422);
    }
    $exists = $db->prepare('SELECT id FROM threads WHERE id = ? LIMIT 1');
    $exists->execute([$threadId]);
    if (!$exists->fetch()) {
        fail('thread not found', 404);
    }
    $post = $db->prepare('INSERT INTO posts (thread_id, user_id, body, created_at) VALUES (?, ?, ?, ?)');
    $post->execute([$threadId, $user['id'], $body, time()]);
    respond(['ok' => true, 'post_id' => (int) $db->lastInsertId()], 201);
}

fail('unknown action', 404);

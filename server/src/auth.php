<?php

declare(strict_types=1);

function auth_register(array $in): never
{
    $name = trim((string) ($in['name'] ?? ''));
    $email = strtolower(trim((string) ($in['email'] ?? '')));
    $password = (string) ($in['password'] ?? '');
    if ($name === '' || mb_strlen($name) > 80) {
        fail('invalid name', 422);
    }
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        fail('invalid email', 422);
    }
    if (strlen($password) < 8) {
        fail('password too short', 422);
    }
    $db = db();
    $exists = $db->prepare('SELECT id FROM users WHERE email = ? LIMIT 1');
    $exists->execute([$email]);
    if ($exists->fetch()) {
        fail('email already registered', 409);
    }
    $hash = password_hash($password, PASSWORD_DEFAULT);
    $insert = $db->prepare(
        'INSERT INTO users (name, email, password_hash, created_at) VALUES (?, ?, ?, ?)'
    );
    $insert->execute([$name, $email, $hash, time()]);
    $id = (int) $db->lastInsertId();
    $token = issue_token($db, $id);
    respond([
        'token' => $token,
        'user' => ['id' => $id, 'name' => $name, 'email' => $email],
    ], 201);
}

function auth_login(array $in): never
{
    $email = strtolower(trim((string) ($in['email'] ?? '')));
    $password = (string) ($in['password'] ?? '');
    $db = db();
    $query = $db->prepare('SELECT id, name, email, password_hash FROM users WHERE email = ? LIMIT 1');
    $query->execute([$email]);
    $user = $query->fetch();
    if (!$user || !password_verify($password, (string) $user['password_hash'])) {
        fail('invalid credentials', 401);
    }
    $token = issue_token($db, (int) $user['id']);
    respond([
        'token' => $token,
        'user' => [
            'id' => (int) $user['id'],
            'name' => (string) $user['name'],
            'email' => (string) $user['email'],
        ],
    ]);
}

function auth_logout(): never
{
    $token = bearer();
    if ($token !== '') {
        $db = db();
        $delete = $db->prepare('DELETE FROM tokens WHERE token_hash = ?');
        $delete->execute([hash('sha256', $token)]);
    }
    respond(['ok' => true]);
}

function auth_forgot(array $in): never
{
    $email = strtolower(trim((string) ($in['email'] ?? '')));
    if ($email === '') {
        fail('email required', 422);
    }
    respond(['ok' => true]);
}

function issue_token(PDO $db, int $userId): string
{
    $token = bin2hex(random_bytes(32));
    $insert = $db->prepare(
        'INSERT INTO tokens (token_hash, user_id, created_at, last_used) VALUES (?, ?, ?, ?)'
    );
    $insert->execute([hash('sha256', $token), $userId, time(), time()]);
    return $token;
}

function current_user(): array
{
    $token = bearer();
    if ($token === '') {
        fail('missing token', 401);
    }
    $db = db();
    $hash = hash('sha256', $token);
    $query = $db->prepare(
        'SELECT u.id, u.name, u.email, u.points, u.xp, u.level
         FROM tokens t JOIN users u ON u.id = t.user_id
         WHERE t.token_hash = ? LIMIT 1'
    );
    $query->execute([$hash]);
    $user = $query->fetch();
    if (!$user) {
        fail('invalid token', 401);
    }
    $touch = $db->prepare('UPDATE tokens SET last_used = ? WHERE token_hash = ?');
    $touch->execute([time(), $hash]);
    return [
        'id' => (int) $user['id'],
        'name' => (string) $user['name'],
        'email' => (string) $user['email'],
        'points' => (int) $user['points'],
        'xp' => (int) $user['xp'],
        'level' => (int) $user['level'],
    ];
}

<?php

declare(strict_types=1);

require dirname(__DIR__) . '/src/bootstrap.php';

session_start();

$adminToken = (string) ($config['admin_token'] ?? '');
if ($adminToken === '') {
    http_response_code(503);
    header('Content-Type: text/html; charset=utf-8');
    echo 'admin_token is not configured';
    exit;
}

if (isset($_POST['token'])) {
    if (hash_equals($adminToken, (string) $_POST['token'])) {
        $_SESSION['admin_ok'] = true;
    } else {
        $_SESSION['admin_ok'] = false;
    }
    header('Location: admin.php');
    exit;
}

if (isset($_GET['logout'])) {
    session_destroy();
    header('Location: admin.php');
    exit;
}

if (empty($_SESSION['admin_ok'])) {
    header('Content-Type: text/html; charset=utf-8');
    echo '<!doctype html><html><head><meta charset="utf-8"><title>Y-Dimension Admin</title></head><body>';
    echo '<form method="post"><input type="password" name="token" placeholder="admin token"><button type="submit">login</button></form>';
    echo '</body></html>';
    exit;
}

function table_count(PDO $pdo, string $table): int
{
    try {
        $row = $pdo->query('SELECT COUNT(*) AS c FROM ' . $table)->fetch();
        return (int) ($row['c'] ?? 0);
    } catch (Throwable $error) {
        return -1;
    }
}

$pdo = db();
$page = (string) ($_GET['page'] ?? 'overview');
$tables = ['users', 'vaults', 'shop_items', 'purchases', 'raids', 'raid_members', 'forum_threads', 'forum_posts', 'sync_states'];

header('Content-Type: text/html; charset=utf-8');
echo '<!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">';
echo '<title>Y-Dimension Admin</title><style>body{font-family:system-ui;margin:24px;background:#121025;color:#eee}';
echo 'a{color:#C0AEFF}table{border-collapse:collapse;width:100%;margin-top:12px}td,th{border:1px solid #444;padding:6px;text-align:left}';
echo 'nav a{margin-right:12px}.card{background:#1c1a33;padding:16px;border-radius:12px;margin-bottom:12px}</style></head><body>';
echo '<h1>Y-Dimension Admin</h1><nav><a href="?page=overview">overview</a><a href="?page=users">users</a>';
echo '<a href="?page=vaults">vaults</a><a href="?page=shop">shop</a><a href="?page=raids">raids</a>';
echo '<a href="?page=forum">forum</a><a href="?logout=1">logout</a></nav>';

if ($page === 'overview') {
    echo '<div class="card"><h2>Counts</h2><table><tr><th>Table</th><th>Rows</th></tr>';
    foreach ($tables as $table) {
        echo '<tr><td>' . htmlspecialchars($table) . '</td><td>' . table_count($pdo, $table) . '</td></tr>';
    }
    echo '</table></div>';
}

if ($page === 'users') {
    $rows = $pdo->query('SELECT id, name, email, created_at FROM users ORDER BY id DESC LIMIT 100')->fetchAll();
    echo '<div class="card"><h2>Users</h2><table><tr><th>ID</th><th>Name</th><th>Email</th><th>Created</th></tr>';
    foreach ($rows as $row) {
        echo '<tr><td>' . (int) $row['id'] . '</td><td>' . htmlspecialchars((string) $row['name']) . '</td>';
        echo '<td>' . htmlspecialchars((string) $row['email']) . '</td><td>' . (int) $row['created_at'] . '</td></tr>';
    }
    echo '</table></div>';
}

if ($page === 'vaults') {
    $rows = $pdo->query('SELECT id, user_id, updated_at, LENGTH(payload) AS size FROM vaults ORDER BY updated_at DESC LIMIT 100')->fetchAll();
    echo '<div class="card"><h2>Vaults (encrypted payload only)</h2><table><tr><th>ID</th><th>User</th><th>Bytes</th><th>Updated</th></tr>';
    foreach ($rows as $row) {
        echo '<tr><td>' . (int) $row['id'] . '</td><td>' . (int) $row['user_id'] . '</td>';
        echo '<td>' . (int) $row['size'] . '</td><td>' . (int) $row['updated_at'] . '</td></tr>';
    }
    echo '</table></div>';
}

if ($page === 'shop') {
    $rows = $pdo->query('SELECT id, title, price, active FROM shop_items ORDER BY id DESC LIMIT 100')->fetchAll();
    echo '<div class="card"><h2>Shop</h2><table><tr><th>ID</th><th>Title</th><th>Price</th><th>Active</th></tr>';
    foreach ($rows as $row) {
        echo '<tr><td>' . (int) $row['id'] . '</td><td>' . htmlspecialchars((string) $row['title']) . '</td>';
        echo '<td>' . (int) $row['price'] . '</td><td>' . ((int) $row['active'] === 1 ? 'yes' : 'no') . '</td></tr>';
    }
    echo '</table></div>';
}

if ($page === 'raids') {
    $rows = $pdo->query('SELECT id, title, starts_at, ends_at, target FROM raids ORDER BY id DESC LIMIT 100')->fetchAll();
    echo '<div class="card"><h2>Raids</h2><table><tr><th>ID</th><th>Title</th><th>Target</th><th>Starts</th><th>Ends</th></tr>';
    foreach ($rows as $row) {
        echo '<tr><td>' . (int) $row['id'] . '</td><td>' . htmlspecialchars((string) $row['title']) . '</td>';
        echo '<td>' . (int) $row['target'] . '</td><td>' . (int) $row['starts_at'] . '</td><td>' . (int) $row['ends_at'] . '</td></tr>';
    }
    echo '</table></div>';
}

if ($page === 'forum') {
    $rows = $pdo->query('SELECT id, title, user_id, created_at FROM forum_threads ORDER BY id DESC LIMIT 100')->fetchAll();
    echo '<div class="card"><h2>Forum threads</h2><table><tr><th>ID</th><th>Title</th><th>User</th><th>Created</th></tr>';
    foreach ($rows as $row) {
        echo '<tr><td>' . (int) $row['id'] . '</td><td>' . htmlspecialchars((string) $row['title']) . '</td>';
        echo '<td>' . (int) $row['user_id'] . '</td><td>' . (int) $row['created_at'] . '</td></tr>';
    }
    echo '</table></div>';
}

echo '</body></html>';

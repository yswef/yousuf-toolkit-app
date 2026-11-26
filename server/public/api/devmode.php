<?php

declare(strict_types=1);

// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
require dirname(__DIR__, 2) . '/src/bootstrap.php';

$user = current_user();
$action = (string) ($_GET['action'] ?? 'status');
$testsFile = dirname(__DIR__) . '/panel/data/devmode_tests.json';
$tests = [];
if (is_file($testsFile)) {
    $decoded = json_decode((string) file_get_contents($testsFile), true);
    if (is_array($decoded)) $tests = $decoded['tests'] ?? [];
}
$enabled = array_values(array_filter($tests, function ($t) { return !empty($t['enabled']); }));

if ($action === 'tests') {
    respond(['app_id' => 'y-dimensions', 'tests' => $enabled]);
}

if ($action === 'status') {
    $role = '';
    try {
        $find = db()->prepare(
            'SELECT role FROM developer_accounts WHERE app_id = ? AND user_id = ? AND active = 1 LIMIT 1'
        );
        $find->execute(['y-dimensions', (int) $user['id']]);
        $row = $find->fetch();
        if ($row) $role = (string) $row['role'];
    } catch (Throwable $e) {
        // جدول developer_accounts غير مستورد بعد.
    }
    respond([
        'app_id' => 'y-dimensions',
        'developer' => $role !== '',
        'role' => $role,
        'max_accounts' => 3,
        'tests' => $enabled,
        'user' => ['id' => (int) $user['id'], 'email' => $user['email']],
    ]);
}

fail('unknown action', 404);
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension

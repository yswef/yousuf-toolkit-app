<?php

declare(strict_types=1);

require dirname(__DIR__, 2) . '/src/bootstrap.php';

// Public read-only: active announcements created from the control panel.
try {
    $rows = db()->query(
        'SELECT id, title, body, created_at FROM announcements WHERE active = 1 ORDER BY id DESC LIMIT 20'
    )->fetchAll();
} catch (Throwable $error) {
    $rows = [];
}
respond(['announcements' => $rows]);

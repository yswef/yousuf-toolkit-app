<?php

declare(strict_types=1);

date_default_timezone_set('UTC');

$configFile = dirname(__DIR__) . '/config/config.php';
if (!is_file($configFile)) {
    http_response_code(500);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['error' => 'server/config/config.php is missing']);
    exit;
}

$config = require $configFile;
if (!is_array($config)) {
    http_response_code(500);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['error' => 'invalid config']);
    exit;
}

require_once __DIR__ . '/http.php';
require_once __DIR__ . '/db.php';
require_once __DIR__ . '/auth.php';
require_once __DIR__ . '/sync_rows.php';

cors($config);

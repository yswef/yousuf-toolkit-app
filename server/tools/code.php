<?php

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    exit(1);
}
$configFile = dirname(__DIR__) . '/config/config.php';
if (!is_file($configFile)) {
    fwrite(STDERR, "config.php is missing\n");
    exit(1);
}
$config = require $configFile;
require_once dirname(__DIR__) . '/src/license.php';
$userId = (int) ($argv[1] ?? 0);
$secret = (string) ($config['license_secret'] ?? '');
if ($userId <= 0 || $secret === '') {
    fwrite(STDERR, "usage: php code.php <user_id> (license_secret must be set)\n");
    exit(1);
}
echo license_code($userId, $secret) . PHP_EOL;

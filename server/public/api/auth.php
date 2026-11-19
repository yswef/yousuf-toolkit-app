<?php

declare(strict_types=1);

require dirname(__DIR__, 2) . '/src/bootstrap.php';

$action = (string) ($_GET['action'] ?? '');

switch ($action) {
    case 'register':
        if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
            fail('use POST', 405);
        }
        auth_register(input());
        break;
    case 'login':
        if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
            fail('use POST', 405);
        }
        auth_login(input());
        break;
    case 'logout':
        if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
            fail('use POST', 405);
        }
        auth_logout();
        break;
    case 'forgot':
        if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
            fail('use POST', 405);
        }
        auth_forgot(input());
        break;
    default:
        fail('unknown action', 404);
}

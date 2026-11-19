<?php

declare(strict_types=1);

require dirname(__DIR__) . '/src/bootstrap.php';

respond([
    'ok' => true,
    'name' => 'y-dimension',
    'time' => time(),
    'version' => '0.5.0',
]);

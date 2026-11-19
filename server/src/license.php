<?php

declare(strict_types=1);

function license_code(int $userId, string $secret): string
{
    $raw = strtoupper(substr(hash_hmac('sha256', (string) $userId, $secret), 0, 16));
    return 'YD-' . implode('-', str_split($raw, 4));
}

<?php

declare(strict_types=1);

function sync_rows(PDO $db, string $sql, int $userId, array $rows, callable $map): int
{
    if ($rows === []) {
        return 0;
    }
    $statement = $db->prepare($sql);
    $count = 0;
    foreach (array_slice($rows, 0, 500) as $row) {
        if (!is_array($row)) {
            continue;
        }
        $statement->execute(array_merge([$userId], $map($row)));
        $count++;
    }
    return $count;
}

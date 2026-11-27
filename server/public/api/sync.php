<?php

/* Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension */

declare(strict_types=1);

require dirname(__DIR__, 2) . '/src/bootstrap.php';

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
    fail('use POST', 405);
}

$user = current_user();
$in = input();
$db = db();
$db->beginTransaction();

try {
    $points = max(0, (int) ($in['points'] ?? 0));
    $xp = max(0, (int) ($in['xp'] ?? 0));
    $level = max(1, (int) ($in['level'] ?? 1));
    $update = $db->prepare('UPDATE users SET points = ?, xp = ?, level = ? WHERE id = ?');
    $update->execute([$points, $xp, $level, $user['id']]);

    $profile = is_array($in['profile'] ?? null) ? $in['profile'] : [];
    $profileStmt = $db->prepare(
        'INSERT INTO profiles (user_id, name, age, goal, team, updated_at)
         VALUES (?, ?, ?, ?, ?, ?)
         ON DUPLICATE KEY UPDATE name = VALUES(name), age = VALUES(age), goal = VALUES(goal),
         team = VALUES(team), updated_at = VALUES(updated_at)'
    );
    $profileStmt->execute([
        $user['id'],
        substr((string) ($profile['name'] ?? ''), 0, 80),
        max(0, (int) ($profile['age'] ?? 0)),
        substr((string) ($profile['goal'] ?? ''), 0, 40),
        substr((string) ($profile['team'] ?? ''), 0, 40),
        time(),
    ]);

    $journalCount = sync_rows(
        $db,
        'INSERT INTO journal (user_id, client_id, title, body, occurred_at, mood, background, rich_body, font_name, colors, drawings, stickers, images, emoji_pack, sticker_pack, voice_path, has_time, synced_at)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
         ON DUPLICATE KEY UPDATE title = VALUES(title), body = VALUES(body), occurred_at = VALUES(occurred_at),
         mood = VALUES(mood), background = VALUES(background), rich_body = VALUES(rich_body), font_name = VALUES(font_name),
         colors = VALUES(colors), drawings = VALUES(drawings), stickers = VALUES(stickers), images = VALUES(images),
         emoji_pack = VALUES(emoji_pack), sticker_pack = VALUES(sticker_pack), voice_path = VALUES(voice_path),
         has_time = VALUES(has_time), synced_at = VALUES(synced_at)',
        $user['id'],
        is_array($in['journal'] ?? null) ? $in['journal'] : [],
        static function (array $row): array {
            return [
                (int) ($row['id'] ?? 0),
                substr((string) ($row['title'] ?? ''), 0, 160),
                substr((string) ($row['body'] ?? ''), 0, 5000),
                (int) ($row['at'] ?? 0),
                substr((string) ($row['mood'] ?? ''), 0, 16),
                substr((string) ($row['background'] ?? 'cream'), 0, 40),
                substr((string) ($row['richBody'] ?? ''), 0, 200000),
                substr((string) ($row['font'] ?? 'Cairo'), 0, 40),
                substr((string) ($row['colors'] ?? ''), 0, 160),
                substr((string) ($row['drawings'] ?? ''), 0, 200000),
                substr((string) ($row['stickers'] ?? ''), 0, 200000),
                substr((string) ($row['images'] ?? ''), 0, 200000),
                substr((string) ($row['emojiPack'] ?? 'free-1'), 0, 40),
                substr((string) ($row['stickerPack'] ?? 'free-1'), 0, 40),
                substr((string) ($row['voicePath'] ?? ''), 0, 255),
                !empty($row['hasTime']) ? 1 : 0,
                (int) ($row['syncedAt'] ?? 0),
            ];
        }
    );

    $ledgerCount = sync_rows(
        $db,
        'INSERT INTO ledger (user_id, client_id, title, amount, income, category, occurred_at)
         VALUES (?, ?, ?, ?, ?, ?, ?)
         ON DUPLICATE KEY UPDATE title = VALUES(title), amount = VALUES(amount), income = VALUES(income),
         category = VALUES(category), occurred_at = VALUES(occurred_at)',
        $user['id'],
        is_array($in['ledger'] ?? null) ? $in['ledger'] : [],
        static function (array $row): array {
            $title = substr((string) ($row['title'] ?? ''), 0, 120);
            $note = substr((string) ($row['note'] ?? ''), 0, 200);
            if ($note !== '') {
                $title = substr($title . ' — ' . $note, 0, 160);
            }
            return [
                (int) ($row['id'] ?? 0),
                $title,
                (float) ($row['amount'] ?? 0),
                !empty($row['income']) ? 1 : 0,
                substr((string) ($row['category'] ?? 'other'), 0, 40),
                (int) ($row['at'] ?? 0),
            ];
        }
    );

    $bookCount = sync_rows(
        $db,
        'INSERT INTO books (user_id, client_id, title, page, pages)
         VALUES (?, ?, ?, ?, ?)
         ON DUPLICATE KEY UPDATE title = VALUES(title), page = VALUES(page), pages = VALUES(pages)',
        $user['id'],
        is_array($in['books'] ?? null) ? $in['books'] : [],
        static function (array $row): array {
            return [
                (int) ($row['id'] ?? 0),
                substr((string) ($row['title'] ?? ''), 0, 160),
                max(0, (int) ($row['page'] ?? 0)),
                max(1, (int) ($row['pages'] ?? 1)),
            ];
        }
    );

    $walletCount = sync_rows(
        $db,
        'INSERT INTO wallets (user_id, client_id, name, balance)
         VALUES (?, ?, ?, ?)
         ON DUPLICATE KEY UPDATE name = VALUES(name), balance = VALUES(balance)',
        $user['id'],
        is_array($in['wallets'] ?? null) ? $in['wallets'] : [],
        static function (array $row): array {
            return [
                (int) ($row['id'] ?? 0),
                substr((string) ($row['name'] ?? ''), 0, 80),
                (float) ($row['balance'] ?? 0),
            ];
        }
    );

    $db->commit();
    respond([
        'ok' => true,
        'server_time' => time(),
        'synced' => [
            'journal' => $journalCount,
            'ledger' => $ledgerCount,
            'books' => $bookCount,
            'wallets' => $walletCount,
        ],
    ]);
} catch (Throwable $error) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    fail('sync failed', 500);
}
/* Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension */

<?php

declare(strict_types=1);

require dirname(__DIR__, 2) . '/src/bootstrap.php';

$user = current_user();
$action = (string) ($_GET['action'] ?? 'info');
$db = db();

function active_season(PDO $db): ?array
{
    $now = time();
    $query = $db->prepare(
        'SELECT id, goal, starts_at, ends_at FROM raid_seasons
         WHERE starts_at <= ? AND ends_at > ? ORDER BY id DESC LIMIT 1'
    );
    $query->execute([$now, $now]);
    $season = $query->fetch();
    return $season ?: null;
}

if ($action === 'info') {
    $season = active_season($db);
    if (!$season) {
        respond(['season' => 0, 'progress' => 0, 'goal' => 0, 'members' => 0, 'rank' => 0]);
    }
    $sum = $db->prepare('SELECT COALESCE(SUM(progress), 0) FROM raid_members WHERE season_id = ?');
    $sum->execute([$season['id']]);
    $progress = (int) $sum->fetchColumn();
    $members = $db->prepare('SELECT COUNT(*) FROM raid_members WHERE season_id = ?');
    $members->execute([$season['id']]);
    $memberCount = (int) $members->fetchColumn();
    $mine = $db->prepare('SELECT COALESCE(progress, 0) FROM raid_members WHERE season_id = ? AND user_id = ?');
    $mine->execute([$season['id'], $user['id']]);
    $myProgress = (int) ($mine->fetchColumn() ?: 0);
    $rankQuery = $db->prepare(
        'SELECT COUNT(*) + 1 FROM raid_members WHERE season_id = ? AND progress > ?'
    );
    $rankQuery->execute([$season['id'], $myProgress]);
    respond([
        'season' => (int) $season['id'],
        'progress' => $progress,
        'goal' => (int) $season['goal'],
        'members' => $memberCount,
        'rank' => (int) $rankQuery->fetchColumn(),
    ]);
}

if ($action !== 'checkin') {
    fail('unknown action', 404);
}
if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
    fail('use POST', 405);
}

$season = active_season($db);
if (!$season) {
    fail('no active season', 409);
}
$cooldown = max(0, (int) ($GLOBALS['config']['raid_cooldown_seconds'] ?? 72000));
$now = time();
$memberQuery = $db->prepare('SELECT progress, last_checkin FROM raid_members WHERE season_id = ? AND user_id = ?');
$memberQuery->execute([$season['id'], $user['id']]);
$member = $memberQuery->fetch();
if ($member) {
    $elapsed = $now - (int) $member['last_checkin'];
    if ($elapsed < $cooldown) {
        respond([
            'connected' => false,
            'next_in_seconds' => $cooldown - $elapsed,
            'day' => (int) floor(($now - (int) $season['starts_at']) / 86400) + 1,
        ]);
    }
    $update = $db->prepare('UPDATE raid_members SET progress = progress + 1, last_checkin = ? WHERE season_id = ? AND user_id = ?');
    $update->execute([$now, $season['id'], $user['id']]);
    $progress = (int) $member['progress'] + 1;
} else {
    $insert = $db->prepare('INSERT INTO raid_members (season_id, user_id, progress, last_checkin) VALUES (?, ?, 1, ?)');
    $insert->execute([$season['id'], $user['id'], $now]);
    $progress = 1;
}
respond([
    'connected' => true,
    'next_in_seconds' => $cooldown,
    'day' => (int) floor(($now - (int) $season['starts_at']) / 86400) + 1,
    'progress' => $progress,
]);

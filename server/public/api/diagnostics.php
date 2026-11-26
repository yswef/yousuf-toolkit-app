<?php

declare(strict_types=1);

// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
// بلاغات تشخيص مجهّلة بالكامل: لا هوية، لا IMEI، لا رقم هاتف، لا SSID حقيقي، لا موقع.
require dirname(__DIR__, 2) . '/src/bootstrap.php';

$action = (string) ($_GET['action'] ?? 'match');
$config = $GLOBALS['config'] ?? [];

function diagnostics_solution(string $signature): ?string {
    try {
        $find = db()->prepare(
            'SELECT solution_ar FROM diagnostic_solutions WHERE app_id = ? AND issue_signature = ? LIMIT 1'
        );
        $find->execute(['y-dimensions', $signature]);
        $row = $find->fetch();
        return $row ? (string) $row['solution_ar'] : null;
    } catch (Throwable $e) {
        return null;
    }
}

function diagnostics_clean($value): bool {
    if (!is_array($value)) return true;
    $forbidden = ['imei', 'phone', 'phone_number', 'email', 'google_account', 'ssid', 'location', 'latitude', 'longitude', 'photo', 'image', 'contact', 'password', 'token'];
    foreach ($value as $k => $v) {
        $key = strtolower((string) $k);
        foreach ($forbidden as $bad) {
            if (str_contains($key, $bad)) return false;
        }
        if (is_array($v) && !diagnostics_clean($v)) return false;
    }
    return true;
}

if ($action === 'match') {
    current_user();
    $signature = (string) ($_GET['issue_signature'] ?? '');
    if ($signature === '') {
        $in = input();
        $signature = (string) ($in['issue_signature'] ?? '');
    }
    $signature = mb_substr(preg_replace('/[^a-z0-9_]/i', '_', $signature) ?? '', 0, 120);
    if ($signature === '') fail('issue_signature required', 422);
    $solution = diagnostics_solution($signature);
    respond(['known' => $solution !== null, 'solution_ar' => $solution, 'issue_signature' => $signature]);
}

if ($action === 'report') {
    current_user();
    if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') fail('use POST', 405);
    $in = input();
    $report = isset($in['server_report']) && is_array($in['server_report']) ? $in['server_report'] : $in;
    if (!diagnostics_clean($report)) fail('report contains identity fields', 422);
    $signature = (string) ($report['issue_signature'] ?? '');
    $signature = mb_substr(preg_replace('/[^a-z0-9_]/i', '_', $signature) ?? '', 0, 120);
    if ($signature === '') fail('issue_signature required', 422);
    $allowed = ['issue_signature', 'symptoms', 'device_model', 'android_version', 'rom_version',
        'relevant_readings', 'diagnosis_ranked_causes', 'attempted_fix_actions', 'resolved_locally'];
    $clean = [];
    foreach ($allowed as $key) {
        if (array_key_exists($key, $report)) $clean[$key] = $report[$key];
    }
    $clean['resolved_locally'] = !empty($clean['resolved_locally']);
    try {
        $insert = db()->prepare(
            'INSERT INTO diagnostic_reports
             (app_id, issue_signature, symptoms, device_model, android_version, rom_version,
              relevant_readings, diagnosis_ranked_causes, attempted_fix_actions, resolved_locally, created_at)
             VALUES (?,?,?,?,?,?,?,?,?,?,?)'
        );
        $insert->execute([
            'y-dimensions', $signature,
            json_encode($clean['symptoms'] ?? [], JSON_UNESCAPED_UNICODE),
            mb_substr((string) ($clean['device_model'] ?? ''), 0, 120),
            mb_substr((string) ($clean['android_version'] ?? ''), 0, 40),
            mb_substr((string) ($clean['rom_version'] ?? ''), 0, 120),
            json_encode($clean['relevant_readings'] ?? new stdClass(), JSON_UNESCAPED_UNICODE),
            json_encode($clean['diagnosis_ranked_causes'] ?? [], JSON_UNESCAPED_UNICODE),
            json_encode($clean['attempted_fix_actions'] ?? [], JSON_UNESCAPED_UNICODE),
            $clean['resolved_locally'] ? 1 : 0, time(),
        ]);
    } catch (Throwable $e) {
        fail('diagnostic_reports table missing — import 004_devmode.sql', 500);
    }
    respond([
        'ok' => true,
        'stored' => true,
        'known' => diagnostics_solution($signature) !== null,
        'solution_ar' => diagnostics_solution($signature),
    ]);
}

if ($action === 'list') {
    $token = (string) ($_SERVER['HTTP_X_ADMIN_TOKEN'] ?? '');
    $expected = (string) ($config['admin_token'] ?? '');
    if ($expected === '' || !hash_equals($expected, $token)) fail('unauthorized', 401);
    try {
        $rows = db()->prepare(
            'SELECT id, issue_signature, device_model, android_version, rom_version, created_at
             FROM diagnostic_reports WHERE app_id = ? ORDER BY id DESC LIMIT 100'
        );
        $rows->execute(['y-dimensions']);
        respond(['reports' => $rows->fetchAll()]);
    } catch (Throwable $e) {
        respond(['reports' => []]);
    }
}

fail('unknown action', 404);
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension

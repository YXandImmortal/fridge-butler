<?php
/**
 * 冰箱管家 - 前端入口文件
 *
 * 功能：检测维护模式标志文件，决定是否展示维护页面
 * 部署说明：
 *   1. 将此文件与 maintenance.html、maintenance-config.php 一同上传到服务器网站根目录
 *   2. 确保服务器默认文档中 index.php 排在 index.html 之前
 *   3. 维护开关通过 enable-maintenance.php / disable-maintenance.php 控制
 */

// =========================================================
// 加载敏感配置（白名单等）
// =========================================================

$configFile = __DIR__ . '/maintenance-config.php';
$config = file_exists($configFile) ? require $configFile : [];

// =========================================================
// 配置区域
// =========================================================

// 维护模式标志文件路径
$maintenanceFile = __DIR__ . '/.maintenance';

// 维护期间仍可正常访问的白名单 IP（从配置文件读取，可覆盖）
$whiteListIps = $config['whitelist_ips'] ?? [];

// 维护页面文件路径
$maintenancePage = __DIR__ . '/maintenance.html';

// 正常前端入口文件路径
$indexPage = __DIR__ . '/index.html';

// =========================================================
// 维护模式检测逻辑
// =========================================================

$isMaintenance = file_exists($maintenanceFile);
$clientIp = $_SERVER['REMOTE_ADDR'] ?? '0.0.0.0';

// 支持通过 X-Forwarded-For 获取真实 IP（如果使用了 CDN/代理）
if (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
    $forwardedIps = explode(',', $_SERVER['HTTP_X_FORWARDED_FOR']);
    $clientIp = trim($forwardedIps[0]);
}

$isWhiteList = in_array($clientIp, $whiteListIps, true);

if ($isMaintenance && !$isWhiteList) {
    // 维护模式且不在白名单 → 展示维护页面
    http_response_code(503); // Service Unavailable
    header('Retry-After: 3600'); // 告诉搜索引擎 1 小时后重试
    header('Content-Type: text/html; charset=utf-8');
    header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
    header('Pragma: no-cache');

    if (file_exists($maintenancePage)) {
        readfile($maintenancePage);
    } else {
        echo '<h1>503 Service Unavailable</h1><p>系统维护中，请稍后再试。</p>';
    }
    exit;
}

// =========================================================
// 正常访问 → 返回前端入口
// =========================================================

header('Content-Type: text/html; charset=utf-8');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Pragma: no-cache');

if (file_exists($indexPage)) {
    readfile($indexPage);
} else {
    echo '<h1>Welcome</h1><p>网站正常运行中。</p>';
}

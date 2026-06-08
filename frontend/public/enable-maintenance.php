<?php
/**
 * 开启系统维护模式
 *
 * 安全验证：
 *   本脚本涉及网站启停，必须配置访问密码或 IP 白名单，防止被恶意访问。
 *   敏感配置统一存放在 maintenance-config.php 中，该文件已被加入 .gitignore。
 *
 * 使用方法：
 *   浏览器访问：https://你的域名/enable-maintenance.php?token=你设置的密码
 */

// =========================================================
// 加载敏感配置
// =========================================================

$configFile = __DIR__ . '/maintenance-config.php';

if (!file_exists($configFile)) {
    http_response_code(500);
    header('Content-Type: text/html; charset=utf-8');
    echo '<h1>500 Internal Server Error</h1>';
    echo '<p>配置文件缺失：请确保 maintenance-config.php 已上传到服务器。</p>';
    echo '<p>参考模板：maintenance-config.php.example</p>';
    exit;
}

$config = require $configFile;

$accessToken = $config['access_token'] ?? '';
$adminIps = $config['admin_ips'] ?? [];

// =========================================================
// 验证逻辑（密码 + IP 双重验证）
// =========================================================

$clientIp = $_SERVER['REMOTE_ADDR'] ?? '0.0.0.0';

// 支持通过 X-Forwarded-For 获取真实 IP（如果使用了 CDN/代理）
if (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
    $forwardedIps = explode(',', $_SERVER['HTTP_X_FORWARDED_FOR']);
    $clientIp = trim($forwardedIps[0]);
}

$requestToken = $_GET['token'] ?? '';
$isIpAllowed = empty($adminIps) || in_array($clientIp, $adminIps, true);
$isTokenValid = !empty($accessToken) && hash_equals($accessToken, $requestToken);

// 双重验证失败
if (!$isIpAllowed || !$isTokenValid) {
    http_response_code(403);
    header('Content-Type: text/html; charset=utf-8');
    echo '<h1>403 Forbidden</h1><p>无权访问此页面。</p>';
    exit;
}

// =========================================================
// 执行开启维护
// =========================================================

$maintenanceFile = __DIR__ . '/.maintenance';

if (file_exists($maintenanceFile)) {
    $startedAt = file_get_contents($maintenanceFile) ?: time();
    $startedTime = date('Y-m-d H:i:s', (int)$startedAt);
    echo "✅ 维护模式已经是开启状态<br>";
    echo "⏰ 开启时间：{$startedTime}<br>";
    echo "⏳ 已持续：" . humanDuration(time() - (int)$startedAt) . "<br>";
} else {
    $now = time();
    file_put_contents($maintenanceFile, (string)$now);
    $startedTime = date('Y-m-d H:i:s', $now);
    echo "✅ 维护模式已开启<br>";
    echo "⏰ 开启时间：{$startedTime}<br>";
    echo "💡 提示：访问 <a href=\"disable-maintenance.php?token=" . htmlspecialchars($accessToken, ENT_QUOTES, 'UTF-8') . "\">disable-maintenance.php</a> 可关闭维护模式<br>";
}

echo "<br>📝 当前访问 IP：{$clientIp}<br>";

/**
 * 将秒数转换为人类可读的持续时间
 */
function humanDuration(int $seconds): string
{
    if ($seconds < 60) {
        return "{$seconds}秒";
    }
    if ($seconds < 3600) {
        $m = floor($seconds / 60);
        $s = $seconds % 60;
        return "{$m}分{$s}秒";
    }
    $h = floor($seconds / 3600);
    $m = floor(($seconds % 3600) / 60);
    return "{$h}小时{$m}分";
}

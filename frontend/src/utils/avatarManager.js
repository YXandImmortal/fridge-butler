/**
 * 系统头像管理工具
 * 用于管理系统预设的头像
 */

/**
 * 获取所有系统预设头像ID
 * @returns {Array} 头像ID数组
 */
export function getSystemAvatarIds() {
    // 系统预设头像列表
    const avatarFiles = [
        'egg.svg',
        'ice.svg',
        'leaf.svg',
        'milk.svg',
        'orange.svg',
        'snowflake.svg'
    ];

    // 提取头像ID（去掉文件扩展名）
    return avatarFiles.map(file => file.replace('.svg', ''));
}

/**
 * 获取头像的完整路径
 * @param {string} avatarId 头像ID
 * @returns {string} 头像完整路径
 */
export function getAvatarPath(avatarId) {
    return `/avatars/${avatarId}.svg`;
}

/**
 * 检查是否为有效的系统头像ID
 * @param {string} avatarId 头像ID
 * @returns {boolean} 是否为有效头像ID
 */
export function isValidSystemAvatar(avatarId) {
    const avatarIds = getSystemAvatarIds();
    return avatarIds.includes(avatarId);
}
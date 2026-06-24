package com.yx.fridgebutler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 徽章编码枚举。
 * <p>定义所有徽章的元数据信息，包括编码、名称、图标类、描述、经验值奖励和解锁条件说明。</p>
 */
@Getter
@AllArgsConstructor
public enum BadgeCode {

    /** 首次添加食材 */
    FIRST_ITEM("first_item", "初出茅庐", "icon-a-Number1Badge", "首次添加食材", 50, "添加第一个食材"),
    /** 管理冰箱数达到3个 */
    FRIDGE_MASTER("fridge_master", "冰箱达人", "icon-a-AcademyAward", "管理冰箱数达到3个", 50, "管理3个冰箱"),
    /** 连续30天无过期 */
    ZERO_WASTE("zero_waste", "零浪费先锋", "icon-a-MovieAward", "连续30天无过期", 100, "连续30天无过期"),
    /** 累计取出临期食材10次 */
    PROPHET("prophet", "预言家", "icon-a-GlassShieldAward", "累计取出临期食材10次", 50, "取出临期食材10次"),
    /** 累计AI菜谱推荐10次 */
    CHEF_COOK("chef_cook", "大厨认证", "icon-Goblet", "累计AI菜谱推荐10次", 50, "AI菜谱推荐10次"),
    /** 累计查看数据中心50次 */
    DATA_MASTER("data_master", "数据控", "icon-a-StarCertificate", "累计查看数据中心50次", 50, "查看数据中心50次"),
    /** 在23:00-05:00操作累计5次 */
    NIGHT_OWL("night_owl", "夜猫子", "icon-a-PrizeAward1", "在23:00-05:00操作累计5次", 30, "夜间操作5次"),
    /** 在05:00-08:00操作累计5次 */
    EARLY_BIRD("early_bird", "早起鸟", "icon-a-PrizeTrophy", "在05:00-08:00操作累计5次", 30, "早起操作5次"),
    /** 注册满1年 */
    ANNIVERSARY("anniversary", "周年用户", "icon-cake", "注册满1年", 100, "注册满1年"),
    /** 连续冰鲜90天 */
    STREAK_GUARDIAN("streak_guardian", "冰鲜守护者", "icon-a-WinnerCup", "连续冰鲜90天", 150, "连续冰鲜90天"),
    /** 保鲜评分达到100分 */
    PERFECT_FRESHNESS("perfect_freshness", "满分保鲜", "icon-a-WinnerTrophy", "保鲜评分达到100分", 100, "保鲜评分100分"),
    /** 累计管理食材500个 */
    DIAMOND_BUTLER("diamond_butler", "钻石管家", "icon-a-PremiumBanner", "累计管理食材500个", 100, "管理食材500个"),
    /** 完成邮箱绑定 */
    SECURITY_EXPERT("security_expert", "安全达人", "icon-a-AwardShield", "完成邮箱绑定", 50, "绑定邮箱"),
    /** 完成新手指引 */
    GUIDE_COMPLETE("guide_complete", "指引完成者", "icon-a-FlutteringFlag", "完成新手指引", 50, "完成新手指引"),
    /** 单日整理≥10件，累计5次 */
    ORGANIZE_EXPERT("organize_expert", "整理专家", "icon-a-Glasstrophy", "单日整理≥10件，累计5次", 50, "单日整理10件×5次"),
    /** 累计AI对话100次 */
    AI_FRIEND("ai_friend", "AI好友", "icon-deepseek", "累计AI对话100次", 50, "AI对话100次"),
    /** 首次创建采购方案 */
    FIRST_PURCHASE_PLAN("first_purchase_plan", "采购新手", "icon-a-MedallionAward1", "首次创建采购方案", 50, "首次创建采购方案"),
    /** 累计批量入库10次 */
    BATCH_MASTER("batch_master", "批量达人", "icon-a-ChampionAward", "累计批量入库10次", 50, "批量入库10次"),
    /** 完成3次特殊场景生成的采购方案 */
    PARTY_PLANNER("party_planner", "派对策划师", "icon-a-StarAward", "完成3次特殊场景生成的采购方案", 100, "完成3次特殊场景生成计划"),
    /** 连续5次实际与计划偏差≤10% */
    ZERO_WASTE_SHOPPER("zero_waste_shopper", "精明采购", "icon-a-AchievementTrophy", "连续5次实际与计划偏差≤10%", 50, "连续5次精准采购");

    /**
     * 徽章唯一编码。
     */
    private final String code;

    /**
     * 徽章名称。
     */
    private final String name;

    /**
     * 图标字体类名（前端 iconfont 使用）。
     */
    private final String iconClass;

    /**
     * 徽章描述。
     */
    private final String description;

    /**
     * 解锁后获得的经验值奖励。
     */
    private final int expReward;

    /**
     * 解锁条件说明（前端展示用）。
     */
    private final String unlockConditionDesc;

    /**
     * 根据编码查找徽章枚举。
     *
     * @param code 徽章编码
     * @return 徽章枚举，找不到则返回 null
     */
    public static BadgeCode fromCode(String code) {
        for (BadgeCode badge : values()) {
            if (badge.getCode().equals(code)) {
                return badge;
            }
        }
        return null;
    }
}

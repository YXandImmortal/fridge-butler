package com.yx.fridgebutler.config;

import com.yx.fridgebutler.entity.BizFridgeType;
import com.yx.fridgebutler.entity.BizItemCategory;
import com.yx.fridgebutler.entity.BizItemUnit;
import com.yx.fridgebutler.entity.BizUnitType;
import com.yx.fridgebutler.repository.BizFridgeTypeRepository;
import com.yx.fridgebutler.repository.BizItemCategoryRepository;
import com.yx.fridgebutler.repository.BizItemUnitRepository;
import com.yx.fridgebutler.repository.BizUnitTypeRepository;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务系统默认数据初始化器。
 * <p>应用启动时自动检测并初始化单位类型、物品分类、物品单位等系统默认数据。
 * 支持幂等执行：已存在的系统默认数据不会重复插入。</p>
 * <p>初始化顺序（严格）：</p>
 * <ol>
 *   <li>BizUnitType（单位类型）</li>
 *   <li>BizItemCategory（物品分类）</li>
 *   <li>BizItemUnit（物品单位，依赖单位类型的自增ID）</li>
 * </ol>
 */
@Component
@ConditionalOnProperty(
        prefix = "system.data.init",
        name = "enabled",
        havingValue = "true"
)
public class BizSystemDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BizSystemDataInitializer.class);

    /**
     * 系统默认单位类型及其下属单位定义。
     * 使用 LinkedHashMap 保持稳定的遍历顺序。
     */
    private static final Map<String, List<String>> UNIT_TYPE_UNIT_MAP;

    static {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("重量", List.of("克", "千克", "斤", "公斤", "两", "磅", "盎司"));
        map.put("容量", List.of("毫升", "升"));
        map.put("数量", List.of("个", "只", "盒", "瓶", "罐", "包", "袋", "箱", "颗", "支", "把", "块", "片", "根", "条", "卷"));
        map.put("长度", List.of("厘米", "米"));
        UNIT_TYPE_UNIT_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * 系统默认物品分类列表。
     */
    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "蔬菜", "水果", "肉类", "海鲜", "蛋奶", "饮料", "调味品",
            "熟食", "烘焙", "冷冻食品", "粮食", "零食", "豆制品", "酱料"
    );

    /**
     * 系统默认冰箱类型列表。
     * <p>严格按照预设 id 和 type_name 初始化，保持顺序稳定。</p>
     */
    private static final List<FridgeTypeDef> DEFAULT_FRIDGE_TYPES = List.of(
            new FridgeTypeDef(1L, "单门冰箱"),
            new FridgeTypeDef(2L, "双门冰箱"),
            new FridgeTypeDef(3L, "三门冰箱"),
            new FridgeTypeDef(4L, "对开门冰箱"),
            new FridgeTypeDef(5L, "十字对开门"),
            new FridgeTypeDef(6L, "T型三门"),
            new FridgeTypeDef(7L, "法式多门冰箱"),
            new FridgeTypeDef(8L, "日式多门冰箱")
    );

    /**
     * 冰箱类型初始化固定时间戳（UTC）。
     * <p>对应北京时间 2026-05-21 20:40:32。</p>
     */
    private static final Instant FRIDGE_TYPE_INIT_TIME = Instant.parse("2026-05-21T12:40:32Z");

    @Autowired
    private BizFridgeTypeRepository fridgeTypeRepository;

    @Autowired
    private BizUnitTypeRepository unitTypeRepository;

    @Autowired
    private BizItemCategoryRepository categoryRepository;

    @Autowired
    private BizItemUnitRepository itemUnitRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        log.info("========== 业务系统默认数据初始化开始 ==========");

        Map<String, BizUnitType> unitTypeMap = initUnitTypes();
        initItemCategories();
        initItemUnits(unitTypeMap);
        initFridgeTypes();

        log.info("========== 业务系统默认数据初始化结束 ==========");
    }

    /**
     * 初始化系统默认单位类型。
     *
     * @return 单位类型名称到实体的映射，供后续物品单位关联使用
     */
    private Map<String, BizUnitType> initUnitTypes() {
        List<BizUnitType> existing = unitTypeRepository.findByIsSystemDefaultTrueAndIsDeletedFalse();
        Map<String, BizUnitType> map = existing.stream()
                .collect(Collectors.toMap(BizUnitType::getUnitTypeName, Function.identity()));

        List<BizUnitType> toSave = new ArrayList<>();
        for (String name : UNIT_TYPE_UNIT_MAP.keySet()) {
            if (!map.containsKey(name)) {
                BizUnitType type = new BizUnitType();
                type.setUnitTypeName(name);
                type.setIsSystemDefault(true);
                type.setOwnerId(null);
                type.setIsDeleted(false);
                type.setCreateTime(Instant.now());
                type.setUpdateTime(Instant.now());
                toSave.add(type);
                log.info("准备创建系统默认单位类型: {}", name);
            }
        }

        if (!toSave.isEmpty()) {
            List<BizUnitType> saved = unitTypeRepository.saveAll(toSave);
            for (BizUnitType t : saved) {
                map.put(t.getUnitTypeName(), t);
            }
            log.info("系统默认单位类型初始化完成，本次新增 {} 条。", saved.size());
        } else {
            log.info("系统默认单位类型已存在，跳过初始化。");
        }

        return map;
    }

    /**
     * 初始化系统默认物品分类。
     */
    private void initItemCategories() {
        List<BizItemCategory> existing = categoryRepository.findByIsSystemDefaultTrueAndIsDeletedFalse();
        Set<String> existingNames = existing.stream()
                .map(BizItemCategory::getCategoryName)
                .collect(Collectors.toSet());

        List<BizItemCategory> toSave = new ArrayList<>();
        for (String name : DEFAULT_CATEGORIES) {
            if (!existingNames.contains(name)) {
                BizItemCategory category = new BizItemCategory();
                category.setCategoryName(name);
                category.setIsSystemDefault(true);
                category.setOwnerId(null);
                category.setIsDeleted(false);
                category.setCreateTime(Instant.now());
                category.setUpdateTime(Instant.now());
                toSave.add(category);
            }
        }

        if (!toSave.isEmpty()) {
            categoryRepository.saveAll(toSave);
            log.info("系统默认物品分类初始化完成，本次新增 {} 条。", toSave.size());
        } else {
            log.info("系统默认物品分类已存在，跳过初始化。");
        }
    }

    /**
     * 初始化系统默认物品单位。
     * <p>依赖 {@link #initUnitTypes()} 返回的单位类型实体，以获取正确的自增ID进行关联。</p>
     *
     * @param unitTypeMap 单位类型名称到实体的映射
     */
    private void initItemUnits(Map<String, BizUnitType> unitTypeMap) {
        List<BizItemUnit> existing = itemUnitRepository.findByIsSystemDefaultTrueAndIsDeletedFalse();
        Map<Long, Set<String>> existingUnitMap = existing.stream()
                .collect(Collectors.groupingBy(
                        BizItemUnit::getUnitTypeId,
                        Collectors.mapping(BizItemUnit::getUnitName, Collectors.toSet())
                ));

        List<BizItemUnit> toSave = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : UNIT_TYPE_UNIT_MAP.entrySet()) {
            String typeName = entry.getKey();
            List<String> unitNames = entry.getValue();
            BizUnitType type = unitTypeMap.get(typeName);

            if (type == null) {
                log.warn("单位类型 [{}] 未找到，跳过其下属单位初始化。", typeName);
                continue;
            }

            Set<String> existingUnits = existingUnitMap.getOrDefault(type.getId(), Set.of());
            for (String unitName : unitNames) {
                if (!existingUnits.contains(unitName)) {
                    BizItemUnit unit = new BizItemUnit();
                    unit.setUnitName(unitName);
                    unit.setUnitTypeId(type.getId());
                    unit.setIsSystemDefault(true);
                    unit.setOwnerId(null);
                    unit.setIsDeleted(false);
                    unit.setCreateTime(Instant.now());
                    unit.setUpdateTime(Instant.now());
                    toSave.add(unit);
                }
            }
        }

        if (!toSave.isEmpty()) {
            itemUnitRepository.saveAll(toSave);
            log.info("系统默认物品单位初始化完成，本次新增 {} 条。", toSave.size());
        } else {
            log.info("系统默认物品单位已存在，跳过初始化。");
        }
    }

    /**
     * 初始化系统默认冰箱类型。
     * <p>严格按照预设的 id 和 type_name 进行幂等判断，任一字段冲突均跳过。</p>
     */
    private void initFridgeTypes() {
        List<BizFridgeType> existing = fridgeTypeRepository.findAllByIsDeletedFalse();
        Set<Long> existingIds = existing.stream()
                .map(BizFridgeType::getId)
                .collect(Collectors.toSet());
        Set<String> existingNames = existing.stream()
                .map(BizFridgeType::getTypeName)
                .collect(Collectors.toSet());

        List<BizFridgeType> toSave = new ArrayList<>();
        for (FridgeTypeDef def : DEFAULT_FRIDGE_TYPES) {
            if (existingIds.contains(def.id())) {
                log.info("冰箱类型 id=[{}] 已存在，跳过创建。", def.id());
                continue;
            }
            if (existingNames.contains(def.typeName())) {
                log.info("冰箱类型 typeName=[{}] 已存在，跳过创建。", def.typeName());
                continue;
            }

            BizFridgeType type = new BizFridgeType();
            type.setId(def.id());
            type.setTypeName(def.typeName());
            type.setIsDeleted(false);
            type.setCreateTime(FRIDGE_TYPE_INIT_TIME);
            type.setUpdateTime(FRIDGE_TYPE_INIT_TIME);
            toSave.add(type);
            log.info("准备创建系统默认冰箱类型: id={}, typeName={}", def.id(), def.typeName());
        }

        if (!toSave.isEmpty()) {
            fridgeTypeRepository.saveAll(toSave);
            log.info("系统默认冰箱类型初始化完成，本次新增 {} 条。", toSave.size());
        } else {
            log.info("系统默认冰箱类型已存在，跳过初始化。");
        }
    }

    /**
     * 冰箱类型定义内部记录。
     */
    private record FridgeTypeDef(Long id, String typeName) {
    }
}

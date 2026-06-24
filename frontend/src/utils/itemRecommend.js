/**
 * 将智能推荐结果回填到表单
 * 规则：仅当表单字段为空时才回填，不覆盖用户已修改的值
 * @param {Object} recommend - /api/item/recommend 返回的 data
 * @param {Object} form - 表单对象
 * @param {Array} unitList - 单位列表，用于根据 unitId 反查 unitTypeId
 */
export function applyRecommendToForm(recommend, form, unitList = []) {
    if (!recommend || !form) return

    // 物品名称
    if (recommend.itemName && !form.itemName) {
        form.itemName = recommend.itemName
    }

    // 分类
    if (recommend.categoryId != null && !form.categoryId) {
        form.categoryId = recommend.categoryId
    }

    // 单位：推荐返回 unitId，需要反查 unitTypeId
    if (recommend.unitId != null && !form.itemUnitId) {
        const unit = unitList.find(u => u.id === Number(recommend.unitId))
        if (unit) {
            if (!form.unitTypeId) {
                form.unitTypeId = unit.unitTypeId
            }
            form.itemUnitId = unit.id
        }
    }

    // 存放位置
    if (recommend.storageLocation && !form.storageLocation) {
        form.storageLocation = recommend.storageLocation
    }

    // 入库时间（推荐默认今天，若与今天不同也值得回填）
    if (recommend.storedDate && !form.storedDate) {
        form.storedDate = recommend.storedDate
    }
}

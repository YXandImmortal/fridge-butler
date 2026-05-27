/**
 * 冰箱类型图标映射工具
 *
 * 与后端预设的 8 种冰箱类型（FRIDGE_TYPE_API_CHANGE.md）一一对应，
 * 通过 ID 即可获取对应的图标资源路径及类型名称。
 */

import singleDoorIcon from '@/assets/icons/fridge-single-door.svg'
import doubleDoorIcon from '@/assets/icons/fridge-double-door.svg'
import tripleDoorIcon from '@/assets/icons/fridge-triple-door.svg'
import sideBySideIcon from '@/assets/icons/fridge-side-by-side.svg'
import crossDoorIcon from '@/assets/icons/fridge-cross-door.svg'
import tTypeIcon from '@/assets/icons/fridge-t-type.svg'
import frenchDoorIcon from '@/assets/icons/fridge-french-door.svg'
import japaneseMultiIcon from '@/assets/icons/fridge-japanese-multi.svg'

/**
 * 冰箱类型预设列表（固定数据）
 * @type {Array<{id: number, name: string, icon: string}>}
 */
export const FRIDGE_TYPE_LIST = [
    {id: 1, name: '单门冰箱', icon: singleDoorIcon},
    {id: 2, name: '双门冰箱', icon: doubleDoorIcon},
    {id: 3, name: '三门冰箱', icon: tripleDoorIcon},
    {id: 4, name: '对开门冰箱', icon: sideBySideIcon},
    {id: 5, name: '十字对开门', icon: crossDoorIcon},
    {id: 6, name: 'T型三门', icon: tTypeIcon},
    {id: 7, name: '法式多门冰箱', icon: frenchDoorIcon},
    {id: 8, name: '日式多门冰箱', icon: japaneseMultiIcon}
]

/**
 * 以 ID 为键的映射对象，便于直接索引
 * @type {Record<number, {id: number, name: string, icon: string}>}
 */
export const FRIDGE_TYPE_MAP = FRIDGE_TYPE_LIST.reduce((map, item) => {
    map[item.id] = item
    return map
}, {})

/**
 * 根据冰箱类型 ID 获取完整类型信息
 * @param {number} id - 冰箱类型 ID
 * @returns {{id: number, name: string, icon: string} | undefined}
 */
export function getFridgeTypeById(id) {
    return FRIDGE_TYPE_MAP[id]
}

/**
 * 根据冰箱类型 ID 获取图标路径（可直接用于 <img :src="..." />）
 * @param {number} id - 冰箱类型 ID
 * @returns {string | undefined}
 */
export function getFridgeIconById(id) {
    return getFridgeTypeById(id)?.icon
}

/**
 * 根据冰箱类型 ID 获取类型名称
 * @param {number} id - 冰箱类型 ID
 * @returns {string | undefined}
 */
export function getFridgeTypeNameById(id) {
    return getFridgeTypeById(id)?.name
}

package com.yx.fridgebutler.service;

import com.yx.fridgebutler.vo.ItemCategoryVO;
import com.yx.fridgebutler.dto.ItemCreateRequest;
import com.yx.fridgebutler.vo.ItemVO;
import com.yx.fridgebutler.dto.ItemSearchRequest;
import com.yx.fridgebutler.dto.ItemTakeOutRequest;
import com.yx.fridgebutler.vo.ItemUnitVO;
import com.yx.fridgebutler.dto.ItemUpdateRequest;
import com.yx.fridgebutler.vo.UnitTypeVO;

import java.util.List;

public interface ItemService {

    Long createItem(ItemCreateRequest request);

    void updateItem(ItemUpdateRequest request);

    List<ItemVO> searchItems(ItemSearchRequest request);

    List<ItemCategoryVO> listItemCategories();

    List<ItemUnitVO> listItemUnits();

    List<UnitTypeVO> listUnitTypes();

    void deleteItem(Long id);

    void takeOutItem(ItemTakeOutRequest request);
}

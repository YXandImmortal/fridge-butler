package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.ItemCategoryDTO;
import com.yx.fridgebutler.dto.ItemCreateRequest;
import com.yx.fridgebutler.dto.ItemDTO;
import com.yx.fridgebutler.dto.ItemSearchRequest;
import com.yx.fridgebutler.dto.ItemUnitDTO;
import com.yx.fridgebutler.dto.UnitTypeDTO;

import java.util.List;

public interface ItemService {

    Long createItem(ItemCreateRequest request);

    List<ItemDTO> searchItems(ItemSearchRequest request);

    List<ItemCategoryDTO> listItemCategories();

    List<ItemUnitDTO> listItemUnits();

    List<UnitTypeDTO> listUnitTypes();
}

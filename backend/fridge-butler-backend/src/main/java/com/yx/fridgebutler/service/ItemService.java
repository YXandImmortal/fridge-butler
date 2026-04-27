package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.ItemDTO;
import com.yx.fridgebutler.dto.ItemSearchRequest;

import java.util.List;

public interface ItemService {

    List<ItemDTO> searchItems(ItemSearchRequest request);
}

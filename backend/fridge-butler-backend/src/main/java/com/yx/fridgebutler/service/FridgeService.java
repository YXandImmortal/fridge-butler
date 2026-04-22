package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.FridgeCreateRequest;
import com.yx.fridgebutler.dto.FridgeQueryRequest;
import com.yx.fridgebutler.dto.FridgeDTO;
import com.yx.fridgebutler.dto.FridgeUpdateRequest;

import java.util.List;

public interface FridgeService {

    List<FridgeDTO> listMyFridges(FridgeQueryRequest request);

    FridgeDTO getFridgeDetail(Long id);

    Long createFridge(FridgeCreateRequest request);

    void updateFridge(Long id, FridgeUpdateRequest request);

    void deleteFridge(Long id);

    List<FridgeDTO> searchFridges(String keyword);
}

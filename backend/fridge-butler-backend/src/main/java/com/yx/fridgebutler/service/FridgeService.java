package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.FridgeCreateRequest;
import com.yx.fridgebutler.dto.FridgeQueryRequest;
import com.yx.fridgebutler.dto.FridgeDTO;

import java.util.List;

public interface FridgeService {

    List<FridgeDTO> listMyFridges(FridgeQueryRequest request);

    FridgeDTO getFridgeDetail(Long id);

    Long createFridge(FridgeCreateRequest request);

    void deleteFridge(Long id);

    List<FridgeDTO> searchFridges(String keyword);
}

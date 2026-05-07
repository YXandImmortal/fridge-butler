package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.FridgeCreateRequest;
import com.yx.fridgebutler.vo.FridgeVO;
import com.yx.fridgebutler.dto.FridgeSearchRequest;
import com.yx.fridgebutler.dto.FridgeUpdateRequest;

import java.util.List;

public interface FridgeService {

    List<FridgeVO> listMyFridges();

    FridgeVO getFridgeDetail(Long id);

    Long createFridge(FridgeCreateRequest request);

    void updateFridge(Long id, FridgeUpdateRequest request);

    void deleteFridge(Long id);

    List<FridgeVO> searchFridges(FridgeSearchRequest request);

    FridgeVO getDefaultFridge();
}

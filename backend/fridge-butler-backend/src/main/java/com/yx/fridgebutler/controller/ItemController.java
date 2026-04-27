package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.ItemDTO;
import com.yx.fridgebutler.dto.ItemSearchRequest;
import com.yx.fridgebutler.service.ItemService;
import com.yx.fridgebutler.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 搜索物品（支持关键字模糊搜索、分类筛选、单位类型筛选、数量排序）
     */
    @PostMapping("/search")
    public Result<List<ItemDTO>> searchItems(@Valid @RequestBody ItemSearchRequest request) {
        return Result.success(itemService.searchItems(request));
    }
}

package com.hmall.search.controller;


import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.search.domain.dto.ItemSearchFilterDTO;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.domain.query.ItemPageQuery;
import com.hmall.search.service.ISearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ISearchService searchService;

    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDoc> search(ItemPageQuery query) throws IOException {
        // 分页查询
        PageDTO<ItemDoc> dto = this.searchService.search(query);
        // 封装并返回
        return dto;
    }

    @ApiOperation("获取搜索条件")
    @PostMapping("/filters")
    public ItemSearchFilterDTO getFilters(ItemPageQuery query) throws IOException {
        return this.searchService.filters(query);
    }

    @ApiOperation("导入")
    @PostMapping("/import")
    public R<Void> importData() throws IOException {
        this.searchService.importData();

        return R.ok();
    }
}

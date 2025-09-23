package com.hmall.search.service;

import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.dto.ItemSearchFilterDTO;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.domain.query.ItemPageQuery;

import java.io.IOException;

public interface ISearchService {

    PageDTO<ItemDoc> search(ItemPageQuery query) throws IOException;

    ItemSearchFilterDTO filters(ItemPageQuery query) throws IOException;

    void importData() throws IOException;

}

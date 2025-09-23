package com.hmall.search.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.PageQuery;
import com.hmall.common.utils.BeanUtils;
import com.hmall.search.domain.dto.ItemSearchFilterDTO;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.domain.query.ItemPageQuery;
import com.hmall.search.service.ISearchService;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ISearchServiceImpl implements ISearchService {

    private final RestHighLevelClient client;

    private final ItemClient itemClient;

    @Override
    public PageDTO<ItemDoc> search(ItemPageQuery query) throws IOException {
        SearchRequest request = new SearchRequest("items");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.query(QueryBuilders.matchQuery("name", query.getKey())); // 匹配所有

        int from = (query.getPageNo() - 1) * query.getPageSize();
        sourceBuilder.from(from);
        sourceBuilder.size(query.getPageSize());
        sourceBuilder.sort("id", SortOrder.DESC);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name").preTags("<em>").postTags("</em>");
        sourceBuilder.highlighter(highlightBuilder);

        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        List<ItemDoc> items = new ArrayList<>();

        long totalHits = Objects.requireNonNull(response.getHits().getTotalHits()).value;

        for (SearchHit hit : response.getHits().getHits()) {
            String sourceAsString = hit.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(sourceAsString, ItemDoc.class);
            itemDoc.setName(hit.getHighlightFields().get("name").fragments()[0].string());
            items.add(itemDoc);
        }

        return new PageDTO<>(totalHits, totalHits / query.getPageSize() + 1, items);
    }

    @Override
    public ItemSearchFilterDTO filters(ItemPageQuery query) throws IOException {
        SearchRequest request = new SearchRequest("items");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.query(QueryBuilders.matchQuery("name", query.getKey()));
        sourceBuilder.aggregation(AggregationBuilders.terms("category_aggs").field("category").size(10));
        sourceBuilder.aggregation(AggregationBuilders.terms("brand_aggs").field("brand").size(10));
        sourceBuilder.size(0);

        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        Terms categories = response.getAggregations().get("category_aggs");
        Terms brands = response.getAggregations().get("brand_aggs");

        ItemSearchFilterDTO filterDTO = new ItemSearchFilterDTO();

        categories.getBuckets().forEach(bucket -> {
            filterDTO.getCategory().add(bucket.getKeyAsString());
        });

        brands.getBuckets().forEach(bucket -> {
            filterDTO.getBrand().add(bucket.getKeyAsString());
        });

        return filterDTO;
    }

    @Override
    public void importData() throws IOException {
        int page = 1;
        int size = 1000;

        while (true) {
            List<ItemDTO> list = itemClient.queryItemByPage(new PageQuery().setPageNo(page).setPageSize(size)).getList();

            if (list.isEmpty()) {
                break;
            }

            BulkRequest bulk = new BulkRequest();

            list.forEach(item -> {
                ItemDoc itemDoc = BeanUtils.copyBean(item, ItemDoc.class);
                bulk.add(
                        new IndexRequest("items").id(item.getId().toString()).source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON)
                );
            });

            client.bulk(bulk, RequestOptions.DEFAULT);
            page++;
        }
    }
}

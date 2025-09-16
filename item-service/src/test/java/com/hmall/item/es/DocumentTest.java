package com.hmall.item.es;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;

@SpringBootTest(properties = {
        "spring.profiles.active=local",
        "seata.enabled=false"
})
public class DocumentTest {

    private RestHighLevelClient client;

    @Autowired
    private IItemService itemService;

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://localhost:9200")));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

    @Test
    void testAddDocument() throws IOException {
        Item item = itemService.getById(100002644680L);
        ItemDoc itemDoc = BeanUtils.copyBean(item, ItemDoc.class);
        String doc = JSONUtil.toJsonStr(itemDoc);

        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        request.source(doc, XContentType.JSON);
        this.client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testGetDocumentById() throws IOException {
        GetRequest request = new GetRequest("items", "100002644680");

        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        String json = response.getSourceAsString();

        ItemDoc itemDoc = JSONUtil.toBean(json, ItemDoc.class);
        System.out.println(itemDoc);
    }

    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("items", "100002644680");

        HashMap<String, Object> map = new HashMap<>();
        map.put("price", 58800);
        map.put("commentCount", 1);
        map.put("name", "测试商品");
        request.doc(map);

        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    void testLoadItemDocs() throws IOException {
        int pageNo = 1;
        int pageSize = 1000;

        while (true) {
            Page<Item> page = itemService.lambdaQuery().eq(Item::getStatus, 1).page(new Page<>(pageNo, pageSize));

            if (page.getRecords().isEmpty()) {
                break;
            }

            BulkRequest request = new BulkRequest("items");
            page.getRecords().forEach(item -> {
                ItemDoc itemDoc = BeanUtils.copyBean(item, ItemDoc.class);
                request.add(
                        new IndexRequest("items")
                                .id(itemDoc.getId())
                                .source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON)
                );
            });

            client.bulk(request, RequestOptions.DEFAULT);
            pageNo++;
        }
    }
}

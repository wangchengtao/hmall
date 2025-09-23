package com.hmall.item.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public class IndexTest {

    private static final String MAPPING_TEMPLATE =
            "{\n" +
                    "  \"mappings\": {\n" +
                    "    \"properties\": {\n" +
                    "      \"id\": {\n" +
                    "        \"type\": \"keyword\"\n" +
                    "      },\n" +
                    "      \"name\":{\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"analyzer\": \"ik_max_word\"\n" +
                    "      },\n" +
                    "      \"price\":{\n" +
                    "        \"type\": \"integer\"\n" +
                    "      },\n" +
                    "      \"image\":{\n" +
                    "        \"type\": \"keyword\",\n" +
                    "        \"index\": false\n" +
                    "      },\n" +
                    "      \"category\":{\n" +
                    "        \"type\": \"keyword\"\n" +
                    "      },\n" +
                    "      \"brand\":{\n" +
                    "        \"type\": \"keyword\"\n" +
                    "      },\n" +
                    "      \"sold\":{\n" +
                    "        \"type\": \"integer\"\n" +
                    "      },\n" +
                    "      \"commentCount\":{\n" +
                    "        \"type\": \"integer\",\n" +
                    "        \"index\": false\n" +
                    "      },\n" +
                    "      \"isAD\":{\n" +
                    "        \"type\": \"boolean\"\n" +
                    "      },\n" +
                    "      \"updateTime\":{\n" +
                    "        \"type\": \"date\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
    private RestHighLevelClient client;

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(
                RestClient.builder(HttpHost.create("http://localhost:9200"))
        );
    }

    // @Test
    void testConnect() {
        System.out.println(client);
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

    // @Test
    void testCreateIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("items");
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        this.client.indices().create(request, RequestOptions.DEFAULT);
    }

    //@Test
    void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("items");
        boolean exists = this.client.indices().exists(request, RequestOptions.DEFAULT);

        System.out.println(exists ? "存在" : "不存在");
    }

    // @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("items");
        this.client.indices().delete(request, RequestOptions.DEFAULT);
    }

}

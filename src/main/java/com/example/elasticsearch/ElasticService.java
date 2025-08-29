package com.example.elasticsearch;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElasticService {

    private final ElasticsearchOperations operations;

    public ElasticService(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    public void insertPagesBulk(List<PageContent> pages) {
        List<IndexQuery> queries = pages.stream().map(page ->
                new IndexQueryBuilder()
                .withId(page.getId())
                .withObject(page)
                .build()).collect(Collectors.toList());

        operations.bulkIndex(queries, PageContent.class);
        operations.indexOps(PageContent.class).refresh();
    }
}
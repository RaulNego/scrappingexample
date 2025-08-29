package com.example.elasticsearch;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


import java.util.List;

public interface ScrapRepository extends ElasticsearchRepository<PageContent, String> {
    List<PageContent> findByPhoneLikeOrCompanyCommercialNameLikeOrSocialsLikeOrDomainLike(
            String phone,String name, String socials, String domain, Pageable pageable);
}
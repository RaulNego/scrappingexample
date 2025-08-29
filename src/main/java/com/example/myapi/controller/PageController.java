package com.example.myapi.controller;

import com.example.Scrap;
import com.example.elasticsearch.PageContent;
import com.example.elasticsearch.ScrapRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
public class PageController {

    private final ScrapRepository repository;
    private final Scrap scrap;

    public PageController(ScrapRepository repository, Scrap scrap
    ) {
        this.repository = repository;
        this.scrap = scrap;
    }

    @GetMapping("/scrape")
    public String runScraper() {
        System.out.println("Scraper started!");
        scrap.scrap();
        System.out.println("Scraper finished!");
        return "Done";
    }

    @GetMapping("/search-simple")
    public PageContent searchSimple(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String input_facebook) {

        List<PageContent> results = repository.findByPhoneLikeOrCompanyCommercialNameLikeOrSocialsLikeOrDomainLike(
                sanitize(phone), sanitize(name), sanitize(input_facebook), sanitize(website),
                PageRequest.of(0, 1, Sort.by(Sort.Order.desc("_score")))
        );
        return results.isEmpty() ? null : results.get(0);
    }

    String sanitize(String input) {
        return input == null ? "" : input.replaceAll("[\"* ]", "");
    }
}
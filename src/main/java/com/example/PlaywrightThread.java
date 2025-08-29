package com.example;

import com.example.util.ProximitySearch;
import com.microsoft.playwright.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlaywrightThread extends Thread {
    Map<String,ProximitySearch.ClosestResult> results = new ConcurrentHashMap<>();
    List<String> urls = new ArrayList<>();
    int  validWebsitesCount;

    PlaywrightThread() {
    }

    PlaywrightThread(List<String> urls, Map<String,ProximitySearch.ClosestResult> results) {
        this.urls = urls;
        this.results = results;
    }

    public  Map<String,ProximitySearch.ClosestResult>  startThreads(List<String> validUrls, int threadsNumber) throws InterruptedException {
        int wholes = validUrls.size() / threadsNumber;
        List<Thread> threads = new ArrayList<>();

        for(int i=0; i<threadsNumber; i++) {
            Thread thread = new PlaywrightThread(validUrls.subList(i * wholes, (i + 1) * wholes), this.results);
            thread.start();
            threads.add(thread);
        }
        Thread thread = new PlaywrightThread(validUrls.subList(wholes * threadsNumber, validUrls.size()), this.results);
        thread.start();
        threads.add(thread);

        for (Thread t : threads) {
            t.join();
        }
        int counter = 0;
        for (Thread t : threads) {
            counter += ((PlaywrightThread) t).getValidWebsiteCount();
        }
        this.validWebsitesCount = counter;
        return getResults();
    }

    @Override
    public void run() {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch()) {
            Page page = browser.newPage();

            for (String url : urls) {
                boolean isValid = true;
                try {
                    page.navigate("https://" + url.strip(), new Page.NavigateOptions().setTimeout(10000));
                    if (page.title().isEmpty() && page.content().isEmpty()) {
                        isValid = false;
                    }
                } catch (Exception e) {
                    isValid = false;
                }
                if(isValid) {
                    this.results.put(url, Scrap.scrapPage(page, url));
                    validWebsitesCount+=1;
                }
                else {
                    this.results.put(url, new ProximitySearch.ClosestResult("", "", "",0));
                }
            }
        }
    }

    public Map<String,ProximitySearch.ClosestResult> getResults() {
        return results;
    }

    public int getValidWebsiteCount() {
        return validWebsitesCount;
    }
}
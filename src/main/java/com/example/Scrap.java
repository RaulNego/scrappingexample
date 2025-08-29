package com.example;

import com.example.elasticsearch.PageContent;
import com.example.elasticsearch.ElasticService;
import com.example.util.ProximitySearch;
import com.microsoft.playwright.Page;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class Scrap {
    static int threadsNumber = 10;
    static boolean useValidUrls = false;

    private final ElasticService elasticService;

    public Scrap(ElasticService elasticService) {
        this.elasticService = elasticService;
    }

    public void scrap() {
        Map<String,ProximitySearch.ClosestResult> results;

        List<String> urls = getWebsiteURLs(useValidUrls);
        long startTime = System.currentTimeMillis();
        System.out.println("Starting..");
        int validUrlsCount;
        int foundContactInfo = 0;
        try {
            PlaywrightThread playwrightThread = new PlaywrightThread();
            results = playwrightThread.startThreads(urls, threadsNumber);
            validUrlsCount = playwrightThread.getValidWebsiteCount();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Successful sites accessed: " + validUrlsCount);
        System.out.println("Process done, elapsed " + (System.currentTimeMillis() - startTime) / 1000);
        List<PageContent> pageContents = new ArrayList<>();

        List<List<String>> companyNames = getCompanyNames();
        for(List<String> company: companyNames) {
            PageContent pageContent = new PageContent("", "", "",
                    company.get(0), company.get(1), company.get(2), company.get(3));

            ProximitySearch.ClosestResult result = results.get(company.get(0));
            if(result != null && (!result.getPhone().isEmpty() || !result.getEmail().isEmpty() || !result.getSocial().isEmpty())) {
                pageContent.setPhone(result.getPhone());
                pageContent.setSocials(result.getSocial());
                pageContent.setEmail(result.getEmail());
                foundContactInfo++;
            }
            pageContents.add(pageContent);
        }
        System.out.println("Found countact info: " + foundContactInfo);
        elasticService.insertPagesBulk(pageContents);
    }

    static List<String> getWebsiteURLs(Boolean valid) {
        List<String> urls = new ArrayList<>();
        List<String> validUrls = new ArrayList<>();
        if(!valid){
            try {
                urls = Files.readAllLines(Paths.get("src/main/resources/files/external/sample-websites.csv"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            urls.remove(0);
            return urls;
        } else {
            try {
                urls = Files.readAllLines(Paths.get("src/main/resources/files/internal/sample-websites-available.csv"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(String urlState: urls){
                String[] urlComponents = urlState.split(", ");
                if(Objects.equals(urlComponents[1], "true")){
                    validUrls.add(urlComponents[0]);
                }
            }
            return validUrls;
        }
    }

    static List<List<String>> getCompanyNames() {
        List<List<String>> companyNames = new ArrayList<>();
        List<String> companies = new ArrayList<>();
        try {
            companies = Files.readAllLines(Paths.get("src/main/resources/files/external/sample-websites-company-names.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        companies.remove(0);
        for(String company: companies){
            companyNames.add(Arrays.stream(company.split(",")).toList());
        }
        return companyNames;
    }

    //used for fast testing
    static void writeToCSVSampleWebsitesAvailable(Map<String,Boolean> validUrlsMap) {
        Set<String> validUrls = validUrlsMap.keySet();
        String outputFilePath = "src/main/resources/files/internal/sample-websites-available.csv";
        try(BufferedWriter bw = Files.newBufferedWriter(Paths.get(outputFilePath))) {
            for(String url: validUrls) {
                bw.write(url + ", " + validUrlsMap.get(url));
                bw.newLine();
            }
        } catch (Exception e){
            System.out.println("Error while writing " + outputFilePath);
        }
    }

    static String getContactUrl(String content, String url) {
        Matcher contactMatcher = Pattern.compile("https?://[^\\s\"]*contact[^\\s\"]*", Pattern.CASE_INSENSITIVE)
                .matcher(content);
        Matcher aboutMatcher = Pattern.compile("https?://[^\\s\"]*about[^\\s\"]*", Pattern.CASE_INSENSITIVE)
                .matcher(content);

        String contactUrl = "";
        while(contactMatcher.find()) {
            String foundUrl = contactMatcher.group();
            if(foundUrl.contains(url.split("\\.")[0])
                    && !foundUrl.contains("wp-content")) {
                contactUrl = foundUrl;
                break;
            }
        }
        if(contactUrl.isEmpty()) {
            while (aboutMatcher.find()) {
                String foundUrl = aboutMatcher.group();
                if (foundUrl.contains(url.split("\\.")[0])
                        && !foundUrl.contains("wp-content")) {
                    contactUrl = foundUrl;
                    break;
                }
            }
        }
        return contactUrl;
    }

    static ProximitySearch.ClosestResult getContactInformation(Page page, String contactUrl) {
        if(!contactUrl.isEmpty()){
            page.navigate(contactUrl, new Page.NavigateOptions().setTimeout(10000));
        }
        String content = page.content();
        List<String> socials = getSocials(content);
        List<String> phones = getPhones(content);
        List<String> emails = getEmails(content);
        ProximitySearch.ClosestResult closestResult = ProximitySearch.findClosestProximity(content, phones, socials, emails);

        if( closestResult == null) {
            if(phones.isEmpty() && socials.isEmpty()){
                return new ProximitySearch.ClosestResult("", "", "",0);
            }
            String phone = !phones.isEmpty() ? phones.get(0) : "";
            String social = !socials.isEmpty() ? socials.get(0) : "";
            closestResult = new ProximitySearch.ClosestResult(phone, social, "",0);
        }
        return closestResult;
    }

    static List<String> getEmails(String content) {
        List<String> emails = new ArrayList<>();
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Matcher emailMatcher = Pattern.compile(emailRegex).matcher(content);

        while(emailMatcher.find()) {
            emails.add(emailMatcher.group());
        }
        return new ArrayList<>(new LinkedHashSet<>(emails));
    }

    static List<String> getPhones(String content) {
        List<String> phones = new ArrayList<>();
        String phoneRegex = "(?:\\+?\\d{1,3}[\\s.-])?(?:\\(\\d{3}\\)|\\d{3})[\\s.-]\\d{3}[\\s.-]\\d{4}";
        Matcher phoneMatcher = Pattern.compile(phoneRegex).matcher(content);

        while(phoneMatcher.find()) {
            phones.add(phoneMatcher.group());
        }
        return new ArrayList<>(new LinkedHashSet<>(phones));
    }

    static List<String> getSocials(String content) {
        List<String> socials = new ArrayList<>();
        String socialRegex = "\\bhttps?://(?:www\\.)?(?:facebook|instagram|twitter|tiktok|linkedin|snapchat|pinterest|youtube)\\.com(?:/[A-Za-z0-9._/-]*)?";
        Matcher socialMatcher = Pattern.compile(socialRegex).matcher(content);
        while(socialMatcher.find()) {
            if(socialMatcher.group().contains("WordPress") ||
                    socialMatcher.group().contains("plugin") ||
                    socialMatcher.group().contains("script")){
                continue;
            }
            socials.add(socialMatcher.group());
        }
        return new ArrayList<>(new LinkedHashSet<>(socials));
    }

    static ProximitySearch.ClosestResult scrapPage(Page page, String url) {
        String content = page.content();
        String contactUrl = getContactUrl(content, url);
        return getContactInformation(page, contactUrl);

    }
}
package com.example.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Document(indexName = "scraped-content")
public class PageContent {
    @Id
    private String id;
    private String phone;
    private String socials;
    private String email;
    private String domain;
    private String companyCommercialName;
    private String companyLegalName;
    private String companyAllAvailableNames;

    public PageContent(String phone, String socials, String email, String domain, String companyCommercialName,
                       String companyLegalName, String companyAllAvailableNames){
        this.phone=phone;
        this.socials=socials;
        this.email=email;
        this.domain=domain;
        this.companyCommercialName=companyCommercialName;
        this.companyLegalName=companyLegalName;
        this.companyAllAvailableNames=companyAllAvailableNames;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSocials() {
        return socials;
    }

    public void setSocials(String socials) {
        this.socials = socials;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCompanyCommercialName() {
        return companyCommercialName;
    }

    public void setCompanyCommercialName(String companyCommercialName) {
        this.companyCommercialName = companyCommercialName;
    }

    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String companyLegalName) {
        this.companyLegalName = companyLegalName;
    }

    public String getCompanyAllAvailableNames() {
        return companyAllAvailableNames;
    }

    public void setCompanyAllAvailableNames(String companyAllAvailableNames) {
        this.companyAllAvailableNames = companyAllAvailableNames;
    }
}

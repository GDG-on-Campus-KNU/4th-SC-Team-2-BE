package com.example.soop.global.knowledge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@Service
public class SearchService {

    @Value("${google.search.api-key}")
    private String API_KEY;

    @Value("${google.search.cx}")
    private String CX;

    public static final List<String> TRUSTED_DOMAINS = List.of(
            // 국내 주요 병원
            "amc.seoul.kr", "snuh.org", "cmc.or.kr", "samsunghospital.com", "chamc.co.kr",
            "yuhs.or.kr", "khuh.or.kr", "hallym.or.kr", "paik.ac.kr", "chonnam.ac.kr",
            "dsmc.or.kr", "pusanpaik.ac.kr", "cmcseoul.or.kr", "skh.or.kr", "kwmc.or.kr",
            "wonkwanghealthcare.or.kr", "chb.chungbuk.ac.kr", "gnch.or.kr", "jejunuh.co.kr",

            // 국내 의학 포털 & 언론
            "health.kr", "druginfo.co.kr", "kormedi.com", "medigate.net", "doctorville.co.kr",
            "health.chosun.com", "health.mk.co.kr", "health.hankyung.com", "m.healthcare.joins.com",
            "medipana.com", "bosa.co.kr",

            // 국내 정부/공공기관
            "khealth.or.kr", "kmd.or.kr", "kdca.go.kr", "cdc.go.kr", "gov.kr",
            "nhis.or.kr", "mentalhealth.go.kr", "ncmh.go.kr", "najumh.go.kr",

            // 국제/글로벌 기관
            "who.int", "mayoclinic.org", "nih.gov", "webmd.com", "msdmanuals.com"
    );

    private static final Map<String, String> DOMAIN_SELECTORS = Map.ofEntries(
            // 병원
            Map.entry("amc.seoul.kr", "dl.descDl"),
            Map.entry("snuh.org", "div.detailWrap"),
            Map.entry("cmc.or.kr", "div.article-view"),
            Map.entry("samsunghospital.com", "div.view-cont"),
            Map.entry("chamc.co.kr", "div.health-content"),
            Map.entry("yuhs.or.kr", "div.health_cont"),
            Map.entry("khuh.or.kr", "div.cont_area"),
            Map.entry("hallym.or.kr", "div.article"),
            Map.entry("paik.ac.kr", "div.content"),
            Map.entry("chonnam.ac.kr", "div.content"),
            Map.entry("dsmc.or.kr", "div.article-body"),
            Map.entry("pusanpaik.ac.kr", "div.content-wrap"),
            Map.entry("cmcseoul.or.kr", "div.inner-wrap"),
            Map.entry("skh.or.kr", "div.content_area"),
            Map.entry("kwmc.or.kr", "div.articleCont"),
            Map.entry("wonkwanghealthcare.or.kr", "div.body_content"),
            Map.entry("chb.chungbuk.ac.kr", "div.view_area"),
            Map.entry("gnch.or.kr", "div.article-wrap"),
            Map.entry("jejunuh.co.kr", "div.cont_detail"),

            // 포털/언론
            Map.entry("health.kr", "div.container"),
            Map.entry("druginfo.co.kr", "div.content"),
            Map.entry("kormedi.com", "div.view-content"),
            Map.entry("medigate.net", "body"),
            Map.entry("doctorville.co.kr", "body"),
            Map.entry("health.chosun.com", "div.article-body"),
            Map.entry("health.mk.co.kr", "div.content"),
            Map.entry("health.hankyung.com", "body"),
            Map.entry("m.healthcare.joins.com", "div.view"),
            Map.entry("medipana.com", "div.view"),
            Map.entry("bosa.co.kr", "div.view"),

            // 정부기관
            Map.entry("khealth.or.kr", "div.wrap"),
            Map.entry("kmd.or.kr", "body"),
            Map.entry("kdca.go.kr", "body"),
            Map.entry("cdc.go.kr", "div.view_body"),
            Map.entry("gov.kr", "body"),
            Map.entry("nhis.or.kr", "div.K_text"),
            Map.entry("mentalhealth.go.kr", "div.content"),
            Map.entry("ncmh.go.kr", "div.content"),
            Map.entry("najumh.go.kr", "div.content"),

            // 국제기관
            Map.entry("who.int", "div.sf-detail-body-wrapper"),
            Map.entry("mayoclinic.org", "div.content"),
            Map.entry("nih.gov", "div.main-content"),
            Map.entry("webmd.com", "article.article-page"),
            Map.entry("msdmanuals.com", "div.content")
    );

    public List<Map<String, String>> searchAndCrawlTop2WithBody(String query) {
        List<Map<String, String>> results = new ArrayList<>();
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String apiUrl = String.format(
                    "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&num=5",
                    API_KEY, CX, encodedQuery
            );

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseText = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseText.append(line);
            }
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseText.toString());
            JsonNode items = root.get("items");

            if (items != null) {
                for (int i = 0; i < Math.min(5, items.size()); i++) {
                    JsonNode item = items.get(i);
                    String link = item.get("link").asText();
                    String title = item.get("title").asText();
                    String snippet = item.get("snippet").asText();

                    if (!isTrusted(link)) continue;

                    Map<String, String> result = new HashMap<>();
                    result.put("title", title);
                    result.put("link", link);
                    result.put("snippet", snippet);

                    try {
                        Document doc = Jsoup.connect(link).userAgent("Mozilla/5.0").get();
                        String domain = getDomain(link);
                        String selector = DOMAIN_SELECTORS.getOrDefault(domain, "body");
                        String text = doc.select(selector).text();

                        result.put("text", text.length() > 1000 ? text.substring(0, 1000) + "..." : text);
                    } catch (Exception e) {
                        result.put("text", "본문을 불러올 수 없습니다.");
                    }

                    results.add(result);
                    if (results.size() >= 2) break; // 상위 2개까지만 수집
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public String getDomain(String url) {
        try {
            URL u = new URL(url);
            return u.getHost().replace("www.", "");
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isTrusted(String url) {
        return TRUSTED_DOMAINS.contains(getDomain(url));
    }
}

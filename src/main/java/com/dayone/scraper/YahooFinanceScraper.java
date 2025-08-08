package com.dayone.scraper;

import com.dayone.model.Company;
import com.dayone.model.Dividend;
import com.dayone.model.ScrapedResult;
import com.dayone.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper { // Scraper 인터페이스를 구현합니다.


    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1d&filter=div";


    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s";


    @Override
    public ScrapedResult scrap(Company company) {
        var scrapedResult = new ScrapedResult();
        scrapedResult.setCompany(company);

        try {

            long now = System.currentTimeMillis() / 1000;

            long start = now - (10L * 365 * 24 * 60 * 60);


            String url = String.format(STATISTICS_URL, company.getTicker(), start, now);
            System.out.println("Attempting to scrape dividend URL: " + url);


            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                    .timeout(10 * 1000) // 10초 타임아웃
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive");


            Document document = connection.get();


            Elements parsingDivs = document.getElementsByAttributeValue("data-testid", "history-table");


            if (parsingDivs.isEmpty()) {
                System.out.println("Error: No history table found for " + company.getTicker());
                return scrapedResult;
            }

            Element tableEle = parsingDivs.get(0);
            Element tbody = tableEle.selectFirst("tbody");


            if (tbody == null) {
                System.out.println("Error: tbody element not found within the history table.");
                return scrapedResult;
            }

            List<Dividend> dividends = new ArrayList<>();

            for (Element e : tbody.children()) {
                String txt = e.text();


                if (!txt.endsWith("Dividend")) {
                    continue;
                }


                String[] splits = txt.split(" ");


                if (splits.length < 4) {
                    System.out.println("Warning: Skipping malformed row: " + txt);
                    continue;
                }

                try {

                    int month = Month.strToNumber(splits[0]);

                    int day = Integer.valueOf(splits[1].replace(",", ""));

                    int year = Integer.valueOf(splits[2]);

                    String dividend = splits[3];


                    if (month < 0 ){
                        throw new RuntimeException("Unexpected Month enum value => " + splits[0]);
                    }


                    dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));

                } catch (NumberFormatException ex) {
                    System.err.println("Error parsing date or dividend value for row: '" + txt + "'");
                } catch (RuntimeException ex) {
                    System.err.println("Error with Month conversion: '" + txt + "'");
                }
            }
            scrapedResult.setDividends(dividends);
            System.out.println("Scraped " + dividends.size() + " dividends for " + company.getTicker());

        } catch (IOException e) {
            System.err.println("Error during dividend scraping for ticker " + company.getTicker() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return scrapedResult;
    }


    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker);
        System.out.println("Attempting to scrape company summary URL: " + url);

        try {

            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                    .timeout(10 * 1000)
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive");


            Document document = connection.get();
            System.out.println("Successfully fetched document for " + ticker);

            String companyName = null;

            Elements h1Tags = document.getElementsByTag("h1");

            for (Element h1 : h1Tags) {
                String h1Text = h1.text().trim();

                if (h1Text.contains("(" + ticker + ")")) {
                    int lastParenIndex = h1Text.lastIndexOf("(");
                    if (lastParenIndex != -1) {

                        companyName = h1Text.substring(0, lastParenIndex).trim();
                    } else {

                        companyName = h1Text;
                    }
                    System.out.println("Found company name in h1 tag: " + companyName);
                    break;
                }
            }


            if (companyName == null) {
                System.out.println("Error: Could not find company name with ticker in h1 tags for: " + ticker);

                String title = document.title();
                if (title != null && !title.isEmpty()) {

                    int firstParenIndex = title.indexOf("(");
                    int lastParenIndex = title.indexOf(")");
                    if (firstParenIndex != -1 && lastParenIndex != -1 && lastParenIndex > firstParenIndex) {
                        companyName = title.substring(0, firstParenIndex).trim();
                    } else {
                        companyName = title.replace(" | Yahoo Finance", "").trim();
                    }
                    System.out.println("Fallback: Extracted company name from title tag: " + companyName);
                } else {
                    System.out.println("Error: Could not find company name in title tag either.");
                }
            }


            return new Company(ticker, companyName);


        } catch (IOException e) {
            System.err.println("Error during company summary scraping for ticker " + ticker + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during company summary scraping for ticker " + ticker + ": " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}

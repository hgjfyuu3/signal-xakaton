package ru.xakaton.signal;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.xakaton.signal.service.EsService;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParserDocs {

    private final String URI_DIAMOND_REGION = "https://www.алмазный-край.рф";

    private final EsService esService;

    @SneakyThrows
    private void parseLegalActs() {
        log.info("Начало парсинга сайта " + URI_DIAMOND_REGION);
        var doc = Jsoup.connect(URI_DIAMOND_REGION + "/administratsiya-mo/postanovleniya-i-rasporyazheniya-glavy-mr/").get();
        Elements elements = doc.select(".item");
        var countDocs = 0;

        for (Element element : elements) {
            Element titleElement = element.selectFirst(".title a");
            var urlForPdf = titleElement.attr("href");
            var pdfFileAddress = parsePdfLegalActs(urlForPdf);
            var numberOfTheLegalAct = element.selectFirst(".number").text().trim();
            var nameOfTheLegalAct = titleElement.text().trim();

            if (esService.existsLegalAct(numberOfTheLegalAct)) {
                log.info("Новых документов на сайте " + URI_DIAMOND_REGION + " не найдено");
                return;
            }

            countDocs++;
            esService.saveLegalAct(numberOfTheLegalAct, nameOfTheLegalAct, pdfFileAddress);
        }
        log.info("На сайте " + URI_DIAMOND_REGION + " найдено " + countDocs + " новых документов");
    }

    @SneakyThrows
    private String parsePdfLegalActs(String uri) {
        var doc = Jsoup.connect(URI_DIAMOND_REGION + uri).get();
        var elements = doc.getElementsByClass("news-detail");
        var links = elements.select("a[href]");

        return links.stream()
                .map(e -> e.attr("href"))
                .filter(href -> href != null && !href.isEmpty() && !href.startsWith("http://") && !href.startsWith("mailto:"))
                .findFirst()
                .orElse(uri);
    }

    @Scheduled(fixedDelay = 3_600_000L)
    private void runParseDocs() {
        CompletableFuture.runAsync(this::parseLegalActs);
    }

}
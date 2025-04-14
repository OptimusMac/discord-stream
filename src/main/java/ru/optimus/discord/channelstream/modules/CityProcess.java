package ru.optimus.discord.channelstream.modules;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class CityProcess {
    protected final String BASE_URL = "https://htmlweb.ru/geo/city/";
    private final WebClient webClient;

    public CityProcess() {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .filter(logRequest())
                .filter(logResponse())
                .build();
        log.debug("Initialized CityProcess with base URL: {}", BASE_URL);
    }

    public Mono<List<String>> searchCities(char letter) {
        // Конвертируем букву в верхний регистр
        char upperLetter = Character.toUpperCase(letter);
        log.debug("Starting city search for letter: {}", upperLetter);

        return webClient.get()
                .uri("/{letter}", upperLetter)  // Используем букву в верхнем регистре
                .exchangeToMono(response -> {
                    log.debug("Received response status: {}", response.statusCode());
                    if (!response.statusCode().is2xxSuccessful()) {
                        log.warn("Non-successful response: {}", response.statusCode());
                        return response.createException()
                                .flatMap(Mono::error);
                    }
                    return response.bodyToMono(String.class)
                            .doOnNext(body -> log.trace("Raw HTML response: {}", body));
                })
                .flatMap(this::parseCitiesFromHtml)
                .doOnNext(cities -> log.debug("Found {} cities for letter {}", cities.size(), upperLetter))
                .doOnError(e -> log.error("Error searching cities for letter " + upperLetter, e))
                .onErrorResume(e -> {
                    log.warn("Returning empty list due to error", e);
                    return Mono.just(Collections.emptyList());
                });
    }

    private Mono<List<String>> parseCitiesFromHtml(String html) {
        return Mono.fromCallable(() -> {
                    log.trace("Starting HTML parsing");
                    long startTime = System.currentTimeMillis();

                    try {
                        Document doc = Jsoup.parse(html);
                        Elements cityElements = doc.select("#hypercontext > ul > li:nth-child(2) > a.big");
                        log.debug("Found {} city elements in HTML", cityElements.size());

                        List<String> cities = new ArrayList<>();
                        for (Element element : cityElements) {
                            String city = element.text();
                            log.trace("Found city: {}", city);
                            cities.add(city);
                        }

                        long duration = System.currentTimeMillis() - startTime;
                        log.debug("HTML parsing completed in {} ms. Found {} cities", duration, cities.size());
                        return cities;
                    } catch (Exception e) {
                        log.error("HTML parsing failed", e);
                        throw e;
                    }
                })
                .subscribeOn(Schedulers.boundedElastic()); // Выносим парсинг из event-loop
    }

    // Логирование запросов
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    values.forEach(value -> log.trace("Request header: {}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    // Логирование ответов
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response status: {}", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) ->
                    values.forEach(value -> log.trace("Response header: {}={}", name, value)));
            return Mono.just(clientResponse);
        });
    }
}

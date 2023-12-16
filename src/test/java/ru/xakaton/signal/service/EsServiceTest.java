package ru.xakaton.signal.service;

import org.junit.jupiter.api.Test;

class EsServiceTest {

    private EsService esService = new EsService("http://94.139.253.188:9200");

    @Test
    void findLegalActByName() {
        esService.findLegalActByName("жизнеобеспечения в период низких температур");
    }

}
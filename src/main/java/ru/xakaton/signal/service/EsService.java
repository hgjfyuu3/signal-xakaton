package ru.xakaton.signal.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.xakaton.signal.model.LegalAct;

import java.io.IOException;

@Slf4j
@Component
public class EsService {

    private final ElasticsearchClient esClient;

    public EsService(@Value("${app.elasticsearch.uris}") final String esUri) {
        RestClient restClient = RestClient
                .builder(HttpHost.create(esUri))
                .build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        this.esClient = new ElasticsearchClient(transport);
        createLegalActIndex();
    }

    public void createLegalActIndex() {
        try {
            var exists = esClient.indices().exists(i -> i.index("legal_act"));

            if (!exists.value()) {
                esClient.indices().create(c -> c.index("legal_act"));
            }
        } catch (IOException e) {
            log.warn("index already exist");
        }
    }

    public void saveLegalAct(String numberOfTheLegalAct, String nameOfTheLegalAct, String pdfFileAddress) {
        LegalAct legalAct = new LegalAct(numberOfTheLegalAct, nameOfTheLegalAct, pdfFileAddress);

        try {
            IndexResponse response = esClient.index(i -> i
                    .index("legal_act")
                    .id(legalAct.numberOfTheLegalAct())
                    .document(legalAct)
            );
        } catch (IOException e) {
            log.error("Ошибка добавления записи в индекс", e);
        }
    }

    public boolean existsLegalAct(String numberOfTheLegalAct) {
        GetResponse<LegalAct> response;

        try {
            response = esClient.get(g -> g
                            .index("legal_act")
                            .id(numberOfTheLegalAct),
                    LegalAct.class
            );
        } catch (IOException e) {
            log.error("Ошибка поиска записи в индексе", e);
            return false;
        }

        return response.found();
    }

}
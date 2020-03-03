package org.webcurator.core.networkmap.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.rest.AbstractRestClient;

import java.net.URI;

public class NetworkMapRemoteClient extends AbstractRestClient implements NetworkMapService {
    public NetworkMapRemoteClient(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        super(scheme, host, port, restTemplateBuilder);
    }

    @Override
    public String get(long job, int harvestResultNumber, String key) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_COMMON))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("key", key);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getNode(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_NODE))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getOutlinks(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_OUTLINKS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getChildren(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_CHILDREN))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getAllDomains(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_ALL_DOMAINS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getSeedUrls(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_ROOT_URLS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getMalformedUrls(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_MALFORMED_URLS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String searchUrl(long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_SEARCH_URLS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(searchCommand);
        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, request, String.class);
        return result;
    }

    @Override
    public String getHopPath(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_HOP_PATH))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }
}

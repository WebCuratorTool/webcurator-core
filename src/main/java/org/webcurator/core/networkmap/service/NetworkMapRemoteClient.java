package org.webcurator.core.networkmap.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.rest.AbstractRestClient;

import java.net.URI;

public class NetworkMapRemoteClient extends AbstractRestClient implements NetworkMapService {
    public NetworkMapRemoteClient(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        super(scheme, host, port, restTemplateBuilder);
    }

    @Override
    public String get(String key) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_COMMON))
                .queryParam("key", key);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getNode(long job, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_NODE))
                .queryParam("job", job)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getOutlinks(long job, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_OUTLINKS))
                .queryParam("job", job)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getChildren(long job, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_CHILDREN))
                .queryParam("job", job)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getAllDomains(long job) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_ALL_DOMAINS))
                .queryParam("job", job);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getSeedUrls(long job) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_ROOT_URLS))
                .queryParam("job", job);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }

    @Override
    public String getMalformedUrls(long job) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(NetworkMapServicePath.PATH_GET_MALFORMED_URLS))
                .queryParam("job", job);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.getForObject(uri, String.class);
        return result;
    }
}

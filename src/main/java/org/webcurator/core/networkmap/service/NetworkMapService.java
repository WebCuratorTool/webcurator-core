package org.webcurator.core.networkmap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.networkmap.metadata.NetworkNodeDomain;
import org.webcurator.core.networkmap.metadata.NetworkNodeUrl;

import java.io.IOException;
import java.util.List;

public interface NetworkMapService {
    public static final Logger log = LoggerFactory.getLogger(NetworkMapService.class);

    public String get(String key);

    public String getNode(long job, long id);

    public String getOutlinks(long job, long id);

    public String getChildren(long job, long id);

    public String getAllDomains(long job);

    public String getSeedUrls(long job);

    public String getMalformedUrls(long job);

    default public List<Long> getArrayList(String json) {
        if (json == null) {
            return null;
        }

        log.debug(json);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    default public NetworkNodeDomain getNodeDomain(String json) {
        if (json == null) {
            return null;
        }

        log.debug(json);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, NetworkNodeDomain.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    default public NetworkNodeUrl getNodeUrl(String json) {
        if (json == null) {
            return null;
        }

        log.debug(json);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, NetworkNodeUrl.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    default public String obj2Json(Object obj) {
        String json = "{}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }
}

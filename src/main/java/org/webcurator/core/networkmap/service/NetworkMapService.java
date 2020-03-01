package org.webcurator.core.networkmap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.networkmap.metadata.NetworkMapNode;

import java.io.IOException;
import java.util.List;

public interface NetworkMapService {
    public static final Logger log = LoggerFactory.getLogger(NetworkMapService.class);

    public String get(long job, int harvestResultNumber, String key);

    public String getNode(long job, int harvestResultNumber,long id);

    public String getOutlinks(long job, int harvestResultNumber,long id);

    public String getChildren(long job, int harvestResultNumber,long id);

    public String getAllDomains(long job,int harvestResultNumber);

    public String getSeedUrls(long job,int harvestResultNumber);

    public String getMalformedUrls(long job, int harvestResultNumber);

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

    default public NetworkMapNode getNodeEntity(String json) {
        if (json == null) {
            return null;
        }

        log.debug(json);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, NetworkMapNode.class);
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

package org.webcurator.core.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.networkmap.WCTResourceIndexer;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorPaths;
import org.webcurator.domain.model.core.ArcHarvestFileDTO;
import org.webcurator.domain.model.core.ArcHarvestResourceDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.HarvestResourceDTO;

// TODO Note that the spring boot application needs @EnableRetry for the @Retryable to work.
public class WCTIndexer extends IndexerBase {
    private static Logger log = LoggerFactory.getLogger(WCTIndexer.class);

    private HarvestResultDTO result;
    private File directory;
    private boolean doCreate = false;

    public WCTIndexer() {
        super();
    }

    public WCTIndexer(RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder);
    }

    protected WCTIndexer(WCTIndexer original) {
        super(original);
    }

    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30_000L))
    protected Long createIndex() {
        Long harvestResultOid = Long.MIN_VALUE;
        // Step 1. Save the Harvest Result to the database.
        log.info("Initialising index for job " + getResult().getTargetInstanceOid());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = objectMapper.writeValueAsString(getResult());
            log.debug(jsonStr);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

            RestTemplate restTemplate = restTemplateBuilder.build();

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.CREATE_HARVEST_RESULT));

            harvestResultOid = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, Long.class);
            log.info("Initialised index for job " + getResult().getTargetInstanceOid());
        } catch (JsonProcessingException e) {
            log.error("Parsing json failed: {}", e.getMessage());
        }
        return harvestResultOid;
    }

    @Override
    public Long begin() {
        Long harvestResultOid = null;
        if (doCreate) {
            harvestResultOid = this.createIndex();
            log.debug("Created new Harvest Result: " + harvestResultOid);
        } else {
            log.debug("Using Harvest Result " + getResult().getOid());
            harvestResultOid = getResult().getOid();
        }

        return harvestResultOid;
    }

    @Override
    public void indexFiles(Long harvestResultOid) {
        // Step 2. Save the Index for each file.
        log.info("Generating indexes for " + getResult().getTargetInstanceOid());
        WCTResourceIndexer indexer = null;
        try {
            indexer = new WCTResourceIndexer(directory, getResult().getTargetInstanceOid());
        } catch (IOException e) {
            log.error("Failed to create directory: {}", directory);
            return;
        }
        List<ArcHarvestFileDTO> arcHarvestFileDTOList = null;
        try {
            arcHarvestFileDTOList = indexer.indexFiles();
        } catch (IOException e) {
            log.error("Failed to index files: {}", directory);
            return;
        }

        arcHarvestFileDTOList.forEach(arcHarvestFileDTO -> {
            addToHarvestResult(harvestResultOid, arcHarvestFileDTO);
        });
        arcHarvestFileDTOList.clear();

        log.info("Completed indexing for job " + getResult().getTargetInstanceOid());
    }

    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30_000L))
    protected void addToHarvestResult(Long harvestResultOid, ArcHarvestFileDTO arcHarvestFileDTO) {
        try {
            // Submit to the server.
            log.info("Sending Arc Harvest File " + arcHarvestFileDTO.getName());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = objectMapper.writeValueAsString(arcHarvestFileDTO);
            log.debug(jsonStr);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);
            RestTemplate restTemplate = restTemplateBuilder.build();

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.ADD_HARVEST_RESULT));

            Map<String, Long> pathVariables = ImmutableMap.of("harvest-result-oid", harvestResultOid);
            restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(), request, Void.class);
        } catch (JsonProcessingException e) {
            log.error("Parsing json failed: {}", e.getMessage());
        }
    }

    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30_000L))
    protected void addHarvestResources(Long harvestResultOid, Collection<HarvestResourceDTO> harvestResourceDTOs) {
        try {
            Collection<ArcHarvestResourceDTO> arcHarvestResourceDTOs = new ArrayList<ArcHarvestResourceDTO>();
            harvestResourceDTOs.forEach(dto -> {
                arcHarvestResourceDTOs.add((ArcHarvestResourceDTO) dto);
            });

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = objectMapper.writeValueAsString(arcHarvestResourceDTOs);
            log.debug(jsonStr);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

            RestTemplate restTemplate = restTemplateBuilder.build();

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.ADD_HARVEST_RESOURCES));

            Map<String, Long> pathVariables = ImmutableMap.of("harvest-result-oid", harvestResultOid);
            restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(), request, Void.class);
        } catch (JsonProcessingException e) {
            log.error("Parsing json failed: {}", e.getMessage());
        }
    }

    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }

    public void setDoCreate(boolean doCreate) {
        this.doCreate = doCreate;
    }

    @Override
    public void initialise(HarvestResultDTO result, File directory) {
        this.result = result;
        this.directory = directory;
    }

    @Override
    protected HarvestResultDTO getResult() {
        return result;
    }

    @Override
    public RunnableIndex getCopy() {
        return new WCTIndexer(this);
    }

    @Override
    public boolean isEnabled() {
        //WCT indexer is always enabled
        return true;
    }

}


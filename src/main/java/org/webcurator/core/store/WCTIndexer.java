package org.webcurator.core.store;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorPaths;
import org.webcurator.domain.model.core.ArcHarvestFileDTO;
import org.webcurator.domain.model.core.ArcHarvestResultDTO;
import org.webcurator.domain.model.core.HarvestResourceDTO;

// TODO Note that the spring boot application needs @EnableRetry for the @Retryable to work.
public class WCTIndexer extends IndexerBase
{
	private static Log log = LogFactory.getLog(WCTIndexer.class);
	
	private ArcHarvestResultDTO result;
	private File directory;
	private boolean doCreate = false;

    public WCTIndexer() {
        super();
    }

    public WCTIndexer(RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder);
    }

    protected WCTIndexer(WCTIndexer original)
	{
		super(original);
	}

    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30_000L))
    protected Long createIndex() {
		// Step 1. Save the Harvest Result to the database.
		log.info("Initialising index for job " + getResult().getTargetInstanceOid());

        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.CREATE_HARVEST_RESULT))
                .queryParam("harvest-result", getResult());

        Long harvestResultOid = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), null,
                Long.class);
        log.info("Initialised index for job " + getResult().getTargetInstanceOid());

        return harvestResultOid;
	}
	
	@Override
	public Long begin() {
    	Long harvestResultOid = null;
    	if(doCreate) { 
    		harvestResultOid = this.createIndex();
    		log.debug("Created new Harvest Result: " + harvestResultOid);
    	}
    	else {
    		log.debug("Using Harvest Result " + getResult().getOid());
    		harvestResultOid = getResult().getOid();
    	}
    	
    	return harvestResultOid;
	}
	
	@Override
	public void indexFiles(Long harvestResultOid) {
		// Step 2. Save the Index for each file.
        log.info("Generating indexes for " + getResult().getTargetInstanceOid());
        File[] fileList = directory.listFiles(new ARCFilter());
        if(fileList == null) { 
        	log.error("Could not find any archive files in directory: " + directory.getAbsolutePath() );
        }
        else {
            for(File f: fileList) {
        		ArcHarvestFileDTO ahf = new ArcHarvestFileDTO();
                ahf.setName(f.getName());
                ahf.setBaseDir(directory.getAbsolutePath());
            	
                try {
                    ahf.setCompressed(ahf.checkIsCompressed());

                	log.info("Indexing " + ahf.getName());
                    Map<String, HarvestResourceDTO> resources = ahf.index();
                    Collection<HarvestResourceDTO> dtos = resources.values();

                    addToHarvestResult(harvestResultOid, ahf);

                    log.info("Sending Resources for " + ahf.getName());
                    addHarvestResources(harvestResultOid, dtos);

                    log.info("Completed indexing of " + ahf.getName());
                }
                catch(IOException ex) { 
                	log.error("Could not index file " + ahf.getName() + ". Ignoring and continuing with other files. "+ex.getClass().getCanonicalName()+": "+ ex.getMessage());
                }
                catch(ParseException ex) { 
                	log.error("Could not index file " + ahf.getName() + ". Ignoring and continuing with other files. "+ex.getClass().getCanonicalName()+": "+ ex.getMessage());
                }
            }
        }
        log.info("Completed indexing for job " + getResult().getTargetInstanceOid());		
	}

    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30_000L))
    protected void addToHarvestResult(Long harvestResultOid, ArcHarvestFileDTO arcHarvestFileDTO) {
        // Submit to the server.
        log.info("Sending Arc Harvest File " + arcHarvestFileDTO.getName());

        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.ADD_HARVEST_RESULT))
                .queryParam("harvest-file", arcHarvestFileDTO);

        Map<String, Long> pathVariables = ImmutableMap.of("harvest-result-oid", harvestResultOid);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30_000L))
    protected void addHarvestResources(Long harvestResultOid, Collection<HarvestResourceDTO> harvestResourceDTOS) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.ADD_HARVEST_RESOURCES))
                .queryParam("harvest-resources", harvestResourceDTOS);

        Map<String, Long> pathVariables = ImmutableMap.of("harvest-result-oid", harvestResultOid);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    @Override
	public String getName() {
		return getClass().getCanonicalName();
	}

	public void setDoCreate(boolean doCreate) {
		this.doCreate = doCreate;
	}

	@Override
	public void initialise(ArcHarvestResultDTO result, File directory) {
		this.result = result;
		this.directory = directory;
	}

	@Override
	protected ArcHarvestResultDTO getResult() {
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


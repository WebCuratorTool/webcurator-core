package org.archive.io;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.store.IndexerBase;
import org.webcurator.core.store.RunnableIndex;
import org.webcurator.domain.model.core.HarvestResultDTO;

public class CDXIndexer extends IndexerBase{
	private static Log log = LogFactory.getLog(CDXIndexer.class);

	private HarvestResultDTO result;
	private File directory;
	private boolean enabled = false;

    public CDXIndexer() {
        super();
    }

    public CDXIndexer(RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder);
    }

    protected CDXIndexer(CDXIndexer original)
	{
		super(original);
		enabled = original.enabled;
	}
	
	
	private void writeCDXIndex(File archiveFile) throws IOException
	{
	   	ArchiveReader r;
		r = ArchiveReaderFactory.get(archiveFile);
    	r.setStrict(false);
    	r.setDigest(true);
    	r.cdxOutput(true);
    	r.close();
	}
	
	@Override
	public void indexFiles(Long harvestResultOid) {
        log.info("Generating indexes for " + getResult().getTargetInstanceOid());
        File[] fileList = directory.listFiles(new IndexerBase.ARCFilter());
        if(fileList == null) { 
        	log.error("Could not find any archive files in directory: " + directory.getAbsolutePath() );
        }
        else {
            for(File f: fileList) {
                try {
                	log.info("Indexing " + f.getName());
                	writeCDXIndex(f);
                    log.info("Completed indexing of " + f.getName());
                }
                catch(IOException ex) { 
                	log.error("Could not index file " + f.getName() + ". Ignoring and continuing with other files. "+ex.getClass().getCanonicalName()+": "+ ex.getMessage());
                }
            }
        }
        log.info("Completed indexing for job " + getResult().getTargetInstanceOid());		
		
	}

	@Override
	public Long begin() {
		return getResult().getOid();
	}

	@Override
	public String getName() {
		return getClass().getCanonicalName();
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
		return new CDXIndexer(this);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
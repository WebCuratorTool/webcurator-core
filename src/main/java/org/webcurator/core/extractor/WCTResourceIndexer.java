package org.webcurator.core.extractor;

import org.archive.io.*;
import org.archive.io.warc.WARCConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.extractor.metadata.NetworkNodeDomain;
import org.webcurator.core.extractor.metadata.ResourceNode;
import org.webcurator.core.store.IndexerBase;
import org.webcurator.core.util.URLResolverFunc;
import org.webcurator.domain.model.core.ArcHarvestFileDTO;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("all")
public class WCTResourceIndexer {
    private static final Logger log = LoggerFactory.getLogger(WCTResourceIndexer.class);

    private File directory;
    private Map<String, NetworkNodeDomain> domainNodeMap = new Hashtable<>();
    private NetworkMapStore store;

    public static void main(String[] args) throws IOException {
        File directory = new File("/usr/local/wct/store/55/1");
        WCTResourceIndexer indexer = new WCTResourceIndexer(directory);
        List<ArcHarvestFileDTO> arcHarvestFileDTOS = indexer.indexFiles();
    }

    public WCTResourceIndexer(File directory) throws IOException {
        this.directory = directory;
        this.store = new NetworkMapStore(this.directory);
    }

    public List<ArcHarvestFileDTO> indexFiles() throws IOException {
        File[] fileList = directory.listFiles(new IndexerBase.ARCFilter());
        if (fileList == null) {
            log.error("Could not find any archive files in directory: {}", directory.getAbsolutePath());
            return null;
        }

        List<ArcHarvestFileDTO> arcHarvestFileDTOList = new ArrayList();
        for (File f : fileList) {
            ArcHarvestFileDTO dto = indexFile(f);
            if (dto != null) {
                arcHarvestFileDTOList.add(dto);
            }
        }

        this.store.writeDomain(this.domainNodeMap);
        this.domainNodeMap.values().forEach(NetworkNodeDomain::clear);
        this.domainNodeMap.clear();

        return arcHarvestFileDTOList;
    }

    private ArcHarvestFileDTO indexFile(File archiveFile) {
        ArchiveReader reader = null;
        try {
            reader = ArchiveReaderFactory.get(archiveFile);
        } catch (IOException e) {
            log.error("Failed to open archive file: {} with exception: {}", archiveFile.getAbsolutePath(), e.getMessage());
            return null;
        }

        ArcHarvestFileDTO arcHarvestFileDTO = new ArcHarvestFileDTO();
        arcHarvestFileDTO.setBaseDir(archiveFile.getPath());
        arcHarvestFileDTO.setName(archiveFile.getName());
        arcHarvestFileDTO.setCompressed(reader.isCompressed());

        ResourceExtractor extractor = null;
        if (isWarcFormat(arcHarvestFileDTO.getName())) {
            extractor = new ResourceExtractorWarc();
        } else {
            extractor = new ResourceExtractorArc();
        }

        try {
            extractor.extract(reader);
        } catch (IOException e) {
            log.error("Failed to index archive file: {} with exception: {}", archiveFile.getAbsolutePath(), e.getMessage());
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            extractor.clear();
        }

        Map<String, ResourceNode> results = extractor.getResults();

        //Summarize domain
        this.statDomain(results);

        this.store.writeUrl(this.domainNodeMap, results);

        results.clear();

        return arcHarvestFileDTO;
    }

    private void statDomain(Map<String, ResourceNode> resources) {
        String json = "{}";

        for (ResourceNode resourceNode : resources.values()) {
            String currentDomainName = URLResolverFunc.url2domain(resourceNode.getUrl());
            if (currentDomainName == null) {
                continue;
            }

            NetworkNodeDomain currentDomain = this.domainNodeMap.get(currentDomainName);
            if (currentDomain == null) {
                currentDomain = new NetworkNodeDomain();
                currentDomain.setTitle(currentDomainName);
                this.domainNodeMap.put(currentDomainName, currentDomain);
            }

            currentDomain.increase(resourceNode.getStatusCode(), resourceNode.getContentLength(), resourceNode.getContentType());

            String parentDomainName = URLResolverFunc.url2domain(resourceNode.getViaUrl());
            if (parentDomainName == null) {
                continue;
            }

            NetworkNodeDomain parentDomain = this.domainNodeMap.get(parentDomainName);
            if (parentDomain == null) {
                parentDomain = new NetworkNodeDomain();
                parentDomain.setTitle(parentDomainName);
                this.domainNodeMap.put(parentDomainName, parentDomain);
            }

            parentDomain.addOutlink(currentDomain.getKey());
        }
    }

    private boolean isWarcFormat(String name) {
        return name.toLowerCase().endsWith(WARCConstants.DOT_WARC_FILE_EXTENSION) ||
                name.toLowerCase().endsWith(WARCConstants.DOT_COMPRESSED_WARC_FILE_EXTENSION);
    }

    public void clear() {
        this.domainNodeMap.values().forEach(NetworkNodeDomain::clear);
        this.domainNodeMap.clear();
    }
}
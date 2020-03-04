package org.webcurator.core.networkmap;

import org.archive.io.*;
import org.archive.io.warc.WARCConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.webcurator.core.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.networkmap.metadata.NetworkMapNode;
import org.webcurator.domain.model.core.ArcHarvestFileDTO;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Component("WCTResourceIndexer")
public class WCTResourceIndexer {
    private static final Logger log = LoggerFactory.getLogger(WCTResourceIndexer.class);

    private File directory;
    private long job;
    private Map<String, NetworkMapNode> domains = new Hashtable<>();
    private Map<String, NetworkMapNode> urls = new Hashtable<>();

    private BDBNetworkMap db;

    public WCTResourceIndexer(File directory, long job, BDBNetworkMap db) throws IOException {
        this.directory = directory;
        this.job = job;
        this.db = db;
    }

    public List<ArcHarvestFileDTO> indexFiles() throws IOException {
        List<ArcHarvestFileDTO> arcHarvestFileDTOList = new ArrayList();

        File[] fileList = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.toLowerCase().endsWith(".arc") ||
                        name.toLowerCase().endsWith(".arc.gz") ||
                        name.toLowerCase().endsWith(".warc") ||
                        name.toLowerCase().endsWith(".warc.gz"));
            }
        });

        if (fileList == null) {
            log.error("Could not find any archive files in directory: {}", directory.getAbsolutePath());
            return arcHarvestFileDTOList;
        }

        ResourceExtractor extractor = new ResourceExtractorWarc(this.domains, this.urls, this.db, this.job);
        for (File f : fileList) {
            if (!isWarcFormat(f.getName())) {
                continue;
            }
            ArcHarvestFileDTO dto = indexFile(f, extractor);
            if (dto != null) {
                arcHarvestFileDTOList.add(dto);
            }
        }

        db.put(job, BDBNetworkMap.PATH_COUNT_DOMAIN, extractor.getDomainCount());
        db.put(job, BDBNetworkMap.PATH_COUNT_URL, extractor.getUrlCount());

        //Process and save url
        List<Long> rootUrls = new ArrayList<>();
        List<Long> malformedUrls = new ArrayList<>();
        this.urls.values().forEach(e -> {
            extractor.addUrl2Domain(e);

            db.put(this.job, e.getId(), e);

            if (e.isSeed() || e.getParentId() <= 0) {
                rootUrls.add(e.getId());
            }

            if (!e.isFinished()) {
                malformedUrls.add(e.getId());
            }
        });
        db.put(this.job, BDBNetworkMap.PATH_ROOT_URLS, rootUrls);
        rootUrls.clear();
        db.put(this.job, BDBNetworkMap.PATH_MALFORMED_URLS, malformedUrls);
        malformedUrls.clear();

        //Summarize and save domain
        List<Long> rootDomains = new ArrayList<>();
        this.statDomain();
        this.domains.values().forEach(e -> {
            db.put(this.job, "d@" + e.getId(), e);
            rootDomains.add(e.getId());
        });
        db.put(this.job, BDBNetworkMap.PATH_ROOT_DOMAINS, rootDomains);
        rootDomains.clear();

        this.clear();

        return arcHarvestFileDTOList;
    }


    private ArcHarvestFileDTO indexFile(File archiveFile, ResourceExtractor extractor) {
        log.info("Indexing file: {}", archiveFile.getAbsolutePath());
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
        }

        return arcHarvestFileDTO;
    }

    private void statDomain() {
        String json = "{}";

        // Groupby domain's children
        domains.values().forEach(domain -> {
            Map<String, List<NetworkMapNode>> mapGroupByContentType = domain.getChildren().stream().collect(Collectors.groupingBy(NetworkMapNode::getContentType));
            domain.clearChildren();
            mapGroupByContentType.forEach((contentType, listOneContentType) -> {
                NetworkMapNode domainOneContentType = new NetworkMapNode();
                domainOneContentType.setTitle(contentType);
                domainOneContentType.setUrl(domain.getUrl());
                domainOneContentType.setContentType(contentType);
                listOneContentType.forEach(e1 -> {
                    domainOneContentType.accumulate(e1);
                });

                Map<Integer, List<NetworkMapNode>> mapGroupByStatusCode = listOneContentType.stream().collect(Collectors.groupingBy(NetworkMapNode::getStatusCode));
                mapGroupByStatusCode.forEach((statusCode, listOneStatusCode) -> {
                    NetworkMapNode domainOneStatusCode = new NetworkMapNode(0);
                    domainOneStatusCode.setTitle(Long.toString(statusCode));
                    domainOneStatusCode.setUrl(domain.getUrl());
                    domainOneStatusCode.setContentType(contentType);
                    domainOneStatusCode.setStatusCode(statusCode);
                    listOneStatusCode.forEach(e2 -> {
                        domainOneStatusCode.accumulate(e2);
                    });
                    domainOneContentType.putChild(Long.toString(statusCode), domainOneStatusCode);
                });

                domain.putChild(contentType, domainOneContentType);
            });
        });
    }

    private boolean isWarcFormat(String name) {
        return name.toLowerCase().endsWith(WARCConstants.DOT_WARC_FILE_EXTENSION) ||
                name.toLowerCase().endsWith(WARCConstants.DOT_COMPRESSED_WARC_FILE_EXTENSION);
    }

    public void clear() {
//        this.db.shutdownDB();
        this.domains.values().forEach(NetworkMapNode::clear);
        this.domains.clear();
        this.urls.values().forEach(NetworkMapNode::clear);
        this.urls.clear();
    }
}
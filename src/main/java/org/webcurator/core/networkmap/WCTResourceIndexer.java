package org.webcurator.core.networkmap;

import org.archive.io.*;
import org.archive.io.warc.WARCConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.networkmap.metadata.NetworkNodeDomain;
import org.webcurator.core.networkmap.metadata.NetworkNodeUrl;
import org.webcurator.core.store.IndexerBase;
import org.webcurator.domain.model.core.ArcHarvestFileDTO;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class WCTResourceIndexer {
    private static final Logger log = LoggerFactory.getLogger(WCTResourceIndexer.class);

    private File directory;
    private long job;
    private Map<String, NetworkNodeDomain> domains = new Hashtable<>();
    private Map<String, NetworkNodeUrl> urls = new Hashtable<>();
    private BDBNetworkMap db;

    public static void main(String[] args) throws IOException {
        File directory = new File("/usr/local/wct/store/36/1");
        WCTResourceIndexer indexer = new WCTResourceIndexer(directory, (long) 36);
        List<ArcHarvestFileDTO> arcHarvestFileDTOS = indexer.indexFiles();
    }

    public WCTResourceIndexer(File directory, long job) throws IOException {
        this.directory = directory;
        this.job = job;
//        this.store = new NetworkMapStore(this.directory);
        this.db = new BDBNetworkMap();
        this.db.initializeDB(String.format("%s%sresource", this.directory.getAbsolutePath(), File.separator), "resource.db");
    }

    public List<ArcHarvestFileDTO> indexFiles() throws IOException {
        NetworkNodeDomain.init();
        File[] fileList = directory.listFiles(new IndexerBase.ARCFilter());
        if (fileList == null) {
            log.error("Could not find any archive files in directory: {}", directory.getAbsolutePath());
            return null;
        }

        ResourceExtractor extractor = new ResourceExtractorWarc(this.domains, this.urls, this.db, this.job);

        List<ArcHarvestFileDTO> arcHarvestFileDTOList = new ArrayList();
        for (File f : fileList) {
            if (!isWarcFormat(f.getName())) {
                continue;
            }
            ArcHarvestFileDTO dto = indexFile(f, extractor);
            if (dto != null) {
                arcHarvestFileDTOList.add(dto);
            }
        }

        //Process and save url
        List<Long> rootUrls = new ArrayList<>();
        List<Long> malformedUrls = new ArrayList<>();
        this.urls.values().forEach(e -> {
            extractor.addUrl2Domain(e);

            db.put(this.job, e.getId(), e);

            if (e.getParentId() <= 0) {
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
            db.put(this.job, e.getId(), e);
            rootDomains.add(e.getId());
        });
        db.put(this.job, BDBNetworkMap.PATH_ROOT_DOMAINS, rootDomains);
        rootDomains.clear();

        this.clear();

        return arcHarvestFileDTOList;
    }


    private ArcHarvestFileDTO indexFile(File archiveFile, ResourceExtractor extractor) {
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
            Map<String, List<NetworkNodeDomain>> mapGroupByContentType = domain.getChildren().stream().collect(Collectors.groupingBy(NetworkNodeDomain::getContentType));
            domain.clearChildren();
            mapGroupByContentType.forEach((contentType, listOneContentType) -> {
                NetworkNodeDomain domainOneContentType = new NetworkNodeDomain();
                domainOneContentType.setUrl(contentType);
                listOneContentType.forEach(e1 -> {
                    domainOneContentType.increaseTotUrls(e1.getTotUrls());
                    domainOneContentType.increaseTotSuccess(e1.getTotSuccess());
                    domainOneContentType.increaseTotFailed(e1.getTotFailed());
                    domainOneContentType.increaseTotSize(e1.getTotSize());
                });

                Map<Long, List<NetworkNodeDomain>> mapGroupByStatusCode = listOneContentType.stream().collect(Collectors.groupingBy(NetworkNodeDomain::getStatusCode));
                mapGroupByStatusCode.forEach((statusCode, listOneStatusCode) -> {
                    NetworkNodeDomain domainOneStatusCode = new NetworkNodeDomain();
                    domainOneStatusCode.setUrl(Long.toString(statusCode));
                    listOneStatusCode.forEach(e2 -> {
                        domainOneStatusCode.increaseTotUrls(e2.getTotUrls());
                        domainOneStatusCode.increaseTotSuccess(e2.getTotSuccess());
                        domainOneStatusCode.increaseTotFailed(e2.getTotFailed());
                        domainOneStatusCode.increaseTotSize(e2.getTotSize());
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
        this.db.shutdownDB();
        this.domains.values().forEach(NetworkNodeDomain::clear);
        this.domains.clear();
        this.urls.values().forEach(NetworkNodeUrl::clear);
        this.urls.clear();
    }

}
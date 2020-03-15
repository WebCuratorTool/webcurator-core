package org.webcurator.core.networkmap;

import org.archive.io.*;
import org.archive.io.warc.WARCConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.webcurator.core.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.networkmap.metadata.NetworkMapDomain;
import org.webcurator.core.networkmap.metadata.NetworkMapDomainManager;
import org.webcurator.core.networkmap.metadata.NetworkMapNode;
import org.webcurator.domain.model.core.ArcHarvestFileDTO;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("all")
@Component("WCTResourceIndexer")
public class WCTResourceIndexer {
    private static final Logger log = LoggerFactory.getLogger(WCTResourceIndexer.class);

    private File directory;
    private Map<String, NetworkMapNode> urls = new Hashtable<>();

    private BDBNetworkMap db;

    public WCTResourceIndexer(File directory, BDBNetworkMap db) throws IOException {
        this.directory = directory;
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

        ResourceExtractor extractor = new ResourceExtractorWarc(this.urls);
        for (File f : fileList) {
            if (!isWarcFormat(f.getName())) {
                continue;
            }
            ArcHarvestFileDTO dto = indexFile(f, extractor);
            if (dto != null) {
                arcHarvestFileDTOList.add(dto);
            }
        }

        this.statAndSave();

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

    private void statAndSave() {
        AtomicLong domainIdGenerator = new AtomicLong();
        NetworkMapDomainManager domainManager = new NetworkMapDomainManager();

        //Statistic by domain
        NetworkMapDomain rootDomainNode = new NetworkMapDomain(NetworkMapDomain.DOMAIN_NAME_LEVEL_ROOT, 0);
        rootDomainNode.addChildren(this.urls.values(), domainIdGenerator, domainManager);
        rootDomainNode.addStatData(this.urls.values());

        //Process parent relationship, outlinks and domain's outlink
        this.urls.values().forEach(node -> {
            /**
             * if url u-->v then domain du->dv, DU->DV, du->DV, DU->dv
             */

            NetworkMapDomain domainNodeHigh = domainManager.getHighDomain(node);
            NetworkMapDomain domainNodeLower = domainManager.getLowerDomain(node);
            if (node.isSeed()) {
                domainNodeHigh.setSeed(true);
                domainNodeLower.setSeed(true);
            }

            String viaUrl = node.getViaUrl();
            if (viaUrl == null || !this.urls.containsKey(viaUrl)) {
                node.setParentId(-1);
            } else {
                NetworkMapNode parentNode = this.urls.get(viaUrl);
                parentNode.addOutlink(node);

                NetworkMapDomain parentDomainNodeHigh = domainManager.getHighDomain(parentNode);
                NetworkMapDomain parentDomainNodeLower = domainManager.getLowerDomain(parentNode);

                node.setParentId(parentNode.getId());

                parentDomainNodeHigh.addOutlink(domainNodeHigh.getId());
                parentDomainNodeHigh.addOutlink(domainNodeLower.getId());
                parentDomainNodeLower.addOutlink(domainNodeHigh.getId());
                parentDomainNodeLower.addOutlink(domainNodeLower.getId());
            }
        });
//        db.put(BDBNetworkMap.PATH_METADATA_DOMAIN_NAME, statDomainMap.keySet());
        db.put(BDBNetworkMap.PATH_GROUP_BY_DOMAIN, rootDomainNode);

        //Process and save url
        List<Long> rootUrls = new ArrayList<>();
        List<Long> malformedUrls = new ArrayList<>();
        this.urls.values().forEach(e -> {
            db.put(e.getId(), e);

            if (e.isSeed() || e.getParentId() <= 0) {
                rootUrls.add(e.getId());
            }

            if (!e.isFinished()) {
                malformedUrls.add(e.getId());
            }
        });
        db.put(BDBNetworkMap.PATH_ROOT_URLS, rootUrls);
        rootUrls.clear();
        db.put(BDBNetworkMap.PATH_MALFORMED_URLS, malformedUrls);
        malformedUrls.clear();
    }

    private boolean isWarcFormat(String name) {
        return name.toLowerCase().endsWith(WARCConstants.DOT_WARC_FILE_EXTENSION) ||
                name.toLowerCase().endsWith(WARCConstants.DOT_COMPRESSED_WARC_FILE_EXTENSION);
    }

    public void clear() {
        this.urls.values().forEach(NetworkMapNode::clear);
        this.urls.clear();
    }
}
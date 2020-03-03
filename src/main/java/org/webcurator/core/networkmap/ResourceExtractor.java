package org.webcurator.core.networkmap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.webcurator.core.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.util.URLResolverFunc;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

abstract public class ResourceExtractor {
    protected static final int MAX_URL_LENGTH = 1020;
    protected AtomicLong atomicIdGeneratorDomain = new AtomicLong();
    protected AtomicLong atomicIdGeneratorUrl = new AtomicLong();

    protected Map<String, NetworkMapNode> domains;
    protected Map<String, NetworkMapNode> results;
    protected BDBNetworkMap db;
    protected long job;

    protected ResourceExtractor(Map<String, NetworkMapNode> domains, Map<String, NetworkMapNode> results, BDBNetworkMap db, long job) {
        this.domains = domains;
        this.results = results;
        this.db = db;
        this.job = job;
    }

    public void extract(ArchiveReader reader) throws IOException {
        preProcess();
        Iterator<ArchiveRecord> it = reader.iterator();
        while (it.hasNext()) {
            extractRecord(it.next());
        }
        postProcess();
    }

    abstract protected void preProcess();

    abstract protected void postProcess();

    abstract protected void extractRecord(ArchiveRecord rec) throws IOException;

    public void clear() {
    }

    /**
     * borrowed(copied) from org.archive.io.arc.ARCRecord...
     *
     * @param bytes Array of bytes to examine for an EOL.
     * @return Count of end-of-line characters or zero if none.
     */
    public int getEolCharsCount(byte[] bytes) {
        int count = 0;
        if (bytes != null && bytes.length >= 1 &&
                bytes[bytes.length - 1] == '\n') {
            count++;
            if (bytes.length >= 2 && bytes[bytes.length - 2] == '\r') {
                count++;
            }
        }
        return count;
    }

    public String getJson(Object obj) {
        String json = "{}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }


    public void addUrl2Domain(NetworkMapNode resourceNode) {
        String currentDomainName = URLResolverFunc.url2domain(resourceNode.getUrl());
        if (currentDomainName == null) {
            return;
        }

        NetworkMapNode currentDomain = this.domains.get(currentDomainName);
        if (currentDomain == null) {
            currentDomain = new NetworkMapNode(atomicIdGeneratorDomain.incrementAndGet());
            currentDomain.setUrl(currentDomainName);
            currentDomain.setTitle(currentDomainName);
            this.domains.put(currentDomainName, currentDomain);
        }

        if (resourceNode.isSeed()) {
            currentDomain.setSeed(true);
        }
        currentDomain.accumulateAsChildren(resourceNode.getStatusCode(), resourceNode.getContentLength(), resourceNode.getContentType());

        String parentDomainName = URLResolverFunc.url2domain(resourceNode.getViaUrl());
        if (parentDomainName == null) {
            return;
        }

        NetworkMapNode parentDomain = this.domains.get(parentDomainName);
        if (parentDomain == null) {
            parentDomain = new NetworkMapNode(atomicIdGeneratorDomain.incrementAndGet());
            parentDomain.setUrl(parentDomainName);
            parentDomain.setTitle(parentDomainName);
            this.domains.put(parentDomainName, parentDomain);
        }

        parentDomain.addOutlink(currentDomain.getId());
    }

    public long getDomainCount() {
        return atomicIdGeneratorDomain.get();
    }

    public long getUrlCount() {
        return atomicIdGeneratorUrl.get();
    }
}
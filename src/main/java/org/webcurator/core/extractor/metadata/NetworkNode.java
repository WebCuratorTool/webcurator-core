package org.webcurator.core.extractor.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class NetworkNode implements BasicNode {
    protected static final int TYPE_URL = 1;
    protected static final int TYPE_DOMAIN = 2;
    protected static AtomicLong IdGenerator = new AtomicLong();

    private long key;
    private String title;
    private boolean isSeed = false; //true: if url equals seed or domain contains seed url.
    private int type; //1: url, 2: domain

    /////////////////////////////////////////////////////////////////////////////////////////
    // 1. Domain: the total items of all urls contained in this domain.
    // 2. URL: the total items of all urls directly link to this url and the url itself
    private int totUrls;
    private int totSuccess;
    private int totFailed;
    private long totSize;
    ///////////////////////////////////////////////////////////////////////////////////////////

    private long domainId = -1; //default: no domain
    private List<Long> outlinks = new ArrayList<>();

    public NetworkNode() {
        this.key = IdGenerator.incrementAndGet();
    }

    public NetworkNode(int type) {
        this();
        this.type = type;
    }

    public static void init() {
        IdGenerator = new AtomicLong();
    }

    public void addOutlink(long key) {
        if (this.key != key && !this.outlinks.contains(key)) {
            this.outlinks.add(key);
        }
    }


    public void clear() {
        this.outlinks.clear();
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }


    public boolean isSeed() {
        return isSeed;
    }

    public void setSeed(boolean seed) {
        isSeed = seed;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTotUrls() {
        return totUrls;
    }

    public void increaseTotUrls(int totUrls) {
        this.totUrls += totUrls;
    }

    public int getTotSuccess() {
        return totSuccess;
    }

    public void increaseTotSuccess(int totSuccess) {
        this.totSuccess += totSuccess;
    }

    public int getTotFailed() {
        return totFailed;
    }

    public void increaseTotFailed(int totFailed) {
        this.totFailed += totFailed;
    }

    public long getTotSize() {
        return totSize;
    }

    public void increaseTotSize(long totSize) {
        this.totSize += totSize;
    }

    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(long domainId) {
        this.domainId = domainId;
    }

    public List<Long> getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(List<Long> outlinks) {
        this.outlinks = outlinks;
    }

    public String toString() {
        return String.format("URLs: %d\n\tSuccess: %d\n\tFailed: %d\nSize: %d", this.totUrls, this.totSuccess, this.totFailed, this.totSize);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void increase(int statusCode, long contentLength, String contentType) {
        this.increaseTotSize(contentLength);
        if (statusCode == 200) {
            this.increaseTotSuccess(1);
        } else {
            this.increaseTotFailed(1);
        }
        this.increaseTotUrls(1);
    }
}

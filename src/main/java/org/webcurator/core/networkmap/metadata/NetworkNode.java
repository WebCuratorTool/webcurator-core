package org.webcurator.core.networkmap.metadata;

import java.util.ArrayList;
import java.util.List;

abstract public class NetworkNode implements BasicNode {
    private long id;
    private String url;
    private boolean isSeed = false; //true: if url equals seed or domain contains seed url.

    /////////////////////////////////////////////////////////////////////////////////////////
    // 1. Domain: the total items of all urls contained in this domain.
    // 2. URL: the total items of all urls directly link to this url and the url itself
    private int totUrls = 0;
    private int totSuccess = 0;
    private int totFailed = 0;
    private long totSize = 0;
    ///////////////////////////////////////////////////////////////////////////////////////////

    private long domainId = -1; //default: no domain
    private List<Long> outlinks = new ArrayList<>();

    public NetworkNode(long id) {
        this.id = id;
    }

    public void addOutlink(long id) {
        if (this.id != id && !this.outlinks.contains(id)) {
            this.outlinks.add(id);
        }
    }


    public void clear() {
        this.outlinks.clear();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isSeed() {
        return isSeed;
    }

    public void setSeed(boolean seed) {
        isSeed = seed;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTotUrls(int totUrls) {
        this.totUrls = totUrls;
    }

    public void setTotSuccess(int totSuccess) {
        this.totSuccess = totSuccess;
    }

    public void setTotFailed(int totFailed) {
        this.totFailed = totFailed;
    }

    public void setTotSize(long totSize) {
        this.totSize = totSize;
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

    public boolean isSuccessStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode < 400;
    }

    public abstract void initialize(String value);
}

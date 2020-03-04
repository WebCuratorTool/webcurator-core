package org.webcurator.core.networkmap.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.webcurator.core.util.URLResolverFunc;

import java.util.*;

public class NetworkMapNode {
    protected long id;
    protected String url;
    protected boolean isSeed = false; //true: if url equals seed or domain contains seed url.

    /////////////////////////////////////////////////////////////////////////////////////////
    // 1. Domain: the total items of all urls contained in this domain.
    // 2. URL: the total items of all urls directly link to this url and the url itself
    protected int totUrls = 0;
    protected int totSuccess = 0;
    protected int totFailed = 0;
    protected long totSize = 0;
    ///////////////////////////////////////////////////////////////////////////////////////////

    protected long domainId = -1; //default: no domain
    protected String viaUrl;
    protected long contentLength;
    protected String contentType;
    protected int statusCode;
    protected long parentId = -1;
    protected long offset;
    protected long fetchTimeMs; //ms: time used to download the page
    protected boolean hasOutlinks; //the number of outlinks>0
    protected boolean requestParseFlag = false;
    protected boolean responseParseFlag = false;
    protected boolean metadataParseFlag = false;

    protected List<Long> outlinks = new ArrayList<>();
    protected Map<String, NetworkMapNode> children = new HashMap<>();

    protected String title;

    public NetworkMapNode() {
    }

    public NetworkMapNode(long id) {
        this.id = id;
    }

    public NetworkMapNode(String contentType, long contentLength, int statusCode) {
        this.contentType = URLResolverFunc.trimContentType(contentType);
        this.contentLength = contentLength;
        this.statusCode = statusCode;
    }

    @JsonIgnore
    public void clear() {
        this.outlinks.clear();
        this.children.values().forEach(NetworkMapNode::clear);
        this.children.clear();
    }

    @JsonIgnore
    public void addOutlink(NetworkMapNode outlink) {
        if (this.id != outlink.getId() && !this.outlinks.contains(outlink.getId())) {
            this.outlinks.add(outlink.getId());
            this.accumulate(outlink.getStatusCode(), outlink.getContentLength(), outlink.getContentType());
        }
    }

    @JsonIgnore
    public void addOutlink(long linkId) {
        if (this.id != linkId && !this.outlinks.contains(linkId)) {
            this.outlinks.add(linkId);
        }
    }

    @JsonIgnore
    public void increaseTotUrls(int totUrls) {
        this.totUrls += totUrls;
    }

    @JsonIgnore
    public void increaseTotSuccess(int totSuccess) {
        this.totSuccess += totSuccess;
    }

    @JsonIgnore
    public void increaseTotFailed(int totFailed) {
        this.totFailed += totFailed;
    }

    @JsonIgnore
    public void increaseTotSize(long totSize) {
        this.totSize += totSize;
    }

    @JsonIgnore
    public void accumulate(int statusCode, long contentLength, String contentType) {
        this.increaseTotSize(contentLength);
        if (isSuccess(statusCode)) {
            this.increaseTotSuccess(1);
        } else {
            this.increaseTotFailed(1);
        }
        this.increaseTotUrls(1);
    }

    @JsonIgnore
    public void accumulate(NetworkMapNode e) {
        this.increaseTotSize(e.getTotSize());
        this.increaseTotUrls(e.getTotUrls());
        this.increaseTotSuccess(e.getTotSuccess());
        this.increaseTotFailed(e.getTotFailed());
    }

    @JsonIgnore
    public void accumulateAsChildren(int statusCode, long contentLength, String contentType) {
        this.accumulate(statusCode, contentLength, contentType);

        String key = String.format("%s@%d", contentType, statusCode);

        NetworkMapNode childDomainNode = this.children.get(key);
        if (childDomainNode == null) {
            childDomainNode = new NetworkMapNode(contentType, contentLength, statusCode); //No separate reference, set the id to 0
            this.children.put(key, childDomainNode);
        }

        childDomainNode.accumulate(statusCode, contentLength, contentType);
    }

    @JsonIgnore
    public void putChild(String key, NetworkMapNode value) {
        this.children.put(key, value);
    }

    @JsonIgnore
    public void clearChildren() {
        this.children.clear(); //Not clear the grand children
    }

    @JsonIgnore
    public boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 400;
    }

    @JsonIgnore
    public boolean isFinished() {
        return requestParseFlag && responseParseFlag && metadataParseFlag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public void setTotUrls(int totUrls) {
        this.totUrls = totUrls;
    }

    public int getTotSuccess() {
        return totSuccess;
    }

    public void setTotSuccess(int totSuccess) {
        this.totSuccess = totSuccess;
    }

    public int getTotFailed() {
        return totFailed;
    }

    public void setTotFailed(int totFailed) {
        this.totFailed = totFailed;
    }

    public long getTotSize() {
        return totSize;
    }

    public void setTotSize(long totSize) {
        this.totSize = totSize;
    }

    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(long domainId) {
        this.domainId = domainId;
    }

    public String getViaUrl() {
        return viaUrl;
    }

    public void setViaUrl(String viaUrl) {
        this.viaUrl = viaUrl;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
        this.totSize += contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = URLResolverFunc.trimContentType(contentType);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        this.totUrls += 1;
        if (this.isSuccess(statusCode)) {
            this.totSuccess += 1;
        } else {
            this.totFailed += 1;
        }
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getFetchTimeMs() {
        return fetchTimeMs;
    }

    public void setFetchTimeMs(long fetchTimeMs) {
        this.fetchTimeMs = fetchTimeMs;
    }

    public boolean isHasOutlinks() {
        return hasOutlinks;
    }

    public void setHasOutlinks(boolean hasOutlinks) {
        this.hasOutlinks = hasOutlinks;
    }

    public boolean isRequestParseFlag() {
        return requestParseFlag;
    }

    public void setRequestParseFlag(boolean requestParseFlag) {
        this.requestParseFlag = requestParseFlag;
    }

    public boolean isResponseParseFlag() {
        return responseParseFlag;
    }

    public void setResponseParseFlag(boolean responseParseFlag) {
        this.responseParseFlag = responseParseFlag;
    }

    public boolean isMetadataParseFlag() {
        return metadataParseFlag;
    }

    public void setMetadataParseFlag(boolean metadataParseFlag) {
        this.metadataParseFlag = metadataParseFlag;
    }

    public List<Long> getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(List<Long> outlinks) {
        this.outlinks = outlinks;
    }

    public Collection<NetworkMapNode> getChildren() {
        return children.values();
    }

    public void setChildren(Collection<NetworkMapNode> children) {
        children.forEach(child -> {
            this.children.put(child.getUrl(), child);
        });
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

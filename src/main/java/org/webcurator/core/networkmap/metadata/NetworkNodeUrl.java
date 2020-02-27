package org.webcurator.core.networkmap.metadata;

public class NetworkNodeUrl extends NetworkNode {
    private String viaUrl;
    private long contentLength;
    private String contentType;
    private int statusCode;
    private long parentId;
    private long offset;
    private long fetchTimeMs; //ms: time used to download the page
    private boolean hasOutlinks; //the number of outlinks>0
    private boolean requestParseFlag = false;
    private boolean responseParseFlag = false;
    private boolean metadataParseFlag = false;


    public NetworkNodeUrl() {
        super(NetworkNode.TYPE_URL);
    }

    @Override
    public void initialize(String value) {

    }

    public boolean isFinished() {
        return requestParseFlag && responseParseFlag && metadataParseFlag;
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
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
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
}

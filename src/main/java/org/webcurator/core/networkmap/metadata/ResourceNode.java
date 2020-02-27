package org.webcurator.core.networkmap.metadata;

import org.webcurator.core.util.URLResolverFunc;

import java.util.ArrayList;
import java.util.List;

public class ResourceNode implements BasicNode {
    protected long id;
    protected String url;
    protected String viaUrl;
    protected long contentLength;
    protected int statusCode;
    protected String contentType;
    protected long resourceOffset;
    protected List<String> outlinks = new ArrayList<>();

    public void clear() {
        this.outlinks.clear();
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

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = URLResolverFunc.trimContentType(contentType);
    }

    public long getResourceOffset() {
        return resourceOffset;
    }

    public void setResourceOffset(long resourceOffset) {
        this.resourceOffset = resourceOffset;
    }

    public List<String> getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(List<String> outlinks) {
        this.outlinks = outlinks;
    }

    public String getDomain() {
        return URLResolverFunc.url2domain(this.url);
    }

    public String toString() {
        return String.format("%d,%s,%s,%d,%d,%d,%s", id, url, contentType, statusCode, contentLength, resourceOffset, viaUrl);
    }

    public static ResourceNode getInstance(String line) {
        if (line == null) {
            return null;
        }

        String[] items = line.split(",");
        if (7 != items.length) {
            return null;
        }
        ResourceNode n = new ResourceNode();
        n.setId(Long.parseLong(items[0]));
        n.setUrl(items[1]);
        n.setContentType(items[2]);
        n.setStatusCode(Integer.parseInt(items[3]));
        n.setContentLength(Long.parseLong(items[4]));
        n.setResourceOffset(Long.parseLong(items[5]));
        n.setViaUrl(items[6]);

        return n;
    }
}

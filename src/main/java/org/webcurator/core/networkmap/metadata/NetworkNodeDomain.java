package org.webcurator.core.networkmap.metadata;

import org.webcurator.core.util.URLResolverFunc;

import java.util.*;

public class NetworkNodeDomain extends NetworkNode {
    private String contentType;
    private long statusCode;
    private Map<String, NetworkNodeDomain> children = new HashMap<>();

    public NetworkNodeDomain() {
        super(NetworkNode.TYPE_DOMAIN);
    }

    public Collection<NetworkNodeDomain> getChildren() {
        return children.values();
    }

    public void setChildren(Collection<NetworkNodeDomain> children) {
        children.forEach(child -> {
            this.children.put(child.getUrl(), child);
        });
    }

    public void clear() {
        super.clear();
        this.children.values().forEach(NetworkNodeDomain::clear);
        this.children.clear();
    }

    public void putChild(String key, NetworkNodeDomain value) {
        this.children.put(key, value);
    }

    public void clearChildren() {
        this.children.clear(); //Not clear the grand children
    }

    public void increase(int statusCode, long contentLength, String contentType) {
        super.increase(statusCode, contentLength, contentType);

        contentType = URLResolverFunc.trimContentType(contentType);
        String key = String.format("%s@%d", contentLength, statusCode);

        NetworkNodeDomain childDomainNode = this.children.get(key);
        if (childDomainNode == null) {
            childDomainNode = new NetworkNodeDomain();
            childDomainNode.setContentType(contentType);
            childDomainNode.setStatusCode(statusCode);
            this.children.put(key, childDomainNode);
        }

        childDomainNode.increaseTotSize(contentLength);
        if (statusCode == 200) {
            childDomainNode.increaseTotSuccess(1);
        } else {
            childDomainNode.increaseTotFailed(1);
        }
        childDomainNode.increaseTotUrls(1);
    }

    @Override
    public void initialize(String value) {

    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(long statusCode) {
        this.statusCode = statusCode;
    }
}

package org.webcurator.core.extractor.metadata;

import org.webcurator.core.util.URLResolverFunc;

import java.util.*;

public class NetworkNodeDomain extends NetworkNode {
    private List<Long> nodes = new ArrayList<>(); //all children
    private Map<String, NetworkNodeDomain> children = new HashMap<>();

    public NetworkNodeDomain() {
        super(NetworkNode.TYPE_DOMAIN);
    }

    public void addNode(long nodeId) {
        this.nodes.add(nodeId);
    }

    public List<Long> getNodes() {
        return nodes;
    }

    public void setNodes(List<Long> nodes) {
        this.nodes = nodes;
    }

    public Collection<NetworkNodeDomain> getChildren() {
        return children.values();
    }

    public void setChildren(Map<String, NetworkNodeDomain> children) {
        this.children = children;
    }

    public void clear() {
        super.clear();
        this.nodes.clear();
        this.children.values().forEach(NetworkNodeDomain::clear);
        this.children.clear();
    }

    public void increase(int statusCode, long contentLength, String contentType) {
        super.increase(statusCode, contentLength, contentType);

        contentType = URLResolverFunc.trimContentType(contentType);
        NetworkNodeDomain childDomainNode = this.children.get(contentType);
        if (childDomainNode == null) {
            childDomainNode = new NetworkNodeDomain();
            this.children.put(contentType, childDomainNode);
        }
        childDomainNode.setTitle(contentType);

        childDomainNode.increaseTotSize(contentLength);
        if (statusCode == 200) {
            childDomainNode.increaseTotSuccess(1);
        } else {
            childDomainNode.increaseTotFailed(1);
        }
        childDomainNode.increaseTotUrls(1);
    }
}

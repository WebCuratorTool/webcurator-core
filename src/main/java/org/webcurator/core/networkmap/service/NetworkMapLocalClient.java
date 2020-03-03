package org.webcurator.core.networkmap.service;

import org.webcurator.core.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.util.URLResolverFunc;

import java.util.ArrayList;
import java.util.List;

public class NetworkMapLocalClient implements NetworkMapService {
    private BDBNetworkMapPool pool;

    public NetworkMapLocalClient(BDBNetworkMapPool pool) {
        this.pool = pool;
    }

    @Override
    public String get(long job, int harvestResultNumber, String key) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        return db.get(key);
    }

    @Override
    public String getNode(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        return db.get(job, id);
    }

    @Override
    public String getOutlinks(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        NetworkMapNode parentNode = this.getNodeEntity(db.get(job, id));
        if (parentNode == null) {
            return null;
        }

        String result = combineUrlResultFromArrayIDs(job, harvestResultNumber, parentNode.getOutlinks());
        parentNode.clear();

        return result;
    }

    @Override
    public String getChildren(long job, int harvestResultNumber, long id) {
        //TODO
        return "{}";
    }

    @Override
    public String getAllDomains(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        List<Long> ids = this.getArrayList(db.get(job, BDBNetworkMap.PATH_ROOT_DOMAINS));
        String result = combineDomainResultFromArrayIDs(job, harvestResultNumber, ids);
        ids.clear();
        return result;
    }

    @Override
    public String getSeedUrls(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        List<Long> ids = this.getArrayList(db.get(job, BDBNetworkMap.PATH_ROOT_URLS));
        String result = combineUrlResultFromArrayIDs(job, harvestResultNumber, ids);
        ids.clear();
        return result;
    }

    @Override
    public String getMalformedUrls(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        List<Long> ids = this.getArrayList(db.get(job, BDBNetworkMap.PATH_MALFORMED_URLS));
        String result = combineUrlResultFromArrayIDs(job, harvestResultNumber, ids);
        ids.clear();
        return result;
    }

    @Override
    public String searchUrl(long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (searchCommand == null || db == null) {
            return null;
        }

        final List<String> result = new ArrayList<>();
        long countUrls = Long.parseLong(db.get(job, BDBNetworkMap.PATH_COUNT_URL));

        for (long id = 1; id <= countUrls; id++) {
            String urlStr = db.get(job, id);
            if (urlStr == null) {
                log.warn("Null value: job={}, harvestResultNumber={}, nodeId={}", job, harvestResultNumber, id);
                continue;
            }
            NetworkMapNode urlNode = getNodeEntity(urlStr);
            if (isIncluded(urlNode, searchCommand)) {
                result.add(urlStr);
            }
        }

        String json = this.obj2Json(result);
        result.clear();

        return json;
    }

    @Override
    public String getHopPath(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return null;
        }

        List<NetworkMapNode> listHopPath = new ArrayList<>();
        NetworkMapNode curNode = this.getNodeEntity(db.get(job, id));
        while (curNode != null) {
            listHopPath.add(curNode);
            long parentId = curNode.getParentId();
            if (parentId <= 0) {
                break;
            }
            curNode = this.getNodeEntity(db.get(job, parentId));
        }
        String json = this.obj2Json(listHopPath);

        listHopPath.forEach(NetworkMapNode::clear);
        listHopPath.clear();

        return json;
    }

    private String combineUrlResultFromArrayIDs(long job, int harvestResultNumber, List<Long> ids) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        if (ids == null || db == null) {
            return null;
        }

        final List<String> result = new ArrayList<>();
        ids.forEach(childId -> {
            String childStr = db.get(job, childId);
            if (childStr != null) {
                result.add(childStr);
            }
        });

        String json = this.obj2Json(result);
        result.clear();

        return json;
    }

    private String combineDomainResultFromArrayIDs(long job, int harvestResultNumber, List<Long> ids) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        if (ids == null || db == null) {
            return null;
        }

        final List<String> result = new ArrayList<>();
        ids.forEach(childId -> {
            String childStr = db.get(job, "d@" + childId);
            if (childStr != null) {
                result.add(childStr);
            }
        });

        String json = this.obj2Json(result);
        result.clear();

        return json;
    }

    private boolean isIncluded(NetworkMapNode node, NetworkMapServiceSearchCommand searchCommand) {
        return isIncludedByDomainName(URLResolverFunc.url2domain(node.getUrl()), searchCommand.getDomainNames()) &&
                isIncludedByContentType(node.getContentType(), searchCommand.getContentTypes()) &&
                isIncludedByStatusCode(node.getStatusCode(), searchCommand.getStatusCodes());
    }

    private boolean isIncludedByDomainName(String domainName, List<String> domainNameCondition) {
        if (domainNameCondition == null || domainNameCondition.size() == 0) {
            return true;
        }

        for (String e : domainNameCondition) {
            if (e.equals(domainName)) {
                return true;
            }
        }

        return false;
    }

    private boolean isIncludedByContentType(String contentType, List<String> contentTypeCondition) {
        if (contentTypeCondition == null || contentTypeCondition.size() == 0) {
            return true;
        }

        for (String e : contentTypeCondition) {
            if (e.equals(contentType)) {
                return true;
            }
        }

        return false;
    }

    private boolean isIncludedByStatusCode(int statusCode, List<Integer> statusCodeCondition) {
        if (statusCodeCondition == null || statusCodeCondition.size() == 0) {
            return true;
        }

        for (int e : statusCodeCondition) {
            if (e == statusCode) {
                return true;
            }
        }

        return false;
    }
}
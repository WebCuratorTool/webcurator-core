package org.webcurator.core.networkmap.service;

import org.webcurator.core.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.networkmap.metadata.NetworkMapNode;

import java.util.ArrayList;
import java.util.List;

public class NetworkMapLocalClient implements NetworkMapService {
    private BDBNetworkMap db;

    public NetworkMapLocalClient(BDBNetworkMap db) {
        this.db = db;
    }

    @Override
    public String get(String key) {
        return db.get(key);
    }

    @Override
    public String getNode(long job, long id) {
        return db.get(job, id);
    }

    @Override
    public String getOutlinks(long job, long id) {
        NetworkMapNode parentNode = this.getNodeEntity(db.get(job, id));
        if (parentNode == null) {
            return null;
        }

        String result = combineResultFromArrayIDs(job, parentNode.getOutlinks());
        parentNode.clear();

        return result;
    }

    @Override
    public String getChildren(long job, long id) {
        //TODO
        return "{}";
    }

    @Override
    public String getAllDomains(long job) {
        List<Long> ids = this.getArrayList(db.get(job, BDBNetworkMap.PATH_ROOT_DOMAINS));
        String result = combineResultFromArrayIDs(job, ids);
        ids.clear();
        return result;
    }

    @Override
    public String getSeedUrls(long job) {
        List<Long> ids = this.getArrayList(db.get(job, BDBNetworkMap.PATH_ROOT_URLS));
        String result = combineResultFromArrayIDs(job, ids);
        ids.clear();
        return result;
    }

    @Override
    public String getMalformedUrls(long job) {
        List<Long> ids = this.getArrayList(db.get(job, BDBNetworkMap.PATH_MALFORMED_URLS));
        String result = combineResultFromArrayIDs(job, ids);
        ids.clear();
        return result;
    }

    private String combineResultFromArrayIDs(long job, List<Long> ids) {
        if (ids == null) {
            return null;
        }

        final List<String> result = new ArrayList<>();
        ids.forEach(childId -> {
            String childStr = db.get(job, childId);
            if (childStr != null) {
                result.add(childStr);
            }
        });

        return this.obj2Json(result);
    }
}

package org.webcurator.core.networkmap.service;

import org.springframework.context.ApplicationContext;
import org.webcurator.core.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.util.ApplicationContextFactory;

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

        String result = combineResultFromArrayIDs(job, harvestResultNumber, parentNode.getOutlinks());
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
        String result = combineResultFromArrayIDs(job, harvestResultNumber, ids);
        ids.clear();
        return result;
    }

    @Override
    public String getSeedUrls(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        List<Long> ids = this.getArrayList(db.get(job, BDBNetworkMap.PATH_ROOT_URLS));
        String result = combineResultFromArrayIDs(job, harvestResultNumber, ids);
        ids.clear();
        return result;
    }

    @Override
    public String getMalformedUrls(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        List<Long> ids = this.getArrayList(db.get(job, BDBNetworkMap.PATH_MALFORMED_URLS));
        String result = combineResultFromArrayIDs(job, harvestResultNumber, ids);
        ids.clear();
        return result;
    }

    private String combineResultFromArrayIDs(long job, int harvestResultNumber, List<Long> ids) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

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

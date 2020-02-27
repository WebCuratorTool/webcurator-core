package org.webcurator.core.networkmap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.webcurator.core.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.networkmap.metadata.NetworkNodeDomain;
import org.webcurator.core.networkmap.metadata.NetworkNodeUrl;

import java.util.ArrayList;
import java.util.List;

@Component("NetworkMapLocalClient")
public class NetworkMapLocalClient implements NetworkMapService {
    @Autowired
    private BDBNetworkMap db;

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
        String result = "{}";

        NetworkNodeUrl parentNode = this.getNodeUrl(db.get(job, id));
        if (parentNode == null) {
            return result;
        }

        List<NetworkNodeUrl> list = new ArrayList<>();
        parentNode.getOutlinks().forEach(childId -> {
            NetworkNodeUrl childNode = this.getNodeUrl(db.get(job, childId));
            list.add(childNode);
        });

        result = this.obj2Json(list);

        parentNode.clear();
        list.forEach(NetworkNodeUrl::clear);
        list.clear();

        return result;
    }

    @Override
    public String getChildren(long job, long id) {
        String result = "{}";

        NetworkNodeDomain parentNode = this.getNodeDomain(db.get(job, id));
        if (parentNode == null) {
            return result;
        }

        List<NetworkNodeDomain> list = new ArrayList<>();
        parentNode.getOutlinks().forEach(childId -> {
            NetworkNodeDomain childNode = this.getNodeDomain(db.get(job, childId));
            list.add(childNode);
        });

        result = this.obj2Json(list);

        parentNode.clear();
        list.forEach(NetworkNodeDomain::clear);
        list.clear();

        return result;
    }

    @Override
    public String getAllDomains(long job) {
        return db.get(job, BDBNetworkMap.PATH_ROOT_DOMAINS);
    }

    @Override
    public String getSeedUrls(long job) {
        return db.get(job,BDBNetworkMap.PATH_ROOT_URLS);
    }

    @Override
    public String getMalformedUrls(long job) {
        return db.get(job,BDBNetworkMap.PATH_MALFORMED_URLS);
    }
}

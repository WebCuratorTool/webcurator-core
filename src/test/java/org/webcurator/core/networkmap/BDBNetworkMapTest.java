package org.webcurator.core.networkmap;

import org.junit.Test;
import org.webcurator.core.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.networkmap.service.NetworkMapLocalClient;


import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class BDBNetworkMapTest {
    private static final String DIR_ROOT = "/usr/local/wct/store/";

    @Test
    public void testBDB() {
        BDBNetworkMap db1 = new BDBNetworkMap();
        try {
            db1.initializeDB(DIR_ROOT + "_db_temp", "resource.db");
        } catch (IOException e) {
            e.printStackTrace();
        }

        int harvestResultNumber = 1;

        long job1 = 36;
        db1.delete(Long.toString(job1));
        testExtractor(db1, job1, harvestResultNumber);
        testReadData(db1, job1, harvestResultNumber);
        db1.shutdownDB();

        BDBNetworkMap db2 = new BDBNetworkMap();
        try {
            db1.initializeDB(DIR_ROOT + "_db_temp", "resource2.db");
        } catch (IOException e) {
            e.printStackTrace();
        }

        long job2 = 53;
        db2.delete(Long.toString(job2));
        testExtractor(db2, job2, harvestResultNumber);
        testReadData(db2, job2, harvestResultNumber);

        db2.shutdownDB();
    }

    public void testExtractor(BDBNetworkMap db, long job, int harvestResultNumber) {
        String directory = String.format("%s/%d/1", DIR_ROOT, job);
        try {
            WCTResourceIndexer indexer = new WCTResourceIndexer(new File(directory), db, job, harvestResultNumber);
            indexer.indexFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void testReadData(BDBNetworkMap db, long job, int harvestResultNumber) {
        NetworkMapLocalClient client = new NetworkMapLocalClient(new BDBNetworkMapPool("/usr/local/store"));

        System.out.println(client.getAllDomains(job, harvestResultNumber));

    }
}

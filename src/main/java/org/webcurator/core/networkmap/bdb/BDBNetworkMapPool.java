package org.webcurator.core.networkmap.bdb;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class BDBNetworkMapPool {
    private final static int MAX_SIZE = 10;
    private final static Logger log = LoggerFactory.getLogger(BDBNetworkMapPool.class);
    private final List<BDBNetworkMap> queue = new ArrayList<>();
    private final Map<String, BDBNetworkMap> map = new Hashtable<>();

    private String dbRootPath;

    public BDBNetworkMapPool(String dbRootPath) {
        this.dbRootPath = dbRootPath;
    }

    //Create and open a DB
    synchronized public BDBNetworkMap createInstance(long job, int harvestResultNumber) {
        String dbName = getDbName(job, harvestResultNumber);
        if (map.containsKey(dbName)) {
            BDBNetworkMap oldDb = map.get(dbName);
            oldDb.shutdownDB();
            queue.remove(oldDb);
            map.remove(dbName);
        }

        if (queue.size() >= MAX_SIZE) {
            BDBNetworkMap oldDb = queue.get(0);
            queue.remove(0);
            map.remove(oldDb.getDbName());
            oldDb.shutdownDB();
        }

        //Clear the path
        String dbPath = this.getDbPath(job, harvestResultNumber);
        try {

            File dbDirectory = new File(dbPath);
            if (dbDirectory.exists()) {
                FileUtils.cleanDirectory(dbDirectory);
            } else {
                log.warn("Recover db files: {}", dbPath);
            }
        } catch (IOException e) {
            log.error("Clear files failed: {}", dbPath);
            return null;
        }

        BDBNetworkMap db = new BDBNetworkMap();
        try {
            db.initializeDB(dbPath, dbName);
            queue.add(db);
            map.put(dbName, db);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to open db: {}-->{}, {}", dbPath, dbName, e.getMessage());
            return null;
        }

        return db;
    }

    //Open with read mode
    synchronized public BDBNetworkMap getInstance(long job, int harvestResultNumber) {
        String dbName = getDbName(job, harvestResultNumber);
        if (map.containsKey(dbName)) {
            return map.get(dbName);
        }

        if (queue.size() >= MAX_SIZE) {
            BDBNetworkMap oldDb = queue.get(0);
            queue.remove(0);
            map.remove(oldDb.getDbName());
            oldDb.shutdownDB();
        }

        BDBNetworkMap db = new BDBNetworkMap();
        String dbPath = this.getDbPath(job, harvestResultNumber);
        try {
            db.initializeDB(dbPath, dbName);
            queue.add(db);
            map.put(dbName, db);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to open db: {}-->{}, {}", dbPath, dbName, e.getMessage());
            return null;
        }

        return db;
    }

    public void close(long job, int harvestResultNumber) {
        String dbName = getDbName(job, harvestResultNumber);
        if (map.containsKey(dbName)) {
            BDBNetworkMap oldDb = map.get(dbName);
            oldDb.shutdownDB();
            queue.remove(oldDb);
            map.remove(dbName);
        }
    }

    private String getDbPath(long job, int harvestResultNumber) {
        return String.format("%s%s%d%s%d%s_resource", this.dbRootPath, File.separator, job, File.separator, harvestResultNumber, File.separator);
    }

    private String getDbName(long job, int harvestResultNumber) {
        return String.format("%d_%d", job, harvestResultNumber);
    }
}

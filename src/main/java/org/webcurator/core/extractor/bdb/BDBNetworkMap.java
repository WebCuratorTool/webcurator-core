package org.webcurator.core.extractor.bdb;

import com.sleepycat.je.*;
import org.springframework.stereotype.Component;
import org.webcurator.core.extractor.metadata.NetworkNode;
import org.webcurator.core.extractor.metadata.NetworkNodeDomain;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * Borrow(copy) from openwayback
 */
@Component("BDBNetworkMap")
public class BDBNetworkMap {
    public final static String PATH_ROOT_URLS="rootUrls";
    public final static String PATH_MALFORMED_URLS="malformedUrls";
    public final static String PATH_ROOT_DOMAINS="rootDomains";

    public final static Charset UTF8 = StandardCharsets.UTF_8;

    /**
     * Maximum BDBJE file size
     */
    private final static String JE_LOG_FILEMAX = "256000000";
    /**
     * path to directory containing the BDBJE files
     */
    private String path;

    /**
     * name of BDBJE db within the path directory
     */
    private String dbName;

    /**
     * BDBJE Environment
     */
    Environment env = null;

    /**
     * BDBJE Database
     */
    Database db = null;

    /**
     * @param thePath   Directory where BDBJE files are stored
     * @param theDbName Name of files in thePath
     * @throws IOException for usual reasons, plus as database exceptions
     */
    public void initializeDB(final String thePath, final String theDbName)
            throws IOException {
        path = thePath;
        dbName = theDbName;

        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setCacheSize(1024 * 1024 * 1024);
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(true);
        environmentConfig.setConfigParam("je.log.fileMax", JE_LOG_FILEMAX);
        File file = new File(path);
        if (!file.isDirectory()) {
            if (!file.mkdirs()) {
                throw new IOException("failed mkdirs(" + path + ")");
            }
        }
        env = new Environment(file, environmentConfig);
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setAllowCreate(true);
        databaseConfig.setTransactional(true);
        // perform other database configurations

        db = env.openDatabase(null, dbName, databaseConfig);
    }

    /**
     * shut down the BDB.
     *
     * @throws DatabaseException
     */
    public synchronized void shutdownDB() throws DatabaseException {

        if (db != null) {
            db.close();
            db = null;
        }

        if (env != null) {
            env.close();
            env = null;
        }
    }

    /**
     * @param s
     * @return byte array representation of String s in UTF-8
     */
    public static byte[] stringToBytes(String s) {
        return s.getBytes(UTF8);
    }

    /**
     * @param ba
     * @return String of UTF-8 encoded bytes ba
     */
    public static String bytesToString(byte[] ba) {
        return new String(ba, UTF8);
    }

    /**
     * @param startKey
     * @return iterator for BDBRecords
     * @throws DatabaseException
     */
    public RecordIterator domainIterator(final String startKey)
            throws DatabaseException {
        return domainIterator(startKey, true);
    }

    /**
     * @param startKey
     * @param forward
     * @return iterator for BDBRecords
     * @throws DatabaseException
     */
    public RecordIterator domainIterator(final String startKey,
                                         final boolean forward) throws DatabaseException {
        Cursor cursor = db.openCursor(null, null);
        return new RecordIterator(cursor, startKey, !forward, NetworkNode.TYPE_DOMAIN);
    }

    /**
     * @param startKey
     * @return iterator for BDBRecords
     * @throws DatabaseException
     */
    public RecordIterator urlIterator(final String startKey)
            throws DatabaseException {
        return urlIterator(startKey, true);
    }

    /**
     * @param startKey
     * @param forward
     * @return iterator for BDBRecords
     * @throws DatabaseException
     */
    public RecordIterator urlIterator(final String startKey,
                                      final boolean forward) throws DatabaseException {
        Cursor cursor = db.openCursor(null, null);
        return new RecordIterator(cursor, startKey, !forward, NetworkNode.TYPE_URL);
    }

    /**
     * @param itr
     */
    public void insertRecords(final Iterator<NetworkNode> itr) {
        OperationStatus status = null;
        try {
            Transaction txn = env.beginTransaction(null, null);
            try {
                Cursor cursor = db.openCursor(txn, null);
                while (itr.hasNext()) {
                    NetworkNode node = itr.next();
                    DatabaseEntry key = new DatabaseEntry(Long.toString(node.getId()).getBytes());
                    DatabaseEntry value = new DatabaseEntry(node.toString().getBytes());
                    status = cursor.put(key, value);
                    if (status != OperationStatus.SUCCESS) {
                        throw new RuntimeException("put() non-success status");
                    }
                }
                cursor.close();
                txn.commit();
            } catch (DatabaseException e) {
                if (txn != null) {
                    txn.abort();
                }
                e.printStackTrace();
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /**
     * persistantly store key-value pair
     *
     * @param keyStr
     * @param valueStr
     * @throws DatabaseException
     */
    public void put(String keyStr, String valueStr) throws DatabaseException {
        DatabaseEntry key = new DatabaseEntry(stringToBytes(keyStr));
        DatabaseEntry data = new DatabaseEntry(stringToBytes(valueStr));
        db.put(null, key, data);
    }

    /**
     * retrieve the value assoicated with keyStr from persistant storage
     *
     * @param keyStr
     * @return String value associated with key, or null if no key is found
     * or an error occurs
     * @throws DatabaseException
     */
    public String get(String keyStr) throws DatabaseException {
        String result = null;
        DatabaseEntry key = new DatabaseEntry(stringToBytes(keyStr));
        DatabaseEntry data = new DatabaseEntry();
        if (db.get(null, key, data, LockMode.DEFAULT)
                == OperationStatus.SUCCESS) {

            result = bytesToString(data.getData());
        }
        return result;
    }

    public static String getKeyPath(long id) {
        return String.format("id_%d", id);
    }


    /**
     * @param keyStr
     * @throws DatabaseException
     */
    public void delete(String keyStr) throws DatabaseException {
        db.delete(null, new DatabaseEntry(stringToBytes(keyStr)));
    }

    /**
     * @return Returns the dbName.
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @param dbName the dbName to set
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}

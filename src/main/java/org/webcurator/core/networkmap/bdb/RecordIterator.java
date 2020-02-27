package org.webcurator.core.networkmap.bdb;

import com.sleepycat.je.*;
import org.webcurator.core.networkmap.metadata.NetworkNode;
import org.webcurator.core.networkmap.metadata.NetworkNodeDomain;
import org.webcurator.core.networkmap.metadata.NetworkNodeUrl;

import java.util.NoSuchElementException;

public class RecordIterator {
    private DatabaseEntry key;
    private DatabaseEntry value;
    private boolean hitLast;
    private boolean gotNext;
    private Cursor cursor;
    private boolean backward;
    private NetworkNode node;

    /**
     * @param cursor
     * @param search
     * @throws DatabaseException
     */
    public RecordIterator(Cursor cursor, String search, int type)
            throws DatabaseException {
        initialize(cursor, search, false, type);
    }

    /**
     * @param cursor
     * @param search
     * @param backward
     * @throws DatabaseException
     */
    public RecordIterator(Cursor cursor, String search, boolean backward, int type)
            throws DatabaseException {
        initialize(cursor, search, backward, type);
    }

    private void initialize(Cursor cursor, String search, boolean backward, int type)
            throws DatabaseException {
        this.cursor = cursor;
        this.backward = backward;
        key = new DatabaseEntry();
        value = new DatabaseEntry();
        key.setData(search.getBytes());
        key.setPartial(false);
        OperationStatus status = cursor.getSearchKeyRange(key, value,
                LockMode.DEFAULT);
        if (backward && (status == OperationStatus.SUCCESS)) {
            // if we are in reverse, immediately back up one record:
            status = cursor.getPrev(key, value, LockMode.DEFAULT);
        }
        if (status == OperationStatus.SUCCESS) {
            gotNext = true;
        }

        if (type == NetworkNode.TYPE_DOMAIN) {
            node = new NetworkNodeDomain();
        } else {
            node = new NetworkNodeUrl();
        }
        node.initialize(BDBNetworkMap.bytesToString(value.getData()));
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        if (hitLast) {
            return false;
        }
        if (cursor == null) {
            return false;
        }
        if (!gotNext) {
            OperationStatus status;
            // attempt to get the next:
            try {
                if (backward) {
                    status = cursor.getPrev(key, value, LockMode.DEFAULT);
                } else {
                    status = cursor.getNext(key, value, LockMode.DEFAULT);
                }
                if (status == OperationStatus.SUCCESS) {
                    gotNext = true;
                } else {
                    close();
                }
            } catch (DatabaseException e) {
                // SLOP: throw a runtime?
                e.printStackTrace();
                close();
            }
        }
        return gotNext;
    }

    public void close() {
        if (!hitLast) {
            hitLast = true;
            try {
                cursor.close();
            } catch (DatabaseException e) {
                // TODO what to do?
                // let's just eat it for now..
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public NetworkNode next() {
        if (!gotNext) {
            throw new NoSuchElementException();
        }
        gotNext = false;
        return node;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public DatabaseEntry getKey() {
        return key;
    }

    public DatabaseEntry getValue() {
        return value;
    }

    public NetworkNode getNode() {
        return node;
    }
}

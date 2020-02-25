package org.webcurator.core.extractor;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.webcurator.core.extractor.metadata.ResourceNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

abstract public class ResourceExtractor {
    protected static final int MAX_URL_LENGTH = 1020;
    protected Map<String, ResourceNode> results = new HashMap<>();

    public void extract(ArchiveReader reader) throws IOException {
        preProcess();
        Iterator<ArchiveRecord> it = reader.iterator();
        while (it.hasNext()) {
            extractRecord(it.next());
        }
        postProcess();
    }

    abstract protected void preProcess();

    abstract protected void postProcess();

    abstract protected void extractRecord(ArchiveRecord rec) throws IOException;

    public Map<String, ResourceNode> getResults() {
        return results;
    }

    public void setResults(Map<String, ResourceNode> results) {
        this.results = results;
    }

    public void clear() {
        this.results.clear();
    }

    /**
     * borrowed(copied) from org.archive.io.arc.ARCRecord...
     *
     * @param bytes Array of bytes to examine for an EOL.
     * @return Count of end-of-line characters or zero if none.
     */
    public int getEolCharsCount(byte[] bytes) {
        int count = 0;
        if (bytes != null && bytes.length >= 1 &&
                bytes[bytes.length - 1] == '\n') {
            count++;
            if (bytes.length >= 2 && bytes[bytes.length - 2] == '\r') {
                count++;
            }
        }
        return count;
    }
}

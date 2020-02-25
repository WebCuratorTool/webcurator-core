package org.webcurator.core.extractor;

import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.archive.format.http.HttpHeaderParser;
import org.archive.format.http.HttpHeaders;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.RecoverableIOException;
import org.archive.io.warc.WARCConstants;
import org.archive.io.warc.WARCRecord;
import org.webcurator.core.extractor.metadata.ResourceNode;

import java.io.IOException;

@SuppressWarnings("all")
public class ResourceExtractorWarc extends ResourceExtractor {
    @Override
    protected void preProcess() {
        //Do nothing
    }

    @Override
    protected void postProcess() {
        //Do nothing
    }

    @Override
    protected void extractRecord(ArchiveRecord rec) throws IOException {
        String mime = rec.getHeader().getMimetype();
        if (mime.equals("text/dns")) {
            return;
        }

        WARCRecord record = (WARCRecord) rec;
        ArchiveRecordHeader header = record.getHeader();

        // If the URL length is too long for the database, skip adding the URL
        // to the index. This ensures that the harvest completes successfully.
        if (header.getUrl() == null || header.getUrl().length() > MAX_URL_LENGTH) {
            return;
        }

        String key = null;
        String warcRecordId = header.getHeaderValue(WARCConstants.HEADER_KEY_ID).toString();
        if (warcRecordId == null) {
            return;
        }
        warcRecordId = warcRecordId.substring(1, warcRecordId.length() - 1);
        if (header.getHeaderValue(WARCConstants.HEADER_KEY_CONCURRENT_TO) != null) {
            String warcConcurrentTo = header.getHeaderValue(WARCConstants.HEADER_KEY_CONCURRENT_TO).toString();
            int lenDelta = warcConcurrentTo.length() - warcRecordId.length() - 2;
            warcConcurrentTo = warcConcurrentTo.substring(1, warcConcurrentTo.length() - 1 - lenDelta);
            key = warcConcurrentTo;
        } else {
            key = warcRecordId;
        }

        ResourceNode res = null;
        if (results.containsKey(key)) {
            res = results.get(key);
        } else {
            res = new ResourceNode();
            results.put(key, res);
        }

        String type = rec.getHeader().getHeaderValue(WARCConstants.HEADER_KEY_TYPE).toString();
        if (type.equals(WARCConstants.RESPONSE)) {
            res.setUrl(header.getUrl());
            res.setResourceOffset(header.getOffset());

            // need to parse the documents HTTP message and headers here: WARCReader
            // does not implement this...

            byte[] statusBytes = HttpParser.readRawLine(record);
            int eolCharCount = getEolCharsCount(statusBytes);
            if (eolCharCount <= 0) {
                throw new RecoverableIOException("Failed to read http status where one " +
                        " was expected: " + new String(statusBytes));
            }
            String statusLine = EncodingUtil.getString(statusBytes, 0,
                    statusBytes.length - eolCharCount, WARCConstants.DEFAULT_ENCODING);
            if (!StatusLine.startsWithHTTP(statusLine)) {
                throw new RecoverableIOException("Failed parse of http status line.");
            }
            StatusLine status = new StatusLine(statusLine);
            res.setStatusCode(status.getStatusCode());

            // Calculate the length.
            long length = header.getLength() - header.getContentBegin();
            res.setContentLength(length);

            HttpHeaders httpHeaders = new HttpHeaderParser().parseHeaders(record);
            String contentType = httpHeaders.getValue(WARCConstants.CONTENT_TYPE);
            if (contentType != null && contentType.length() > 0) {
                res.setContentType(httpHeaders.getValue(WARCConstants.CONTENT_TYPE));
            }
            httpHeaders.clear();
        } else if (type.equals(WARCConstants.METADATA)) {
            HttpHeaders httpHeaders = new HttpHeaderParser().parseHeaders(record);
            res.setViaUrl(httpHeaders.getValue("via"));
        }
    }
}

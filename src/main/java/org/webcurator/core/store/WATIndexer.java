package org.webcurator.core.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.archive.extract.*;
import org.archive.resource.Resource;
import org.archive.resource.ResourceParseException;
import org.archive.resource.ResourceProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.util.URLResolverFunc;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class WATIndexer {
    private static final Logger log = LoggerFactory.getLogger(WATIndexer.class);
    protected static final URLResolverFunc urlResolverFunc = new URLResolverFunc();
    protected static final String TEMP_METADATA_WAT_FILE_NAME = "temp_metadata.wat.gz";
    protected static final String TEMP_METADATA_JSON_FILE_NAME = "temp_metadata.json";
    protected static final String LGA_METADATA_CSV_FILE_NAME = "lga.csv";
    protected static final String JSON_METADATA_PREFIX = "{\"Container\":";

    public static void main(String[] args) {
        WATIndexer watIndexer = new WATIndexer();
        File directory = new File("/home/leefr/wct/archive-visualization/src/test/resources");
        File[] fileList = directory.listFiles(new IndexerBase.ARCFilter());

        try {
            watIndexer.extract(directory, fileList);
        } catch (IOException | ResourceParseException e) {
            e.printStackTrace();
        }
    }


    /**
     * Extract metadata from archive file and static nodes and outlinks
     *
     * @param directory: archived path
     * @param fileList:  warc, warc.gz, arc, arc.gz files
     * @return json string
     * @throws IOException:            when read or write file failed
     * @throws ResourceParseException: when parse file failed
     */
    public String extract(File directory, File[] fileList) throws IOException, ResourceParseException {
        if (fileList == null || fileList.length == 0) {
            log.error("Null or empty input params");
            return null;
        }

        //Extracting archived file and caching in disk to avoid consuming huge memory
        File jsonFile = new File(String.format("%s%s%s", directory.getAbsolutePath(), File.separator, TEMP_METADATA_JSON_FILE_NAME));
//        BufferedWriter jsonWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jsonFile)));
//        for (File f : fileList) {
//            extract(directory, f, jsonWriter);
//        }
//        jsonWriter.close();

        //Parsing wat file and writing to "csv" file
        parseWatFromJsonFile(directory, jsonFile);
//        Files.deleteIfExists(jsonFile.toPath());
        return "{}";
    }

    /**
     * Extract metadata from archive file and static nodes and outlinks
     *
     * @param file:       warc, warc.gz, arc, arc.gz file
     * @param jsonWriter: temp metadata of json format
     * @throws IOException:            when read or write file failed
     * @throws ResourceParseException: when parse file failed
     */
    public void extract(File directory, File file, BufferedWriter jsonWriter) throws IOException, ResourceParseException {
        File watFile = new File(String.format("%s%s%s", directory.getAbsolutePath(), File.separator, TEMP_METADATA_WAT_FILE_NAME));

        Files.deleteIfExists(watFile.toPath());

        //Parse warc/arc file and extract metadata to wat file==============================================
        OutputStream watTempGzipOutputStream = new BufferedOutputStream(new FileOutputStream(watFile));
        ExtractorOutput out = new WATExtractorOutput(watTempGzipOutputStream);
        ResourceProducer producer = ProducerUtils.getProducer(file.getAbsolutePath());
        ResourceFactoryMapper mapper = new ExtractingResourceFactoryMapper();
        ExtractingResourceProducer exProducer = new ExtractingResourceProducer(producer, mapper);
        Resource r;
        while ((r = exProducer.getNext()) != null) {
            out.output(r);
        }
        exProducer.close();
        watTempGzipOutputStream.close();
        //===================================================================================================

        //Read wat file and filter out metadata and save to json file=========================================
        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new BufferedInputStream(new FileInputStream(watFile)))));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith(JSON_METADATA_PREFIX)) {
                jsonWriter.write(line);
                jsonWriter.newLine();
            }
        }
        reader.close();
        //===================================================================================================

        Files.deleteIfExists(watFile.toPath());
    }


    /**
     * Parse metadata from json containers
     *
     * @param directory: the fold storing warc/arc files
     * @param jsonFile   : the temp json file
     * @throws IOException: when read file failed
     */
    private void parseWatFromJsonFile(File directory, File jsonFile) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(jsonFile))));

        //Find the first line of WAT file, and set the first line as starting flag of a section.
        Map<String, MetadataNode> mapWarc = new HashMap<>();
        Map<String, MetadataNode> mapArc = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            try {
                String sessionId = MetadataNode.getSessionId(line);
                MetadataNode node = mapWarc.get(sessionId);
                if (node == null) {
                    if (sessionId.startsWith(WarcMetadataNode.TYPE)) {
                        node = new WarcMetadataNode();
                        mapWarc.put(sessionId, node);
                    } else {
                        node = new ArcMetadataNode();
                        mapArc.put(sessionId, node);
                    }
                }
                node.parse(line);
            } catch (IOException e) {
                log.info("Invalid json metadata: {}", line);
            }
        }
        reader.close();

        //Codec extracting data:
        Path csvPath = Paths.get(String.format("%s%s%s", directory.getAbsolutePath(), File.separator, LGA_METADATA_CSV_FILE_NAME));
        Files.deleteIfExists(csvPath);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvPath.toFile())));

        mapWarc.forEach((key, node) -> {
            try {
                writer.write(node.toString());
                writer.newLine();
            } catch (IOException e) {
                log.error("Write to csv file failed");
            }
            node.clear();
        });
        mapWarc.clear();

        writer.close();
    }
}

abstract class MetadataNode {
    private static final String PATH_FORMAT = "Envelope/Format";
    private static final String PATH_TARGET_URI_OF_ARC = "Envelope/ARC-Header-Metadata/Target-URI";
    private static final String PATH_TARGET_URI_OF_WARC = "Envelope/WARC-Header-Metadata/Target-URI";
    private static final String PATH_CONTENT_TYPE = "Envelope/Payload-Metadata/HTTP-Response-Metadata/Headers/Content-Type";
    private static final String PATH_CONTENT_LENGTH = "Envelope/Actual-Content-Length";
    private static final String PATH_STATUS_CODE = "Envelope/Payload-Metadata/HTTP-Response-Metadata/Response-Message/Status";
    private static final String PATH_HOST_BASE = "Envelope/Payload-Metadata/HTTP-Response-Metadata/HTML-Metadata.Head/Base";
    private static final String PATH_LINKS = "Envelope/Payload-Metadata/HTTP-Response-Metadata/HTML-Metadata/Links";

    public boolean isSeed = false;
    public String url = "";
    public String referer = "";
    public String host = "";
    public String contentType = "";
    public String contentLength = "";
    public String statusCode = "";

    protected JsonNode jsonRoot;

    public static String getSessionId(String jsonContainer) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonObj = mapper.readTree(jsonContainer);

        String archive_type = getValue(jsonObj, PATH_FORMAT);
        String sessionId = null;
        if (archive_type.equalsIgnoreCase(WarcMetadataNode.TYPE)) {
            sessionId = WarcMetadataNode.TYPE;
            String targetId = getValue(jsonObj, WarcMetadataNode.PATH_WARC_RECORD_ID);
            targetId = targetId.substring(1, targetId.length() - 1);
            String concurrentId = getValue(jsonObj, WarcMetadataNode.PATH_WARC_CONCURRENT_TO);
            if (concurrentId == null || concurrentId.length() == 0) {
                concurrentId = targetId;
            } else {
                int lenDelta = concurrentId.length() - targetId.length() - 2;
                concurrentId = concurrentId.substring(1, concurrentId.length() - 1 - lenDelta);
            }
            sessionId += concurrentId;
        } else if (archive_type.equalsIgnoreCase(ArcMetadataNode.TYPE)) {
            sessionId = ArcMetadataNode.TYPE;
            sessionId += getValue(jsonObj, ArcMetadataNode.PATH_TARGET_URI);
        } else {
            throw new IOException("Invalid json container.");
        }

        return sessionId;
    }

    protected String getValue(String path) {
        return getValue(this.jsonRoot, path);
    }

    protected JsonNode findJsonNodes(String path) {
        return findJsonNodes(this.jsonRoot, path);
    }

    public static String getValue(JsonNode target, String path) {
        JsonNode jsonNode = findJsonNodes(target, path);
        return jsonNode == null ? "" : jsonNode.asText();
    }

    public static JsonNode findJsonNodes(JsonNode target, String path) {
        JsonNode jsonNode = target;
        String[] items = path.split("/");
        for (int i = 0; i < items.length && jsonNode != null; i++) {
            jsonNode = jsonNode.findValue(items[i]);
        }
        return jsonNode;
    }

    abstract public boolean parse(String jsonContainer) throws IOException;

    abstract public void clear();
}

class ArcMetadataNode extends MetadataNode {
    public static final String TYPE = "ARC";
    public static final String PATH_TARGET_URI = "Envelope/ARC-Header-Metadata/Target-URI";
    public static final String PATH_CONTENT_TYPE = "Envelope/ARC-Header-Metadata/Content-Type";
    public static final String PATH_CONTENT_LENGTH = "Envelope/ARC-Header-Metadata/Content-Length";
    public static final String PATH_STATUS_CODE = "Envelope/Payload-Metadata/HTTP-Response-Metadata/Response-Message/Status";
    public static final String PATH_HOST_BASE = "Envelope/Payload-Metadata/HTTP-Response-Metadata/HTML-Metadata/Head/Base";
    public static final String PATH_HEAD_SCRIPT = "Envelope/Payload-Metadata/HTTP-Response-Metadata/HTML-Metadata/Head/Scripts";
    public static final String PATH_HEAD_LINK = "Envelope/Payload-Metadata/HTTP-Response-Metadata/HTML-Metadata/Head/Link";
    public static final String PATH_LINKS = "Envelope/Payload-Metadata/HTTP-Response-Metadata/HTML-Metadata/Links";

    private List<String> children = new ArrayList<>();

    @Override
    public boolean parse(String jsonContainer) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.jsonRoot = mapper.readTree(jsonContainer);
        return true;
    }

    @Override
    public void clear() {
        this.children.clear();
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }
}

class WarcMetadataNode extends MetadataNode {
    public static final String TYPE = "WARC";
    public static final String PATH_TARGET_URI = "Envelope/WARC-Header-Metadata/WARC-Target-URI";
    public static final String PATH_WARC_CONCURRENT_TO = "Envelope/WARC-Header-Metadata/WARC-Concurrent-To";
    public static final String PATH_WARC_RECORD_ID = "Envelope/WARC-Header-Metadata/WARC-Record-ID";
    public static final String PATH_WARC_TYPE = "Envelope/WARC-Header-Metadata/WARC-Type";
    public static final String WARC_TYPE_REQUEST = "request";
    public static final String WARC_TYPE_RESPONSE = "response";
    public static final String WARC_TYPE_METADATA = "metadata";
    public static final String PATH_REQUEST_REFERER = "Envelope/Payload-Metadata/HTTP-Request-Metadata/Headers/Referer";
    public static final String PATH_REQUEST_HOST = "Envelope/Payload-Metadata/HTTP-Request-Metadata/Headers/Host";
    public static final String PATH_RESPONSE_CONTENT_TYPE = "Envelope/Payload-Metadata/HTTP-Response-Metadata/Headers/Content-Type";
    public static final String PATH_RESPONSE_CONTENT_LENGTH = "Envelope/Payload-Metadata/HTTP-Response-Metadata/Headers/Content-Length";
    public static final String PATH_RESPONSE_STATUS_CODE = "Envelope/Payload-Metadata/HTTP-Response-Metadata/Response-Message/Status";
    public static final String PATH_METADATA_RECORDS = "Envelope/Payload-Metadata/WARC-Metadata-Metadata/Metadata-Records";

//    private static final String PATH_HOST_BASE = "Envelope/Payload-Metadata/HTTP-Response-Metadata/HTML-Metadata/Head/Base";
//    private static final String PATH_LINKS = "Envelope/Payload-Metadata/HTTP-Response-Metadata/HTML-Metadata/Links";

    public String parent;


    @Override
    public boolean parse(String jsonContainer) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.jsonRoot = mapper.readTree(jsonContainer);

        this.url = this.getValue(PATH_TARGET_URI);

        String warcType = this.getValue(PATH_WARC_TYPE);
        if (warcType.equalsIgnoreCase(WARC_TYPE_REQUEST)) {
            this.referer = this.getValue(PATH_REQUEST_REFERER);
            this.host = this.getValue(PATH_REQUEST_HOST);
        } else if (warcType.equalsIgnoreCase(WARC_TYPE_RESPONSE)) {
            this.contentType = this.getValue(PATH_RESPONSE_CONTENT_TYPE);
            this.contentLength = this.getValue(PATH_RESPONSE_CONTENT_LENGTH);
            this.statusCode = this.getValue(PATH_RESPONSE_STATUS_CODE);
        } else if (warcType.equalsIgnoreCase(WARC_TYPE_METADATA)) {
            JsonNode metadataRecords = this.findJsonNodes(PATH_METADATA_RECORDS);
            if (metadataRecords == null) {
                return false;
            }
            Iterator<JsonNode> records = metadataRecords.elements();
            while (records.hasNext()) {
                JsonNode element = records.next();
                String name = element.get("Name").asText();
                if (name != null && name.equalsIgnoreCase("via")) {
                    this.parent = element.get("Value").asText();
                }
                if (name != null && name.equalsIgnoreCase("seed")) {
                    this.isSeed = true;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%b", url, contentType, contentLength, statusCode, parent, isSeed);
    }

    @Override
    public void clear() {

    }
}
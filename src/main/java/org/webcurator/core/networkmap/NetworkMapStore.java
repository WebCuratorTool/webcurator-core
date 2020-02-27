package org.webcurator.core.networkmap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.webcurator.core.networkmap.metadata.NetworkNodeDomain;
import org.webcurator.core.networkmap.metadata.ResourceNode;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NetworkMapStore {
    private static final String DIR_RESOURCE = "resource";
    private static final String FILE_NAME_DOMAIN = "domain.json";
    private File directory;

    public NetworkMapStore(File rootDirectory) throws IOException {
        this.directory = new File(rootDirectory, DIR_RESOURCE);
        Files.deleteIfExists(this.directory.toPath());
        Files.createDirectory(this.directory.toPath());
    }

    public void writeUrl(Map<String, NetworkNodeDomain> domainMap, Map<String, ResourceNode> results) {
        Map<String, List<ResourceNode>> groupedResources = results.values().stream().collect(Collectors.groupingBy(ResourceNode::getDomain));
        groupedResources.forEach((k, v) -> {
            NetworkNodeDomain domain = domainMap.get(k);
            if (domain == null) {
                return;
            }
            String fileName = String.format("%d.unl", domain.getId());
            File urlFile = new File(directory, fileName);
            try {
                BufferedWriter fw = new BufferedWriter(new FileWriter(urlFile, true));
                v.forEach(resourceNode -> {
                    try {
                        fw.write(resourceNode.toString());
                        fw.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void writeDomain(Map<String, NetworkNodeDomain> domainMap) throws IOException {
        String json = "{}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(domainMap.values());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        File domainFile = new File(this.directory, FILE_NAME_DOMAIN);
        Files.write(domainFile.toPath(), json.getBytes());
    }
}

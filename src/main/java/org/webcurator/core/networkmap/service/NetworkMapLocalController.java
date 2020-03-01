package org.webcurator.core.networkmap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NetworkMapLocalController implements NetworkMapService {
    @Autowired
    private NetworkMapLocalClient client;

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_COMMON, method = {RequestMethod.POST, RequestMethod.GET})
    public String get(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("key") String key) {
        return client.get(job, harvestResultNumber, key);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_NODE, method = {RequestMethod.POST, RequestMethod.GET})
    public String getNode(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("id") long id) {
        return client.getNode(job, harvestResultNumber, id);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_OUTLINKS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getOutlinks(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("id") long id) {
        return client.getOutlinks(job, harvestResultNumber, id);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_CHILDREN, method = {RequestMethod.POST, RequestMethod.GET})
    public String getChildren(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("id") long id) {
        return client.getChildren(job, harvestResultNumber, id);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_ALL_DOMAINS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getAllDomains(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        return client.getAllDomains(job, harvestResultNumber);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_ROOT_URLS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getSeedUrls(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        return client.getSeedUrls(job, harvestResultNumber);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_MALFORMED_URLS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getMalformedUrls(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        return client.getMalformedUrls(job, harvestResultNumber);
    }
}

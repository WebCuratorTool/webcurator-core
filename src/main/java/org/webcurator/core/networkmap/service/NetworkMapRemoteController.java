package org.webcurator.core.networkmap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NetworkMapRemoteController implements NetworkMapService {
    @Autowired
    private NetworkMapRemoteClient client;

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_COMMON, method = {RequestMethod.POST, RequestMethod.GET})
    public String get(@RequestParam("key") String key) {
        return client.get(key);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_NODE, method = {RequestMethod.POST, RequestMethod.GET})
    public String getNode(@RequestParam("job") long job, @RequestParam("id") long id) {
        return client.getNode(job, id);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_OUTLINKS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getOutlinks(@RequestParam("job") long job, @RequestParam("id") long id) {
        return client.getOutlinks(job, id);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_CHILDREN, method = {RequestMethod.POST, RequestMethod.GET})
    public String getChildren(@RequestParam("job") long job, @RequestParam("id") long id) {
        return client.getChildren(job, id);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_ALL_DOMAINS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getAllDomains(@RequestParam("job") long job) {
        return client.getAllDomains(job);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_ROOT_URLS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getSeedUrls(@RequestParam("job") long job) {
        return client.getSeedUrls(job);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_MALFORMED_URLS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getMalformedUrls(@RequestParam("job") long job) {
        return client.getMalformedUrls(job);
    }
}

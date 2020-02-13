package org.webcurator.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class URLResolverFunc {
    private static final Logger log = LoggerFactory.getLogger(URLResolverFunc.class);
    private URL baseURL;
    private String lastBase;

    public URLResolverFunc() {
        baseURL = null;
        lastBase = null;
    }

    public static boolean isAbsolute(String url) {
        if (url == null) {
            return false;
        } else {
            return url.startsWith("http://")
                    || url.startsWith("https://")
                    || url.startsWith("ftp://")
                    || url.startsWith("feed://")
                    || url.startsWith("mailto:")
                    || url.startsWith("mail:")
                    || url.startsWith("javascript:")
                    || url.startsWith("rtsp://");
        }
    }

    private String resolve(String base, String rel) {
        URL absURL = null;
        if (lastBase != null) {
            if (lastBase.equals(base)) {
                try {
                    absURL = new URL(baseURL, rel);
                } catch (MalformedURLException e) {
                    log.warn("Malformed rel url: {}", rel);
                    return null;
                }
            }
        }
        if (absURL == null) {
            try {
                baseURL = new URL(base);
                lastBase = base;
            } catch (MalformedURLException e) {
                log.warn("Malformed base url: {}", base);
                return null;
            }
            try {
                absURL = new URL(baseURL, rel);
            } catch (MalformedURLException e) {
                log.warn("Malformed rel url: {}", rel);
                return null;
            }
        }
        return absURL.toString();
    }

    public String doResolve(String page, String base, String url) {
        //Filter valid urls
        if (!isAbsolute(page) && !isAbsolute(base) && !isAbsolute(url)) {
            return url;
        }
        if ((url == null) || (url.length() == 0)) {
            return url;
        }
        if (isAbsolute(url)) {
            return url;
        }
        if ((base != null) && (base.length() > 0)) {
            String tmp = resolve(base, url);
            if (tmp != null) {
                return tmp;
            }
        }
        if ((page != null) && (page.length() > 0)) {
            String tmp = resolve(page, url);
            if (tmp != null) {
                return tmp;
            }
        }
        return url;
    }

    public static String url2domain(String url) {
        int idx = url.indexOf("://");
        int idxEnd = idx > 0 ? url.indexOf('/', idx + 3) : url.indexOf('/');
        idx = idx < 0 ? 0 : idx + 3;
        idxEnd = idxEnd > 0 ? idxEnd : url.indexOf('?');
        if (idxEnd > 0) {
            return url.substring(idx, idxEnd);
        }
        return url;
    }
}

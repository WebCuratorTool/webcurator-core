package org.webcurator.core.profiles;

import org.springframework.web.context.WebApplicationContext;
import org.webcurator.core.util.ApplicationContextFactory;

public class PolitenessOptions {
    private double delayFactor;
    private long minDelayMs;
    private long maxDelayMs;
    private long respectCrawlDelayUpToSeconds;
    private long maxPerHostBandwidthUsageKbSec;
    public static final String POLITE = "Polite";
    public static final String MEDIUM = "Medium";
    public static final String AGGRESSIVE = "Aggressive";
    public static final String CUSTOM = "Custom";
    public static final String[] POLITENESS_OPTIONS = {POLITE, MEDIUM, AGGRESSIVE, CUSTOM};

    private String politeness = CUSTOM;

    public PolitenessOptions(String politeness, double delayFactor, long minDelayMs, long maxDelayMs, long respectCrawlDelayUpToSeconds, long maxPerHostBandwidthUsageKbSec) {
        this.politeness = politeness;
        this.delayFactor = delayFactor;
        this.minDelayMs = minDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.respectCrawlDelayUpToSeconds = respectCrawlDelayUpToSeconds;
        this.maxPerHostBandwidthUsageKbSec = maxPerHostBandwidthUsageKbSec;
    }

    public PolitenessOptions(double delayFactor, long minDelayMs, long maxDelayMs, long respectCrawlDelayUpToSeconds, long maxPerHostBandwidthUsageKbSec) {
        this(CUSTOM, delayFactor, minDelayMs, maxDelayMs, respectCrawlDelayUpToSeconds, maxPerHostBandwidthUsageKbSec);
    }

    public double getDelayFactor() {
        return delayFactor;
    }

    public void setDelayFactor(double delayFactor) {
        this.delayFactor = delayFactor;
    }

    public long getMinDelayMs() {
        return minDelayMs;
    }

    public void setMinDelayMs(long minDelayMs) {
        this.minDelayMs = minDelayMs;
    }

    public long getMaxDelayMs() {
        return maxDelayMs;
    }

    public void setMaxDelayMs(long maxDelayMs) {
        this.maxDelayMs = maxDelayMs;
    }

    public long getRespectCrawlDelayUpToSeconds() {
        return respectCrawlDelayUpToSeconds;
    }

    public void setRespectCrawlDelayUpToSeconds(long respectCrawlDelayUpToSeconds) {
        this.respectCrawlDelayUpToSeconds = respectCrawlDelayUpToSeconds;
    }

    public long getMaxPerHostBandwidthUsageKbSec() {
        return maxPerHostBandwidthUsageKbSec;
    }

    public void setMaxPerHostBandwidthUsageKbSec(long maxPerHostBandwidthUsageKbSec) {
        this.maxPerHostBandwidthUsageKbSec = maxPerHostBandwidthUsageKbSec;
    }

    public boolean isPolite() {
        return this.politeness.equals(POLITE);
    }

    public boolean isMedium() {
        return this.politeness.equals(MEDIUM);
    }

    public boolean isAggressive() {
        return this.politeness.equals(AGGRESSIVE);
    }

    public String getPoliteness() {
        return politeness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PolitenessOptions that = (PolitenessOptions) o;

        if (Double.compare(that.delayFactor, delayFactor) != 0) return false;
        if (minDelayMs != that.minDelayMs) return false;
        if (maxDelayMs != that.maxDelayMs) return false;
        if (respectCrawlDelayUpToSeconds != that.respectCrawlDelayUpToSeconds) return false;
        return maxPerHostBandwidthUsageKbSec == that.maxPerHostBandwidthUsageKbSec;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(delayFactor);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (minDelayMs ^ (minDelayMs >>> 32));
        result = 31 * result + (int) (maxDelayMs ^ (maxDelayMs >>> 32));
        result = 31 * result + (int) (respectCrawlDelayUpToSeconds ^ (respectCrawlDelayUpToSeconds >>> 32));
        result = 31 * result + (int) (maxPerHostBandwidthUsageKbSec ^ (maxPerHostBandwidthUsageKbSec >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "PolitenessOptions{" +
                "delayFactor=" + delayFactor +
                ", minDelayMs=" + minDelayMs +
                ", maxDelayMs=" + maxDelayMs +
                ", respectCrawlDelayUpToSeconds=" + respectCrawlDelayUpToSeconds +
                ", maxPerHostBandwidthUsageKbSec=" + maxPerHostBandwidthUsageKbSec +
                '}';
    }
}

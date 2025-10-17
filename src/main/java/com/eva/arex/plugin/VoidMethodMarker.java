package com.eva.arex.plugin;

import java.io.Serializable;

/**
 * Marker class to represent void method execution in recordings
 * Since void method don't return values, we use this marker to:
 * 1. Indicate the nethod was executed successfully during recording
 * 2. Allow replay to skip the method execution safely
 * 
 * Perfect for use with mock services during replay
 */
public class VoidMethodMarker implements Serializable {

    private static final long serialVersionUID = 1L;

    private long timestamp;

    /**
     * No-arg constructor for deserialization
     */
    public VoidMethodMarker() {
        System.out.println("[AREX] VoidMethodMarker no-arg constructor called");;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String extractMethodSignature(String info) {
        if (info == null) {
            System.out.println("[AREX] VoidMethodMarker Info is null, returning 'unknown'");
            return "unknown";
        }

        if (info.isEmpty()) {
            System.out.println("[AREX] VoidMethodMarker Info is empty, returning 'unknown'");
            return "unknown";
        }

        try {
            System.out.println("[AREX] VoidMethodMarker Searching for ' executed' in info...");
            int endIndex = info.indexOf(" executed");
            if (endIndex > 0) {
                System.out.println("[AREX] VoidMethodMarker executedIndex > 0, extracting substring");
                return info.substring(0, endIndex);
        }
            System.out.println("[AREX] VoidMethodMarker ' executed' not found or at position 0, returning original info");
            return info;
        } catch (Throwable t) {
            // If any error occurs during execution, return the original info or "unknown"
            System.out.println("[AREX] VoidMethodMarker Error in extractMethodSignature: info - " + info +
                    ", error type - " +
                    t.getClass().getName() +
                    ", error msg - " +
                    t.getMessage());
            return info;
        }
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "VoidMethodExecution{" +
                "timestamp=" + timestamp +
                "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoidMethodMarker that = (VoidMethodMarker)  o;
        return timestamp == that.timestamp;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(timestamp);
    }
}

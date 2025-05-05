package com.devproject.dpinUptime.service;
import org.springframework.stereotype.Component;
@Component
public class StatusIndicator {
    
    public String getStatusColor(String status) {
        return switch (status.toLowerCase()) {
            case "online" -> "bg-green-500";
            case "degraded" -> "bg-yellow-500";
            case "offline" -> "bg-red-500";
            default -> "bg-gray-300";
        };
    }

    public String getResponseTimeClass(int responseTime) {
        if (responseTime == 0) return "text-red-500";
        if (responseTime < 200) return "text-green-500";
        if (responseTime < 500) return "text-green-600";
        if (responseTime < 1000) return "text-yellow-500";
        return "text-red-500";
    }
}
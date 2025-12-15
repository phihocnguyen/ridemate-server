package com.ridemate.ridemate_server.application.dto.report;

public enum ReportActionType {
    /**
     * Lock account for 7 days
     */
    LOCK_7_DAYS,
    
    /**
     * Lock account for 30 days
     */
    LOCK_30_DAYS,
    
    /**
     * Lock account permanently
     */
    LOCK_PERMANENT,
    
    /**
     * Send warning to user
     * 3 warnings = 7-day auto-ban
     */
    WARNING
}

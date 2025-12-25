package com.ridemate.ridemate_server.application.service.dailyspin;

import com.ridemate.ridemate_server.application.dto.dailyspin.DailySpinResponse;

public interface DailySpinService {
    DailySpinResponse checkDailySpin(Long userId);
    DailySpinResponse performSpin(Long userId);
}


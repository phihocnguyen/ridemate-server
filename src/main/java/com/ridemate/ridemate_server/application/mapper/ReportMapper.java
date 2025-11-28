package com.ridemate.ridemate_server.application.mapper;

import com.ridemate.ridemate_server.application.dto.report.ReportResponse;
import com.ridemate.ridemate_server.domain.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(target = "reporterId", source = "reporter.id")
    @Mapping(target = "reporterName", source = "reporter.fullName")
    @Mapping(target = "reportedUserId", source = "reportedUser.id")
    @Mapping(target = "reportedUserName", source = "reportedUser.fullName")
    @Mapping(target = "matchId", source = "match.id")
    ReportResponse toResponse(Report report);
}
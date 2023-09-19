package com.suite.suite_suite_room_service.suiteRoom.service;

import com.suite.suite_suite_room_service.suiteRoom.dto.*;
import com.suite.suite_suite_room_service.suiteRoom.entity.SuiteRoom;
import com.suite.suite_suite_room_service.suiteRoom.security.dto.AuthorizerDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SuiteRoomService {
    List<ResSuiteRoomListDto> getSuiteRooms(AuthorizerDto authorizerDto, List<StudyCategory> subjects, Pageable pageable);
    ResSuiteRoomDto getSuiteRoom(Long suiteRoomId, AuthorizerDto authorizerDto);
    void validateTitle(String title);
    void validatePassword(Long suiteRoomId, int password);
    Optional<List<SuiteRoom>> getAllProgressRooms();
    Optional<List<SuiteRoom>> getAllCompletionRooms();
    ResSuiteRoomCreationDto createSuiteRoom(ReqSuiteRoomCreationDto reqSuiteRoomCreationDto, AuthorizerDto authorizerDto);
    void deleteSuiteRoom(Long suiteRoomId, AuthorizerDto authorizerDto);
    void updateSuiteRoom(ReqUpdateSuiteRoomDto reqUpdateSuiteRoomDto, AuthorizerDto authorizerDto);

}

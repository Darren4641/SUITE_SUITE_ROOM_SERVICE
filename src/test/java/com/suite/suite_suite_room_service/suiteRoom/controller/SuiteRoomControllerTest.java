package com.suite.suite_suite_room_service.suiteRoom.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suite.suite_suite_room_service.suiteRoom.dto.Message;
import com.suite.suite_suite_room_service.suiteRoom.dto.ReqSuiteRoomDto;
import com.suite.suite_suite_room_service.suiteRoom.dto.ReqUpdateSuiteRoomDto;
import com.suite.suite_suite_room_service.suiteRoom.dto.ResSuiteRoomDto;
import com.suite.suite_suite_room_service.suiteRoom.entity.SuiteRoom;
import com.suite.suite_suite_room_service.suiteRoom.mockEntity.MockAuthorizer;
import com.suite.suite_suite_room_service.suiteRoom.mockEntity.MockSuiteRoom;
import com.suite.suite_suite_room_service.suiteRoom.repository.ParticipantRepository;
import com.suite.suite_suite_room_service.suiteRoom.repository.SuiteRoomRepository;
import com.suite.suite_suite_room_service.suiteRoom.security.dto.AuthorizerDto;
import com.suite.suite_suite_room_service.suiteRoom.service.SuiteRoomService;
import com.suite.suite_suite_room_service.suiteRoom.service.SuiteRoomServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class SuiteRoomControllerTest {


    @Autowired private SuiteRoomService suiteRoomService;

    @Autowired private ObjectMapper mapper;
    @Autowired private MockMvc mockMvc;
    @Autowired private ParticipantRepository participantRepository;
    @Autowired private SuiteRoomRepository suiteRoomRepository;
    public static final String YH_JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJod2FueTkxODFAZ21haWwuY29tIiwiSUQiOiI0IiwiTkFNRSI6IuuwmOyYge2ZmCIsIk5JQ0tOQU1FIjoiaHdhbnk5OSIsIkFDQ09VTlRTVEFUVVMiOiJBQ1RJVkFURSIsIlJPTEUiOiJST0xFX1VTRVIiLCJpYXQiOjE2OTE0MjA3NzAsImV4cCI6MTY5MjAyNTU3MH0.HBeRgdr5hoknYOYRSHcv9p1vDDmi4uIyodQ5NNFPhGM";
    public static final String DR_JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ6eHo0NjQxQGdtYWlsLmNvbSIsIklEIjoiNiIsIk5BTUUiOiLquYDrjIDtmIQiLCJOSUNLTkFNRSI6ImRhcnJlbiIsIkFDQ09VTlRTVEFUVVMiOiJBQ1RJVkFURSIsIlJPTEUiOiJST0xFX1VTRVIiLCJpYXQiOjE2OTE0MjA3NDksImV4cCI6MTY5MjAyNTU0OX0.1WAKPpRRhliVXMrPJ8U1OlGsDxYenq5SUyn4Esk2UH4";
    @Test
    @DisplayName("스위트룸 생성")
    public void createSuiteRoom() throws Exception {
        //given
        ReqSuiteRoomDto reqSuiteRoomDto = MockSuiteRoom.getMockSuiteRoom("title2", true);
        String body = mapper.writeValueAsString(reqSuiteRoomDto);
        //when
        String responseBody = postRequest("/suite/suiteroom/registration", DR_JWT, body);
        Message message = mapper.readValue(responseBody, Message.class);
        //then
        Assertions.assertAll(
                () -> assertThat(message.getStatusCode()).isEqualTo(200)
        );

        System.out.println(responseBody);

    }

    @Test
    @DisplayName("스위트룸 수정")
    public void renewalSuiteRoom() throws Exception {
        //given
        final String url = "/suite/suiteroom/update";
        ReqSuiteRoomDto reqSuiteRoomDto = MockSuiteRoom.getMockSuiteRoom("title2", true);
        suiteRoomService.createSuiteRoom(reqSuiteRoomDto, MockAuthorizer.getMockAuthorizer("반영환", 4L));
        Long suiteRoomId = suiteRoomRepository.findAll().get(0).getSuiteRoomId();
        String body = mapper.writeValueAsString(ReqUpdateSuiteRoomDto.builder()
                .suiteRoomId(suiteRoomId)
                .content("updated content")
                .channelLink("http://www.naver.com").build());
        //when
        String responseBody = patchRequest(url, YH_JWT, body);
        Message message = mapper.readValue(responseBody, Message.class);
        //then
        Assertions.assertAll(
                () -> assertThat(message.getStatusCode()).isEqualTo(200)
        );
    }

    @Test
    @DisplayName("스위트룸 그룹 목록 확인")
    public void listUpSuiteRooms() throws Exception {
        //given
        final String url = "/suite/suiteroom/";

        ReqSuiteRoomDto reqSuiteRoomDto1 = MockSuiteRoom.getMockSuiteRoom("hwany", true);
        ReqSuiteRoomDto reqSuiteRoomDto2 = MockSuiteRoom.getMockSuiteRoom("mini", true);
        suiteRoomService.createSuiteRoom(reqSuiteRoomDto1, MockAuthorizer.getMockAuthorizer("hwany", 1L));
        suiteRoomService.createSuiteRoom(reqSuiteRoomDto2, MockAuthorizer.getMockAuthorizer("hwany", 1L));

        List<ResSuiteRoomDto> resService = suiteRoomService.getAllSuiteRooms(MockAuthorizer.getMockAuthorizer("hwany", 1L));
        //when
        String responseBody = getRequest(url, YH_JWT);
        Message message = mapper.readValue(responseBody, Message.class);
        List<ResSuiteRoomDto> result = (List<ResSuiteRoomDto>) message.getData();

        //then
        if(result.get(0) instanceof ResSuiteRoomDto) {
            Assertions.assertAll(
                    () -> assertThat(result.get(0)).isEqualTo(resService.get(0)),
                    () -> assertThat(message.getStatusCode()).isEqualTo(200)
            );
        }

    }

    @Test
    @DisplayName("스위트룸 모집글 확인")
    public void listUpSuiteRoom() throws Exception {
        //given
        final String url = "/suite/suiteroom/detail/1";
        final Long expectedSuiteRoomId = 1L;
        ReqSuiteRoomDto reqSuiteRoomDto1 = MockSuiteRoom.getMockSuiteRoom("hwany", true);
        ReqSuiteRoomDto reqSuiteRoomDto2 = MockSuiteRoom.getMockSuiteRoom("mini", true);
        AuthorizerDto mockAuthorizer1 = MockAuthorizer.getMockAuthorizer("hwany", 1L);
        AuthorizerDto mockAuthorizer2 = MockAuthorizer.getMockAuthorizer("mini", 2L);
        suiteRoomService.createSuiteRoom(reqSuiteRoomDto1, mockAuthorizer1);
        suiteRoomService.createSuiteRoom(reqSuiteRoomDto2, mockAuthorizer2);

        ResSuiteRoomDto findSuiteRoomBySuiteRoomIdResult = suiteRoomService.getSuiteRoom(expectedSuiteRoomId, mockAuthorizer1);

        //when
        String responseBody = getRequest(url, YH_JWT);
        Message message = mapper.readValue(responseBody, new TypeReference<Message<ResSuiteRoomDto>>() {
        });
        ResSuiteRoomDto result = (ResSuiteRoomDto) message.getData();

        //then
        Assertions.assertAll(
                () -> assertThat(result.getContent()).isEqualTo(findSuiteRoomBySuiteRoomIdResult.getContent()),
                () -> assertThat(result.getTitle()).isEqualTo(findSuiteRoomBySuiteRoomIdResult.getTitle()),
                () -> assertThat(message.getStatusCode()).isEqualTo(200)
        );


    }

    @Test
    @DisplayName("스터디 파투")
    public void deleteSuiteRoom() throws Exception {
        //given
        ReqSuiteRoomDto reqSuiteRoomDto = MockSuiteRoom.getMockSuiteRoom("title2", true);
        suiteRoomService.createSuiteRoom(reqSuiteRoomDto, MockAuthorizer.getMockAuthorizer("반영환", 4L));
        Long suiteRoomId = suiteRoomRepository.findAll().get(0).getSuiteRoomId();
        final String url = "/suite/suiteroom/delete/" + String.valueOf(suiteRoomId);
        //when
        String responseBody = deleteRequest(url, YH_JWT);
        Message message = mapper.readValue(responseBody, Message.class);
        //then
        Assertions.assertAll(
                () -> assertThat(message.getStatusCode()).isEqualTo(200)
        );

    }

    private String postRequest(String url, String jwt, String body) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                        .content(body) //HTTP body에 담는다.
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwt)
                )
                .andExpect(status().isOk()).andReturn();

        return result.getResponse().getContentAsString();
    }

    private String patchRequest(String url, String jwt, String body) throws Exception {
        MvcResult result = mockMvc.perform(patch(url)
                        .content(body) //HTTP body에 담는다.
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwt)
                )
                .andExpect(status().isOk()).andReturn();

        return result.getResponse().getContentAsString();
    }

    private String getRequest(String url, String jwt) throws Exception {
        MvcResult result = mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwt)
                )
                .andExpect(status().isOk()).andReturn();

        return result.getResponse().getContentAsString();
    }

    private String deleteRequest(String url, String jwt) throws Exception {
        MvcResult result = mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwt)
                )
                .andExpect(status().isOk()).andReturn();

        return result.getResponse().getContentAsString();
    }

}
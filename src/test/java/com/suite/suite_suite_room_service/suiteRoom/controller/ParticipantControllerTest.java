package com.suite.suite_suite_room_service.suiteRoom.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.suite.suite_suite_room_service.suiteRoom.dto.*;
import com.suite.suite_suite_room_service.suiteRoom.entity.Participant;
import com.suite.suite_suite_room_service.suiteRoom.entity.SuiteRoom;
import com.suite.suite_suite_room_service.suiteRoom.handler.CustomException;
import com.suite.suite_suite_room_service.suiteRoom.handler.StatusCode;
import com.suite.suite_suite_room_service.suiteRoom.mockEntity.MockAuthorizer;
import com.suite.suite_suite_room_service.suiteRoom.mockEntity.MockCheckInInfo;
import com.suite.suite_suite_room_service.suiteRoom.mockEntity.MockParticipant;
import com.suite.suite_suite_room_service.suiteRoom.mockEntity.MockSuiteRoom;
import com.suite.suite_suite_room_service.suiteRoom.repository.ParticipantRepository;
import com.suite.suite_suite_room_service.suiteRoom.repository.SuiteRoomRepository;
import com.suite.suite_suite_room_service.suiteRoom.security.dto.AuthorizerDto;
import com.suite.suite_suite_room_service.suiteRoom.service.ParticipantService;
import com.suite.suite_suite_room_service.suiteRoom.service.ParticipantServiceImpl;
import com.suite.suite_suite_room_service.suiteRoom.service.SuiteRoomService;
import com.suite.suite_suite_room_service.suiteRoom.service.SuiteRoomServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@Rollback
class ParticipantControllerTest {

    @Autowired private ObjectMapper mapper;
    @Autowired private MockMvc mockMvc;
    @Autowired private ParticipantRepository participantRepository;
    @Autowired private SuiteRoomRepository suiteRoomRepository;



    public static final String YH_JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJod2FueTkxODFAZ21haWwuY29tIiwiSUQiOiI0IiwiTkFNRSI6IuuwmOyYge2ZmCIsIk5JQ0tOQU1FIjoiaHdhbnk5OSIsIkFDQ09VTlRTVEFUVVMiOiJBQ1RJVkFURSIsIlJPTEUiOiJST0xFX1VTRVIiLCJpYXQiOjE2OTE0MjA3NzAsImV4cCI6MTY5MjAyNTU3MH0.HBeRgdr5hoknYOYRSHcv9p1vDDmi4uIyodQ5NNFPhGM";
    public static final String DR_JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ6eHo0NjQxQGdtYWlsLmNvbSIsIklEIjoiNiIsIk5BTUUiOiLquYDrjIDtmIQiLCJOSUNLTkFNRSI6ImRhcnJlbiIsIkFDQ09VTlRTVEFUVVMiOiJBQ1RJVkFURSIsIlJPTEUiOiJST0xFX1VTRVIiLCJpYXQiOjE2OTE0MjA3NDksImV4cCI6MTY5MjAyNTU0OX0.1WAKPpRRhliVXMrPJ8U1OlGsDxYenq5SUyn4Esk2UH4";
    private final SuiteRoom suiteRoom = MockSuiteRoom.getMockSuiteRoom("test", true).toSuiteRoomEntity();
    private final Participant participantHost = MockParticipant.getMockParticipant(true, MockParticipant.getMockAuthorizer("1"));
    @BeforeEach
    public void setUp() {
        suiteRoom.addParticipant(participantHost);
        suiteRoomRepository.save(suiteRoom);
        participantRepository.save(participantHost);
    }


    @Test
    @DisplayName("스위트룸 참가하기")
    public void joinSuiteRoom() throws Exception {
        //given
        Map<String, Long> suiteRoomId = new HashMap<String, Long>();
        suiteRoomId.put("suiteRoomId", suiteRoom.getSuiteRoomId());
        String body = mapper.writeValueAsString(suiteRoomId);
        //when
        String responseBody = postRequest("/suite/suiteroom/attend", DR_JWT, body);
        Message message = mapper.readValue(responseBody, Message.class);
        //then
        Assertions.assertAll(
                () -> assertThat(message.getStatusCode()).isEqualTo(200)
        );
    }

    @Test
    @DisplayName("스위트룸 참가 취소")
    public void cancelSuiteRoom() throws Exception {
        //given
        //실제 토큰의 Claim 값과 동일하게 넣어주어야 합니다. 서비스 로직상 다르면 테스트가 터져요!!!
        Participant participantGuest = MockParticipant.getMockParticipant(false, MockAuthorizer.getMockAuthorizer("김대현", 6L));

        participantHost.updateStatus(SuiteStatus.READY);
        suiteRoom.openSuiteRoom();

        saveParticipantWithTransaction(participantGuest, suiteRoom);

        Map<String, Long> suiteRoomId = new HashMap<String, Long>();
        suiteRoomId.put("suiteRoomId", suiteRoom.getSuiteRoomId());
        String body = mapper.writeValueAsString(suiteRoomId);

        //when
        String responseBody = postRequest("/suite/suiteroom/attend/cancel", DR_JWT, body);
        Message message = mapper.readValue(responseBody, Message.class);
        //then
        Assertions.assertAll(
                () -> assertThat(message.getStatusCode()).isEqualTo(200)
        );
    }

    @Test
    @DisplayName("스위트룸 체크인 완료")
    public void checkInSuiteRoom() throws Exception {
        //given
        final String url = "/suite/payment/completion";

        Map<String, MockCheckInInfo> messageCard = new HashMap<String, MockCheckInInfo>();
        MockCheckInInfo checkInInfo = new MockCheckInInfo(suiteRoom.getSuiteRoomId(), participantHost.getMemberId());
        messageCard.put("checkInInfo", checkInInfo);
        String body = mapper.writeValueAsString(messageCard);

        //when
        String responseBody = postRequest(url, DR_JWT, body);

        Message message = mapper.readValue(responseBody, Message.class);
        //then
        Assertions.assertAll(
                () -> assertThat(message.getStatusCode()).isEqualTo(200)
        );
    }

    @Test
    @DisplayName("스위트룸 체크인 목록 확인 - 납부자")
    public void getCheckInList() throws Exception {
        //given
        final String url = "/suite/payment/ready/" + String.valueOf(suiteRoom.getSuiteRoomId());

        Participant participantGuest = MockParticipant.getMockParticipant(false, MockAuthorizer.getMockAuthorizer("darren", 2L));

        participantHost.updateStatus(SuiteStatus.READY);
        suiteRoom.openSuiteRoom();

        saveParticipantWithTransaction(participantGuest, suiteRoom);
        //when
        String responseBody = getRequest(url, YH_JWT);
        Message message = mapper.readValue(responseBody, new TypeReference<Message<List<ResPaymentParticipantDto>>>() {
        });
        List<ResPaymentParticipantDto> result = (List<ResPaymentParticipantDto>) message.getData();
        //then
        Assertions.assertAll(
                () -> assertThat(message.getStatusCode()).isEqualTo(200),
                () -> assertThat(result).allMatch(dto -> dto.getStatus() == SuiteStatus.READY)
        );
    }
    @Test
    @DisplayName("스위트룸 체크인 목록 확인 - 미납부 신청자")
    public void getNotYetCheckInList() throws Exception {
        //given
        final String url = "/suite/payment/plain/" + String.valueOf(suiteRoom.getSuiteRoomId());
        Participant participantGuest = MockParticipant.getMockParticipant(false, MockAuthorizer.getMockAuthorizer("darren", 2L));

        participantHost.updateStatus(SuiteStatus.READY);
        suiteRoom.openSuiteRoom();

        saveParticipantWithTransaction(participantGuest, suiteRoom);

        //when
        String responseBody = getRequest(url, YH_JWT);
        Message message = mapper.readValue(responseBody, new TypeReference<Message<List<ResPaymentParticipantDto>>>() {
        });
        List<ResPaymentParticipantDto> result = (List<ResPaymentParticipantDto>) message.getData();
        System.out.println(result.get(0).getStatus());
        //then
        Assertions.assertAll(
                () -> assertThat(message.getStatusCode()).isEqualTo(200),
                () -> assertThat(result).allMatch(dto -> dto.getStatus() == SuiteStatus.PLAIN)
        );
    }
    @Rollback
    protected void saveParticipantWithTransaction(Participant participantGuest, SuiteRoom suiteRoom) {
        suiteRoom.addParticipant(participantGuest);
        participantRepository.save(participantGuest);
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
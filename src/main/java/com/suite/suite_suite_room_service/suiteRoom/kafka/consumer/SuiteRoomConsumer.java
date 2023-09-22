package com.suite.suite_suite_room_service.suiteRoom.kafka.consumer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.suite.suite_suite_room_service.suiteRoom.dto.SuiteStatus;
import com.suite.suite_suite_room_service.suiteRoom.entity.Participant;
import com.suite.suite_suite_room_service.suiteRoom.entity.SuiteRoom;
import com.suite.suite_suite_room_service.suiteRoom.handler.CustomException;
import com.suite.suite_suite_room_service.suiteRoom.handler.StatusCode;
import com.suite.suite_suite_room_service.suiteRoom.repository.ParticipantRepository;
import com.suite.suite_suite_room_service.suiteRoom.repository.SuiteRoomRepository;
import com.suite.suite_suite_room_service.suiteRoom.security.dto.AuthorizerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuiteRoomConsumer {
    private final SuiteRoomRepository suiteRoomRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    @KafkaListener(topics = { "${topic.SUITEROOM_JOIN}", "${topic.SUITEROOM_CANCELJOIN_ERROR}" }, groupId = "suite", containerFactory = "kafkaListenerContainerFactory")
    public void suiteRoomJoinConsume(ConsumerRecord<String, String> record) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jsonObject = (JSONObject) parser.parse(record.value());
        JSONObject data = ((JSONObject) jsonObject.get("data"));
        Long suiteRoomId = Long.parseLong(data.get("suiteRoomId").toString());
        boolean isHost = Boolean.parseBoolean(data.get("isHost").toString());
        JSONObject authorizerDtoObject = ((JSONObject) data.get("authorizerDto"));
        AuthorizerDto authorizerDto = objectMapper.readValue(authorizerDtoObject.toString(), AuthorizerDto.class);

        if(isHost) updateHostStatus(suiteRoomId, authorizerDto.getMemberId());
        else addParticipant(suiteRoomId, isHost, authorizerDto);
    }

    @Transactional
    @KafkaListener(topics = "${topic.SUITEROOM_START_ERROR}", groupId = "suite", containerFactory = "kafkaListenerDefaultContainerFactory")
    public void suiteRoomStartErrorConsume(ConsumerRecord<String, String> record) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(record.value());
        JSONObject data = ((JSONObject) jsonObject.get("data"));
        Long suiteRoomId = Long.parseLong(data.get("suiteRoomId").toString());

        SuiteRoom suiteRoom = suiteRoomRepository.findBySuiteRoomId(suiteRoomId).orElseThrow(() -> new CustomException(StatusCode.NOT_FOUND));
        suiteRoom.startErrorSuiteRoom();

        participantRepository.findBySuiteRoom_SuiteRoomId(suiteRoomId).stream().map(
                p -> {
                    p.updateStatus(SuiteStatus.READY);
                    return p;
                });
    }

    @Transactional
    @KafkaListener(topics = "${topic.SUITEROOM_TERMINATE_COMPLETE}", groupId = "suite", containerFactory = "kafkaListenerDefaultContainerFactory")
    public void terminateSuiteRoomCompleteConsume(ConsumerRecord<String, String> record) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(record.value());
        JSONObject data = ((JSONObject) jsonObject.get("data"));
        Long suiteRoomId = Long.parseLong(data.get("suiteRoomId").toString());

        suiteRoomRepository.deleteBySuiteRoomId(suiteRoomId);
    }

    private void addParticipant(Long suiteRoomId, boolean isHost, AuthorizerDto authorizerDto) {
        SuiteRoom suiteRoom = suiteRoomRepository.findBySuiteRoomId(suiteRoomId).get();

        Participant participant = Participant.builder()
                .authorizerDto(authorizerDto)
                .status(SuiteStatus.READY)
                .isHost(isHost).build();
        suiteRoom.addParticipant(participant);

        participantRepository.save(participant);
    }


    private void updateHostStatus(Long suiteRoomId, Long memberId) {
        Participant host = participantRepository.findBySuiteRoom_SuiteRoomIdAndMemberIdAndIsHost(suiteRoomId, memberId, true).get();
        host.updateStatus(SuiteStatus.READY);
    }
}

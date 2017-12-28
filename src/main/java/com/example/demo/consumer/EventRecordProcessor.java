package com.example.demo.consumer;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.types.InitializationInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput;
import com.amazonaws.services.kinesis.model.Record;
import com.example.demo.entity.MatchApplicationData;
import com.example.demo.entity.MessageManager;
import com.example.demo.enums.MessageType;
import com.example.demo.service.CurriculumDataService;

import java.util.List;

public class EventRecordProcessor implements IRecordProcessor {

    private CurriculumDataService curriculumDataService;

    private String shard;


    public EventRecordProcessor(CurriculumDataService curriculumDataService){
        this.curriculumDataService = curriculumDataService;
    }

    @Override
    public void initialize(InitializationInput initializationInput) {
        shard = initializationInput.getShardId();
    }

    @Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        List<Record> records = processRecordsInput.getRecords();

        records.stream()
            .map(r -> MessageManager.parse(r.getData().array()))
            .filter(m -> m.getName().equals(MessageType.EVT_APPLICATION_CREATED.name()))
            .map(m -> MatchApplicationData.fromJson(m.getPayload()))
            .forEach(data -> {
                //Updates curriculum and emit APPROVED/REJECTED messages
                curriculumDataService.updateCurriculum(data.getId(), data.getClassId());
            });

        updateCheckpoint(processRecordsInput);
    }

    @Override
    public void shutdown(ShutdownInput shutdownInput) {

    }

    private void updateCheckpoint(ProcessRecordsInput processRecordsInput) {
        try {
            processRecordsInput.getCheckpointer().checkpoint();
        } catch (Exception e) {

        }
    }
}

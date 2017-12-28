package com.example.demo.consumer;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.example.demo.service.CurriculumDataService;

public class EventRecordProcessorFactory implements IRecordProcessorFactory {
    private CurriculumDataService curriculumDataService;

    public EventRecordProcessorFactory(CurriculumDataService curriculumDataService){
        this.curriculumDataService = curriculumDataService;
    }

    @Override
    public IRecordProcessor createProcessor() {
        return new EventRecordProcessor(curriculumDataService);
    }
}

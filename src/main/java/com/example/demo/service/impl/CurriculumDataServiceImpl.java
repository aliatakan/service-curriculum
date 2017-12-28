package com.example.demo.service.impl;

import com.example.demo.entity.ApplicationStatusUpdateEvent;
import com.example.demo.entity.Lesson;
import com.example.demo.entity.MessageManager;
import com.example.demo.enums.MessageType;
import com.example.demo.producer.EventProducer;
import com.example.demo.repository.CurriculumRepository;
import com.example.demo.service.CurriculumDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by aliatakan on 18/12/17.
 */
@Service
public class CurriculumDataServiceImpl implements CurriculumDataService {

    @Autowired
    private CurriculumRepository curriculumRepository;

    @Autowired
    private EventProducer eventProducer;

    private Lesson lesson = new Lesson();

    @Override
    public void updateCurriculum(Long applicationId, Integer classId) {
        lesson = curriculumRepository.findOne(classId);

        if (lesson.getStudentlimit() > 0) {
            //there is enough space for this application, decrease the limit
            lesson.setStudentlimit(lesson.getStudentlimit() - 1);

            //decrease from postgres
            curriculumRepository.save(lesson);

            //produce EVT_APPLICATION_APPROVED event
            eventProducer.emit(Long.toString(classId), MessageManager.create(MessageType.EVT_APPLICATION_APPROVED, new ApplicationStatusUpdateEvent(applicationId, "APPROVED")).toByteArray());
        } else {
            //the lesson limit is full ! produce EVT_APPLICATION_REJECTED event
            eventProducer.emit(Long.toString(classId), MessageManager.create(MessageType.EVT_APPLICATION_REJECTED, new ApplicationStatusUpdateEvent(applicationId, "REJECTED")).toByteArray());
        }


    }
}

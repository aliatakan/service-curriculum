package com.example.demo.repository;

import com.example.demo.entity.Lesson;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by aliatakan on 15/12/17.
 */
public interface CurriculumRepository extends CrudRepository<Lesson,Integer> {
}

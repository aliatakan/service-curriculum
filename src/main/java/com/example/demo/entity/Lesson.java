package com.example.demo.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by aliatakan on 15/12/17.
 */
@Entity
@Table(name = "lesson")
public @Data class Lesson {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "studentlimit")
    private Integer studentlimit;
}

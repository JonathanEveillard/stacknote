package com.stacknote.app.entities;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Note {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="note_id")
    private Integer id;

    @Column(name="note_title")
    private String title;

    @Column(name="note_description", length = 1000)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name="note_create_time")
    private Date createTime;

    @ManyToOne
    private User user;
}

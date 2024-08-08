package com.sismics.books.core.model.jpa;

import javax.persistence.*;

@Entity
@Table(name = "T_GENRE")
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GENRE_ID_C")
    private Long id;

    @Column(name = "GENRE_NAME_C", nullable = false, length = 255)
    private String name;

    // Constructors, getters, and setters

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


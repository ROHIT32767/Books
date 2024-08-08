package com.sismics.books.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "common_book_genre")
public class CommonBookGenre {
    @Id
    @Column(name = "genre_id",length=36)
    private String id;
    
    @Column(name = "common_book_id")
    private String commonBookId;
    
    @Column(name = "genre")
    private String genre;
    
    public CommonBookGenre() {
    }

    public CommonBookGenre(String commonBookId, String genre) {
        this.commonBookId = commonBookId;
        this.genre = genre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommonBookId() {
        return commonBookId;
    }

    public void setCommonBookId(String commonBookId) {
        this.commonBookId = commonBookId;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}

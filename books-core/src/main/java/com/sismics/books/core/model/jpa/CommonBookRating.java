package com.sismics.books.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Common Book Rating entity.
 * Represents a rating given by a user to a common book.
 * 
 * @author [Your Name]
 */
@Entity
@Table(name = "common_book_rating")
public class CommonBookRating {
    @Id
    @Column(name = "rating_id",length=36)
    private String id;
    
    @Column(name = "common_book_id")
    private String commonBookId;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "rating")
    private int rating;

    public CommonBookRating() {
    }
    
    public CommonBookRating(String commonBookId, String userId, int rating) {
        this.commonBookId = commonBookId;
        this.userId = userId;
        this.rating = rating;
    }
    
    // Getters and setters

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}

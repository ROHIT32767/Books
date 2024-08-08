package com.sismics.books.core.model.jpa;

import javax.persistence.*;

@Entity
@Table(name = "T_USER_COMMON_BOOK_RATING")
public class UserCommonBookRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RATING_ID_C")
    private Long ratingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID_C", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMMON_BOOK_ID_C", nullable = false)
    private CommonBook commonBook;

    @Column(name = "RATING_N", nullable = false)
    private int rating;

    // Constructors, getters, and setters

    // Getters and setters

    public Long getRatingId() {
        return ratingId;
    }

    public void setRatingId(Long ratingId) {
        this.ratingId = ratingId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CommonBook getCommonBook() {
        return commonBook;
    }

    public void setCommonBook(CommonBook commonBook) {
        this.commonBook = commonBook;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}

package com.sismics.books.core.model.jpa;

import javax.persistence.*;

@Entity
@Table(name = "T_GENRE_COMMON_BOOK")
public class GenreCommonBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GENRE_COMMON_BOOK_ID_C")
    private Long genreCommonBookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GENRE_ID_C", nullable = false)
    private Genre genre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMMON_BOOK_ID_C", nullable = false)
    private CommonBook commonBook;

    // Constructors, getters, and setters

    // Getters and setters

    public Long getGenreCommonBookId() {
        return genreCommonBookId;
    }

    public void setGenreCommonBookId(Long genreCommonBookId) {
        this.genreCommonBookId = genreCommonBookId;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public CommonBook getCommonBook() {
        return commonBook;
    }

    public void setCommonBook(CommonBook commonBook) {
        this.commonBook = commonBook;
    }
}


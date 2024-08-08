package com.sismics.books.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_AUDIOBOOK")
public class AudioBook {

    @Id
    @Column(name = "AUD_ID_C", length = 36)
    private String id;

    @Column(name = "AUD_EXTID_C", length = 50)
    private String externalId;

    @Column(name = "AUD_NAME_C", nullable = false, length = 1000)
    private String name;

    @Column(name = "AUD_AUTHORS_C", nullable = false, length = 250)
    private String authors;

    @Column(name = "AUD_TMBURL_C", nullable = false, length = 250)
    private String thumbnailUrl;

    @Column(name = "AUD_DESC_C", nullable = false, length = 10000)
    private String description;
    
    @Column(name = "AUD_URL_C", nullable = false, length = 250)
    private String url;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getAuthors() {
        return authors;
    }
    public void setAuthors(String authors) {
        this.authors = authors;
    }
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getExternalId() {
        return externalId;
    }
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}

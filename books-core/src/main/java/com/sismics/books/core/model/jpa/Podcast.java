package com.sismics.books.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_PODCAST")
public class Podcast {
    @Id
    @Column(name = "POD_ID_C", length = 36)
    private String id;

    @Column(name = "POD_EXTID_C", length = 50)
    private String externalId;
    
    @Column(name = "POD_NAME_C", nullable = false, length = 1000)
    private String name;

    @Column(name = "POD_TMBURL_C", nullable = false, length = 250)
    private String thumbnailUrl;

    @Column(name = "POD_DURN_C", nullable = false)
    private float duration;

    @Column(name = "POD_URL_C", nullable = false, length = 1000)
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}

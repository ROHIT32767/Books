package com.sismics.books.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_USER_PODCAST")
public class UserPodcast {
    @Id
    @Column(name = "UP_ID_C", length = 36)
    private String id;

    @Column(name = "UP_UID_C", nullable = false, length = 36)
    private String userId;

    @Column(name = "UP_PODID_C", nullable = false, length = 36)
    private String podcastId;

    // @Column(name = "PODCAST_EXT_ID_C", nullable = false, length = 36)
    // private String podcastExternalId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(String podcastId) {
        this.podcastId = podcastId;
    }

    // public String getAudioBookExternalId()
    // {
    //     return podcastExternalId;
    // }

    // public void setAudioBookExternalId(String podcastExternalId)
    // {
    //     this.podcastExternalId = podcastExternalId;
    // }
}

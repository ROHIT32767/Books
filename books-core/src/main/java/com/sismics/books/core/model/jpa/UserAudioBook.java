package com.sismics.books.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_USER_AUDIOBOOK")
public class UserAudioBook {
    @Id
    @Column(name = "UAB_ID_C", length = 36)
    private String id;

    @Column(name = "UAB_UID_C", nullable = false, length = 36)
    private String userId;

    @Column(name = "UAB_ABID_C", nullable = false, length = 36)
    private String audioBookId;

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

    public String getAudioBookId() {
        return audioBookId;
    }

    public void setAudioBookId(String audioBookId) {
        this.audioBookId = audioBookId;
    }
}

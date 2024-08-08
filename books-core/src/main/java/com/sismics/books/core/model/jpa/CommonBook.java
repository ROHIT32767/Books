package com.sismics.books.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "T_COMMON_BOOK")
public class CommonBook {
    /**
     * CommonBook ID.
     */
    @Id
    @Column(name = "COMMON_BOOK_ID", length = 36)
    private String id;
    
    /**
     * Book ID.
     */
    @Column(name = "BOOK_ID", length = 36)
    private String bookId;
    
    /**
     * Create date.
     */
    @Column(name = "CREATE_DATE")
    private Date createDate;
    
    /**
     * Delete date.
     */
    @Column(name = "DELETE_DATE")
    private Date deleteDate;

    /**
     * Read date.
     */
    @Column(name = "READ_DATE")
    private Date readDate;
    
    /**
     * Getter of id.
     * 
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     * 
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of bookId.
     * 
     * @return bookId
     */
    public String getBookId() {
        return bookId;
    }

    /**
     * Setter of bookId.
     * 
     * @param bookId bookId
     */
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    /**
     * Getter of createDate.
     * 
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Setter of createDate.
     * 
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * Getter of deleteDate.
     * 
     * @return deleteDate
     */
    public Date getDeleteDate() {
        return deleteDate;
    }

    /**
     * Setter of deleteDate.
     * 
     * @param deleteDate deleteDate
     */
    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    /**
     * Getter of ReadDate.
     * 
     * @return ReadDate
     */
    public Date getReadDate() {
        return readDate;
    }

    /**
     * Setter of ReadDate.
     * 
     * @param ReadDate ReadDate
     */
    public void setReadDate(Date readDate) {
        this.readDate = readDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("bookId", bookId)
                .add("createDate", createDate)
                .add("deleteDate", deleteDate)
                .add("readDate", readDate)
                .toString();
    }
}

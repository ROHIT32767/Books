package com.sismics.books.core.dao.jpa.dto;

import javax.persistence.Id;
import java.util.Date;

public class CommonBookDto {
    @Id
    private String id;
    private String bookId;
    private Date createDate;
    private Date deleteDate;
    private Date readDate;

    public CommonBookDto(){
        
    }
    
    public CommonBookDto(String id, String bookId, Date createDate, Date deleteDate, Date readDate) {
        this.id = id;
        this.bookId = bookId;
        this.createDate = createDate;
        this.deleteDate = deleteDate;
        this.readDate = readDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    public Date getReadDate() {
        return readDate;
    }

    public void setReadDate(Date readDate) {
        this.readDate = readDate;
    }

    @Override
    public String toString() {
        return "CommonBookDTO [id=" + id + ", bookId=" + bookId + ", createDate=" + createDate + ", deleteDate="
                + deleteDate + ", readDate=" + readDate + "]";
    }
}

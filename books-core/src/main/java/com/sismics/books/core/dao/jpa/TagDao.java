package com.sismics.books.core.dao.jpa;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.sismics.books.core.dao.jpa.dto.TagDto;
import com.sismics.books.core.model.jpa.Tag;
import com.sismics.books.core.model.jpa.UserBookTag;
import com.sismics.util.context.ThreadLocalContext;


public class TagDao implements BaseDao<Tag> {
    private final EntityManager em;

    public TagDao() {
        this.em = ThreadLocalContext.get().getEntityManager();
    }

    @Override
    public Tag getById(String id) {
        return findTagById(id);
    }

    private Tag findTagById(String id) {
        try {
            return em.find(Tag.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Tag> getByUserId(String userId) {
        return findTagsByUserId(userId);
    }

    public List<Tag> getPublicTags() {
        Query q = em
                .createQuery("select t from Tag t where t.isPublic = TRUE and t.deleteDate is null order by t.name");
        return q.getResultList();
    }

    private List<Tag> findTagsByUserId(String userId) {
        Query q = em
                .createQuery("select t from Tag t where t.userId = :userId and t.deleteDate is null order by t.name");
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    public void updateTagList(String userBookId, Set<String> tagIds) {
        deleteOldTagLinks(userBookId);
        createNewTagLinks(userBookId, tagIds);
    }

    private void deleteOldTagLinks(String userBookId) {
        Query q = em.createQuery("delete UserBookTag bt where bt.userBookId = :userBookId");
        q.setParameter("userBookId", userBookId);
        q.executeUpdate();
    }

    private void createNewTagLinks(String userBookId, Set<String> tagIds) {
        for (String tagId : tagIds) {
            UserBookTag userBookTag = new UserBookTag();
            userBookTag.setId(UUID.randomUUID().toString());
            userBookTag.setUserBookId(userBookId);
            userBookTag.setTagId(tagId);
            em.persist(userBookTag);
        }
    }

    public List<TagDto> getByUserBookId(String userBookId) {
        return assembleTagDtosFromUserBookId(userBookId);
    }

    private List<TagDto> assembleTagDtosFromUserBookId(String userBookId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select t.TAG_ID_C, t.TAG_NAME_C, t.TAG_COLOR_C from T_USER_BOOK_TAG bt ");
        sb.append(" join T_TAG t on t.TAG_ID_C = bt.BOT_IDTAG_C ");
        sb.append(" where bt.BOT_IDUSERBOOK_C = :userBookId and t.TAG_DELETEDATE_D is null ");
        sb.append(" order by t.TAG_NAME_C ");

        // Perform the query
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("userBookId", userBookId);
        List<Object[]> l = q.getResultList();

        // Assemble results
        List<TagDto> tagDtoList = new ArrayList<TagDto>();
        for (Object[] o : l) {
            int i = 0;
            TagDto tagDto = new TagDto();
            tagDto.setId((String) o[i++]);
            tagDto.setName((String) o[i++]);
            tagDto.setColor((String) o[i++]);
            tagDtoList.add(tagDto);
        }
        return tagDtoList;
    }

    @Override
    public String create(Tag tag) {
        tag.setId(UUID.randomUUID().toString());
        tag.setCreateDate(new Date());
        em.persist(tag);
        return tag.getId();
    }

    public Tag getByName(String userId, String name) {
        return findTagByNameAndUserId(userId, name);
    }

    private Tag findTagByNameAndUserId(String userId, String name) {
        Query q = em.createQuery(
                "select t from Tag t where t.name = :name and t.userId = :userId and t.deleteDate is null");
        q.setParameter("userId", userId);
        q.setParameter("name", name);
        try {
            return (Tag) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Tag getByTagId(String userId, String tagId) {
        return findTagByIdAndUserId(userId, tagId);
    }

    private Tag findTagByIdAndUserId(String userId, String tagId) {
        Query q = em
                .createQuery("select t from Tag t where t.id = :tagId and t.userId = :userId and t.deleteDate is null");
        q.setParameter("userId", userId);
        q.setParameter("tagId", tagId);
        try {
            return (Tag) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void delete(String tagId) {
        Tag tagDb = findTagById(tagId);
        if (tagDb != null) {
            Date dateNow = new Date();
            tagDb.setDeleteDate(dateNow);
            deleteLinkedData(tagId);
        }
    }

    private void deleteLinkedData(String tagId) {
        Query q = em.createQuery("delete UserBookTag bt where bt.tagId = :tagId");
        q.setParameter("tagId", tagId);
        q.executeUpdate();
    }

    public List<Tag> findByName(String userId, String name) {
        return findTagsByNameAndUserId(userId, name);
    }

    private List<Tag> findTagsByNameAndUserId(String userId, String name) {
        Query q = em.createQuery(
                "select t from Tag t where t.name like :name and t.userId = :userId and t.deleteDate is null");
        q.setParameter("userId", userId);
        q.setParameter("name", "%" + name + "%");
        return q.getResultList();
    }
}
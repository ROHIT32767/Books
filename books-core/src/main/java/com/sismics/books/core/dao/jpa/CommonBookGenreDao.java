package com.sismics.books.core.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import com.sismics.books.core.model.jpa.CommonBookGenre;
import com.sismics.util.context.ThreadLocalContext;
import java.util.*;
import java.util.logging.Logger;


public class CommonBookGenreDao {

    private static final Logger logger = Logger.getLogger(CommonBookGenreDao.class.getName());

    public String create(CommonBookGenre commonBookGenre) {
        commonBookGenre.setId(UUID.randomUUID().toString());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(commonBookGenre);
        return commonBookGenre.getId();
    }

    public CommonBookGenre getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(CommonBookGenre.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    public void deleteById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        CommonBookGenre commonBookGenre = getById(id);
        if (commonBookGenre != null) {
            em.remove(commonBookGenre);
        }
    }

    public CommonBookGenre getByCommonBookIdAndGenre(String commonBookId, String genre) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery("SELECT cbg FROM CommonBookGenre cbg WHERE cbg.commonBookId = :commonBookId AND cbg.genre = :genre");
        query.setParameter("commonBookId", commonBookId);
        query.setParameter("genre", genre);
        try {
            return (CommonBookGenre) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<CommonBookGenre> getByCommonBookId(String commonBookId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery("SELECT cbg FROM CommonBookGenre cbg WHERE cbg.commonBookId = :commonBookId");
        query.setParameter("commonBookId", commonBookId);
        return query.getResultList();
    }

    public List<String> getGenresByCommonBookId(String commonBookId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery("SELECT cbg.genre FROM CommonBookGenre cbg WHERE cbg.commonBookId = :commonBookId");
        query.setParameter("commonBookId", commonBookId);
        return query.getResultList();
    }
}

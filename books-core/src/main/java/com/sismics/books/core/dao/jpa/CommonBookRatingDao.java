package com.sismics.books.core.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import com.sismics.books.core.model.jpa.CommonBookRating;
import com.sismics.util.context.ThreadLocalContext;
import java.util.*;
import java.util.logging.Logger;

/** 
 * CommonBookRating DAO.
 * 
 * @author [Your Name]
 */
public class CommonBookRatingDao {

    private static final Logger logger = Logger.getLogger(CommonBookRatingDao.class.getName());

    public String create(CommonBookRating commonBookRating) {
        commonBookRating.setId(UUID.randomUUID().toString());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(commonBookRating);
        return commonBookRating.getId();
    }   

    public CommonBookRating getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(CommonBookRating.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    public void deleteById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        CommonBookRating commonBookRating = getById(id);
        if (commonBookRating != null) {
            em.remove(commonBookRating);
        }
    }

    // update function
    public void update(CommonBookRating commonBookRating) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.merge(commonBookRating);
    }


    // function to get CommonBookRating given commonBookId and userId
    public CommonBookRating getByCommonBookIdAndUserId(String commonBookId, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery("SELECT cbr FROM CommonBookRating cbr WHERE cbr.commonBookId = :commonBookId AND cbr.userId = :userId");
        query.setParameter("commonBookId", commonBookId);
        query.setParameter("userId", userId);
        try {
            return (CommonBookRating) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<CommonBookRating> getAllCommonBookRatings() {
        EntityManager em = ThreadLocalContext.get().getEntityManager(); 
        Query query = em.createQuery("SELECT cbr FROM CommonBookRating cbr");
        return query.getResultList();
    }

    

    public List<CommonBookRating> getByCommonBookId(String commonBookId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery("SELECT cbr FROM CommonBookRating cbr WHERE cbr.commonBookId = :commonBookId");
        query.setParameter("commonBookId", commonBookId);
        return query.getResultList();
    }

    public List<CommonBookRating> getByUserId(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery("SELECT cbr FROM CommonBookRating cbr WHERE cbr.userId = :userId");
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public double getAverageRatingByCommonBookId(String commonBookId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery("SELECT AVG(cbr.rating) FROM CommonBookRating cbr WHERE cbr.commonBookId = :commonBookId");
        query.setParameter("commonBookId", commonBookId);
        try {
            Object result = query.getSingleResult();
            if (result != null) {
                return (double) result;
            }
        } catch (NoResultException e) {
            return 0; // Default if no result found
        }
        return 0; // Default if no result found
    }

    public long getNumberOfRatingsByCommonBookId(String commonBookId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery("SELECT COUNT(cbr) FROM CommonBookRating cbr WHERE cbr.commonBookId = :commonBookId");
        query.setParameter("commonBookId", commonBookId);
        try {
            Object result = query.getSingleResult();
            if (result != null) {
                return (long) result;
            }
        } catch (NoResultException e) {
            return 0; // Default if no result found
        }
        return 0; // Default if no result found
    }
    
}

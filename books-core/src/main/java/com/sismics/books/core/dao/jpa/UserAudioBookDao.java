package com.sismics.books.core.dao.jpa;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;

import org.json.JSONException;

import com.sismics.books.core.model.jpa.UserAudioBook;
import com.sismics.util.context.ThreadLocalContext;

/**
 * UserAudioBook DAO.
 * 
 * @author jtremeaux
 */
public class UserAudioBookDao {

  public String create(UserAudioBook userAudiobook) throws Exception {
    userAudiobook.setId(UUID.randomUUID().toString());

    EntityManager em = ThreadLocalContext.get().getEntityManager();

    Query q = em
        .createQuery("select ua from UserAudioBook ua where ua.userId = :userId and ua.audioBookId = :audioBookId");
    q.setParameter("userId", userAudiobook.getUserId());
    q.setParameter("audioBookId", userAudiobook.getAudioBookId());

    List<?> l = q.getResultList();
    if (l.size() > 0) {
      throw new Exception("AlreadyExistingAudiobookFavourite");
    }
    
    em.persist(userAudiobook);

    return userAudiobook.getId();
  }

  public void delete(String userId, String audioBookId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("delete UserAudioBook uab where uab.userId = :userId and uab.audioBookId = :audioBookId");
        q.setParameter("userId", userId);
        q.setParameter("audioBookId", audioBookId);
        q.executeUpdate();
    }

  @SuppressWarnings("unchecked")
  public List<String> findAudiobooksByUserId(String userId) {
    EntityManager em = ThreadLocalContext.get().getEntityManager();
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ua.audioBookId FROM UserAudioBook ua ");
    sb.append("WHERE ua.userId = :userId");
    Query q = em.createQuery(sb.toString());
    q.setParameter("userId", userId);
    return q.getResultList();
  }
}
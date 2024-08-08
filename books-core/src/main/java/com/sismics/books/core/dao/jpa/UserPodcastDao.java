package com.sismics.books.core.dao.jpa;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.sismics.books.core.model.jpa.UserPodcast;
import com.sismics.util.context.ThreadLocalContext;

/**
 * UserPodcast DAO.
 * 
 * @author jtremeaux
 */
public class UserPodcastDao {
    
    public String create(UserPodcast userPodcast) throws Exception {
        userPodcast.setId(UUID.randomUUID().toString());
        
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        Query q = em.createQuery("select up from UserPodcast up where up.userId = :userId and up.podcastId = :podcastId");
        q.setParameter("userId", userPodcast.getUserId());
        q.setParameter("podcastId", userPodcast.getPodcastId());
        List<?> l = q.getResultList();
        if (l.size() > 0) {
            throw new Exception("AlreadyExistingPodcastFavourite");
        }
        
        em.persist(userPodcast);

        return userPodcast.getId();
    }

    @SuppressWarnings("unchecked")
    public List<String> findPodcastsByUserId(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT up.podcastId FROM UserPodcast up ");
        sb.append("WHERE up.userId = :userId");
        Query q = em.createQuery(sb.toString());
        q.setParameter("userId", userId);
        return q.getResultList();
    }
    
    public void delete(String userId, String podcastId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("delete UserPodcast up where up.userId = :userId and up.podcastId = :podcastId");
        q.setParameter("userId", userId);
        q.setParameter("podcastId", podcastId);
        q.executeUpdate();
    }
}
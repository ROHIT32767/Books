package com.sismics.books.core.dao.jpa;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.sismics.books.core.model.jpa.Podcast;
import com.sismics.util.context.ThreadLocalContext;

public class PodcastDao {

    public String create(Podcast podcast) throws Exception {
        podcast.setId(UUID.randomUUID().toString());
        
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        Query q = em.createQuery("select * from Podcast p where p.externalId = :externalId");
        q.setParameter("externalId", podcast.getExternalId());
        List<?> l = q.getResultList();
        if (l.size() > 0) {
            throw new Exception("AlreadyExistingPodcast");
        }
        
        em.persist(podcast);

        return podcast.getId();
    }  
    
    public Podcast findById(String id){
        try {
            EntityManager em = ThreadLocalContext.get().getEntityManager();
            return em.find(Podcast.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    public void delete(String podcastId) {
        deleteLinkedData(podcastId);
    }
    
    private void deleteLinkedData(String podcastId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("delete Podcast p where p.id = :id");
        q.setParameter("id", podcastId);
        q.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    private Podcast findByExternalId(String externalId){
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select p from Podcast p where p.externalId = :externalId");
        q.setParameter("externalId", externalId);
        List<Podcast> l = q.getResultList();
        if(l.isEmpty()){
            System.out.println("Did not find any existing poscast with " + externalId);
            return null;
        }else{
            System.out.println("Found existing podcast with " + externalId);
            return l.get(0);
        }
    }

    public Podcast createIfNotExistAndReturn(Podcast podcast){
        Podcast existingPodcast = findByExternalId(podcast.getExternalId());
        if(existingPodcast != null){
            return existingPodcast;
        }

        podcast.setId(UUID.randomUUID().toString());
        
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(podcast);

        return podcast;
    }
}


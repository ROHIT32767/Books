package com.sismics.books.core.dao.jpa;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.sismics.books.core.model.jpa.AudioBook;
import com.sismics.util.context.ThreadLocalContext;

public class AudioBookDao {
    public String create(AudioBook audioBook) throws Exception {
        audioBook.setId(UUID.randomUUID().toString());
        
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        Query q = em.createQuery("select * from AudioBook ab where ab.externalId = :externalId");
        q.setParameter("externalId", audioBook.getExternalId());
        List<?> l = q.getResultList();
        if (l.size() > 0) {
            throw new Exception("AlreadyExistingAudioBook");
        }
        
        em.persist(audioBook);

        return audioBook.getId();
    }
    
    public void delete(String audioBookId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("delete AudioBook ab where ab.id = :id");
        q.setParameter("id", audioBookId);
        q.executeUpdate();
    }

    public AudioBook findById(String id){
        try {
            EntityManager em = ThreadLocalContext.get().getEntityManager();
            return em.find(AudioBook.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private AudioBook findByExternalId(String externalId){
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select ab from AudioBook ab where ab.externalId = :externalId");
        q.setParameter("externalId", externalId);
        List<AudioBook> l = q.getResultList();
        if(l.isEmpty()){
            System.out.println("Did not find any existing audioBook with " + externalId);
            return null;
        }else{
            System.out.println("Found existing audioBook with " + externalId);
            return l.get(0);
        }
    }

    public AudioBook createIfNotExistAndReturn(AudioBook audioBook){
        AudioBook existingBook = findByExternalId(audioBook.getExternalId());
        if(existingBook != null){
            return existingBook;
        }

        audioBook.setId(UUID.randomUUID().toString());
        
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(audioBook);

        return audioBook;
    }
}

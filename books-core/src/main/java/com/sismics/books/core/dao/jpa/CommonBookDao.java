package com.sismics.books.core.dao.jpa;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sismics.books.core.dao.jpa.criteria.CommonBookCriteria;
import com.sismics.books.core.dao.jpa.dto.CommonBookDto;
import com.sismics.books.core.model.jpa.CommonBook;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedLists;
import com.sismics.books.core.util.jpa.QueryParam;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import java.util.*;
import java.util.logging.Logger;

/**
 * CommonBook DAO.
 * 
 * @author [Your Name]
 */
public class CommonBookDao {

    private static final Logger logger = Logger.getLogger(CommonBookDao.class.getName());
    /**
     * Creates a new common book.
     * 
     * @param commonBook CommonBook
     * @return New ID
     */

    public String create(CommonBook commonBook) {
        commonBook.setId(UUID.randomUUID().toString());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(commonBook);
        return commonBook.getId();
    }

    /**
     * Gets a common book by its ID.
     * 
     * @param id CommonBook ID
     * @return CommonBook
     */
    public CommonBook getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(CommonBook.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Deletes a common book by its ID.
     * 
     * @param id CommonBook ID
     */
    public void deleteById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        CommonBook commonBook = getById(id);
        if (commonBook != null) {
            Date dateNow = new Date();
            commonBook.setDeleteDate(dateNow);
            em.remove(commonBook);
        }
    }

    public CommonBook getByIsbn(String isbn) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em
                .createQuery("SELECT cb FROM CommonBook cb JOIN cb.book b WHERE b.isbn10 = :isbn OR b.isbn13 = :isbn");
        q.setParameter("isbn", isbn);
        try {
            return (CommonBook) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public CommonBook getByBookId(String bookId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery("SELECT cb FROM CommonBook cb WHERE cb.bookId = :bookId");
        query.setParameter("bookId", bookId);
        try {
            return (CommonBook) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<CommonBook> getAllCommonBooks() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery("SELECT cb FROM CommonBook cb");
        return query.getResultList();
    }

    public void findByCriteria(PaginatedList<CommonBookDto> paginatedList, CommonBookCriteria criteria,
            SortCriteria sortCriteria) throws Exception {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        List<String> criteriaList = new ArrayList<String>();
        StringBuilder sb = new StringBuilder(
                "select cb.COMMON_BOOK_ID c0, b.BOK_ID_C c1, cb.CREATE_DATE c2, cb.READ_DATE c3");
        sb.append(" from T_BOOK b ");
        sb.append(" join T_COMMON_BOOK cb on cb.BOOK_ID = b.BOK_ID_C and cb.DELETE_DATE is null ");
        if (!Strings.isNullOrEmpty(criteria.getSearch())) {
            criteriaList.add(
                    " (b.BOK_TITLE_C like :search or b.BOK_SUBTITLE_C like :search or b.BOK_AUTHOR_C like :search) ");
            parameterMap.put("search", "%" + criteria.getSearch() + "%");
        }
        if (criteria.getRead() != null) {
            criteriaList.add(" cb.READ_DATE is " + (criteria.getRead() ? "not" : "") + " null ");
        }
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        QueryParam queryParam = new QueryParam(sb.toString(), parameterMap);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        List<CommonBookDto> commonBookDtoList = new ArrayList<CommonBookDto>();
        for (Object[] o : l) {
            int i = 0;
            CommonBookDto commonBookDto = new CommonBookDto();
            commonBookDto.setId((String) o[i++]);
            commonBookDto.setBookId((String) o[i++]);
            commonBookDto.setCreateDate((Date) o[i++]);
            commonBookDto.setReadDate((Date) o[i++]);
            logger.info("Book ID: " + commonBookDto.getBookId());
            logger.info("ID: " + commonBookDto.getId());
            logger.info("Create Date: " + commonBookDto.getCreateDate());
            commonBookDtoList.add(commonBookDto);
        }
        paginatedList.setResultList(commonBookDtoList);
    }

    public void findByRatingCriteria(PaginatedList<CommonBookDto> paginatedList, CommonBookCriteria criteria,
            SortCriteria sortCriteria, String sortBy) throws Exception {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        RatingSQLQueryBuilder ratingSQLQueryBuilder = new RatingSQLQueryBuilder();
        String sqlQuery = ratingSQLQueryBuilder.buildQuery(criteria);
        QueryParam queryParam = new QueryParam(sqlQuery, parameterMap);
        List<Object[]> resultList = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        List<CommonBookDto> commonBookDtoList = new ArrayList<CommonBookDto>();
        for (Object[] o : resultList) {
            int i = 0;
            CommonBookDto commonBookDto = new CommonBookDto();
            commonBookDto.setId((String) o[i++]);
            commonBookDto.setBookId((String) o[i++]);
            commonBookDto.setCreateDate((Date) o[i++]);
            commonBookDto.setReadDate((Date) o[i++]);
            commonBookDtoList.add(commonBookDto);
        }
        paginatedList.setResultList(commonBookDtoList);
    }

    public void findByFilteredCriteria(PaginatedList<CommonBookDto> paginatedList, CommonBookCriteria criteria,
            SortCriteria sortCriteria, String sortBy, List<String> authors, List<String> genres, Integer ratingFilter)
            throws Exception {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        FilteredSQLQueryBuilder filteredSQLQueryBuilder = new FilteredSQLQueryBuilder();
        String sqlQuery = filteredSQLQueryBuilder.buildQuery(criteria);
        QueryParam queryParam = new QueryParam(sqlQuery, parameterMap);
        List<Object[]> resultList = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        List<CommonBookDto> commonBookDtoList = new ArrayList<CommonBookDto>();
        for (Object[] o : resultList) {
            int i = 0;
            CommonBookDto commonBookDto = new CommonBookDto();
            commonBookDto.setId((String) o[i++]);
            commonBookDto.setBookId((String) o[i++]);
            commonBookDto.setCreateDate((Date) o[i++]);
            commonBookDto.setReadDate((Date) o[i++]);
            String author = (String) o[i++];
            logger.info(author);
            Double rating = (Double) o[i++];
            String genreString = (String) o[i++];
            logger.info(genreString);
            List<String> genresList = Arrays.asList(genreString.split(","));
            boolean authorFound = authors == null;
            if(!authorFound){
                logger.info("authors is empty");           
            }
            else{
                logger.info("authors is not empty");
            }
            boolean genreFound = genres == null;
            if (!authorFound && authors.contains(author)) {
                logger.info("Contains Author");
                authorFound = true;
            }
            if (!genreFound) {
                for (String genre : genresList) {
                    if (genres.contains(genre)) {
                        logger.info("Found genre");
                        genreFound = true;
                        break;
                    }
                }
            }
            else{
                logger.info("genreFound is true");
            }
            boolean ratingPass = ratingFilter == null || (rating != null && rating > ratingFilter);
            if(ratingPass){
                logger.info("greater rating than ratingFIlter");
            }
            if (authorFound && genreFound && ratingPass) {
                logger.info("added something to dto");
                commonBookDtoList.add(commonBookDto);
            }
        }
        paginatedList.setResultList(commonBookDtoList);
    }
}

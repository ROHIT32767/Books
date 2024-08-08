package com.sismics.books.rest.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.io.FileInputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.sismics.books.core.event.BookImportedEvent;
import com.sismics.books.core.model.context.AppContext;
import com.sismics.books.core.model.jpa.User;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.nio.file.Paths;
import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hsqldb.persist.Log;
import com.sismics.books.core.util.DirectoryUtil;
import com.google.common.base.Strings;
import com.sismics.books.core.dao.jpa.BookDao;
import com.sismics.books.core.dao.jpa.TagDao;
import com.sismics.books.core.dao.jpa.UserBookDao;
import com.sismics.books.core.dao.jpa.CommonBookDao;
import com.sismics.books.core.dao.jpa.CommonBookRatingDao;
import com.sismics.books.core.dao.jpa.CommonBookGenreDao;
import com.sismics.books.core.dao.jpa.UserDao;
import com.sismics.books.core.dao.jpa.dto.TagDto;
import com.sismics.books.core.dao.jpa.dto.UserBookDto;
import com.sismics.books.core.dao.jpa.dto.CommonBookDto;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.model.jpa.Tag;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.books.core.model.jpa.CommonBook;
import com.sismics.books.core.model.jpa.CommonBookRating;
import com.sismics.books.core.model.jpa.CommonBookGenre;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.google.common.collect.Lists;
import com.sismics.books.core.dao.jpa.criteria.UserBookCriteria;
import com.sismics.books.core.dao.jpa.criteria.CommonBookCriteria;
import com.sismics.books.core.dao.jpa.dto.UserBookDto;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedLists;
import com.sismics.books.core.util.jpa.SortCriteria;
import java.util.logging.Logger;

@Path("/commonbook")
public class CommonBookResource extends BaseResource {

    private static final Logger logger = Logger.getLogger(CommonBookResource.class.getName());

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("isbn") String isbn,
            @FormParam("genre") String genre) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(isbn, "isbn");
        String[] genreArray = genre.split(",");
        List<String> genreList = Arrays.asList(genreArray);
        BookDao bookDao = new BookDao();
        Book book = bookDao.getByIsbn(isbn);
        if (book == null) {
            try {
                book = AppContext.getInstance().getBookDataService().searchBook(isbn);
            } catch (Exception e) {
                throw new ClientException("BookNotFound", e.getCause().getMessage(), e);
            }
            bookDao.create(book);
        }

        CommonBookDao commonBookDao = new CommonBookDao();
        CommonBook commonBook = commonBookDao.getByBookId(book.getId());
        if (commonBook == null) {
            commonBook = new CommonBook();
            commonBook.setBookId(book.getId());
            commonBook.setCreateDate(new Date());
            commonBookDao.create(commonBook);
        } else {
            throw new ClientException("BookAlreadyAdded", "Book already added");
        }

        CommonBookGenreDao commonBookGenreDao = new CommonBookGenreDao();
        for (String genreName : genreList) {
            CommonBookGenre commonBookGenre = commonBookGenreDao.getByCommonBookIdAndGenre(commonBook.getId(),
                    genreName);
            if (commonBookGenre == null) {
                commonBookGenre = new CommonBookGenre();
                commonBookGenre.setCommonBookId(commonBook.getId());
                commonBookGenre.setGenre(genreName);
                commonBookGenreDao.create(commonBookGenre);
            }
        }

        JSONObject response = new JSONObject();
        response.put("id", commonBook.getId());
        return Response.ok().entity(response).build();
    }

    @PUT
    @Path("manual")
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("title") String title,
            @FormParam("subtitle") String subtitle,
            @FormParam("author") String author,
            @FormParam("description") String description,
            @FormParam("isbn10") String isbn10,
            @FormParam("isbn13") String isbn13,
            @FormParam("page_count") Long pageCount,
            @FormParam("language") String language,
            @FormParam("publish_date") String publishDateStr,
            @FormParam("genre") String genre) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        String[] genreArray = genre.split(",");
        List<String> genreList = Arrays.asList(genreArray);
        title = ValidationUtil.validateLength(title, "title", 1, 255, false);
        subtitle = ValidationUtil.validateLength(subtitle, "subtitle", 1, 255, true);
        author = ValidationUtil.validateLength(author, "author", 1, 255, false);
        description = ValidationUtil.validateLength(description, "description", 1, 4000, true);
        isbn10 = ValidationUtil.validateLength(isbn10, "isbn10", 10, 10, true);
        isbn13 = ValidationUtil.validateLength(isbn13, "isbn13", 13, 13, true);
        language = ValidationUtil.validateLength(language, "language", 2, 2, true);
        Date publishDate = ValidationUtil.validateDate(publishDateStr, "publish_date", false);
        if (Strings.isNullOrEmpty(isbn10) && Strings.isNullOrEmpty(isbn13)) {
            throw new ClientException("ValidationError", "At least one ISBN number is mandatory");
        }
        BookDao bookDao = new BookDao();
        Book bookIsbn10 = bookDao.getByIsbn(isbn10);
        Book bookIsbn13 = bookDao.getByIsbn(isbn13);
        if (bookIsbn10 != null || bookIsbn13 != null) {
            throw new ClientException("BookAlreadyAdded", "Book already added");
        }
        Book book = new Book();
        book.setId(UUID.randomUUID().toString());

        if (title != null) {
            book.setTitle(title);
        }
        if (subtitle != null) {
            book.setSubtitle(subtitle);
        }
        if (author != null) {
            book.setAuthor(author);
        }
        if (description != null) {
            book.setDescription(description);
        }
        if (isbn10 != null) {
            book.setIsbn10(isbn10);
        }
        if (isbn13 != null) {
            book.setIsbn13(isbn13);
        }
        if (pageCount != null) {
            book.setPageCount(pageCount);
        }
        if (language != null) {
            book.setLanguage(language);
        }
        if (publishDate != null) {
            book.setPublishDate(publishDate);
        }
        bookDao.create(book);

        CommonBookDao commonBookDao = new CommonBookDao();
        CommonBook commonBook = commonBookDao.getByBookId(book.getId());
        if (commonBook == null) {
            commonBook = new CommonBook();
            commonBook.setBookId(book.getId());
            commonBook.setCreateDate(new Date());
            commonBookDao.create(commonBook);
        } else {
            throw new ClientException("BookAlreadyAdded", "Book already added");
        }

        CommonBookGenreDao commonBookGenreDao = new CommonBookGenreDao();
        for (String genreName : genreList) {
            CommonBookGenre commonBookGenre = commonBookGenreDao.getByCommonBookIdAndGenre(book.getId(), genreName);
            if (commonBookGenre == null) {
                commonBookGenre = new CommonBookGenre();
                commonBookGenre.setCommonBookId(commonBook.getId());
                commonBookGenre.setGenre(genreName);
                commonBookGenreDao.create(commonBookGenre);
            }
        }

        JSONObject response = new JSONObject();
        response.put("id", commonBook.getId());
        return Response.ok().entity(response).build();
    }

    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        CommonBookDao commonBookDao = new CommonBookDao();
        CommonBook commonBook = commonBookDao.getById(id);
        if (commonBook == null) {
            throw new ClientException("BookNotFound", "Book not found");
        }
        commonBookDao.deleteById(commonBook.getId());
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /* Give rating to commonbook */
    @PUT
    @Path("{id: [a-z0-9\\-]+}/rate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rate(
            @PathParam("id") String id,
            @FormParam("rating") Integer rating) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        if (rating < 1 || rating > 10) {
            throw new ClientException("ValidationError", "Rating must be between 1 and 10");
        }
        CommonBookDao commonBookDao = new CommonBookDao();
        CommonBook commonBook = commonBookDao.getById(id);
        if (commonBook == null) {
            throw new ClientException("BookNotFound", "Book not found");
        }
        CommonBookRatingDao commonBookRatingDao = new CommonBookRatingDao();
        CommonBookRating commonBookRating = commonBookRatingDao.getByCommonBookIdAndUserId(id, principal.getId());
        if (commonBookRating == null) {
            commonBookRating = new CommonBookRating();
            commonBookRating.setCommonBookId(id);
            commonBookRating.setUserId(principal.getId());
            commonBookRating.setRating(rating);
            commonBookRatingDao.create(commonBookRating);
        } else {
            commonBookRating.setRating(rating);
            commonBookRatingDao.update(commonBookRating);
        }
        JSONObject response = new JSONObject();
        response.put("id", commonBookRating.getId());
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Returns all books.
     * 
     * @param limit  Page limit
     * @param offset Page offset
     * @return Response
     * @throws JSONException
     */
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search,
            @QueryParam("read") Boolean read) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        List<JSONObject> books = new ArrayList<>();
        CommonBookDao commonBookDao = new CommonBookDao();
        PaginatedList<CommonBookDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        CommonBookCriteria criteria = new CommonBookCriteria();
        criteria.setSearch(search);
        criteria.setRead(read);
        try {
            commonBookDao.findByCriteria(paginatedList, criteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in books", e);
        }
        CommonBookRatingDao commonBookRatingDao = new CommonBookRatingDao();
        CommonBookGenreDao commonBookGenreDao = new CommonBookGenreDao();
        BookDao bookDao = new BookDao();

        for (CommonBookDto commonBookDto : paginatedList.getResultList()) {
            ResponseBuilder bookBuilder = new ResponseBuilder();
            Book book = bookDao.getById(commonBookDto.getBookId());
            double avgRating = commonBookRatingDao.getAverageRatingByCommonBookId(commonBookDto.getId());
            long numRatings = commonBookRatingDao.getNumberOfRatingsByCommonBookId(commonBookDto.getId());
            List<String> GenreList = commonBookGenreDao.getGenresByCommonBookId(commonBookDto.getId());
            String genreString = String.join(",", GenreList);

            bookBuilder.add("id", commonBookDto.getId())
                    .add("createDate", commonBookDto.getCreateDate())
                    .add("bookId", commonBookDto.getBookId())
                    .add("book", book)
                    .add("author", book.getAuthor())
                    .add("title", book.getTitle())
                    .add("subtitle", book.getSubtitle())
                    .add("description", book.getDescription())
                    .add("publishTime", book.getPublishDate())
                    .add("pageCount", book.getPageCount())
                    .add("language", book.getLanguage())
                    .add("isbn10", book.getIsbn10())
                    .add("isbn13", book.getIsbn13())
                    .add("avgRating", avgRating)
                    .add("genre", genreString)
                    .add("numRatings", numRatings);

            books.add(bookBuilder.build());
        }
        logger.info(books.toString());
        JSONObject response = new JSONObject();
        response.put("total", paginatedList.getResultCount());
        response.put("books", books);
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        CommonBookDao commonBookDao = new CommonBookDao();
        CommonBook commonBook = commonBookDao.getById(id);
        if (commonBook == null) {
            throw new ClientException("BookNotFound", "Book not found");
        }
        BookDao bookDao = new BookDao();
        Book book = bookDao.getById(commonBook.getBookId());
        CommonBookRatingDao commonBookRatingDao = new CommonBookRatingDao();
        CommonBookGenreDao commonBookGenreDao = new CommonBookGenreDao();
        double avgRating = commonBookRatingDao.getAverageRatingByCommonBookId(id);
        List<String> GenreList = commonBookGenreDao.getGenresByCommonBookId(commonBook.getId());
        long numRatings = commonBookRatingDao.getNumberOfRatingsByCommonBookId(id);
        String genreString = String.join(",", GenreList);
        ResponseBuilder responseBuilder = new ResponseBuilder()
                .add("id", commonBook.getId())
                .add("createDate", commonBook.getCreateDate())
                .add("bookId", commonBook.getBookId())
                .add("book", book)
                .add("author", book.getAuthor())
                .add("title", book.getTitle())
                .add("subtitle", book.getSubtitle())
                .add("description", book.getDescription())
                .add("publishTime", book.getPublishDate())
                .add("pageCount", book.getPageCount())
                .add("language", book.getLanguage())
                .add("isbn10", book.getIsbn10())
                .add("isbn13", book.getIsbn13())
                .add("avgRating", avgRating)
                .add("genre", genreString)
                .add("numRatings", numRatings);

        JSONObject response = new JSONObject();
        response = responseBuilder.build();
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("list/genre")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listGenre(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search,
            @QueryParam("read") Boolean read,
            @QueryParam("genre") String genre,
            @QueryParam("author") String author,
            @QueryParam("rating") Integer rating) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        String[] genreArray = null;
        List<String> genrelist = null;
        if (genre != null) {
            genreArray = genre.split(",");
            genrelist = Arrays.asList(genreArray);
        }

        String[] authorArray = null;
        List<String> authorlist = null;
        if (author != null) {
            authorArray = author.split(",");
            authorlist = Arrays.asList(authorArray);
        }

        List<JSONObject> filteredBooks = new ArrayList<>();
        CommonBookDao commonBookDao = new CommonBookDao();
        PaginatedList<CommonBookDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        CommonBookCriteria criteria = new CommonBookCriteria();
        criteria.setSearch(search);
        criteria.setRead(read);
        try {
            commonBookDao.findByFilteredCriteria(paginatedList, criteria, sortCriteria, "title", authorlist, genrelist,
                    rating);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in books", e);
        }
        CommonBookRatingDao commonBookRatingDao = new CommonBookRatingDao();
        CommonBookGenreDao commonBookGenreDao = new CommonBookGenreDao();
        BookDao bookDao = new BookDao();
        for (CommonBookDto commonBookDto : paginatedList.getResultList()) {
            Book book = bookDao.getById(commonBookDto.getBookId());
            double avgRating = commonBookRatingDao.getAverageRatingByCommonBookId(commonBookDto.getId());
            long numRatings = commonBookRatingDao.getNumberOfRatingsByCommonBookId(commonBookDto.getId());
            List<String> GenreList = commonBookGenreDao.getGenresByCommonBookId(commonBookDto.getId());
            String genreString = String.join(",", GenreList);
            ResponseBuilder bookBuilder = new ResponseBuilder()
                    .add("id", commonBookDto.getId())
                    .add("createDate", commonBookDto.getCreateDate())
                    .add("bookId", commonBookDto.getBookId())
                    .add("author", book.getAuthor())
                    .add("title", book.getTitle())
                    .add("subtitle", book.getSubtitle())
                    .add("description", book.getDescription())
                    .add("publishTime", book.getPublishDate())
                    .add("pageCount", book.getPageCount())
                    .add("language", book.getLanguage())
                    .add("isbn10", book.getIsbn10())
                    .add("isbn13", book.getIsbn13())
                    .add("avgRating", avgRating)
                    .add("numRatings", numRatings)
                    .add("genre", genreString);

            filteredBooks.add(bookBuilder.build());
        }
        JSONObject response = new JSONObject();
        response.put("total", paginatedList.getResultCount());
        response.put("books", filteredBooks);
        return Response.ok().entity(response).build();
    }

    // write list/rating where there is a Queryparam to check whether if we have to
    // get top 10 by number of ratings ir average ratings
    @GET
    @Path("list/toprated")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listRating(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search,
            @QueryParam("read") Boolean read,
            @QueryParam("sortBy") String sortBy) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        List<JSONObject> topRatedBooks = new ArrayList<>();
        CommonBookDao commonBookDao = new CommonBookDao();
        PaginatedList<CommonBookDto> paginatedList = PaginatedLists.create(limit, offset);
        sortColumn = 4;
        if ("numRating".equalsIgnoreCase(sortBy)) {
            sortColumn = 5;
        }
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        CommonBookCriteria criteria = new CommonBookCriteria();
        criteria.setSearch(search);
        criteria.setRead(read);
        try {
            commonBookDao.findByRatingCriteria(paginatedList, criteria, sortCriteria, sortBy);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in books", e);
        }
        CommonBookRatingDao commonBookRatingDao = new CommonBookRatingDao();
        BookDao bookDao = new BookDao();
        for (CommonBookDto commonBookDto : paginatedList.getResultList()) {
            Book book = bookDao.getById(commonBookDto.getBookId());
            double avgRating = commonBookRatingDao.getAverageRatingByCommonBookId(commonBookDto.getId());
            long numRatings = commonBookRatingDao.getNumberOfRatingsByCommonBookId(commonBookDto.getId());
            ResponseBuilder bookBuilder = new ResponseBuilder()
                    .add("id", commonBookDto.getId())
                    .add("createDate", commonBookDto.getCreateDate())
                    .add("bookId", commonBookDto.getBookId())
                    .add("author", book.getAuthor())
                    .add("title", book.getTitle())
                    .add("subtitle", book.getSubtitle())
                    .add("description", book.getDescription())
                    .add("publishTime", book.getPublishDate())
                    .add("pageCount", book.getPageCount())
                    .add("language", book.getLanguage())
                    .add("isbn10", book.getIsbn10())
                    .add("isbn13", book.getIsbn13())
                    .add("avgRating", avgRating)
                    .add("numRatings", numRatings);

            topRatedBooks.add(bookBuilder.build());
        }
        JSONObject response = new JSONObject();
        response.put("total", paginatedList.getResultCount());
        response.put("books", topRatedBooks);
        return Response.ok().entity(response).build();
    }
}
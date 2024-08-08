public enum AppId {
    
    FACEBOOK,
}

public enum ConfigType {
    /**
     * Google API key.
     */
    API_KEY_GOOGLE
}

public class UserBookCriteria {
    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Search query.
     */
    private String search;
    
    /**
     * Read state.
     */
    private Boolean read;
    
    /**
     * Tag IDs.
     */
    private List<String> tagIdList;
    
    /**
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of search.
     *
     * @return the search
     */
    public String getSearch() {
        return search;
    }

    /**
     * Setter of search.
     *
     * @param search search
     */
    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * Getter of tagIdList.
     *
     * @return the tagIdList
     */
    public List<String> getTagIdList() {
        return tagIdList;
    }

    /**
     * Setter of tagIdList.
     *
     * @param tagIdList tagIdList
     */
    public void setTagIdList(List<String> tagIdList) {
        this.tagIdList = tagIdList;
    }

    /**
     * Getter of read.
     * @return read
     */
    public Boolean getRead() {
        return read;
    }

    /**
     * Setter of read.
     * @param read read
     */
    public void setRead(Boolean read) {
        this.read = read;
    }
}

public class BookDao {
    /**
     * Creates a new book.
     * 
     * @param book Book
     * @return New ID
     * @throws Exception
     */
    public String create(Book book) {
        // Create the book
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(book);
        return book.getId();
    }
    
    /**
     * Gets a book by its ID.
     * 
     * @param id Book ID
     * @return Book
     */
    public Book getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(Book.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Returns a book by its ISBN number (10 or 13)
     * 
     * @param isbn ISBN Number (10 or 13)
     * @return Book
     */
    public Book getByIsbn(String isbn) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select b from Book b where b.isbn10 = :isbn or b.isbn13 = :isbn");
        q.setParameter("isbn", isbn);
        try {
            return (Book) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}

public class BookImportedEvent {
    /**
     * User requesting the import.
     */
    private User user;
    
    /**
     * Temporary file to import.
     */
    private File importFile;
    
    /**
     * Getter of user.
     *
     * @return user
     */
    public User getUser() {
        return user;
    }

    /**
     * Setter of user.
     *
     * @param user user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Getter of importFile.
     *`
     * @return importFile
     */
    public File getImportFile() {
        return importFile;
    }

    /**
     * Setter of importFile.
     *
     * @param importFile importFile
     */
    public void setImportFile(File importFile) {
        this.importFile = importFile;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("user", user)
                .add("importFile", importFile)
                .toString();
    }
}

public class BookImportAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(BookImportAsyncListener.class);

    /**
     * Process the event.
     * 
     * @param bookImportedEvent Book imported event
     * @throws Exception
     */
    @Subscribe
    public void on(final BookImportedEvent bookImportedEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Books import requested event: {0}", bookImportedEvent.toString()));
        }
        
        // Create books and tags
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                CSVReader reader = null;
                BookDao bookDao = new BookDao();
                UserBookDao userBookDao = new UserBookDao();
                TagDao tagDao = new TagDao();
                try {
                    reader = new CSVReader(new FileReader(bookImportedEvent.getImportFile()));
                } catch (FileNotFoundException e) {
                    log.error("Unable to read CSV file", e);
                }
                
                // Goodreads date format
                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd");
                
                String [] line;
                try {
                    while ((line = reader.readNext()) != null) {
                        if (line[0].equals("Book Id")) {
                            // Skip header
                            continue;
                        }
                        
                        // Retrieve ISBN number
                        String isbn = Strings.isNullOrEmpty(line[6]) ? line[5] : line[6];
                        if (Strings.isNullOrEmpty(isbn)) {
                            log.warn("No ISBN number for Goodreads book ID: " + line[0]);
                            continue;
                        }
                        
                        // Fetch the book from database if it exists
                        Book book = bookDao.getByIsbn(isbn);
                        if (book == null) {
                            // Try to get the book from a public API
                            try {
                                book = AppContext.getInstance().getBookDataService().searchBook(isbn);
                            } catch (Exception e) {
                                continue;
                            }
                            
                            // Save the new book in database
                            bookDao.create(book);
                        }
                        
                        // Create a new user book if needed
                        UserBook userBook = userBookDao.getByBook(book.getId(), bookImportedEvent.getUser().getId());
                        if (userBook == null) {
                            userBook = new UserBook();
                            userBook.setUserId(bookImportedEvent.getUser().getId());
                            userBook.setBookId(book.getId());
                            userBook.setCreateDate(new Date());
                            if (!Strings.isNullOrEmpty(line[14])) {
                                userBook.setReadDate(formatter.parseDateTime(line[14]).toDate());
                            }
                            if (!Strings.isNullOrEmpty(line[15])) {
                                userBook.setCreateDate(formatter.parseDateTime(line[15]).toDate());
                            }
                            userBookDao.create(userBook);
                        }
                        
                        // Create tags
                        String[] bookshelfArray = line[16].split(",");
                        Set<String> tagIdSet = new HashSet<String>();
                        for (String bookshelf : bookshelfArray) {
                            bookshelf = bookshelf.trim();
                            if (Strings.isNullOrEmpty(bookshelf)) {
                                continue;
                            }
                            
                            Tag tag = tagDao.getByName(bookImportedEvent.getUser().getId(), bookshelf);
                            if (tag == null) {
                                tag = new Tag();
                                tag.setName(bookshelf);
                                tag.setColor(MathUtil.randomHexColor());
                                tag.setUserId(bookImportedEvent.getUser().getId());
                                tagDao.create(tag);
                            }
                            
                            tagIdSet.add(tag.getId());
                        }
                        
                        // Add tags to the user book
                        if (tagIdSet.size() > 0) {
                            List<TagDto> tagDtoList = tagDao.getByUserBookId(userBook.getId());
                            for (TagDto tagDto : tagDtoList) {
                                tagIdSet.add(tagDto.getId());
                            }
                            tagDao.updateTagList(userBook.getId(), tagIdSet);
                        }
                        
                        TransactionUtil.commit();
                    }
                } catch (Exception e) {
                    log.error("Error parsing CSV line", e);
                }
            }
        });
    }
}

@Entity
@Table(name = "T_BOOK")
public class Book {
    /**
     * Book ID.
     */
    @Id
    @Column(name = "BOK_ID_C", length = 36)
    private String id;
    
    /**
     * Title.
     */
    @Column(name = "BOK_TITLE_C", nullable = false, length = 255)
    private String title;
    
    /**
     * Subtitle.
     */
    @Column(name = "BOK_SUBTITLE_C", length = 255)
    private String subtitle;
    
    /**
     * Author.
     */
    @Column(name = "BOK_AUTHOR_C", nullable = false, length = 255)
    private String author;
    
    /**
     * Description.
     */
    @Column(name = "BOK_DESCRIPTION_C", length = 4000)
    private String description;
    
    /**
     * ISBN 10.
     */
    @Column(name = "BOK_ISBN10_C", length = 10)
    private String isbn10;
    
    /**
     * ISBN 13.
     */
    @Column(name = "BOK_ISBN13_C", length = 13)
    private String isbn13;
    
    /**
     * Page count.
     */
    @Column(name = "BOK_PAGECOUNT_N")
    private Long pageCount;
    
    /**
     * Language (ISO 639-1).
     */
    @Column(name = "BOK_LANGUAGE_C", length = 2)
    private String language;
    
    /**
     * Publication date.
     */
    @Column(name = "BOK_PUBLISHDATE_D", nullable = false)
    private Date publishDate;
    
    /**
     * Getter of id.
     * 
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     * 
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of title.
     * 
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter of title.
     * 
     * @param title title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter of subtitle.
     * 
     * @return subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * Setter of subtitle.
     * 
     * @param subtitle subtitle
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Getter of author.
     * 
     * @return author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Setter of author.
     * 
     * @param author author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Getter of description.
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter of description.
     * 
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter of isbn10.
     * 
     * @return isbn10
     */
    public String getIsbn10() {
        return isbn10;
    }

    /**
     * Setter of isbn10.
     * 
     * @param isbn10 isbn10
     */
    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    /**
     * Getter of isbn13.
     * 
     * @return isbn13
     */
    public String getIsbn13() {
        return isbn13;
    }

    /**
     * Setter of isbn13.
     * 
     * @param isbn13 isbn13
     */
    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    /**
     * Getter of pageCount.
     * 
     * @return pageCount
     */
    public Long getPageCount() {
        return pageCount;
    }

    /**
     * Setter of pageCount.
     * 
     * @param pageCount pageCount
     */
    public void setPageCount(Long pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * Getter of language.
     * 
     * @return language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Setter of language.
     * 
     * @param language language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Getter of publishDate.
     * 
     * @return publishDate
     */
    public Date getPublishDate() {
        return publishDate;
    }

    /**
     * Setter of publishDate.
     * 
     * @param publishedDate publishDate
     */
    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("title", title)
                .toString();
    }
}

@Entity
@Table(name = "T_USER_BOOK")
public class UserBook implements Serializable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * User book ID.
     */
    @Id
    @Column(name = "UBK_ID_C", length = 36)
    private String id;
    
    /**
     * Book ID.
     */
    @Id
    @Column(name = "UBK_IDBOOK_C", nullable = false, length = 36)
    private String bookId;
    
    /**
     * User ID.
     */
    @Id
    @Column(name = "UBK_IDUSER_C", nullable = false, length = 36)
    private String userId;
    
    /**
     * Creation date.
     */
    @Column(name = "UBK_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "UBK_DELETEDATE_D")
    private Date deleteDate;
    
    /**
     * Read date.
     */
    @Column(name = "UBK_READDATE_D")
    private Date readDate;
    
    /**
     * Getter of id.
     * 
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     * 
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of bookId.
     * 
     * @return bookId
     */
    public String getBookId() {
        return bookId;
    }

    /**
     * Setter of bookId.
     * 
     * @param bookId bookId
     */
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    /**
     * Getter of userId.
     * 
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     * 
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of createDate.
     * 
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Setter of createDate.
     * 
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * Getter of deleteDate.
     * 
     * @return deleteDate
     */
    public Date getDeleteDate() {
        return deleteDate;
    }

    /**
     * Setter of deleteDate.
     * 
     * @param deleteDate deleteDate
     */
    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    /**
     * Getter of readDate.
     * 
     * @return readDate
     */
    public Date getReadDate() {
        return readDate;
    }

    /**
     * Setter of readDate.
     * 
     * @param readDate readDate
     */
    public void setReadDate(Date readDate) {
        this.readDate = readDate;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bookId == null) ? 0 : bookId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserBook other = (UserBook) obj;
        if (bookId == null) {
            if (other.bookId != null) {
                return false;
            }
        } else if (!bookId.equals(other.bookId)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .toString();
    }
}

public class BookDataService extends AbstractIdleService {
    private static final Logger log = LoggerFactory.getLogger(BookDataService.class);
    private static final String GOOGLE_BOOKS_SEARCH_FORMAT = "https://www.googleapis.com/books/v1/volumes?q=isbn:%s&key=%s";
    private static final String OPEN_LIBRARY_FORMAT = "http://openlibrary.org/api/volumes/brief/isbn/%s.json";
    private ExecutorService executor;
    private RateLimiter googleRateLimiter = RateLimiter.create(20);
    private RateLimiter openLibraryRateLimiter = RateLimiter.create(0.33332);
    private String apiKeyGoogle = null;
    private static DateTimeFormatter formatter;
    
    static {
        // Initialize date parser
        DateTimeParser[] parsers = { 
                DateTimeFormat.forPattern("yyyy").getParser(),
                DateTimeFormat.forPattern("yyyy-MM").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
                DateTimeFormat.forPattern("MMM d, yyyy").getParser()};
        formatter = new DateTimeFormatterBuilder().append( null, parsers ).toFormatter();
    }
    
    @Override
    protected void startUp() throws Exception {
        initConfig();
        executor = Executors.newSingleThreadExecutor(); 
        if (log.isInfoEnabled()) {
            log.info("Book data service started");
        }
    }
    
    public void initConfig() {
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                apiKeyGoogle = ConfigUtil.getConfigStringValue(ConfigType.API_KEY_GOOGLE);
            }
        });
    }

    public Book searchBook(String rawIsbn) throws Exception {
        // Sanitize ISBN (keep only digits)
        final String isbn = rawIsbn.replaceAll("[^\\d]", "");
        
        // Validate ISBN
        if (Strings.isNullOrEmpty(isbn)) {
            throw new Exception("ISBN is empty");
        }
        if (isbn.length() != 10 && isbn.length() != 13) {
            throw new Exception("ISBN must be 10 or 13 characters long");
        }

        Callable<Book> callable = new Callable<Book>() {
            
            @Override
            public Book call() throws Exception {
                try {
                    return searchBookWithGoogle(isbn);
                } catch (Exception e) {
                    log.warn("Book not found with Google: " + isbn + " with error: " + e.getMessage());
                    try {
                        return searchBookWithOpenLibrary(isbn);
                    } catch (Exception e0) {
                        log.warn("Book not found with Open Library: " + isbn + " with error: " + e0.getMessage());
                        log.error("Book not found with any API: " + isbn);
                        throw e0;
                    }
                }
            }
        };
        FutureTask<Book> futureTask = new FutureTask<Book>(callable);
        executor.submit(futureTask);
        
        return futureTask.get();
    }

    private Book searchBookWithGoogle(String isbn) throws Exception {
        googleRateLimiter.acquire();
        
        URL url = new URL(String.format(Locale.ENGLISH, GOOGLE_BOOKS_SEARCH_FORMAT, isbn, apiKeyGoogle));
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Accept-Charset", "utf-8");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        InputStream inputStream = connection.getInputStream();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readValue(inputStream, JsonNode.class);
        ArrayNode items = (ArrayNode) rootNode.get("items");
        if (rootNode.get("totalItems").getIntValue() <= 0) {
            throw new Exception("No book found for ISBN: " + isbn);
        }
        JsonNode item = items.get(0);
        JsonNode volumeInfo = item.get("volumeInfo");
        
        // Build the book
        Book book = new Book();
        book.setId(UUID.randomUUID().toString());
        book.setTitle(volumeInfo.get("title").getTextValue());
        book.setSubtitle(volumeInfo.has("subtitle") ? volumeInfo.get("subtitle").getTextValue() : null);
        ArrayNode authors = (ArrayNode) volumeInfo.get("authors");
        if (authors.size() <= 0) {
            throw new Exception("Author not found");
        }
        book.setAuthor(authors.get(0).getTextValue());
        book.setDescription(volumeInfo.has("description") ? volumeInfo.get("description").getTextValue() : null);
        ArrayNode industryIdentifiers = (ArrayNode) volumeInfo.get("industryIdentifiers");
        Iterator<JsonNode> iterator = industryIdentifiers.getElements();
        while (iterator.hasNext()) {
            JsonNode industryIdentifier = iterator.next();
            if ("ISBN_10".equals(industryIdentifier.get("type").getTextValue())) {
                book.setIsbn10(industryIdentifier.get("identifier").getTextValue());
            } else if ("ISBN_13".equals(industryIdentifier.get("type").getTextValue())) {
                book.setIsbn13(industryIdentifier.get("identifier").getTextValue());
            }
        }
        book.setLanguage(volumeInfo.get("language").getTextValue());
        book.setPageCount(volumeInfo.has("pageCount") ? volumeInfo.get("pageCount").getLongValue() : null);
        book.setPublishDate(formatter.parseDateTime(volumeInfo.get("publishedDate").getTextValue()).toDate());
        
        // Download the thumbnail
        JsonNode imageLinks = volumeInfo.get("imageLinks");
        if (imageLinks != null && imageLinks.has("thumbnail")) {
            String imageUrl = imageLinks.get("thumbnail").getTextValue();
            downloadThumbnail(book, imageUrl);
        }
        
        return book;
    }

    private Book searchBookWithOpenLibrary(String isbn) throws Exception {
        openLibraryRateLimiter.acquire();
        
        URL url = new URL(String.format(Locale.ENGLISH, OPEN_LIBRARY_FORMAT, isbn));
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Accept-Charset", "utf-8");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        InputStream inputStream = connection.getInputStream();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readValue(inputStream, JsonNode.class);
        if (rootNode instanceof ArrayNode) {
            throw new Exception("No book found for ISBN: " + isbn);
        }
        
        JsonNode bookNode = rootNode.get("records").getElements().next();
        JsonNode details = bookNode.get("details").get("details");
        JsonNode data = bookNode.get("data");
        
        // Build the book
        Book book = new Book();
        book.setId(UUID.randomUUID().toString());
        book.setTitle(details.get("title").getTextValue());
        book.setSubtitle(details.has("subtitle") ? details.get("subtitle").getTextValue() : null);
        if (!data.has("authors") || data.get("authors").size() == 0) {
            throw new Exception("Book without author for ISBN: " + isbn);
        }
        book.setAuthor(data.get("authors").get(0).get("name").getTextValue());
        book.setDescription(details.has("first_sentence") ? details.get("first_sentence").get("value").getTextValue() : null);
        book.setIsbn10(details.has("isbn_10") && details.get("isbn_10").size() > 0 ? details.get("isbn_10").get(0).getTextValue() : null);
        book.setIsbn13(details.has("isbn_13") && details.get("isbn_13").size() > 0 ? details.get("isbn_13").get(0).getTextValue() : null);
        if (details.has("languages") && details.get("languages").size() > 0) {
            String language = details.get("languages").get(0).get("key").getTextValue();
            LanguageCode languageCode = LanguageCode.getByCode(language.split("/")[2]);
            book.setLanguage(languageCode.name());
        }
        book.setPageCount(details.has("number_of_pages") ? details.get("number_of_pages").getLongValue() : null);
        if (!details.has("publish_date")) {
            throw new Exception("Book without publication date for ISBN: " + isbn);
        }
        book.setPublishDate(details.has("publish_date") ? formatter.parseDateTime(details.get("publish_date").getTextValue()).toDate() : null);
        
        // Download the thumbnail
        if (details.has("covers") && details.get("covers").size() > 0) {
            String imageUrl = "http://covers.openlibrary.org/b/id/" + details.get("covers").get(0).getLongValue() + "-M.jpg";
            downloadThumbnail(book, imageUrl);
        }
        
        return book;
    }
    
    public void downloadThumbnail(Book book, String imageUrl) throws Exception {
        URLConnection imageConnection = new URL(imageUrl).openConnection();
        imageConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");
        imageConnection.setConnectTimeout(10000);
        imageConnection.setReadTimeout(10000);
        try (InputStream inputStream = new BufferedInputStream(imageConnection.getInputStream())) {
            if (MimeTypeUtil.guessMimeType(inputStream) != MimeType.IMAGE_JPEG) {
                throw new Exception("Only JPEG images are supported as thumbnails");
            }
            
            Path imagePath = Paths.get(DirectoryUtil.getBookDirectory().getPath(), book.getId());
            Files.copy(inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    @Override
    protected void shutDown() throws Exception {
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        if (log.isInfoEnabled()) {
            log.info("Book data service stopped");
        }
    }
}

public class FacebookService extends AbstractScheduledService {
    
    private String facebookAppId;
    
    private String facebookAppSecret;
    
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FacebookService.class);

    @Override
    protected void startUp() throws Exception {
        ResourceBundle configBundle = ConfigUtil.getConfigBundle();
        facebookAppId = configBundle.getString("app_key.facebook.id");
        facebookAppSecret = configBundle.getString("app_key.facebook.secret");
    }

    @Override
    protected void shutDown() throws Exception {
    }

    @Override
    protected void runOneIteration() throws Exception {
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                // Synchronize Facebook contacts
                synchronizeAllContact();
            }
        });
    }

    private void synchronizeAllContact() {
        if (log.isInfoEnabled()) {
            log.info("Synchronizing all Facebook contacts...");
        }
        
        UserAppDao userAppDao = new UserAppDao();
        List<UserAppDto> userAppList = userAppDao.findByAppId(AppId.FACEBOOK.name());
        for (UserAppDto userApp : userAppList) {
            try {
                synchronizeContact(userApp.getAccessToken(), userApp.getUserId());
            } catch (Throwable t) {
                log.error(MessageFormat.format("Error synchronizing Facebook contacts for user {0}", userApp.getUserId()), t);
            }
        }
        
        if (log.isInfoEnabled()) {
            log.info("Synchronizing all Facebook contacts : done!");
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.DAYS);
    }

    public String getExtendedAccessToken(String accessToken) throws AuthenticationException {
        FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
        AccessToken extendedAccessToken = null;
        try {
            extendedAccessToken = facebookClient.obtainExtendedAccessToken(facebookAppId, facebookAppSecret, accessToken);

            if (log.isDebugEnabled()) {
                log.debug(MessageFormat.format("Got long lived session token {0} for token {1}", extendedAccessToken, accessToken));
            }
        } catch (FacebookException e) {
            if (e.getMessage().contains("Error validating access token")) {
                throw new AuthenticationException("Error validating access token");
            }
                
            throw new RuntimeException("Error exchanging short lived token for long lived token", e);
        }
        return extendedAccessToken.getAccessToken();
    }
    
    public void validatePermission(String accessToken) throws PermissionException {
        FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
        Connection<JsonObject> dataList = facebookClient.fetchConnection("me/permissions", JsonObject.class);
        boolean installed = false;
        boolean email = false;
        boolean publishStream = false;
        boolean readStream = false;
        for (JsonObject data : dataList.getData()) {
            if (data.optInt("installed") == 1) {
                installed = true;
            }
            if (data.optInt("email") == 1) {
                email = true;
            }
            if (data.optInt("publish_stream") == 1) {
                publishStream = true;
            }
            if (data.optInt("read_stream") == 1) {
                readStream = true;
            }
        }
        if (!installed) {
            throw new PermissionException("Permission not found: installed");
        }
        if (!email) {
            throw new PermissionException("Permission not found: email");
        }
        if (!publishStream) {
            throw new PermissionException("Permission not found: publish_stream");
        }
        if (!readStream) {
            throw new PermissionException("Permission not found: read_stream");
        }
    }
    
    /**
     * Synchronize user's contact.
     * 
     * @param accessToken Access token
     * @param userId User ID
     */
    public void synchronizeContact(String accessToken, String userId) {
        if (log.isDebugEnabled()) {
            log.debug(MessageFormat.format("Synchronizing Facebook contacts for user {0}", userId));
        }
        
        FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
        Connection<User> connection = null;
        connection = facebookClient.fetchConnection("me/friends", User.class);
        Map<String, String> newFriendMap = new HashMap<String, String>();
        for (List<User> friendList : connection) {
            for (User friend : friendList) {
                String friendName = friend.getName();
                String externalId = friend.getId();
                newFriendMap.put(externalId, friendName);
            }
        }
        
        // Load current contacts
        UserContactDao userContactDao = new UserContactDao();
        List<UserContactDto> currentUserContactList = userContactDao.findByUserIdAndAppId(userId, AppId.FACEBOOK.name());
        Set<String> currentFriendSet = new HashSet<String>();
        for (UserContactDto userContact : currentUserContactList) {
            currentFriendSet.add(userContact.getExternalId());
        }
        
        // Update contact updated date
        userContactDao.updateByUserIdAndAppId(userId, AppId.FACEBOOK.name());
        
        // Delete unfriended contacts
        for (UserContactDto userContact : currentUserContactList) {
            if (!newFriendMap.containsKey(userContact.getExternalId())) {
                userContactDao.delete(userContact.getId());
            }
        }
        
        // Add new contacts
        for (Entry<String, String> entry : newFriendMap.entrySet()) {
            if (!currentFriendSet.contains(entry.getKey())) {
                UserContact userContact = new UserContact();
                userContact.setAppId(AppId.FACEBOOK.name());
                userContact.setExternalId(entry.getKey());
                userContact.setFullName(StringUtils.mid(entry.getValue(), 0, 100));
                userContact.setUserId(userId);
                userContactDao.create(userContact);
            }
        }
    }

    /**
     * Updates user's personal informations.
     * 
     * @param accessToken Access token
     * @param userApp User app (updated by side effect)
     */
    public void updateUserData(String accessToken, UserApp userApp) {
        FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
        User user = facebookClient.fetchObject("me", User.class);
        userApp.setExternalId(user.getId());
        userApp.setEmail(user.getEmail());
        userApp.setUsername(user.getUsername());
    }
    public void publishAction(final UserBook userBook) {
    }
}

public abstract class BaseResource {
    @Context
    protected HttpServletRequest request;
    @QueryParam("app_key")
    protected String appKey;
    protected IPrincipal principal;
    protected boolean authenticate() {
        Principal principal = (Principal) request.getAttribute(TokenBasedSecurityFilter.PRINCIPAL_ATTRIBUTE);
        if (principal != null && principal instanceof IPrincipal) {
            this.principal = (IPrincipal) principal;
            return !this.principal.isAnonymous();
        } else {
            return false;
        }
    }
    protected void checkBaseFunction(BaseFunction baseFunction) throws JSONException {
        if (!hasBaseFunction(baseFunction)) {
            throw new ForbiddenClientException();
        }
    }
    
    protected boolean hasBaseFunction(BaseFunction baseFunction) throws JSONException {
        if (principal == null || !(principal instanceof UserPrincipal)) {
            return false;
        }
        Set<String> baseFunctionSet = ((UserPrincipal) principal).getBaseFunctionSet();
        return baseFunctionSet != null && baseFunctionSet.contains(baseFunction.name());
    }
}

@Path("/book")
public class BookResource extends BaseResource {
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("isbn") String isbn) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        ValidationUtil.validateRequired(isbn, "isbn");
        
        // Fetch the book
        BookDao bookDao = new BookDao();
        Book book = bookDao.getByIsbn(isbn);
        if (book == null) {
            // Try to get the book from a public API
            try {
                book = AppContext.getInstance().getBookDataService().searchBook(isbn);
            } catch (Exception e) {
                throw new ClientException("BookNotFound", e.getCause().getMessage(), e);
            }
            
            // Save the new book in database
            bookDao.create(book);
        }
        
        // Create the user book if needed
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getByBook(book.getId(), principal.getId());
        if (userBook == null) {
            userBook = new UserBook();
            userBook.setUserId(principal.getId());
            userBook.setBookId(book.getId());
            userBook.setCreateDate(new Date());
            userBookDao.create(userBook);
        } else {
            throw new ClientException("BookAlreadyAdded", "Book already added");
        }
        
        JSONObject response = new JSONObject();
        response.put("id", userBook.getId());
        return Response.ok().entity(response).build();
    }
    
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String userBookId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException("BookNotFound", "Book not found with id " + userBookId);
        }
        
        // Delete the user book
        userBookDao.delete(userBook.getId());
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
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
            @FormParam("tags") List<String> tagList) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
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
        
        // Check if this book is not already in database
        BookDao bookDao = new BookDao();
        Book bookIsbn10 = bookDao.getByIsbn(isbn10);
        Book bookIsbn13 = bookDao.getByIsbn(isbn13);
        if (bookIsbn10 != null || bookIsbn13 != null) {
            throw new ClientException("BookAlreadyAdded", "Book already added");
        }
        
        // Create the book
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
        
        // Create the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = new UserBook();
        userBook.setUserId(principal.getId());
        userBook.setBookId(book.getId());
        userBook.setCreateDate(new Date());
        userBookDao.create(userBook);
        
        // Update tags
        if (tagList != null) {
            TagDao tagDao = new TagDao();
            Set<String> tagSet = new HashSet<>();
            Set<String> tagIdSet = new HashSet<>();
            List<Tag> tagDbList = tagDao.getByUserId(principal.getId());
            for (Tag tagDb : tagDbList) {
                tagIdSet.add(tagDb.getId());
            }
            for (String tagId : tagList) {
                if (!tagIdSet.contains(tagId)) {
                    throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", tagId));
                }
                tagSet.add(tagId);
            }
            tagDao.updateTagList(userBook.getId(), tagSet);
        }
        
        // Returns the book ID
        JSONObject response = new JSONObject();
        response.put("id", userBook.getId());
        return Response.ok().entity(response).build();
    }

    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") String userBookId,
            @FormParam("title") String title,
            @FormParam("subtitle") String subtitle,
            @FormParam("author") String author,
            @FormParam("description") String description,
            @FormParam("isbn10") String isbn10,
            @FormParam("isbn13") String isbn13,
            @FormParam("page_count") Long pageCount,
            @FormParam("language") String language,
            @FormParam("publish_date") String publishDateStr,
            @FormParam("tags") List<String> tagList) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        title = ValidationUtil.validateLength(title, "title", 1, 255, true);
        subtitle = ValidationUtil.validateLength(subtitle, "subtitle", 1, 255, true);
        author = ValidationUtil.validateLength(author, "author", 1, 255, true);
        description = ValidationUtil.validateLength(description, "description", 1, 4000, true);
        isbn10 = ValidationUtil.validateLength(isbn10, "isbn10", 10, 10, true);
        isbn13 = ValidationUtil.validateLength(isbn13, "isbn13", 13, 13, true);
        language = ValidationUtil.validateLength(language, "language", 2, 2, true);
        Date publishDate = ValidationUtil.validateDate(publishDateStr, "publish_date", true);
        
        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        BookDao bookDao = new BookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException("BookNotFound", "Book not found with id " + userBookId);
        }
        
        // Get the book
        Book book = bookDao.getById(userBook.getBookId());
        
        // Check that new ISBN number are not already in database
        if (!Strings.isNullOrEmpty(isbn10) && book.getIsbn10() != null && !book.getIsbn10().equals(isbn10)) {
            Book bookIsbn10 = bookDao.getByIsbn(isbn10);
            if (bookIsbn10 != null) {
                throw new ClientException("BookAlreadyAdded", "Book already added");
            }
        }
        
        if (!Strings.isNullOrEmpty(isbn13) && book.getIsbn13() != null && !book.getIsbn13().equals(isbn13)) {
            Book bookIsbn13 = bookDao.getByIsbn(isbn13);
            if (bookIsbn13 != null) {
                throw new ClientException("BookAlreadyAdded", "Book already added");
            }
        }
        
        // Update the book
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
        
        // Update tags
        if (tagList != null) {
            TagDao tagDao = new TagDao();
            Set<String> tagSet = new HashSet<>();
            Set<String> tagIdSet = new HashSet<>();
            List<Tag> tagDbList = tagDao.getByUserId(principal.getId());
            for (Tag tagDb : tagDbList) {
                tagIdSet.add(tagDb.getId());
            }
            for (String tagId : tagList) {
                if (!tagIdSet.contains(tagId)) {
                    throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", tagId));
                }
                tagSet.add(tagId);
            }
            tagDao.updateTagList(userBookId, tagSet);
        }
        
        // Returns the book ID
        JSONObject response = new JSONObject();
        response.put("id", userBookId);
        return Response.ok().entity(response).build();
    }
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("id") String userBookId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Fetch the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException("BookNotFound", "Book not found with id " + userBookId);
        }
        
        // Fetch the book
        BookDao bookDao = new BookDao();
        Book bookDb = bookDao.getById(userBook.getBookId());
        
        // Return book data
        JSONObject book = new JSONObject();
        book.put("id", userBook.getId());
        book.put("title", bookDb.getTitle());
        book.put("subtitle", bookDb.getSubtitle());
        book.put("author", bookDb.getAuthor());
        book.put("page_count", bookDb.getPageCount());
        book.put("description", bookDb.getDescription());
        book.put("isbn10", bookDb.getIsbn10());
        book.put("isbn13", bookDb.getIsbn13());
        book.put("language", bookDb.getLanguage());
        if (bookDb.getPublishDate() != null) {
            book.put("publish_date", bookDb.getPublishDate().getTime());
        }
        book.put("create_date", userBook.getCreateDate().getTime());
        if (userBook.getReadDate() != null) {
            book.put("read_date", userBook.getReadDate().getTime());
        }
        
        // Add tags
        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.getByUserBookId(userBookId);
        List<JSONObject> tags = new ArrayList<>();
        for (TagDto tagDto : tagDtoList) {
            JSONObject tag = new JSONObject();
            tag.put("id", tagDto.getId());
            tag.put("name", tagDto.getName());
            tag.put("color", tagDto.getColor());
            tags.add(tag);
        }
        book.put("tags", tags);
        
        return Response.ok().entity(book).build();
    }
    @GET
    @Path("{id: [a-z0-9\\-]+}/cover")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response cover(
            @PathParam("id") final String userBookId) throws JSONException {
        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId);
        
        // Get the cover image
        File file = Paths.get(DirectoryUtil.getBookDirectory().getPath(), userBook.getBookId()).toFile();
        InputStream inputStream = null;
        try {
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = new FileInputStream(new File(getClass().getResource("/dummy.png").getFile()));
            }
        } catch (FileNotFoundException e) {
            throw new ServerException("FileNotFound", "Cover file not found", e);
        }

        return Response.ok(inputStream)
                .header("Content-Type", "image/jpeg")
                .header("Expires", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000))
                .build();
    }
    
    /**
     * Updates a book cover.
     * 
     * @param id User book ID
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/cover")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCover(
            @PathParam("id") String userBookId,
            @FormParam("url") String imageUrl) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException("BookNotFound", "Book not found with id " + userBookId);
        }
        
        // Get the book
        BookDao bookDao = new BookDao();
        Book book = bookDao.getById(userBook.getBookId());

        // Download the new cover
        try {
            AppContext.getInstance().getBookDataService().downloadThumbnail(book, imageUrl);
        } catch (Exception e) {
            throw new ClientException("DownloadCoverError", "Error downloading the cover image");
        }
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok(response).build();
    }
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search,
            @QueryParam("read") Boolean read,
            @QueryParam("tag") String tagName) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        JSONObject response = new JSONObject();
        List<JSONObject> books = new ArrayList<>();
        
        UserBookDao userBookDao = new UserBookDao();
        TagDao tagDao = new TagDao();
        PaginatedList<UserBookDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        UserBookCriteria criteria = new UserBookCriteria();
        criteria.setSearch(search);
        criteria.setRead(read);
        criteria.setUserId(principal.getId());
        if (!Strings.isNullOrEmpty(tagName)) {
            Tag tag = tagDao.getByName(principal.getId(), tagName);
            if (tag != null) {
                criteria.setTagIdList(Lists.newArrayList(tag.getId()));
            }
        }
        try {
            userBookDao.findByCriteria(paginatedList, criteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in books", e);
        }

        for (UserBookDto userBookDto : paginatedList.getResultList()) {
            JSONObject book = new JSONObject();
            book.put("id", userBookDto.getId());
            book.put("title", userBookDto.getTitle());
            book.put("subtitle", userBookDto.getSubtitle());
            book.put("author", userBookDto.getAuthor());
            book.put("language", userBookDto.getLanguage());
            book.put("publish_date", userBookDto.getPublishTimestamp());
            book.put("create_date", userBookDto.getCreateTimestamp());
            book.put("read_date", userBookDto.getReadTimestamp());
            
            // Get tags
            List<TagDto> tagDtoList = tagDao.getByUserBookId(userBookDto.getId());
            List<JSONObject> tags = new ArrayList<>();
            for (TagDto tagDto : tagDtoList) {
                JSONObject tag = new JSONObject();
                tag.put("id", tagDto.getId());
                tag.put("name", tagDto.getName());
                tag.put("color", tagDto.getColor());
                tags.add(tag);
            }
            book.put("tags", tags);
            
            books.add(book);
        }
        response.put("total", paginatedList.getResultCount());
        response.put("books", books);
        
        return Response.ok().entity(response).build();
    }
    @PUT
    @Consumes("multipart/form-data") 
    @Path("import")
    public Response importFile(
            @FormDataParam("file") FormDataBodyPart fileBodyPart) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        ValidationUtil.validateRequired(fileBodyPart, "file");

        UserDao userDao = new UserDao();
        User user = userDao.getById(principal.getId());
        
        InputStream in = fileBodyPart.getValueAs(InputStream.class);
        File importFile = null;
        try {
            // Copy the incoming stream content into a temporary file
            importFile = File.createTempFile("books_import", null);
            IOUtils.copy(in, new FileOutputStream(importFile));
            
            BookImportedEvent event = new BookImportedEvent();
            event.setUser(user);
            event.setImportFile(importFile);
            AppContext.getInstance().getImportEventBus().post(event);
            
            // Always return ok
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            return Response.ok().entity(response).build();
        } catch (Exception e) {
            if (importFile != null) {
                try {
                    importFile.delete();
                } catch (SecurityException e2) {
                    // NOP
                }
            }
            throw new ServerException("ImportError", "Error importing books", e);
        }
    }
    @POST
    @Path("{id: [a-z0-9\\-]+}/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(
            @PathParam("id") final String userBookId,
            @FormParam("read") boolean read) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        
        // Update the read date
        userBook.setReadDate(read ? new Date() : null);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}

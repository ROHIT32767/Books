package com.sismics.books.core.model.context;

import com.sismics.books.core.service.BookDataService;
import com.sismics.books.core.service.FacebookService;

public class ServiceManager {
    private BookDataService bookDataService;
    private FacebookService facebookService;

    public ServiceManager() {
        initializeServices();
    }

    private void initializeServices() {
        bookDataService = new BookDataService();
        bookDataService.startAndWait();

        facebookService = new FacebookService();
        facebookService.startAndWait();
    }

    public BookDataService getBookDataService() {
        return bookDataService;
    }

    public FacebookService getFacebookService() {
        return facebookService;
    }
}

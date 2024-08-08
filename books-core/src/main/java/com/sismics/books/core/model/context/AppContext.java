package com.sismics.books.core.model.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.books.core.listener.async.BookImportAsyncListener;
import com.sismics.books.core.listener.async.UserAppCreatedAsyncListener;
import com.sismics.books.core.listener.sync.DeadEventListener;
import com.sismics.books.core.service.BookDataService;
import com.sismics.books.core.service.FacebookService;
import com.sismics.util.EnvironmentUtil;

/**
 * Global application context.
 *
 * @author jtremeaux 
 */
public class AppContext {
    private static AppContext instance;

    private ServiceManager serviceManager;
    private EventBusManager eventBusManager;

    private AppContext() {
        serviceManager = new ServiceManager();
        eventBusManager = new EventBusManager();
    }

    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public EventBus getEventBus() {
        return eventBusManager.getEventBus();
    }

    public EventBus getAsyncEventBus() {
        return eventBusManager.getAsyncEventBus();
    }

    public EventBus getImportEventBus() {
        return eventBusManager.getImportEventBus();
    }

    public BookDataService getBookDataService() {
        return serviceManager.getBookDataService();
    }

    public FacebookService getFacebookService() {
        return serviceManager.getFacebookService();
    }
}

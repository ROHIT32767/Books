package com.sismics.books.core.model.context;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.books.core.listener.async.BookImportAsyncListener;
import com.sismics.books.core.listener.async.UserAppCreatedAsyncListener;
import com.sismics.books.core.listener.sync.DeadEventListener;
import com.sismics.util.EnvironmentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventBusManager {
    private EventBus eventBus;
    private EventBus asyncEventBus;
    private EventBus importEventBus;
    private List<ExecutorService> asyncExecutorList = new ArrayList<>();

    public EventBusManager() {
        resetEventBus();
    }

    private void resetEventBus() {
        eventBus = new EventBus();
        eventBus.register(new DeadEventListener());
        
        asyncEventBus = createAsyncEventBus();
        asyncEventBus.register(new UserAppCreatedAsyncListener());
        
        importEventBus = createAsyncEventBus();
        importEventBus.register(new BookImportAsyncListener());
    }

    private EventBus createAsyncEventBus() {
        if (EnvironmentUtil.isUnitTest()) {
            return new EventBus();
        } else {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            asyncExecutorList.add(executor);
            return new AsyncEventBus(executor);
        }
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public EventBus getAsyncEventBus() {
        return asyncEventBus;
    }

    public EventBus getImportEventBus() {
        return importEventBus;
    }
}

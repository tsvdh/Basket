package util;

import basket.api.util.Threads;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Getter;

public class ThreadHandler {

    @Getter
    private static final ExecutorService executorService;

    private ThreadHandler() {}

    static {
        executorService = Executors.newCachedThreadPool();

        Threads.addExecutorShutdownHookOnJavaFXApplicationClose(executorService);
    }

    public static void execute(Runnable runnable) {
        executorService.execute(runnable);
    }
}


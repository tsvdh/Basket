package util;

import basket.api.util.Threads;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.Getter;

public class ThreadHandler {

    @Getter
    private static final ScheduledExecutorService executorService;

    private ThreadHandler() {}

    static {
        executorService = Executors.newScheduledThreadPool(1);

        Threads.addExecutorShutdownHookOnJavaFXApplicationClose(executorService);
    }

    public static void execute(Runnable runnable) {
        executorService.execute(runnable);
    }
}


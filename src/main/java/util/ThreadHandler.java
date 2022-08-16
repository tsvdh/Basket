package util;

import basket.api.util.Threads;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javafx.concurrent.Task;
import lombok.Getter;

public class ThreadHandler {

    public static class PrintingExecutor extends ScheduledThreadPoolExecutor {

        public PrintingExecutor(int corePoolSize) {
            super(corePoolSize);
        }

        @Override
        protected void afterExecute(Runnable runnable, Throwable throwable) {
            super.afterExecute(runnable, throwable);

            if (throwable == null && runnable instanceof Future<?> future && future.isDone()) {
                try {
                    future.get();
                } catch (CancellationException e) {
                    throwable = e;
                } catch (ExecutionException e) {
                    throwable = e.getCause();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (throwable != null) {
                System.err.print("Exception in thread '");
                System.err.print(Thread.currentThread().getName());
                System.err.print("': ");
                throwable.printStackTrace();
            }
        }
    }

    @Getter
    private static final ScheduledExecutorService executorService;

    private ThreadHandler() {}

    static {
        executorService = new PrintingExecutor(10);

        Threads.addExecutorShutdownHookOnJavaFXApplicationClose(executorService);
    }

    public static void execute(Runnable runnable) {
        if (runnable instanceof Task<?> task) {
            task.exceptionProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    System.err.printf("Exception in thread '%s': ", Thread.currentThread().getName());
                    newValue.printStackTrace();
                }
            });
        }

        executorService.execute(runnable);
    }
}


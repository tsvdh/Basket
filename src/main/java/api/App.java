package api;

public abstract class App {

    public abstract void start();

    public static void run() {
        // TODO
        // perform checks to ensure state is as ensured:
        // - settings file exists

        // determine calling class
        String callingClassName = null;
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            StackTraceElement stackTraceElement = stack[i];

            String className = stackTraceElement.getClassName();
            String methodName = stackTraceElement.getMethodName();
            // if at current method, go down one more for calling class
            if (className.equals(App.class.getName()) && methodName.equals("run")) {
                callingClassName = stack[i + 1].getClassName();
                break;
            }
        }

        if (callingClassName == null) {
            throw new RuntimeException("Can't find calling class");
        }

        try {
            Class<?> callingClass = Class.forName(callingClassName);
            if (callingClass.getSuperclass() == App.class) {
                App app = (App) callingClass.getConstructor().newInstance();
                app.start();
            }
            else {
                throw new IllegalStateException(callingClass.getName() + " must extend " + App.class.getName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

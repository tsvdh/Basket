package update;

import common.FileHandler;
import common.popups.Message;
import javafx.application.Application;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Launcher {

    public static void boot(Application.Parameters parameters) {
        if (!FileHandler.inJar()) {
            return;
        }

        List<String> unnamed = parameters.getUnnamed();

        boolean switching = unnamed.contains("-switch");
        Updater updater = new Updater(switching);

        Map<String, String> named = parameters.getNamed();
        String deleteKey = "delete";

        if (named.containsKey(deleteKey)) {
            updater.delete(named.get(deleteKey));
        }
        else {
            updater.downloadNew();

            if (updater.isDownloadAttempt()) {
                if (updater.isSuccess()) {

                    updater.updateShortcut();

                    String oldPath = FileHandler.getCurrentFile().getPath();
                    String newPath = updater.getNewFile().getPath();

                    String deleteArg = "--delete=" + oldPath;
                    try {
                        new ProcessBuilder(newPath, deleteArg).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                } else {
                    new Message("Download failed", true);
                }
            }
        }
    }

    public static void reboot(String... args) {
        String[] newArgs = new String[args.length + 1];

        newArgs[0] = FileHandler.getCurrentFile().getPath();

        System.arraycopy(args, 0, newArgs, 1, args.length);

        try {
            new ProcessBuilder(newArgs).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}

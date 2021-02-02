package update;

import common.FileHandler;
import common.SettingsHandler;
import common.options.BuildVersion;
import common.popups.Message;
import mslinks.ShellLink;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Updater {

    enum Mode {
        INSTALLING,
        UPDATING,
        SWITCHING
    }

    private static final String NAME_START = "Sudoku-";
    private static final String NAME_INSTALLER = "installer";
    private static final String SHORTCUT_NAME = "Sudoku solver.lnk";

    private boolean downloadAttempt;
    private boolean success;
    private String currentName;
    private File newFile;
    private Mode mode;

    Updater(boolean switching) {
        if (!FileHandler.inJar()) {
            throw new IllegalStateException("Can only update when running in jar");
        }

        this.success = false;
        this.downloadAttempt = false;

        currentName = FileHandler.getCurrentFile().getName();

        if (switching) {
            this.mode = Mode.SWITCHING;
        } else if (currentName.contains(NAME_INSTALLER)) {
            this.mode = Mode.INSTALLING;
        } else {
            this.mode = Mode.UPDATING;
        }
    }

    boolean isSuccess() {
        return success;
    }

    void setSuccess(boolean success) {
        this.success = success;
    }

    boolean isDownloadAttempt() {
        return downloadAttempt;
    }

    String getCurrentName() {
        return currentName;
    }

    File getNewFile() {
        return newFile;
    }

    Mode getMode() {
        return mode;
    }

    private static int[] getVersionAsArray(String name) {
        if (!name.contains(NAME_START)) {
            throw new IllegalArgumentException("File name must start with '" + NAME_START + "'");
        }

        String[] version = name.replace(NAME_START, "").split("\\.");

        int[] versionNumber = new int[3];

        try {
            for (int i = 0; i < 3; i++) {
                versionNumber[i] = Integer.parseInt(version[i]);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw new IllegalArgumentException("Name must contain three consecutive numbers separated by dots");
        }

        return versionNumber;
    }

    public static String getVersionAsString(String name) {
        int[] versionArray = getVersionAsArray(name);
        return versionArray[0] + "." + versionArray[1] + "." + versionArray[2];
    }

    private boolean shouldUpdate(String newName) {
        if (mode == Mode.INSTALLING) {
            return true;
        }

        int[] oldVersion = getVersionAsArray(currentName);
        int[] newVersion = getVersionAsArray(newName);

        for (int i = 0; i < 3; i++) {
            int oldPart = oldVersion[i];
            int newPart = newVersion[i];

            if (mode == Mode.UPDATING) {
                if (newPart > oldPart) {
                    return true;
                }
                if (newPart < oldPart) {
                    return false;
                }
            }
            if (mode == Mode.SWITCHING) {
                if (newPart != oldPart) {
                    return true;
                }
            }
        }

        // versions are equal
        return false;
    }

    void downloadNew() {
        DropBoxHandler dropBoxHandler = DropBoxHandler.getInstance();

        BuildVersion version;
        if (mode == Mode.INSTALLING) {
            version = BuildVersion.STABLE;
        } else {
            version = SettingsHandler.getInstance().getBuildVersion();
        }

        Optional<String> optionalFileName = dropBoxHandler.getFileName(version);

        if (!optionalFileName.isPresent()) {
            System.out.println("No file available for download");
            return;
        }

        String fileName = optionalFileName.get();

        if (shouldUpdate(fileName)) {
            downloadAttempt = true;

            try {
                newFile = FileHandler.getExternalFileInProgramFolder(fileName);
            } catch (IOException e) {
                success = false;
                return;
            }

            // sets 'this.success'
            dropBoxHandler.downloadToFile(newFile, this);
        }
        else {
            System.out.println("No update available");
        }
    }

    private File getShortcut() {
        File desktop = FileHandler.getDesktop();
        String shortcutPath = desktop.getPath() + "/" + SHORTCUT_NAME;
        return new File(shortcutPath);
    }

    void delete(String path) {
        File file = new File(path);
        if (!file.delete()) {
            new Message("Could not delete old file at: " + file.getPath(), true);
        }
    }

    void updateShortcut() {
        File shortcut = getShortcut();
        String shortcutPath = shortcut.getPath();
        String newPath = newFile.getPath();

        if (mode != Mode.INSTALLING) {
            if (!shortcut.delete()) {
                new Message("Could not delete old shortcut", true);
                return;
            }
        }

        try {
            ShellLink.createLink(newPath, shortcutPath);
        } catch (IOException e) {
            new Message("Could not create shortcut", true);
        }
    }
}

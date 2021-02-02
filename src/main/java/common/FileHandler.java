package common;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.WinDef;
import update.Updater;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Observable;

public class FileHandler extends Observable {

    // 128 KiB
    private static final int BUFFER_SIZE = 128 << 10;

    private static final String PROGRAM_FOLDER = "/Sudoku solver/";

    public static File getExternalFileInProgramFolder(String fileName) throws IOException {
        String userHome = System.getProperty("user.home");
        File userHomeFile = new File(userHome);
        String externalPath = userHomeFile.getPath() + PROGRAM_FOLDER + fileName;

        File file = new File(externalPath);
        File parent = file.getParentFile();

        if (userHomeFile.equals(parent)) {
            throw new IllegalArgumentException("Can only put folders in user home");
        }

        // If the folder does not exist, try to create it.
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create " + externalPath);
        }
        else {
            return file;
        }
    }

    public static boolean installed() {
        String userHome = System.getProperty("user.home");
        File userHomeFile = new File(userHome);

        String programPath = userHomeFile.getPath() + PROGRAM_FOLDER;
        File programFolder = new File(programPath);

        if (!programFolder.exists() || !programFolder.isDirectory()) {
            return false;
        }

        FileFilter fileFilter = file -> file.getPath().contains("Sudoku") && file.canExecute();

        File[] files = programFolder.listFiles(fileFilter);

        return files.length > 0;
    }

    public static File getCurrentFile() {
        URL url = FileHandler.class.getProtectionDomain().getCodeSource().getLocation();
        String path = null;
        try {
            path = url.toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assert path != null;

        return new File(path);
    }

    public static File getLocalFile(String fileName) {
        File file = getCurrentFile();

        String joinedPath = file.getParent() + "/" + fileName;
        return new File(joinedPath);
    }

    public static void writeToFile(InputStream inputStream, File file) throws IOException {
        new FileHandler(file, inputStream).writeToFile();
    }

    public static File getDesktop() {
        char[] pszPath = new char[WinDef.MAX_PATH];
        Shell32.INSTANCE.SHGetFolderPath(null, ShlObj.CSIDL_DESKTOPDIRECTORY, null, ShlObj.SHGFP_TYPE_CURRENT, pszPath);

        return new File(Native.toString(pszPath));
    }

    public static String getVersion() {
        if (inJar()) {
            return Updater.getVersionAsString(getCurrentFile().getName());
        } else {
            return "?";
        }
    }

    private enum Protocol {
        FILE,
        JAR
    }

    private static final Protocol protocol = Protocol.JAR;

    private static Protocol getProtocol() {
        return Protocol.valueOf(FileHandler.class.getResource("").getProtocol().toUpperCase());
    }

    public static boolean inJar() {
        return protocol == Protocol.JAR;
    }

    private File file;
    private InputStream inputStream;

    public FileHandler(File file, InputStream inputStream) {
        this.file = file;
        this.inputStream = inputStream;
    }

    public void writeToFile() throws IOException {
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
        } catch (IOException e) {
            inputStream.close();
            System.out.println(e.getMessage());
            return;
        }

        byte[] buffer = new byte[BUFFER_SIZE];

        long bytesWritten = 0;
        int amountRead;
        while ((amountRead = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, amountRead);

            bytesWritten += amountRead;

            // This code only serves a purpose if this class is being observed
            setChanged();
            notifyObservers(bytesWritten);
        }

        inputStream.close();
        outputStream.close();
    }
}

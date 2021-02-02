package update;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Launcher.boot(getParameters());
        //
        // new Sudoku(primaryStage);

        char[] pszPath = new char[WinDef.MAX_PATH];
        Shell32.INSTANCE.SHGetFolderPath(null, ShlObj.CSIDL_DESKTOPDIRECTORY, null, ShlObj.SHGFP_TYPE_CURRENT, pszPath);
        System.out.println(Native.toString(pszPath));
        Shell32.INSTANCE.SHGetFolderPath(null, ShlObj.CSIDL_APPDATA, null, ShlObj.SHGFP_TYPE_CURRENT, pszPath);
        System.out.println(Native.toString(pszPath));
        Shell32.INSTANCE.SHGetFolderPath(null, ShlObj.CSIDL_LOCAL_APPDATA, null, ShlObj.SHGFP_TYPE_CURRENT, pszPath);
        System.out.println(Native.toString(pszPath));
        Shell32.INSTANCE.SHGetFolderPath(null, ShlObj.CSIDL_PROGRAM_FILES, null, ShlObj.SHGFP_TYPE_CURRENT, pszPath);
        System.out.println(Native.toString(pszPath));
        Shell32.INSTANCE.SHGetFolderPath(null, ShlObj.CSIDL_STARTUP, null, ShlObj.SHGFP_TYPE_CURRENT, pszPath);
        System.out.println(Native.toString(pszPath));
        System.out.println(System.getProperty("user.home"));

        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

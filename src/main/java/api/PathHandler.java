package api;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.WinDef;

public class PathHandler {

    private static String getPath(int location) {
        char[] pszPath = new char[WinDef.MAX_PATH];
        Shell32.INSTANCE.SHGetFolderPath(null, location, null, ShlObj.SHGFP_TYPE_CURRENT, pszPath);
        return Native.toString(pszPath);
    }

    public static String getAppdataPath() {
        return getPath(ShlObj.CSIDL_APPDATA) + "/" + FileHandler.launcherName;
    }

    public static String getProgramFilesPath() {
        return getPath(ShlObj.CSIDL_PROGRAM_FILES) + "/" + FileHandler.launcherName;
    }

    public static String getDesktopPath() {
        return getPath(ShlObj.CSIDL_DESKTOP);
    }

    public static String getStartupPath() {
        return getPath(ShlObj.CSIDL_STARTUP);
    }

    public static String getSettingsFilePath() {
        String appName = PropertiesHandler.getInstance().getAppName();
        return PathHandler.getAppdataPath() + "/" + appName + "settings.properties";
    }
}

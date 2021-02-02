package update;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.files.FileMetadata;
import common.FileHandler;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

class DownloadTask extends Task<Void> implements Observer {

    private long totalBytes;
    private FileHandler fileHandler;

    DownloadTask(DbxDownloader<FileMetadata> downloader, File file) {
        this.totalBytes = downloader.getResult().getSize();
        this.fileHandler = new FileHandler(file, downloader.getInputStream());
        fileHandler.addObserver(this);
    }

    @Override
    protected Void call() throws IOException {
        fileHandler.writeToFile();
        return null;
    }

    private static float toMB(long bytes) {
        double MB = 1 << 10 << 10;

        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        double megaBytes = bytes / MB;

        return Float.parseFloat(decimalFormat.format(megaBytes));
    }

    private void updateProgress(long bytesWritten) {
        updateProgress(bytesWritten, totalBytes);
        updateMessage(toMB(bytesWritten) + " / " + toMB(totalBytes) + " MB");
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof FileHandler) {
            updateProgress((long) arg);
        }
    }
}

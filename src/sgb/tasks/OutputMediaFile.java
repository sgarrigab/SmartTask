package sgb.tasks;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import java.io.File;

/**
 * Created by julia on 03/08/16.
 */
public class OutputMediaFile {

    private Activity activity;

    public OutputMediaFile(Activity activity) {

        this.activity = activity;
    }

    /** Create a file Uri for saving an image or video */
    public Uri getOutputMediaFileUri(String filename) {
        return Uri.fromFile(getOutputMediaFile(filename));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(String filename) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.


        File mediaStorageDir;
        mediaStorageDir = new File(Utilitats.getWorkFolder(activity, Utilitats.IMAGES), "");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Gestvia", "failed to create directory");
                return null;
            }
        }

        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + filename +
                    ".png");

        return mediaFile;
    }
}

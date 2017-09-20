package sgb.tasks;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Signatura_julia extends Activity {

    private Button okButton, adjustButton, cancelButton, loadButton;
    private String path1, path2;
    private MyDrawView myDrawView;
    private LinearLayout imagesLayout;
    private boolean sign = false;
    private Bitmap signature;
    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signatura_julia);

        filename = "prova";

        path1 = Utilitats.getWorkFolder(this, Utilitats.IMAGES) + File.separator + filename + ".png"; //TODO: REBRE COM A PARAMETRES
        path2 = Utilitats.getWorkFolder(this, Utilitats.IMAGES) + "/prova2.png";

        imagesLayout = (LinearLayout) findViewById(R.id.imagesLayout);

        setButtons();

        setImage();
    }

    private void setButtons() {

        okButton = (Button) findViewById(R.id.okButton);
        adjustButton = (Button) findViewById(R.id.adjustButton);
        loadButton = (Button) findViewById(R.id.loadButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDrawView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(myDrawView.getDrawingCache(true));

                OutputMediaFile outputMediaFile;
                outputMediaFile = new OutputMediaFile(getParent());
                Uri uri = outputMediaFile.getOutputMediaFileUri(filename);

                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, new FileOutputStream(new File(uri.getPath())));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(path1);
                file.delete();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        adjustButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!sign) {
                    sign = true;
                    adjustButton.setVisibility(View.GONE);
                    Matrix matrix = new Matrix();
                    myDrawView.getAttacher().getSuppMatrix(matrix);
                    myDrawView.setZoomable(false);
                    myDrawView.setTouchable(true);
                    myDrawView.getAttacher().setDisplayMatrix(matrix);
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void setImage() {

        myDrawView = (MyDrawView) findViewById(R.id.signature_image);

        File file = new File(path1);
        if(file.exists()) {
            signature = Bitmap.createBitmap(BitmapFactory.decodeFile(path1));
            try {
                signature.compress(Bitmap.CompressFormat.PNG, 95, new FileOutputStream(path1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            file = new File(path2);
            if(file.exists()) {
                signature = Bitmap.createBitmap(BitmapFactory.decodeFile(path2));
                try {
                    signature.compress(Bitmap.CompressFormat.PNG, 95, new FileOutputStream(path2));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                signature = null;
            }

        }

        myDrawView.setImageBitmap(signature);
    }
}

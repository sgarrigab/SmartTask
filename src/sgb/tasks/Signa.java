package sgb.tasks;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;


public class Signa extends Activity
{
    MyDrawView myDrawView;
    String wdocument;
    OrdersHelper helper;
    File SignFile;
    String NomSignFile;
    private SGEdit edAgents;
    Boolean swExist=false;
    int document;



    public void clearCanvas(View v) {
        myDrawView.clear();
    }

    public void saveCanvas(View v) {
        boolean success = false;

        String wagents = edAgents.getText().toString();

        if (swExist)
            helper.getWritableDatabase().execSQL("update Cap set agents='" + wagents + "' where _id = " + document);


        if ( !SignFile.exists() )
        {
            try {
                success = SignFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




        FileOutputStream ostream = null;
        try
        {
            ostream = new FileOutputStream(SignFile);

            System.out.println(ostream);
            View targetView = myDrawView;

            Bitmap well = myDrawView.getBitmap();
            Bitmap save = Bitmap.createBitmap(320, 480, Config.ARGB_8888);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            Canvas now = new Canvas(save);
            now.drawRect(new Rect(0,0,320,480), paint);
            now.drawBitmap(well, new Rect(0,0,well.getWidth(),well.getHeight()), new Rect(0,0,320,480), null);

            if(save == null) {
                System.out.println("NULL bitmap save\n");
            }
            save.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            finish();
        }catch (NullPointerException e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Null error", Toast.LENGTH_SHORT).show();
        }

        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "File error", Toast.LENGTH_SHORT).show();
        }

        catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "IO error", Toast.LENGTH_SHORT).show();
        }

    }



        @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        helper = new OrdersHelper(this);

        super.onCreate(savedInstanceState);
        // myDrawView = new MyDrawView(this, null);
        setContentView(R.layout.signatura);
        myDrawView = (MyDrawView)findViewById(R.id.signature_canvas);

        edAgents = (SGEdit) findViewById(R.id.agents);



        Bundle extras = getIntent().getExtras();
        int position = 0;
        if (extras != null) {
            wdocument = extras.getString("document");
            DecimalFormat formatter = new DecimalFormat("00000000_");
            document = Integer.parseInt(wdocument);
            wdocument = formatter.format(document);
        }

        NomSignFile = Utilitats.getWorkFolder(Signa.this, Utilitats.FOTOS).getAbsolutePath()+"/_"+wdocument + "_sgn.jpg";
        SignFile = new File(NomSignFile);

        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        final Bitmap b = BitmapFactory.decodeFile(NomSignFile,
                options);
        if (b!=null)
            myDrawView.setImageBitmap(b);

        String sql = "select agents from Cap where _id = "+document;
        Cursor ctr = helper.execSQL(sql);
        if (ctr.moveToNext()) {
            int i = ctr.getColumnIndex("agents");
            String ss = ctr.getString(i);
            edAgents.setText(ctr.getString(ctr.getColumnIndex("agents")));
            swExist=true;
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
      //  getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
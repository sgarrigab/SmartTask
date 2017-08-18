package sgb.tasks;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

public class Signatura extends Activity {

	private CanvasView customCanvas;
	private OrdersHelper helper;
	private SGEdit edAgents;
	private boolean swExist = false;
	private String imageFileName;
	private long document;
	private String wdocument;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		helper = new OrdersHelper(this);

		Bundle extras = getIntent().getExtras();
		int position = 0;
		if (extras != null) {
			wdocument = extras.getString("document");
			DecimalFormat formatter = new DecimalFormat("00000000_");
			document = Integer.parseInt(wdocument);
			wdocument = formatter.format(document);
		}

		imageFileName = Utilitats.getWorkFolder(this, Utilitats.FOTOS).getAbsolutePath()+"/"+wdocument + "_sgn.jpg";
		setContentView(R.layout.signatura);

		customCanvas = (CanvasView) findViewById(R.id.signature_canvas);
		edAgents = (SGEdit) findViewById(R.id.agents);


		Bitmap mBitmap = BitmapFactory.decodeFile(imageFileName);
		if (mBitmap != null) {
			Bitmap mutableBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
			Canvas myCanvas = new Canvas(mutableBitmap);
			customCanvas.draw(myCanvas);
		}

		String sql = "select agents from Cap where _id = "+document;
		Cursor ctr = helper.execSQL(sql);
		if (ctr.moveToNext()) {
			int i = ctr.getColumnIndex("agents");
			String ss = ctr.getString(i);
			edAgents.setText(ctr.getString(ctr.getColumnIndex("agents")));
			swExist=true;
		}
	}


	public void clearCanvas(View v) {
		Canvas canvas = null;
		FileOutputStream fos = null;
		Bitmap bmpBase = null;

		bmpBase = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bmpBase);
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		canvas.drawCircle(50, 50, 30, paint);

		canvas.clipRect(10,10,30,30);
		canvas.drawARGB(0,255,0,0);
// draw what ever you want canvas	q.draw...

// Save Bitmap to File
		try
		{
			String 		imageFileName = Utilitats.getWorkFolder(this, Utilitats.FOTOS).getAbsolutePath()+"/"+wdocument + "_sgn.jpg";

			fos = new FileOutputStream(imageFileName);
			bmpBase.compress(Bitmap.CompressFormat.PNG, 100, fos);

			fos.flush();
			fos.close();
			fos = null;
			finish();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
					fos = null;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}





		customCanvas.netejaCanvas();
	}

	public void saveCanvas(View v) {
		customCanvas.saveCanvas(imageFileName);

/*		Bitmap  bitmap = Bitmap.createBitmap( customCanvas.getCanvas().getWidth(), customCanvas.getCanvas().getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		customCanvas.draw(canvas);

		OutputStream outStream = null;
		try {
			outStream = new FileOutputStream(imageFileName);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.flush();
			outStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String wagents = edAgents.getText().toString();
		if (swExist)
			helper.execSQL("update Cap set agents='" + wagents + "' where _id = " + document);


//		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);  */
		finish();
	}
	
}
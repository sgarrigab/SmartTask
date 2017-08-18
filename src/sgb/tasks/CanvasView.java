package sgb.tasks;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CanvasView extends View {

	public int width;
	public int height;
	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Path mPath;
	Context context;
	private Paint mPaint;
	private float mX, mY;
	private static final float TOLERANCE = 5;

	public CanvasView(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = c;

		// we set a new Path
		mPath = new Path();

		// and we set a new Paint with the desired attributes
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeWidth(4f);
	}

	// override onSizeChanged
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// your Canvas will draw onto the defined Bitmap
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
	}


	void netejaCanvas() {
		mCanvas.drawColor(Color.WHITE);
		clearCanvas();
	}


	void saveCanvas(String imageFileName) {


		Paint paint = new Paint();
		paint.setColor(Color.YELLOW);
		mCanvas.drawCircle(50, 50, 30, paint);


		OutputStream outStream = null;
	try

	{
		outStream = new FileOutputStream(imageFileName);
		mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
		outStream.flush();
		outStream.close();
	}

	catch(
	FileNotFoundException e
	)

	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	catch(
	IOException e
	)

	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

}


	// override onDraw
		
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// draw the mPath with the mPaint on the canvas when onDraw
		canvas.drawPath(mPath, mPaint);
	}

	// when ACTION_DOWN start touch according to the x,y values
	private void startTouch(float x, float y) {
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	// when ACTION_MOVE move touch according to the x,y values
	private void moveTouch(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOLERANCE || dy >= TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	public Canvas getCanvas()
	{
		return mCanvas;
	}

	public void clearCanvas() {
		mPath.reset();
		invalidate();
	}

	// when ACTION_UP stop touch
	private void upTouch() {
		mPath.lineTo(mX, mY);
	}

	//override the onTouchEvent
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startTouch(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			moveTouch(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			upTouch();
			invalidate();
			break;
		}
		return true;
	}
}

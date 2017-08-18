package sgb.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class GaleriaImatges extends Activity {

	static final int REQUEST_IMAGE_CAPTURE = 1;
	Gallery gallery;
	GalleryImageAdapter imgAdapter;
	File files[];

	private int Document;
	private Bitmap bitmap;
	private Zoom imageView;
	ImageView selectedImage;
	private String document;

	FilenameFilter DocFilter = new FilenameFilter() {
		public boolean accept(File file, String name) {
			if (name.startsWith(document)) {
				return true;
			} else {
				return false;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.galleryimage);
		Bundle extras = getIntent().getExtras();
		int position = 0;
		if (extras != null) {
			document = extras.getString("document");
//			DecimalFormat formatter = new DecimalFormat("_00000000_");
//			document = formatter.format(Integer.parseInt(document));
		}

		gallery = (Gallery) findViewById(R.id.gallery1);

		selectedImage = (ImageView) findViewById(R.id.imageView1);

		File f = Utilitats.getWorkFolder(this, Utilitats.FOTOS);
		gallery.setSpacing(40);
		files = f.listFiles(DocFilter);
		imgAdapter = new GalleryImageAdapter(this, files);

		gallery.setAdapter(imgAdapter);

		// clicklistener for Gallery
		gallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				String f = files[position].getAbsolutePath();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 10;
				Bitmap b = BitmapFactory.decodeFile(f, options);
/*				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				Bitmap bm = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), matrix, true); */
				selectedImage.setImageBitmap(b);
			}
		});

	}

	String mCurrentPhotoPath;


	private File getTmpImageFile() throws IOException {
		// Create an image file name
		String imageFileName = "tmp.jpg";
		File storageDir = Utilitats.getWorkFolder(this, Utilitats.FOTOS);
		return new File(storageDir,"tmp.jpg");
	}


	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = document + /*timeStamp + */ "_pic";
		File storageDir = Utilitats.getWorkFolder(this, Utilitats.FOTOS);

		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				storageDir /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		return image;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE
				&& resultCode == Activity.RESULT_OK) {

			if (bitmap != null) {
				bitmap.recycle();
			}

			OutputStream outStream = null;


			try {
				Bitmap captureBmp = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(getTmpImageFile()));
				File photoFile = createImageFile();
				outStream = new FileOutputStream(photoFile);
				captureBmp.compress(Bitmap.CompressFormat.JPEG, 20, outStream);
				outStream.flush();
				outStream.close();

				File f = Utilitats.getWorkFolder(this, Utilitats.FOTOS);
				files = f.listFiles(DocFilter);
				gallery.setAdapter(new GalleryImageAdapter(this, files));
				takeFoto();  // Pren un altre foto.

			} catch (Exception e) {
				Utilitats.ShowModal(this, e.getMessage());

			}

		}
		super.onActivityResult(requestCode, resultCode, data);

	}

	public void btCamera(View v) {
		if (v != null) {
			takeFoto();
		}
	}



public void takeFoto() {

			Intent takePictureIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			// Ensure that there's a camera activity to handle the intent
			if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
				try {
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTmpImageFile()) );
				} catch (IOException e) {
					e.printStackTrace();
				}
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			}
		}
	}



class GalleryImageAdapter extends BaseAdapter {
	private Context mContext;
	File files[];
	private Activity act;

	public GalleryImageAdapter(Activity act, File fil[]) {
		files = fil;
		mContext = act;

	}

	public int getCount() {
		return files.length;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	// Override this method according to your need
	public View getView(int index, View view, ViewGroup viewGroup) {
		// TODO Auto-generated method stub
		ImageView i = new ImageView(mContext);
		String f = files[index].getAbsolutePath();

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 10;

		Bitmap b = BitmapFactory.decodeFile(f, options);
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		Bitmap bm = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(),
				matrix, true);

		i.setImageBitmap(bm);
		i.setLayoutParams(new Gallery.LayoutParams(400, 400));

		i.setScaleType(ImageView.ScaleType.FIT_XY);

		return i;
	}
}
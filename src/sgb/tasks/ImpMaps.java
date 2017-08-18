package sgb.tasks;

import it.sauronsoftware.ftp4j.FTPClient;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;

public class ImpMaps extends Activity {

	MapTables mapTables = new MapTables();

	String perSD = null;
	String PROGRAMA = "ImpExp";

	private ProgressBar progressFtp = null;
	private ProgressBar progressImp = null;
	private OrdersHelper helper = null;
	private TextView progressText;
	int len = 0;
	String file = "";
	String taula = "";
	FTPClient ftp;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			progressFtp.setProgress(msg.what);
		}
	};

	class NotifCsv implements NotifyCsv {

		@Override
		public void Avisa(final int pos) {
			handler.post(new Runnable() {
				public void run() {
					progressImp.setProgress(pos);
				}
			});

		}

	}

	void DescarregaFitxers(String sd) {
		mapTables.Load(Utilitats.getWorkFolder(this, Utilitats.CONFIG) + "/"
				+ "import.properties");

		FTPTransferListener listener = new FTPTransferListener(progressFtp,
				len, handler);
		Utilitats.Descarrega(ImpMaps.this, mapTables, listener);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.impexp);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		helper = new OrdersHelper(this);

		progressFtp = (ProgressBar) findViewById(R.id.progress_ftp);
		progressImp = (ProgressBar) findViewById(R.id.progress_imp);
		progressText = (TextView) findViewById(R.id.progress_text);

		Bundle extras = getIntent().getExtras();
		if (extras != null)
			perSD = extras.getString("PerSD");
		Thread proces = new Thread(new Runnable() {
			public void run() {
				if (Utilitats.isOnline(ImpMaps.this) == false)
					Utilitats.Toast(ImpMaps.this,
							"Atenció. No hi ha connexió a Internet");
				else {
					Boolean load = perSD != null && perSD.equals("S");
					if (load
							|| Utilitats.DescarregaConfiguracio(ImpMaps.this) == true) {
						Rebre(perSD);
						
//						Utilitats.ShowModal(ImpMaps.this,"Importació Finalitzada");
						finish();
					}
				}
				/*
				 * AlertDialog alertDialog = new
				 * AlertDialog.Builder(ImpMaps.this).create();
				 * alertDialog.setTitle("Atenci");
				 * alertDialog.setMessage("Procs de descrrega finalitzat");
				 * alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new
				 * DialogInterface.OnClickListener() { public void
				 * onClick(DialogInterface dialog, int which) {
				 * dialog.dismiss(); } });
				 * 
				 * alertDialog.show();
				 */
			}
		});

		proces.start();

	}

	public void Importar() {

	}

	void Msg(final String missatge) {
		handler.post(new Runnable() {
			public void run() {
				progressText.setText(missatge);
			}
		});
	}

	public void Rebre(final String perSD) {
		String pSD = perSD == null ? "N" : perSD;

		FTPTransferListener listener = new FTPTransferListener(progressFtp,
				len, handler);
		mapTables.Load(Utilitats.getWorkFolder(this, Utilitats.CONFIG) + "/"
				+ "import.properties");

		Prefs prefs = Prefs.getInstance(getApplicationContext());
		String carpeta = prefs.getString("ftpFolder", "");
		prefs.close();

		int NumFiles = Utilitats.DescarregaFitxers(this,carpeta,"*.PC",listener,true);

		String Folder = Utilitats.getWorkFolder(this, Utilitats.WORK).getAbsolutePath();
		String FolderImp = Utilitats.getWorkFolder(this, Utilitats.IMPORT).getAbsolutePath();
		File f=new File(FolderImp);
		File fileNames[] = f.listFiles();

		/* Ordenem per Nom ja que està per data */
		if (fileNames != null && fileNames.length > 1) {
			Arrays.sort(fileNames, new Comparator<File>() {
				@Override
				public int compare(File object1, File object2) {
					return object1.getName().compareTo(object2.getName());
				}
			});
		}

		for (File file : fileNames) {
			Utilitats.InicialitzaBBDD(helper);
			String fs = file.getName();
			if(!file.isDirectory() && file.getName().endsWith(".PC"))
			{
				Utilitats.EsborrarDirectori(Utilitats.getWorkFolder(this, Utilitats.WORK));
				InputStream input = null;

				File archivo = null;
				FileReader fr = null;
				BufferedReader br = null;
				Boolean headers=true;

				try {

					archivo = new File(FolderImp+"/"+fs);
			//		fr = new FileReader(archivo);
			//		br = new BufferedReader(fr);

					br = new BufferedReader(new InputStreamReader(new
							FileInputStream(FolderImp+"/"+fs), "ISO-8859-1"));


					String linea;
					Taules act = null;
					BufferedWriter out = null;
					while ((linea = br.readLine()) != null) {
						if (linea.length() > 3
								&& linea.substring(0, 3).equals("</@") == true) {
							out.close();
							out = null;
						} else
						if (linea.length() > 3
								&& linea.substring(0, 2).equals("<@") == true) {
							int p = linea.indexOf(">");
							if (p > 3) {
								String Fitxer = linea.substring(2,p);
								try
								{
									FileWriter fstream = new FileWriter(Folder+"/"+Fitxer+".TMP", false);
									out = new BufferedWriter(fstream);
								} catch (IOException e)
								{
									System.err.println("Error: " + e.getMessage());
								}
							}
						}
						else
						{
							out.write(linea+"\n");
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (null != fr) {
							fr.close();
						}
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}

				for (final Taules tb : mapTables.getTaules()) {
					File fp = new File(Folder+"/"+tb.getKey()+".TMP");
					if (fp.exists()) {
						Csv2Sqlite sq = new Csv2Sqlite(tb);
						progressImp.setMax(sq.ImportCount(tb.getKey(), ImpMaps.this));
						Msg(tb.getKey());
						sq.ImportFile(tb.getKey(), tb.getValue(), helper, ImpMaps.this,
								new NotifCsv());
					}

				}

			}
		}

		/* Esborrem els fitxers Importats */
		for (File file : fileNames) {
			String fs = file.getName();
			if (!file.isDirectory() && file.getName().endsWith(".PC"))
				file.delete();
		}


		Msg("Procés Finalitzat");

	}

	public void runProces(View arg0) {
		Rebre("tarifes.csv");
	}

	class FTPTransferListener implements FTPListener {

		private int bytesTransferred = 0;
		long lengthFile;

		Context cv;
		final ProgressBar progressBar;
		int bytesReaded = 0;
		int calcul = 0;

		FTPTransferListener(ProgressBar progressBar, long lengthFile,
				Handler progressBarHandler) {
			this.progressBar = progressBar;
			this.lengthFile = lengthFile;

		}

		public void init(final String fileName, long lengthFile) {
			this.progressBar.setProgress(0);
			this.progressBar.setMax((int) lengthFile);
			this.lengthFile = lengthFile;
			handler.post(new Runnable() {
				public void run() {
					progressText.setText(fileName);

				}
			});

		}

		public void started() {
			System.out.println("Transfer started....");
		}

		public void transferred(int length) {
			bytesTransferred += length;
			int MB = 1024 * 1024;
			if (bytesTransferred % MB == 0)
				System.out.println(bytesTransferred / MB + "MB transferred.");
			if (lengthFile != 0) {
				bytesReaded += length;
				// handler.sendMessage(handler.obtainMessage(bytesReaded));

			}

			handler.post(new Runnable() {
				public void run() {
					progressBar.setProgress(bytesReaded);
				}
			});
		}

		@Override
		public void aborted() {

		}

		@Override
		public void completed() {

		}

		@Override
		public void failed() {

		}
	}

	public void completed() {
		System.out.println("Transfer completed.");
	}

	public void aborted() {
		System.out.println("Transfer aborted.");
	}

	public void failed() {
		System.out.println("Transfer failed.");
	}
}

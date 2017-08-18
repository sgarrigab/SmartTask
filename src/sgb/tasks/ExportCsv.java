package sgb.tasks;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.text.format.DateFormat;
import android.widget.Toast;

class TransferListener implements FTPDataTransferListener {
	ExportCsv expcsv;

	int factor;

	TransferListener(ExportCsv expcsv, long fileLength) {
		this.expcsv = expcsv;
		factor = (int) (fileLength);
	}

	public void started() {

		expcsv.setProgres(0, ExportCsv.INI_TRAMESA);
	}

	public void transferred(int length) {
		expcsv.setProgres(factor * length, 0);
	}

	public void completed() {
		expcsv.setProgres(0, ExportCsv.FI_TRAMESA);
	}

	public void aborted() {
		expcsv.setProgres(0, ExportCsv.FI_TRAMESA);
	}

	public void failed() {
		// Transfer failed
	}

}

public class ExportCsv extends Thread {
	Activity act;
	FTPClient ftp;
	String PROGRAMA = "ExportCSV";
	int comandesAEnviar;
	String perSD = "";

	static final int INI_EXPORTACIO = -1;
	static final int INI_TRAMESA = -2;
	static final int FI_EXPORTACIO = -3;
	static final int FI_TRAMESA = -4;


	static Boolean swEnviant = false;


	ExportCsv(Activity act, String perSD) {
		this.act = act;
		this.perSD = perSD;
	}

	void setProgres(final int pos, final int ope) {

		act.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (pos >= 0)

					act.setProgress(pos);

				if (ope < 0)
					switch (ope) {
					case ExportCsv.FI_TRAMESA:
						act.setProgressBarVisibility(false);
						MediaPlayer mp = MediaPlayer.create(
								act.getApplicationContext(),
								R.raw.fi_transmissio);
						mp.start();
						break;
					case ExportCsv.INI_EXPORTACIO:
						act.setProgressBarVisibility(true);
						break;
					case ExportCsv.INI_TRAMESA:
						break;
					case ExportCsv.FI_EXPORTACIO:
						// Toast.makeText(act.getApplicationContext(),
						// "Fi exportacio", Toast.LENGTH_SHORT).show();
						break;
					}
			}

		});
	}

	int enviar(String fitxers[]) {
		int ContFiles=0;
		Prefs prefs = Prefs.getInstance(act.getApplicationContext());
		String carpeta = prefs.getString("ftpFolder", "");
		String host = prefs.getString("ftpServer", "");
		String userName = prefs.getString("ftpUser", "??");
		String password = prefs.getString("ftpPwd", "??");
		prefs.close();

		if (host.length() <= 0) {
			host = "ftp.reset.cat";
			userName = "dem01";
			password = "dem01pda";
		}

		try {
			ftp = new FTPClient();
			ftp.setPassive(true);
			ftp.connect(host);
			ftp.login(userName, password);
			ftp.setType(FTPClient.TYPE_BINARY);
			if (!carpeta.isEmpty())
					ftp.changeDirectory(carpeta);

			for (int i = 0; i < fitxers.length; i++) {
				String file = fitxers[i];
				setProgres(0, 0);
				File fp = new java.io.File(file);
				if (fp.length() > 0) {
					ftp.upload(fp, new TransferListener(this, fp.length()));
					ContFiles++;
				}

			}

			File storageDir = Utilitats.getWorkFolder(act, Utilitats.FOTOS);
            String dirName = storageDir.getAbsolutePath();
            String upLoaded = dirName+"/uploaded";
            File dirUploaded = new File(upLoaded);
            if (!dirUploaded.exists()) {
                if (dirUploaded.mkdirs() == false)
                {
                    Utilitats.ShowModal(act,"Error creant directory : "+upLoaded);
                }
            }

            File f = new File(storageDir.getPath());
			File[] files = f.listFiles();
			for (File inFile : files) {
				if (!inFile.isDirectory()) {
					String Nom = inFile.getName();  // is directory

					String abs = inFile.getAbsolutePath();
                    File fp = new java.io.File(inFile.getAbsolutePath());
                    ftp.upload(fp, new TransferListener(this, fp.length()));
                    String nouNom = upLoaded+"/"+Nom;
                    File nouFile = new File(nouNom);
                    inFile.renameTo(nouFile);
				}
			}

            ftp.changeDirectory("/");
			ftp.disconnect(true);
		} catch (FTPAbortedException e) {
			Utilitats.ShowModal(act,e.getMessage());
		} catch (FTPDataTransferException e) {
			Utilitats.ShowModal(act,e.getMessage());
		} catch (FTPException e) {
			Utilitats.ShowModal(act,e.getMessage());
		}

		catch (FTPIllegalReplyException ex) {
			Toast.makeText(act.getApplicationContext(),
					ex.getMessage() + " Codi:" + ex.getCause(),
					Toast.LENGTH_SHORT).show();
			ex.printStackTrace();
		} catch (IOException e) {
			Utilitats.ShowModal(act,e.getMessage());

		} finally {

		}
	return  ContFiles;
	}


	Boolean comprovar(String fitxers[]) {

		Prefs prefs = Prefs.getInstance(act.getApplicationContext());
		String host = prefs.getString("ftpServer", "");
		String userName = prefs.getString("ftpUser", "??");
		String password = prefs.getString("ftpPwd", "??");
		prefs.close();

		if (host.length() <= 0) {
			host = "ftp.reset.cat";
			userName = "dem01";
			password = "dem01pda";
		}

		try {
			ftp = new FTPClient();
			ftp.setPassive(true);
			ftp.connect(host);
			ftp.login(userName, password);
			ftp.setType(FTPClient.TYPE_BINARY);

			for (int i = 0; i < fitxers.length; i++) {
				String file = fitxers[i];

				try {
					FTPFile list[] = ftp.list(file);
					if (list.length != 1)
						return false;
				} catch (IllegalStateException e) {
					return false;
				} catch (FTPListParseException e) {
					return false;
				}

			}

		} catch (FTPAbortedException e) {
			Errors.appendLog(act, Errors.ERROR, "ExportCsv", "FTP Abort", e,
					null, true);
			return false;

		} catch (FTPDataTransferException e) {
			Errors.appendLog(act, Errors.ERROR, "ExportCsv",
					"FTP Transfer Exception", e, null, true);
			return false;
		} catch (FTPException e) {
			Errors.appendLog(act, Errors.ERROR, "ExportCsv", "FTP Exception",
					e, null, true);
			return false;

		}

		catch (FTPIllegalReplyException ex) {
			Toast.makeText(act.getApplicationContext(),
					ex.getMessage() + " Codi:" + ex.getCause(),
					Toast.LENGTH_SHORT).show();
			ex.printStackTrace();
			return false;
		} catch (IOException e) {
			Errors.appendLog(act, Errors.ERROR, "ExportCsv",
					"FTP Illegal Reply", e, null, true);

			return false;

		}

		return true;
	}

	void marcar() {
		OrdersHelper helper = new OrdersHelper(act);
		helper.getWritableDatabase().execSQL(
				"update Clients set state='E' where state = 'F' ");		
		helper.getWritableDatabase().execSQL(
						"update Linia set state='E' where state = 'F' ");
		helper.getWritableDatabase().execSQL(
				"update Cap set state='E' where state = 'F' ");
		helper.getWritableDatabase().execSQL(
				"update Clients  set comandespendents = 0 where comandespendents <> 0 ");
		
		helper.close();
	}

	public void GeneraFitxerCsv(OrdersHelper helper, String Sql,String taula,
								  BufferedOutputStream cap) throws IOException {
		Cursor ctr = helper.execSQL(Sql);

//		DatabaseProperties dbProp = new DatabaseProperties(helper);

		int num = ctr.getColumnCount();

		cap.write( ("<@"+taula+">\n").getBytes());
		for (int i = 0; i < num; ++i)
			cap.write((ctr.getColumnName(i)+";").getBytes());
		cap.write("\n".getBytes());
		if (ctr.getCount() > 0) {
			ctr.moveToFirst();
			do {
				num = ctr.getColumnCount();
				for (int i = 0; i < num; ++i) {
					String cmp = ctr.getString(i);
					if (cmp == null) cmp= " ";
					cmp = cmp.replace(';', ',').replace('\n', '\t').replace('\r', '\t');
					cap.write(cmp.getBytes());
					cap.write(";".getBytes());
				}
				cap.write("\n".getBytes());

			} while (ctr.moveToNext() == true);

		}
		cap.write( ("</@"+taula+">\n").getBytes());
	}

	public void run() {



		OrdersHelper helper = null;
		String sql = "select * from cap where state = 'F' ";
		helper = new OrdersHelper(act);
		Cursor ctr = helper.execSQL(sql);

		if ( ctr.getCount() <= 0) {
			Utilitats.Toast(act, "No hi ha comandes pendents d'enviament");
			return;
		}
		if (swEnviant == true) {
			Utilitats.Toast(act, "Ja s'està realitzant una exportació.");
			return;
		}
		swEnviant = true;


		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String s = formatter.format(date);
		String fitxer = "TM-"+s+".CSV";
		String pathFile;


		comandesAEnviar = 0;


		try {
			FileOutputStream fcap = null;
			BufferedOutputStream cap = null;
			pathFile = Utilitats.getWorkFolder(act, Utilitats.EXPORT)
					+ "/" + fitxer;
			File wcap = new File(pathFile);
			wcap.createNewFile();
			fcap = new FileOutputStream(wcap);
			cap = new BufferedOutputStream(fcap);
			sql = "select * from cap where state = 'F' ";
//			GeneraFitxerCsv(helper, sql, "CAPCALER", cap);
			sql = "select * from Cap where Cap.tipus like 'AF%' AND Cap.state =  'F' ";
			GeneraFitxerCsv(helper, sql, "CAPCALER", cap);
//			sql = "select Cap.Client,Cap.data,Cap.operari,Cap.Tipus_Exp,Cap.Doc_Exp,Linia.* from Linia LEFT OUTER JOIN Cap on Cap.docum = Linia.docum where Cap.state =  'F' ";
			sql = "select Cap.Tipus,Cap.doc_ordre,Linia.docum,unic_origen,Cap.parent,Cap.Data,Cap.Operari,Cap.client subaux,Linia.* from Linia LEFT OUTER JOIN Cap on Cap.docum = Linia.docum where Cap.tipus like 'AF%' AND Cap.state =  'F' ";
			GeneraFitxerCsv(helper, sql, "MOVIMENT", cap);
			cap.close();
		} catch (IOException e) {
			Utilitats.ShowModal(act,e.getMessage());
			return;
		}

		setProgres(0, ExportCsv.FI_EXPORTACIO); // Tancar progress bar
		if (perSD != "S") {
			enviar(new String[] { pathFile} );
			comprovar(new String[] { fitxer});
		}
		marcar();
		Utilitats.ShowModal(act,"Procés Finalitzat");
		swEnviant=false;

		act.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(act,
						"S'ha exportat " + comandesAEnviar + " comandes",
						Toast.LENGTH_LONG).show();
			}
		});

	}
}

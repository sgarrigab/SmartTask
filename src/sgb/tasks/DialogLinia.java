package sgb.tasks;

import java.text.DecimalFormat;

import sgb.tasks.Utilitats.TPreus;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class DialogLinia extends Dialog implements OnClickListener,
		OnReturnEvent {
	String PROGRAMA = "DialogLinia";
	String taula;
	Double preuFinal;
	Spinner obs_lin;

	Double QuantInicial = 0.0;
	Double lat_ini, lng_ini;

	TFormField fObs_lin;

	String idLinia;
	Button gravar;
	Button imatges;
	Button signa;
	Button borrar;
	Button regal;
	// ExecTask act;
	Activity act;

	String wtarifa;
	String wsubjecte;
	String wdescripcio;
	String wdocument;
	String wLinia;
	OnCanvia onCanvia;
	int swCanviPreu = 0;
	Double quantitatOriginal = 0.0;
	long id;
	Double lat, lng;
	String adr;

	/* GPS */

	Button startIniGps, endIniGps;
	protected LocationManager locationManager = null;
	protected LocationListener locationListener;
	protected String latitude, longitude;
	protected boolean gps_enabled, network_enabled;

	TextView format;
	SGEdit quantitat, stock, preu, preutarifa, tipdte, dte, descripcio, total,
			article, notes, quantitatRegal, articleRegal, descripcioRegal,
			time_inici,  lloc_inici, linies_matricula,
			linies_marca, linies_model, linies_butlleti;

	TPreus tmpPreus = new TPreus();

	void setOnCanviaListener(OnCanvia pt) {
		this.onCanvia = pt;
	}

	OnEditorActionListener onEditor = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			// if (event != null && event.getKeyCode() == event.KEYCODE_ENTER)
			if (actionId == EditorInfo.IME_ACTION_DONE)
				gravar.requestFocus();
			return true;
		}
	};

	private OrdersHelper helper;
	private Cursor cursor;

	public void printPreus(TPreus preus) {

		/* Per evitar activar l'event de Canvi de Valor */

		preu.setFireOnTextChanged(false);
		tipdte.setFireOnTextChanged(false);
		dte.setFireOnTextChanged(false);
		total.setFireOnTextChanged(false);

		if (preus.tipDte.equals("="))
			preu.setTextNotFocus(new DecimalFormat("####0.00###")
					.format(preus.dte));
		else
			preu.setTextNotFocus(new DecimalFormat("####0.00###")
					.format(preus.preuBase));
		tipdte.setTextNotFocus(preus.tipDte);
		dte.setTextNotFocus(new DecimalFormat("####0.00").format(preus.dte));
		total.setTextNotFocus(new DecimalFormat("####0.00")
				.format(preus.preuNet));
		sendReturnValue(preus.articleRegal);
		quantitatRegal.setTextNotFocus(new DecimalFormat("####0.00")
				.format(preus.quantitatRegal));
		articleRegal.setTextNotFocus(preus.articleRegal);

		preu.setFireOnTextChanged(true);
		tipdte.setFireOnTextChanged(true);
		dte.setFireOnTextChanged(true);
		total.setFireOnTextChanged(true);

	}

	public void getPrintedPreus() {
		tmpPreus.quantitat = quantitat.toDouble();
		tmpPreus.preuBase = preu.toDouble();
		tmpPreus.tipDte = tipdte.getText().toString();
		tmpPreus.dte = dte.toDouble();
		tmpPreus.preuNet = total.toDouble();
		tmpPreus.quantitatRegal = quantitatRegal.toDouble();
		tmpPreus.articleRegal = articleRegal.getText().toString();

	}

	Boolean read() {
		String param[] = { Long.toString(id) };
		cursor = helper.getWritableDatabase().rawQuery(
				"select * from Linia where _id=?", param);
		QuantInicial = 0.0;
		if (cursor.getCount() > 0) {
			cursor.moveToNext();

			lng_ini = cursor.getDouble(cursor.getColumnIndex("geo_lng_ini"));
			lat_ini = cursor.getDouble(cursor.getColumnIndex("geo_lat_ini"));

			int pos = cursor.getColumnIndex("quant");
			swCanviPreu = cursor.getInt(cursor.getColumnIndex("canviPreu"));
			tmpPreus.quantitat = cursor.getFloat(pos);
			quantitatOriginal = tmpPreus.quantitat;
			QuantInicial = tmpPreus.quantitat;
			tmpPreus.quantitat = cursor
					.getFloat(cursor.getColumnIndex("quant"));
			tmpPreus.preuNet = cursor
					.getFloat(cursor.getColumnIndex("preunet"));
			tmpPreus.dte = cursor.getFloat(cursor.getColumnIndex("dte"));
			tmpPreus.preuBase = cursor.getFloat(cursor.getColumnIndex("preu"));
			tmpPreus.tipDte = cursor.getString(cursor.getColumnIndex("tipdte"));
			article.setText(cursor.getString(cursor.getColumnIndex("article")));

			time_inici.setText(cursor.getString(cursor
					.getColumnIndex("time_ini")));
			lloc_inici.setText(cursor.getString(cursor
					.getColumnIndex("loc_ini")));
			linies_matricula.setText(cursor.getString(cursor
					.getColumnIndex("matricula")));
			linies_marca.setText(cursor.getString(cursor
					.getColumnIndex("marca")));
			linies_model.setText(cursor.getString(cursor
					.getColumnIndex("model")));
			linies_butlleti.setText(cursor.getString(cursor
					.getColumnIndex("butlleti")));
//			agents.setText(cursor.getString(cursor.getColumnIndex("agents")));

			tmpPreus.quantitatRegal = cursor.getFloat(cursor
					.getColumnIndex("quantitat_regal"));
			tmpPreus.articleRegal = cursor.getString(cursor
					.getColumnIndex("article_regal"));
			notes.setText(cursor.getString(cursor.getColumnIndex("notes")));
			quantitat.setText(new DecimalFormat("####0.00###")
					.format(tmpPreus.quantitat));
			linies_matricula.requestFocus();
			this.fObs_lin.setValue(cursor.getString(cursor
					.getColumnIndex("codi_obs")));

			return true;
		} else
			return false;
	}

	@Override
	public void onWindowFocusChanged(boolean nowFocused) {
		if (Utilitats.lat != 0.0 && Utilitats.lng != 0) {
			Utilitats.Toast(act, Utilitats.adr);
			if (Utilitats.numId == 1) {
				lat_ini = Utilitats.lat;
				lng_ini = Utilitats.lat;
				lloc_inici.setText(Utilitats.adr);
				android.text.format.DateFormat df = new android.text.format.DateFormat();
				time_inici.setText(df.format("HH:mm", new java.util.Date()));

				Utilitats.InicialitzaGps(0);
			}

		}

	}

	public DialogLinia(Context context, final Activity act, String taula,
			String subjecte, final long id, OrdersHelper ahelper, Cursor aCursor) {
		super(context);
		this.act = act;
		this.taula = taula;

		this.helper = ahelper;
		this.cursor = aCursor;
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		/* Recuperem tarifa i client actual */

		Prefs prefs = Prefs.getInstance(getContext());
		wtarifa = prefs.getString("tarifa_cli", "");
		wsubjecte = prefs.getString("codi_cli", "");
		wdescripcio = prefs.getString("desc_cli", "");
		wdocument = prefs.getString("document", "");

		this.id = id;
		prefs.close();

		this.setContentView(R.layout.dialoglinia);
		LayoutParams params = getWindow().getAttributes();
		params.height = LayoutParams.MATCH_PARENT;
		params.width = LayoutParams.MATCH_PARENT;
		getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);

		startIniGps = (Button) findViewById(R.id.linies_ini_gps);
		startIniGps.setOnClickListener(this);

		endIniGps = (Button) findViewById(R.id.linies_delete_ini_gps);
		endIniGps.setOnClickListener(this);

		linies_matricula = (SGEdit) findViewById(R.id.linies_matricula);
		linies_marca = (SGEdit) findViewById(R.id.linies_marca);
		linies_model = (SGEdit) findViewById(R.id.linies_model);
		linies_butlleti = (SGEdit) findViewById(R.id.linies_butlleti);
		time_inici = (SGEdit) findViewById(R.id.time_inici);
		lloc_inici = (SGEdit) findViewById(R.id.lloc_inici);
//		agents = (SGEdit) findViewById(R.id.agents);

		article = (SGEdit) findViewById(R.id.linies_article);
		// format = (TextView) findViewById(R.id.linies_format);
		descripcio = (SGEdit) findViewById(R.id.linies_descripcio);
		quantitat = (SGEdit) findViewById(R.id.linies_quantitat);
		quantitatRegal = (SGEdit) findViewById(R.id.quantitat_regal);
		descripcioRegal = (SGEdit) findViewById(R.id.descripcio_regal);
		articleRegal = (SGEdit) findViewById(R.id.article_regal);
		articleRegal.setOnClickListener(this);
		try {
			fObs_lin = new TFormField("codi_obs",
					findViewById(R.id.spin_lin_obs));
		} catch (Exception e) {
			e.printStackTrace();
		}
		fObs_lin.setSqlLink("SELECT clau _id, descripcio FROM TAULES WHERE TAULA = 'OPE' order BY descripcio");
		fObs_lin.LoadSpinner(this.getContext(), ahelper);

		quantitat.setOnEditorActionListener(onEditor);

		preu = (SGEdit) findViewById(R.id.linies_preu);
		// preu.setOnEditorActionListener(onEditor);
		OnFocusChangeListener recalcula = new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus == false) {
					getPrintedPreus();
					Utilitats.reCalculaPreus(tmpPreus);
					printPreus(tmpPreus);
				}
			}
		};

		preu.setOnFocusChangeListener(recalcula);
		tipdte = (SGEdit) findViewById(R.id.linies_tipdte);
		tipdte.setOnFocusChangeListener(recalcula);

		dte = (SGEdit) findViewById(R.id.linies_dte);
		dte.setOnFocusChangeListener(recalcula);
		// dte.setOnEditorActionListener(onEditor);
		total = (SGEdit) findViewById(R.id.linies_total_lin);
		// total.setOnEditorActionListener(onEditor);
		total.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				/*
				 * if (hasFocus == false) { getPrintedPreus(); tmpPreus.preuBase
				 * = tmpPreus.preuNet; tmpPreus.dte = 0; tmpPreus.tipDte = " ";
				 * Utilitats.reCalculaPreus(tmpPreus); printPreus(tmpPreus); }
				 */
			}
		});

		notes = (SGEdit) findViewById(R.id.observacions);

		class OnValidate extends OnValidateEvent {
			@Override
			public Boolean validate(View emissor) {

				/*
				 * Si el que canvia és la quantitat hem de rellegir per mirar si
				 * hi ha una oferta per quantitat. si no hi és no tindriem que
				 * fer res.
				 * 
				 * En cas dels camps tip_dte, preuBase i Dte si que cal només
				 * recalcular
				 */
				act.runOnUiThread(new Runnable() {
					public void run() {
						// getPrintedPreus();
						if (id != 0)
							return;
						double quant = 0.0;
						double tot = 0.0;

						/* Per evitar loops */

						quantitat.setFireOnTextChanged(false);

						quant = quantitat.toDouble();

						double real = quant - QuantInicial;
						View p = getCurrentFocus();
						if (getCurrentFocus() == dte
								|| getCurrentFocus() == tipdte) {
							getPrintedPreus();
							Utilitats.reCalculaPreus(tmpPreus);
							printPreus(tmpPreus);

						} /*
						 * else if (getCurrentFocus() == total) {
						 * tmpPreus.preuNet = tot; tmpPreus.tipDte = "=";
						 * tmpPreus.dte = tot; printPreus(tmpPreus); }
						 */else if (getCurrentFocus() == quantitat) {
							TPreus preus = Utilitats.readPreus(act, helper,
									wsubjecte, wtarifa, article.getText()
											.toString(), tmpPreus.familia,
									tmpPreus.linia, quant);
							printPreus(preus);
						} else {
							// getPrintedPreus();
							// printPreus(tmpPreus);
						}

						// printPreus(tmpPreus);
						/*
						 * Toast.makeText(getContext(), "...",
						 * Toast.LENGTH_SHORT) .show();
						 */

					}
				});
				return true;
			}
		}
		;

		quantitat.setTimer(5);
		tipdte.setTimer(5);
		dte.setTimer(5);
		// total.setTimer(1);

		quantitat.setOnValidateEvent(new OnValidate());
		tipdte.setOnValidateEvent(new OnValidate());

		dte.setOnValidateEvent(new OnValidate());
		// total.setOnTimerEvent(new OnTimer());
		// tipdte.setOnTimerEvent(new OnTimer());
		// dte.setOnTimerEvent(new OnTimer());

		Cursor curArt = cursor;
		idLinia = curArt.getString(0);
		article.setText(curArt.getString(curArt.getColumnIndex("article")));
		if (idLinia == null)
			idLinia = article.getText().toString();

		Double wQuantitat = 0.0;

		String wFamilia = "", wLinia = "";

		if (id != 0)
			if (read() == true) {

			} else
				Toast.makeText(getContext(),
						"Atenció !! No s'ha trobat el registre ",
						Toast.LENGTH_SHORT).show();

		String warticle = cursor.getString(cursor.getColumnIndex("article"));
		TPreus preus = Utilitats.readPreus(act, helper, wsubjecte, wtarifa,
				warticle, wFamilia, wLinia, wQuantitat);

		Utilitats.readArticles(helper, warticle, tmpPreus);
		if (id != 0)
			printPreus(tmpPreus);
		else
			printPreus(preus);

		Cursor cursorArt = Utilitats.getCursorArt();
		descripcio.setText(cursorArt.getString(cursorArt
				.getColumnIndex("descripcio")));
		// format.setText(cursorArt.getString(cursorArt.getColumnIndex("format")));

		regal = (Button) findViewById(R.id.linies_regal);
		regal = (Button) findViewById(R.id.linies_regal);
		gravar = (Button) findViewById(R.id.linies_gravar);
		imatges = (Button) findViewById(R.id.linies_imatges);
		signa = (Button) findViewById(R.id.linies_signa);
		borrar = (Button) findViewById(R.id.linies_borrar);
		signa.setOnClickListener(this);
		imatges.setOnClickListener(this);
		gravar.setOnClickListener(this);
		borrar.setOnClickListener(this);
		gravar.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus == true) {
					getPrintedPreus();
					gravarRegistre();
					dismiss();

//					article.requestFocus();

				}
			}
		});

		regal.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus == true) {
					// act.setOnReturnEvent(DialogLinia.this);
					Intent FamiliesIntent = new Intent(act, ExecTask.class);
					FamiliesIntent.putExtra("programa", "FamiliesWithReturn");
					act.startActivityForResult(FamiliesIntent, 1);
					article.requestFocus();

					// new DialogRegal(getContext(), act, "", "", 0,
					// llistaPreComandes).show();

				}
			}
		});

		setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(quantitat, InputMethodManager.SHOW_IMPLICIT);
			}
		});

		this.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {

				quantitat.close();

				InputMethodManager imm = (InputMethodManager) getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(null, 0);

			}
		});
			quantitat.requestFocus();
			if (lat_ini == null || lat_ini <= 0.0) {
				Utilitats.InicialitzaGps(1);
				Intent it = new Intent(act, Gps.class);
				act.startActivity(it);
			}

		}

	void gravarRegistre() {

		{
			double real = tmpPreus.quantitat - QuantInicial;

			InputMethodManager imm = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(quantitat, InputMethodManager.SHOW_IMPLICIT);

			getPrintedPreus();

			ContentValues cv = new ContentValues();

			// cv.put("_id", idLinia);
			cv.put("docum", wdocument);
			cv.put("codi_obs", this.fObs_lin.getValue());
			cv.put("article", article.getText().toString());
			cv.put("quant", tmpPreus.quantitat);
			cv.put("dte", tmpPreus.dte);
			cv.put("tipdte", tmpPreus.tipDte);

			cv.put("time_ini", time_inici.getText().toString());
			cv.put("loc_ini", lloc_inici.getText().toString());
//			cv.put("agents", agents.getText().toString());
			cv.put("matricula", linies_matricula.getText().toString());
			cv.put("marca", linies_marca.getText().toString());
			cv.put("model", linies_model.getText().toString());
			cv.put("butlleti", linies_butlleti.getText().toString());

			cv.put("geo_lng_ini", lng_ini);
			cv.put("geo_lat_ini", lat_ini);

			cv.put("preu", tmpPreus.preuBase);
			cv.put("preunet", tmpPreus.preuNet);
			cv.put("quantitat_regal", tmpPreus.quantitatRegal);
			cv.put("article_regal", tmpPreus.articleRegal);
			cv.put("canviPreu", swCanviPreu); // Han Modificat el preu

			/*
			 * cv.put("tipdte", tipdte.getText().toString()); cv.put("dte",
			 * dte.getText().toString());
			 */
			cv.put("tipdte", tipdte.getText().toString());
			cv.put("notes", notes.getText().toString());
			try {
				if (id == 0) {
					id = helper.getWritableDatabase().insertOrThrow(
							"Linia", "_id", cv);
					if (id < 0)
						Errors.appendLog(act, Errors.ERROR, PROGRAMA,
								"Error Modificant linia", null, cv);
				} else {
					cv.put("_id", id);
					helper.update("Linia", "_id", cv);
				}

			} catch (SQLiteConstraintException e) {
				Errors.appendLog(act, Errors.ERROR, PROGRAMA,
						"Error Insert linia", e, cv);
			}

			// Actualitzem els demanats a Precomanda

			cv.clear();
			String clau = "_id";
			cv.put(clau, idLinia);
			cv.put("servit", quantitat.getText().toString());
			long rt = helper.update("PreComanda", clau, cv);
			if (rt < 0)
				Errors.appendLog(act, Errors.ERROR, PROGRAMA, "Error Update "
						+ taula, null, cv);

			// Actualitzem els demanats i l'estoc a Articles

			QuantInicial -= tmpPreus.quantitat;

			// String sq = "update Articles set servit = servit-("+QuantInicial
			// + "),stock = stock + ("+QuantInicial
			String sq = "update Articles set servit = 1,stock = stock + ("
					+ QuantInicial + ") where article ='"
					+ article.getText().toString() + "' ";
			helper.getWritableDatabase().execSQL(sq);

			if (rt < 0)
				Errors.appendLog(act, Errors.ERROR, PROGRAMA, "Error Update "
						+ taula, null, cv);

			// Actualitzem els demanats a Preus Esp

			sq = "update PreusEsp set servit =" + "1"
					+ " where subjecte = 'OFERTES' and objecte ='"
					+ article.getText().toString() + "' ";
			helper.getWritableDatabase().execSQL(sq);

			if (onCanvia != null)
				onCanvia.haCanviat(true);

		}

	}

	private static final int SPEECH_REQUEST_CODE = 0;

	// Create an intent that can start the Speech Recognizer activity
	private void displaySpeechRecognizer() {
/*		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
		startActivityForResult(intent, SPEECH_REQUEST_CODE); */
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

/*		if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
			List<String> results = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);
			String spokenText = results.get(0);
			// Do something with spokenText
		}
		super.onActivityResult(requestCode, resultCode, data); */

		if (requestCode == 3) {
			if (resultCode == Activity.RESULT_OK) {
				adr = data.getStringExtra("adr");
				lat = data.getDoubleExtra("lat", 0.0);
				lng = data.getDoubleExtra("lng", 0.0);
				Utilitats.Toast(act, adr + " " + lat + " " + lng);
			}
			if (resultCode == Activity.RESULT_CANCELED) {
				// Write your code if there's no result
			}
		}
	}// on

	@Override
	public void onClick(View v) {
		Utilitats.so(act, R.raw.insert);
		if (v == signa) {
			Intent it = new Intent(act, Signa.class);
			it.putExtra("document", wdocument);
			act.startActivity(it);

		}
		if (v == imatges) {
			gravarRegistre();
			Intent it = new Intent(act, GaleriaImatges.class);
			it.putExtra("document", wdocument);
			act.startActivity(it);

		}
		if (v == endIniGps) {

			Utilitats.InicialitzaGps(0);
			lat_ini = lng_ini = 0.0;
			lloc_inici.setText("");

		}
		if (v == startIniGps) {
			Utilitats.InicialitzaGps(1);
			Intent it = new Intent(act, Gps.class);
			act.startActivity(it);

		}

		if (v == articleRegal) {
			sendReturnValue(article.getText().toString());
			quantitatRegal.setText("1");

		}
		if (v == borrar) {
			if (id == 0) {
				Toast.makeText(getContext(),
						"Encara no s'ha creat el registre", Toast.LENGTH_LONG)
						.show();
				return;
			}

			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						helper.getWritableDatabase().delete("Linia", " _id=?",
								new String[] { Long.toString(id) });
						// String sq =
						// "update Articles set servit = servit-"+quantitatOriginal
						// + ",stock = stock + "+QuantInicial
						String sq = "update Articles set servit = 1,stock = stock + "
								+ QuantInicial
								+ " where article ='"
								+ article.getText().toString() + "' ";
						helper.getWritableDatabase().execSQL(sq);

						if (onCanvia != null)
							onCanvia.haCanviat(true);
						dismiss();

						Toast.makeText(getContext(), "Registre Esborrat",
								Toast.LENGTH_LONG).show();

						break;

					case DialogInterface.BUTTON_NEGATIVE:
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			builder.setMessage("Vol esborrar el registre " + id + "?")
					.setPositiveButton("Yes", dialogClickListener)
					.setNegativeButton("No", dialogClickListener).show();

		}
	}

	@Override
	public void sendReturnValue(String value) {
		if (value == null) {
			descripcioRegal.setText("..");
			articleRegal.setText("");
			quantitatRegal.setText(" ");
			return;
		}
		articleRegal.setText(value);
		TPreus art = new TPreus();

		String sql = "select descripcio from articles where article = '"
				+ value + "' ";
		Cursor curArt = helper.execSQL(sql);
		if (curArt.getCount() > 0) {
			curArt.moveToFirst();
			descripcioRegal.setText(curArt.getString(curArt
					.getColumnIndexOrThrow("descripcio")));
		}
		quantitatRegal.setText("1");

	}
}
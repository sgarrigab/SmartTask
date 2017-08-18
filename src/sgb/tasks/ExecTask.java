package sgb.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ExecTask extends Activity {
	Intent svc;
	String PROGRAMA = "ExecTask";
	PowerManager.WakeLock wl = null;

	private DrawerLayout NavDrawerLayout;
	private String[] titulos;
	private TypedArray NavIcons;
	private ListView NavList;
	private ArrayList<Item_objct> NavItms;
	NavigationAdapter NavAdapter;

	Intent it;

	OnReturnEvent onReturnEvent = null;
	private Bundle extras;
	private String programa = "";
	private String parametre1 = "";
	private String parametre2 = "";
	private String tipus = "";
	private OrdersHelper helper;
	private static String grupcli;
	private static String client;
	private static String document;
	private static String ruta;
	private String titol[] = new String[30];
	private TextView tvHeader;
	private ProgressBar progressHeader;
	ViewPager viewPager;
	MyPagerAdapter adapter;
	int paginaActual = 0;

	public void setOnReturnEvent(OnReturnEvent onReturnEvent) {
		this.onReturnEvent = onReturnEvent;
	}

	public void MenuSlide() {


		NavDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		// Lista
		NavList = (ListView) findViewById(R.id.lista);
		// Declaramos el header el caul sera el layout de header.xml
		View header = getLayoutInflater().inflate(R.layout.header, null);
		// Establecemos header
		NavList.addHeaderView(header);
		// Tomamos listado de imgs desde drawable
		NavIcons = getResources().obtainTypedArray(R.array.navigation_iconos);
		// Tomamos listado de titulos desde el string-array de los recursos
		// @string/nav_options
		titulos = getResources().getStringArray(R.array.nav_options);
		// Listado de titulos de barra de navegacion
		NavItms = new ArrayList<Item_objct>();
		// Agregamos objetos Item_objct al array
		// Perfil
		NavItms.add(new Item_objct(titulos[0], NavIcons.getResourceId(0+1
				, -1)));
		// Favoritos
		NavItms.add(new Item_objct(titulos[1], NavIcons.getResourceId(0, -1)));
		NavItms.add(new Item_objct(titulos[2], NavIcons.getResourceId(0+2, -1)));
		NavItms.add(new Item_objct(titulos[3], NavIcons.getResourceId(0+3, -1)));
		NavItms.add(new Item_objct(titulos[4], NavIcons.getResourceId(0+4, -1)));
		NavItms.add(new Item_objct(titulos[5], NavIcons.getResourceId(0+5, -1)));
		NavItms.add(new Item_objct(titulos[6], NavIcons.getResourceId(0+6, -1)));
		// Eventos
		/*
		 * NavItms.add(new Item_objct(titulos[2], NavIcons.getResourceId(2,
		 * -1))); //Lugares NavItms.add(new Item_objct(titulos[3],
		 * NavIcons.getResourceId(3, -1))); //Etiquetas NavItms.add(new
		 * Item_objct(titulos[4], NavIcons.getResourceId(4, -1)));
		 * //Configuracion NavItms.add(new Item_objct(titulos[5],
		 * NavIcons.getResourceId(5, -1))); //Share NavItms.add(new
		 * Item_objct(titulos[6], NavIcons.getResourceId(6, -1)));
		 */
		// Declaramos y seteamos nuestrp adaptador al cual le pasamos el array
		// con los titulos
		NavAdapter = new NavigationAdapter(this, NavItms);
		NavList.setAdapter(NavAdapter);
		// Siempre vamos a mostrar el mismo titulo
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option, menu);

		Locale locale = new Locale("en");
		Locale.setDefault(locale);

		return true;
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Toast.makeText(ExecTask.this, "Servei Engegat", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			Toast.makeText(ExecTask.this, "Parant Servei", Toast.LENGTH_SHORT)
					.show();
		}
	};

	/****************************************
	 * Gestió de Menus
	 * 
	 * @param item
	 * @return
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// check selected menu item
		switch (item.getItemId()) {
			case R.id.copydb:
				helper.close();
				Utilitats.CopiaBBDD(this);
				break;

			case R.id.restoredb: {
				if (!Environment.MEDIA_MOUNTED.equals(Environment
						.getExternalStorageState())) {
					Toast.makeText(getApplicationContext(),
							"External SD card not mounted", Toast.LENGTH_LONG)
							.show();
				}

				File sd2 = Utilitats.getWorkFolder(this, Utilitats.BACKUP);
				if (sd2 == null)
					return false;
				File data2 = Environment.getDataDirectory();

				try {
					helper.close();
					String currentDBPath = "/orders.db";
					String backupDBPath = "/data/sgb.tasks/databases/andorders.db";
					File currentDB = new File(sd2, currentDBPath);
					File backupDB = new File(data2, backupDBPath);

					if (currentDB.exists() == true) {
						FileChannel src = new FileInputStream(currentDB)
								.getChannel();
						FileChannel dst = new FileOutputStream(backupDB)
								.getChannel();
						dst.transferFrom(src, 0, src.size());
						src.close();
						dst.close();
						Toast.makeText(getApplicationContext(), "RESTAURADA",
								Toast.LENGTH_SHORT).show();

					}
				} catch (Exception e) {

				}
				break;
			}

			case R.id.init:

				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								// Yes button clicked
								// helper.close();
								Utilitats.inicialitzaBBDD(ExecTask.this);
								break;

							case DialogInterface.BUTTON_NEGATIVE:
						}
					}
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Vol esborrar la Base de Dades?")
						.setPositiveButton("Yes", dialogClickListener)
						.setNegativeButton("No", dialogClickListener).show();
				break;

			case R.id.exportarSD:
			case R.id.exportar:

			final String perSD = item.getItemId() == R.id.exportarSD ? "S"
					: null;
			if (Utilitats.isOnline(this) == false)
				break;

			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
					"Sending Files");
			wl.acquire();

			Thread proc = new Thread(new Runnable() {
				public void run() {
					if (Utilitats.DescarregaFitxerSeguretat(ExecTask.this) == true) {
						if (Utilitats.ComprovaSeguretat(ExecTask.this) == false)
							Utilitats.Toast(ExecTask.this,
									"Aplicació en modo demo. No es pot enviar",
									true);
						else {
							if (Utilitats
									.getConfig(ExecTask.this, "ModeImpExp")
									.endsWith("ros"))
								new ExportRos(ExecTask.this, perSD).start();
							else
								new ExportCsv(ExecTask.this, perSD).start();
						}
					}
				}
			}

			);
			proc.start();

			break;
		case R.id.importarSD:
			if (Utilitats.isOnline(this) == false)
				break;

		case R.id.importar:
			String prSD = item.getItemId() == R.id.importarSD ? "S" : null;

			PowerManager pm2 = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wl = pm2.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
					"Receiving Files");
			wl.acquire();
			it = new Intent(this, ImpMaps.class);
			it.putExtra("PerSD", prSD);
			startActivity(it);
			break;
		case R.id.gpsstart: {

		}
		case R.id.gpsstop: {
		}
		case R.id.gps:
			try {
				/*
				 * Intent searchAddress = new Intent(Intent.ACTION_VIEW,
				 * Uri.parse("geo:0,0?z=20&q=" +
				 * startActivity(searchAddress);
				 */

				// it = new Intent(this, Mapa.class);
				// startActivity(it);

			} catch (Exception e) {
				System.out.println(e);
			}
			break;

		case R.id.prefs:

			new DialogPreferences(this, this).show();
			break;

		}
		return false;
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		View pg = adapter.views.get(paginaActual);
		if (pg instanceof TPlantilla) {
			((TPlantilla) pg).activate();
		}
	}

	void refrescaPlana(int page) {
		paginaActual = page;
		String rt = titol[page];
		tvHeader.setText(rt);
		View pg = adapter.views.get(page);
		if (pg instanceof TPlantillaList) {
			((TPlantillaList) pg).Canvia();
			((TPlantillaList) pg).Actualitza();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		refrescaPlana(0);

	}

	@Override
	public void onBackPressed() {
		View pg = adapter.views.get(paginaActual);
		if (pg instanceof TPlantilla)
			((TPlantilla) pg).onBackPressed();
//		this.finish();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (wl != null)
			wl.release();
		if (helper != null) {
			helper.close();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 2) {
			Intent returnIntent = new Intent();
			if (data != null)
				returnIntent
						.putExtra("article", data.getStringExtra("article"));
			setResult(Utilitats.RETURN_ARTICLE, returnIntent);
			finish();
		} else if (requestCode == 1) {
			if (resultCode == Utilitats.RETURN_ARTICLE) {
				if (onReturnEvent != null && data != null)
					onReturnEvent.sendReturnValue(data
							.getStringExtra("article"));
			}
		}
		
	}

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Utilitats.onActivityCreateSetTheme(this);
		// Utilitats.changeToTheme(this, Utilitats.THEME_WHITE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// getWindow().requestFeature(Window.PROGRESS_INDETERMINATE_ON);

		setContentView(R.layout.mainpager);

		// new DialogPreferences(this).show();

		helper = new OrdersHelper(this);
		extras = getIntent().getExtras();
		if (extras != null) {
			programa = extras.getString("programa");
			if (extras.getString("client") != null)
				client = extras.getString("client");
			if (extras.getString("grupcli") != null)
				grupcli = extras.getString("grupcli");
			if (extras.getString("document") != null)
				document = extras.getString("document");
			parametre1 = extras.getString("parametre1");
			parametre2 = extras.getString("parametre2");
			if (extras.getString("tipus") != null)
				tipus  = extras.getString("tipus");
		}

		progressHeader = (ProgressBar) findViewById(R.id.progressViewHeader);
		tvHeader = (TextView) findViewById(R.id.textViewHeader);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		adapter = new MyPagerAdapter(this);
		viewPager.setAdapter(adapter);

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int page) {

				refrescaPlana(page);

				/*
				 * if (page < 4) { ListView p = (ListView) v
				 * .findViewById(R.id.tplantillalist_list); if (p != null) {
				 * p.refreshDrawableState(); adapter.notifyDataSetChanged(); } }
				 */
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		MenuSlide();
	}

	private class MyPagerAdapter extends PagerAdapter {
		Activity activity;

		private ArrayList<View> views;

		public MyPagerAdapter(Activity activity) {
			this.activity = activity;
			views = new ArrayList<View>();

			/** Nivell 2 Clients per Ruta **/

			try {
				/*
				 * progressHeader.setIndeterminate(false);
				 * progressHeader.setProgress(100); progressHeader.setMax(150);
				 */
				if (programa.equals("LlistaClientsRuta")) {
					ruta = parametre1;
					titol[0] = "Clients per Línia";
					views.add(new LlistaClientsRuta(activity, helper,
							parametre1));
					// views.add(new Subjectes(activity, helper, parametre1));
				} else if (programa.equals("NouDocument")) {
					titol[0] = "Nou Albarà";
					views.add(new Cap(activity, helper, client,tipus, "0"));
					titol[1] = "Client";
					views.add(new ClientsSgb(activity, helper, client, "0"));
					titol[2] = "Efectes Pendents";
					views.add(new LlistaEfectes(activity, helper, client));

				}

				else if (programa.equals("Clients")) {
					titol[1] = "Client";
					views.add(new ClientsSgb(activity, helper, "", ruta));

				} else if (programa.equals("Families")) {
					titol[0] = "Families";
					views.add(new LlistaFamiliesLinies(activity, helper, false,
							"F"));
				} else if (programa.equals("Linies")) {
					titol[0] = "Línies";
					views.add(new LlistaFamiliesLinies(activity, helper, true,
							"L"));
				} else if (programa.equals("Familia")) {
					titol[0] = parametre2;
					Boolean onReturn = extras.getBoolean("retorn");
					String tipus = extras.getString("tipus");
					String sql;
					if (tipus.equals("L"))
						sql = "select article _id,* from Articles A where "
								+ "Linia = '" + parametre1 + "' ";
					else
						sql = "select article _id,* from Articles A where "
								+ "Familia = '" + parametre1 + "' ";
					views.add(new LlistaPreComandes((ExecTask) activity,
							helper, client, document, sql, "Articles", true,
							onReturn));
				} else if (programa.equals("XLinia")) {
					int i = 0;

					titol[i++] = "Linia d'albarà";
					views.add(new XLinia(activity, helper, document, Integer
							.parseInt(parametre2)));
				} else if (programa.equals("Linia")) {
					int i = 0;

					titol[i++] = "Tipus de servei";
					String sql = "select article _id,article,descripcio,' ' servit ,' ' text3,' ' text4 from Articles A where article is not null";
					views.add(new LlistaPreComandes((ExecTask) activity,
							helper, client, document, sql, "Articles", false,
							false));
					if (client != null) {
						sql = "select _id,P.article,A.descripcio,' ' quantitat,' ' text3,' ' text4,' ' text5 from PreComanda P LEFT OUTER JOIN Articles A ON A.article = P.article where P.subjecte='"
								+ client + "' ";
						titol[i++] = "Precomandes";
						views.add(new LlistaPreComandes((ExecTask) activity,
								helper, client, document, sql, "Precomanda",
								true, false));

						/*
						 * Ofertes del Client. Tots els preus especials per
						 * aquest client convenció (Client $ de la gestió)
						 */
						String Op[] = client.split("-");
						grupcli = Op[0];
						sql = "select _id,P.OBJECTE article,A.descripcio,A.servit,A.stock text3,A.format  from PreusEsp P LEFT OUTER JOIN Articles A ON A.article = P.OBJECTE where P.subjecte="
								+ "'" + grupcli + "'";
						titol[i++] = "Ofertes Client";
						views.add(new LlistaPreComandes((ExecTask) activity,
								helper, client, document, sql, "Precomanda",
								true, false));
					}


					sql = "select _id,P.OBJECTE article,A.descripcio,A.servit, A.stock text3,A.format text4  from PreusEsp P LEFT OUTER JOIN Articles A ON A.article = P.OBJECTE where P.subjecte="
							+ "'~OFERTES'";
					titol[i++] = "Ofertes Genériques";
					views.add(new LlistaPreComandes((ExecTask) activity,
							helper, client, document, sql, "Precomanda", true,
							false));

					/*
					 *
					 * 
					 * sql =
					 * "select FAMILIA _id,F.familia,F.descripcio,0 servit, '$$$' familia   from Families F"
					 * ; titol[i++] = "Families"; views.add(new
					 * LlistaPreComandes(activity, helper, client, document,
					 * sql, "Families"));
					 */

					Prefs prefs = Prefs.getInstance(getApplicationContext());
					String perFamilies = prefs.getString("perFamilia", "??");
					if (!perFamilies.equals("L")) {
						sql = "select article _id,descripcio,article,servit,stock text3,format text4 from Articles where familia = 'F$$$' ";

						Cursor cur = helper.getReadableDatabase().rawQuery(
								"select * from Families", null);
						while (cur.moveToNext()) {
							if (i < 30) {
								titol[i++] = cur.getString(cur
										.getColumnIndex("descripcio"));
								sql = "select article _id,article,descripcio,servit,stock text3,format text4 from Articles A  where "
										+ " familia = '"
										+ cur.getString(cur
												.getColumnIndex("familia"))
										+ "' ";
								views.add(new LlistaPreComandes(
										(ExecTask) activity, helper, client,
										document, sql, "Articles", true, false));
							}

						}
						cur.close();
					} else {

						sql = "select article _id,descripcio,article,servit, stock text3,format text4 from Articles where familia = 'L$$$' ";

						Cursor cur = helper.getReadableDatabase().rawQuery(
								"select * from Linies", null);
						while (cur.moveToNext()) {
							if (i < 30) {
								titol[i++] = cur.getString(cur
										.getColumnIndex("descripcio"));
/*								sql = "select article _id,article,descripcio,servit,stock text3,format text4 from Articles A where "
										+ " familia = '"
										+ cur.getString(cur
												.getColumnIndex("linia"))
										+ "' "; */
								sql = "select article _id,article,descripcio,' ' servit,' ' text3,' ' text4 from Articles A where "
									+ " familia = '"
										+ cur.getString(cur
												.getColumnIndex("linia"))
										+ "' ";


								views.add(new LlistaPreComandes(
										(ExecTask) activity, helper, client,
										document, sql, "Articles", true, false));
							}

						}
						cur.close();
					}

					/*
					 * views.add(new ConsultaArticles( activity, helper,
					 * "select article _id,descripcio from Articles where descripcio like '%POLLASTR%' "
					 * )); views.add(new ConsultaArticles(activity, helper,
					 * "select article _id,descripcio from Articles where descripcio like '%OUS%' "
					 * )); views.add(new ConsultaArticles(activity, helper,
					 * "select article _id,descripcio from Articles where descripcio like '%PEIX%' "
					 * ));
					 */
				}

				else if (programa.equals("Arbre")) {
					titol[0] = "Selecció";
					String sql = "select article _id,descripcio,servit,article,familia from Articles where familia = '"
							+ parametre1 + "' ";
					/*
					 * // + "' and (P.servit is null or P.servit = 0) "; //
					 * views.add(new LlistaPreComandes(activity, helper, client,
					 * // document, sql, "Articles",false));
					 */

				}

				else if (programa.equals("DetallAlta")) {
					/* LLista de línies de comanda */
					titol[0] = "Linies de Comanda";
					views.add(new LlistaLinies(activity, helper, tipus,document, true));
					/* Nova línia d'Article */
					titol[1] = "Gestió de línia";
				} else if (programa.equals("Detall")) {
					/* LLista de línies de comanda */
					titol[0] = "Linies de Comanda";
					views.add(new LlistaLinies(activity, helper, tipus,document,
							false));
					/* Nova línia d'Article */
					titol[1] = "Gestió de línia";
					/*
					 * views.add(new XLinia(activity, helper, document));
					 * Consulta d'Articles
					 */
				} else if (programa.equals("LlistaDocuments")) {
					// views.add(new Client(activity,ruta));
					titol[0] = "Albarans Actius";
					client = parametre1;
					views.add(new LlistaDocumentsClient(activity, helper,
							parametre1, " and Cap.state <> 'E' "));
					titol[1] = "Històric Albarans";
					views.add(new LlistaDocumentsClient(activity, helper,
							parametre1, " and Cap.state == 'E' "));
					titol[2] = "Efectes Pendents";
					views.add(new LlistaEfectes(activity, helper, parametre1));
				} else if (programa.equals("Cap")) {
					document = parametre2;
					client = parametre1;
					titol[0] = "Modificació de Document";
					views.add(new Cap(activity, helper, client, tipus,document));
					titol[1] = "Client";
					views.add(new ClientsSgb(activity, helper, client, "0"));
				} else if (programa.equals("Client")) {
					client = parametre1;
					titol[0] = "Client";
					views.add(new ClientsSgb(activity, helper, client, "0"));
					titol[1] = "Efectes Pendents";
					views.add(new LlistaEfectes(activity, helper, parametre1));

				}

				else {

					String sql = "select * from rutes";
					Cursor cursor = helper.execSQL(sql);
					if (false && cursor.getCount() <= 0) {
						if (Utilitats.isOnline(activity) == true) {

							AlertDialog.Builder builder = new AlertDialog.Builder(
									activity);
							builder.setMessage(
									"Vol importar dades de demostració ?")
									.setCancelable(true)
									.setNegativeButton(
											"No",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int id) {
												}

											})
									.setPositiveButton(
											"Si",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int id) {

													PowerManager pm2 = (PowerManager) getSystemService(Context.POWER_SERVICE);
													wl = pm2.newWakeLock(
															PowerManager.SCREEN_DIM_WAKE_LOCK,
															"Receiving Files");
													wl.acquire();
													it = new Intent(
															ExecTask.this,
															ImpMaps.class);
													startActivity(it);
												}

											});

							AlertDialog alert = builder.create();
							alert.show();

						} else
							Toast.makeText(
									ExecTask.this,
									"No hi ha connexió a internet per Importar Dades Demo",
									Toast.LENGTH_SHORT).show();

					}
					titol[0] = "Serveis diaris Pendents";
					views.add(new LlistaDocumentPendents(activity, helper," Cap.Tipus LIKE 'CF%' ",true));
					titol[1] = "Tots els Serveis pendents";
					views.add(new LlistaDocumentPendents(activity, helper," Cap.Tipus LIKE 'CF%' ",false));
					titol[2] = "Serveis Realitzats";
					views.add(new LlistaDocumentPendents(activity, helper," Cap.Tipus LIKE 'AF%' and Cap.state <> 'E' ",false));
					titol[3] = "Serveis Enviats";
					views.add(new LlistaDocumentPendents(activity, helper," Cap.Tipus LIKE 'AF%' and Cap.State = 'E' ",false));
					titol[4] = "Nou document";
//					views.add(new LlistaRutes(activity, helper));
					views.add(new LlistaClientsRuta(activity, helper,
							null));


				}
				tvHeader.setText(titol[0]);
			} catch (Exception e) {
				Errors.appendLog(this.activity, Errors.ERROR, "ExecTask",
						e.getMessage(), true);
			}
		}


		@Override
		public void destroyItem(ViewGroup view, int arg1, Object object) {
			((ViewPager) view).removeView((View) object);
		}

		@Override
		public void finishUpdate(ViewGroup arg0) {
		}

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public Object instantiateItem(View view, int position) {
			View myView = views.get(position);
			/*
			 * ListView p = (ListView)myView.findViewById(R.id.list_find); if (p
			 * != null) { p.refreshDrawableState(); }
			 */
			((ViewPager) view).addView(myView);

			return myView;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}





	}
}
package sgb.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.LOCATION_SERVICE;

public class Cap extends TPlantillaMant implements LocationListener {
    private String client = "";
    private long document = 0;
    private long _id = 0;
    private String tipus = "";
    private Double lat,lng;
    Boolean sw=false;
    String adr;
    SGEdit data;
    SGEdit entrega;
    Boolean swLocation;
    SGEdit hora,hora_final;
    TextView total;
    Comptadors ct;
    String PROGRAMA = "Cap";
    DatePickerDialog dpk;
    TFormField fDocument;
    ImageButton iniciar_servei;
    Button imatges;
    Button signa;
    Boolean swAlta;
    double saldo = 0;
    double risc = 0;
    double valor_total = 0;
    SGEdit time_inici, lloc_inici;
    Button startIniGps, endIniGps;
    Double lat_ini, lng_ini;
    protected LocationManager locationManager = null;
    protected LocationListener locationListener;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;
    private String unicImatges ="";


    @Override
    public void activate() {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        total.setText(Utilitats.calculaTotals(helper, document));

		/*
         * AlertDialog.Builder builder = new AlertDialog.Builder(act);
		 * builder.setMessage("Activant!!") .setCancelable(false)
		 * .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		 * public void onClick(DialogInterface dialog, int id) { DecimalFormat
		 * twoDForm = new DecimalFormat("#.##");
		 * total.setText(Utilitats.calculaTotals(helper, document));
		 * 
		 * } }); AlertDialog alert = builder.create(); alert.show();
		 */

    }

    public Cap(Activity act, OrdersHelper helper, String codi, String tipus, String document) {
        super(act, helper);
        this.document = Long.parseLong(document);
        this.tipus = tipus;
        this.client = codi;
        ct = new Comptadors(helper);
        swAlta = false;
        swLocation=false;
        lat_ini = lng_ini= 0.0;

        init();
    }


    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

        HttpURLConnection urlConnection = null;

        URL url = new URL(urlString);

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);

        urlConnection.setDoOutput(true);

        urlConnection.connect();

        BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));

        char[] buffer = new char[1024];

        String jsonString = new String();

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
        }
        br.close();

        jsonString = sb.toString();

//        System.out.println("JSON: " + jsonString);

        return new JSONObject(jsonString);
    }


    private static List<Address> getAddrByWeb(JSONObject jsonObject){
        List<Address> res = new ArrayList<Address>();
        try
        {
            JSONArray array = (JSONArray) jsonObject.get("results");
            for (int i = 0; i < array.length(); i++)
            {
                Double lon = new Double(0);
                Double lat = new Double(0);
                String name = "";
                try
                {
                    lon = array.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                    lat = array.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    name = array.getJSONObject(i).getString("formatted_address");
                    Address addr = new Address(Locale.getDefault());
                    addr.setLatitude(lat);
                    addr.setLongitude(lon);
                    addr.setAddressLine(0, name != null ? name : "");
                    res.add(addr);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();

                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();

        }

        return res;
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {


        StringBuilder strReturnedAddress = new StringBuilder("");
        Geocoder geocoder = new Geocoder(act, Locale.getDefault());
        try {
            JSONObject json = getJSONObjectFromURL("http://maps.google.com/maps/api/geocode/json?address="+LATITUDE+","+LONGITUDE+"&sensor=false");
            List<Address> addresses = getAddrByWeb(json);
            if (addresses != null) {
                    Address returnedAddress = addresses.get(0);
                    int l = returnedAddress.getMaxAddressLineIndex();
                        strReturnedAddress
                                .append(returnedAddress.getAddressLine(0)).append(
                                "\n");
//				Log.w("My Current loction address",						"" + strReturnedAddress.toString());
            } else {
//				Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
//			Log.w("My Current loction address", "Canont get Address!");
        }
        return strReturnedAddress.toString();
    }



    @Override
    public void onLocationChanged(final Location location) {
        new Thread(new Runnable() {
            public void run(){
                if (sw==false && swLocation == false) {
                    sw=true;
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    if (lat != 0.0 && lng != 0.0) {
                        Utilitats.so(act, R.raw.insert);
                        Utilitats.so(act, R.raw.insert);
                        adr = getCompleteAddressString(lat, lng);

                        if (adr != null && adr.length() > 5) {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getCamps().setValue("lloc_inici", adr);
                                }
                            });

                            Utilitats.so(act, R.raw.insert2);
                            swLocation = true;
                        }
                    sw = true;
                    }

                }

            }
        }).start();


    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }



    void postNotRead() {
        postRead();
    }


    void turnGpsOn() {
        if (tipus.startsWith("A")) {  // Només es fa la geolocalització quan és un albarà de servei

            swLocation = false;
            locationManager = (LocationManager) act.getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                act.startActivity(new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);

            } catch (SecurityException e) {
                Utilitats.ShowModal(act, "No es pot inicialitzar GPS.\n Error de permisos");
            }
        }
}


    void postRead() {

        time_inici = (SGEdit) findViewById(R.id.time_inici);
        lloc_inici = (SGEdit) findViewById(R.id.lloc_inici);
        startIniGps = (Button) findViewById(R.id.linies_ini_gps);
        startIniGps.setOnClickListener(this);
        if (document > 0) {
            lat = cursor.getDouble(cursor.getColumnIndex("latitud"));
            lng = cursor.getDouble(cursor.getColumnIndex("longitud"));
            adr = cursor.getString(cursor.getColumnIndex("lloc_inici"));
            unicImatges = cursor.getString(cursor.getColumnIndex("unic_imatges"));
        }
        if (adr==null || adr.length() < 5)
            turnGpsOn();
        else
            swLocation = true;


        if (client == null)
            client = cursor.getString(cursor.getColumnIndex("client"));
        if (cursor != null) _id = cursor.getInt(cursor.getColumnIndex("_id"));


        Cursor c = Utilitats.readSubjecte(helper, client);
        TextView desc = (TextView) view.findViewById(R.id.client_desc);
        if (c != null) {
            String grup = c.getString(c.getColumnIndex("grup"));
            Cursor cGrup = Utilitats.readGrupCli(helper, grup);
            if (cGrup != null) {
                risc = cGrup.getDouble(cGrup.getColumnIndex("risc"));
                saldo = cGrup.getDouble(cGrup.getColumnIndex("saldo"));
            }

            desc.setText(c.getString(c.getColumnIndex("nom")));
            if (document == 0) {
                SGEdit dtepp = (SGEdit) view.findViewById(R.id.client_dte_pp);
                dtepp.setText(c.getString(c.getColumnIndex("dtepp")));
                SGEdit dtecom = (SGEdit) view.findViewById(R.id.client_dte_com);
                dtecom.setText(c.getString(c.getColumnIndex("dtecomercial")));
                SGEdit dtegrup = (SGEdit) view.findViewById(R.id.client_dte_grup);
                dtegrup.setText(c.getString(c.getColumnIndex("dtegrup")));

            }

            c.close();
        } else
            desc.setText("???????");

    }

    Boolean read() {
        if (document > 0) {
            String param[] = {tipus, Long.toString(document)};
            cursor = helper.getWritableDatabase().rawQuery(
                    "select * from Cap where tipus=? and docum=?", param);
            return cursor.getCount() > 0;
        } else {

        }

        return false;
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
        }
    };


    void Esborrar() {
        if (document == 0)
            return;
        getHelper().getWritableDatabase().delete("Linia",
                " docum=?",
                new String[]{Long.toString(document)});


        getHelper().getWritableDatabase().delete("Cap", " docum=?",
                new String[]{Long.toString(document)});
        helper.getReadableDatabase().execSQL(
                "update clients set comandespendents = comandespendents -1  "
                        + " where subjecte = '" + client + "' ");

    }

    String generaunicImatges(Activity act){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");
        Date date = new Date();
        String dt = dateFormat.format(date);
        dt = Utilitats.getTerminalUser(act)+"-"+dt;
        return dt;
    }



    void esborrarDocument() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Esborrar();
                        Toast.makeText(getContext(),
                                "Document esborrat " + Long.toString(document),
                                Toast.LENGTH_SHORT).show();

                        act.finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(
                                getContext(),
                                "NO s'ha esborrat el document "
                                        + Long.toString(document),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setMessage("Vol esborrar el document?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }


    public void gravar() {
        ContentValues cv = getCamps().UItoCv();

        try {
//			if (saldo <= 0)
//				Utilitats.Toast(act, "Atenció!! Client sense saldo",true);
            if (unicImatges == null || unicImatges.length() <= 5) {
                unicImatges = generaunicImatges(act);
            }

            cv.put("unic_imatges", unicImatges);
            cv.put("longitud",String.valueOf(lng));
            cv.put("latitud",String.valueOf(lat));



            if (document == 0) {
                swAlta = true;
                document = ct.getComptador();
                for (; ; ) {
                    String param[] = {Long.toString(document)};
                    Cursor c = helper.getWritableDatabase().rawQuery(
                            "select _id from Cap where _id=?", param);
                    if (c.getCount() == 0)
                        break;
                    document++;

                    Utilitats.inicialitzaPrecomandes(helper, null);
                }
                if (document < 0)
                    Errors.appendLog(act, Errors.ERROR, "Cap",
                            "No hi ha definits els comptadors");

                else {
                    tipus = "AFA";

                    String operari = Utilitats.getCurrentUser(getContext());
                    cv.put("operari", Utilitats.getTerminalUser(act));  // Serie Manual
                    cv.put("tipus", tipus);  // Serie Manual
                    cv.put("tipus", tipus);  // Serie Manual
                    cv.put("docum", document);
                    cv.put("state", "F");


//					cv.put("_id", document);
                    long numregs = helper.insert("Cap", "", cv);
                    _id = numregs;
                    ct.setComptador(document + 1);
                    fDocument.setText(Long.toString(document));
                    helper.getReadableDatabase()
                            .execSQL(
                                    "update clients set comandespendents = comandespendents +1  "
                                            + " where subjecte = '"
                                            + client + "' ");

                }
            } else {
                cv.put("_id", _id);
                helper.update("Cap", "_id", cv);
            }
        } catch (SQLiteException e) {
            Errors.appendLog(act, Errors.ERROR, "Cap",
                    "Error Gravacio registre", e, cv, true);

        } finally {

        }

    }


    /*********************************************************************************************
     * convertirAlbara
     * ---------------
     * Quan executem un servei passem de comanda a albarà , d'aquesta manera si hi ha una actualització
     * de serveis ja no ens esborrarà el servei ja que només esborra els tipus 'CFA' mentre que els
     * albarans son 'AFA' (Enviats des de PC) o 'AFM' (Creats des de el terminal)
     *********************************************************************************************/

    public void convertirAlbara() {
        String sql = "select docum from cap where docum = "+document;
        Cursor cr = helper.getWritableDatabase().rawQuery(sql,null);
        if (cr.getCount() > 1) {
            Utilitats.ShowModal(act, "No es Iniciar un servei ja realitzat");
            return;
        }




        if (document == 0)
            gravar();
        ContentValues cv = getCamps().UItoCv();
        tipus = "AFA";
        cv.put("tipus", tipus);  // Feine en curs
        cv.put("_id", _id);
        cv.put("state", "P");
        cv.put("operari",Utilitats.getTerminalUser(act));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String currentDateandTime = sdf.format(new Date());
        cv.put("hora",currentDateandTime);
        getCamps().setValue("state","P");
        turnGpsOn(); // Arranquem el GPSJa


		/* Quan generem un nou albarà posem data entrega la data d'inici del servei */

        Date now = new java.util.Date();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String f = df.format(now);
        cv.put("entrega", f);
        getCamps().setValue("entrega",f);

        df = new SimpleDateFormat("yyMMdd");
        f = df.format(now);
        String doc_ordre = f + Utilitats.getTerminalUser(act);

        cv.put("doc_ordre", doc_ordre);
        df = new SimpleDateFormat("HH:mm");
        f = df.format(now);
        cv.put("hora", f);
        getCamps().setValue("hora",f);

        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minute = rightNow.get(Calendar.MINUTE);
        int seconds = rightNow.get(Calendar.SECOND);
        f = Integer.toString(hour * 3600 + minute * 60 + seconds);
        cv.put("parent", f);
        cv.put("operari", Utilitats.getTerminalUser(act));
        helper.update("Cap", "_id", cv);
        String Sql = "update Linia set Tipus = '" + tipus + "' where Tipus = 'CFA' and Docum =" + document;
        helper.getWritableDatabase()
                .execSQL(Sql);
        cridaLinies();
        iniciar_servei.setVisibility(GONE);
	/*	Utilitats.InicialitzaGps(0);
		Intent it = new Intent(act, Gps.class);
		act.startActivity(it); */

    }

    @Override
    public void onWindowFocusChanged(boolean nowFocused) {
        if (Utilitats.lat != 0.0 && Utilitats.lng != 0) {
//			Utilitats.Toast(act, Utilitats.adr);
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

    public void onClick(View v) {
        if (v == signa) {
            Intent it = new Intent(act, Signa.class);
            it.putExtra("document", unicImatges);
            act.startActivity(it);

        }
        if (v == imatges) {
            gravar();
            Intent it = new Intent(act, GaleriaImatges.class);
            it.putExtra("document", unicImatges);
            act.startActivity(it);

        }
        if (v == startIniGps) {
//            Utilitats.InicialitzaGps(1);
            if (swLocation == true) {
                swLocation = false;
                turnGpsOn();
                getCamps().setValue("lloc_inici", "");
                lat=lng=0.0;
            }

        }


        if (v == this.iniciar_servei) {
            convertirAlbara();
        }

        if (v == this.ico_delete) {
            esborrarDocument();
        }

        if (v == ico_back) {
            onBackPressed();
            return; // Perque no cridi al finish de la superclasse.
        }

        super.onClick(v);
        boolean cridaDetall = false;
        if (v == ico_help) {
            final Calendar c = Calendar.getInstance();

            dpk = new DatePickerDialog(act, mDateSetListener,
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));
            dpk.show();

        }

        if (v == this || v == this.ico_save
                || (v == this.ico_showlist && document == 0)) {
            cridaDetall = true;
//			if (!tipus.startsWith("A"))  // És una comanda
//				convertirAlbara();
//			else
            gravar();
        }
        if (cridaDetall || v.getId() == R.id.tplant_list_llista) {
            cridaLinies();
			/*
			 * Si és una alta passem directament a Llistes de Selecció ja que no
			 * hi ha linies
			 */
        }
    }

    public void turnGPSOff() {

        swLocation = false;
        locationManager = (LocationManager) act.getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            act.startActivity(new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        try {
            locationManager.removeUpdates(this);

        } catch (SecurityException e) {
            Utilitats.ShowModal(act, "No es pot para la Geolocalització");
        }

    }

    @Override
    public void destroy() {

    }

    public void cridaLinies() {

        Prefs prefs = Prefs.getInstance(getContext());
        prefs.setString("tipus", tipus);
        prefs.setString("document", Long.toString(document));
        prefs.close();

        String programa = "Detall";
        if (swAlta) {
            programa = "DetallAlta";
            swAlta = false; // La propera vegada ha d'entrar a linies
        }
        Intent ClientsPerRutaIntent = new Intent(getAct().getBaseContext(),
                ExecTask.class);
        ClientsPerRutaIntent.putExtra("tipus", tipus);
        ClientsPerRutaIntent.putExtra("document", Long.toString(document));
        ClientsPerRutaIntent.putExtra("programa", programa);
        getAct().startActivity(ClientsPerRutaIntent);
    }


    @Override
    public void onBackPressed() {
        turnGPSOff();

		/* Si no hi ha linies sortim sense gravar */


        /*****
         String param[] = { Long.toString(document) };
         cursor = helper.getWritableDatabase().rawQuery(
         "select * from Linia where docum=?", param);

         if (cursor.getCount() <= 0) { // Si no hi ha linies esborrar la comanda
         Esborrar();
         act.finish();
         return;
         }
         *****/


        gravar();

        if (tipus.startsWith("A") == false)  // No és una comanda
        {
//            Utilitats.ShowModal(act, "No es pot finalitzar un document No iniciat.");
            act.finish();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setMessage("Vol finalitzar la comanda?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                        finalitzarComanda();
                                        if (Utilitats.isOnline(act) == true) {
                                            new ExportCsv(act, "N").start();

                                            //                                    if (false) {
                                            //									SendData p = new SendData();
                                            //									p.send(act, "<AIXO ES UNA PROVA DE DADES>");
                                            //                                  Utilitats.enviarComandaPerMail(act, helper,
                                            //                                           document);
                                        } else
                                            Utilitats.ShowModal(act, "No hi ha connexió internet.");
                                        act.finish();

//							}

                                }
                            })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            act.finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }


    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    void finalitzarComanda() {
        Cursor c = helper.getReadableDatabase().rawQuery(
                "select * from Cap where docum =" + document, null);
        int s = c.getCount();

        helper.getReadableDatabase().execSQL(
                "update linia set state='F' where docum =" + document);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String currentDateandTime = sdf.format(new Date());

        String sq = "update Cap set state='F',hora_final='"+currentDateandTime+"' where docum =" + document;
        helper.getReadableDatabase().execSQL(sq);


        Toast.makeText(getContext(),
                "Comanda Finalitzada en espera de tramesa : " + s,
                Toast.LENGTH_LONG).show();

    }

    @Override
    void build() throws Exception {

        getCamps().setTable("Cap");
        getCamps().setKey("_id");
        getCamps().setSqlList(
                "select _id,data from Cap where client='" + client + "'");
        getCamps().getCamps().add(
                (fDocument = new TFormField("docum", view
                        .findViewById(R.id.client_doc))));
        getCamps().getCamps().add(
                new TFormField("client", view.findViewById(R.id.client_code)));
        getCamps().getCamps().add(
                new TFormField("hora", view.findViewById(R.id.client_hora)));
        getCamps().getCamps().add(
                new TFormField("hora_final", view.findViewById(R.id.client_hora_final)));
        getCamps().getCamps().add(
                new TFormField("entrega", view
                        .findViewById(R.id.client_entrega), 0, "", "", InputType.TYPE_DATETIME_VARIATION_DATE));
        getCamps().getCamps().add(
                new TFormField("value", view.findViewById(R.id.client_total)));
        getCamps().getCamps().add(
                new TFormField("data", view.findViewById(R.id.client_date), 0, "", "", InputType.TYPE_DATETIME_VARIATION_DATE));
        getCamps().getCamps().add(
                new TFormField("lloc_inici", view
                        .findViewById(R.id.lloc_inici)));
        getCamps().getCamps().add(
                new TFormField("state", view.findViewById(R.id.client_state)));
        getCamps().getCamps().add(
                new TFormField("notes", view.findViewById(R.id.client_obs)));

        TFormField pt = new TFormField("comentari",
                view.findViewById(R.id.spin_comentari));
        pt.setSqlLink("SELECT clau _id, descripcio FROM TAULES WHERE TAULA = 'OPE'");
        getCamps().getCamps().add(pt);

        pt = new TFormField("vehicle",
                view.findViewById(R.id.spin_entrega_mati));
        pt.setSqlLink("SELECT vehicle _id, descripcio FROM VEHICLES");
        getCamps().getCamps().add(pt);

        getCamps().ClearUI();
        getCamps().initialize();
        total = (SGEdit) view.findViewById(R.id.client_total);

        iniciar_servei = (ImageButton) view.findViewById(R.id.iniciar_servei);
        imatges = (Button) findViewById(R.id.linies_imatges);
        signa = (Button) findViewById(R.id.linies_signa);
        signa.setOnClickListener(this);
        imatges.setOnClickListener(this);


        if (tipus.startsWith("A")) { // No és una comanda
            iniciar_servei.setVisibility(GONE);
        } else {
            iniciar_servei.setVisibility(VISIBLE);
            iniciar_servei.setOnClickListener(this);
        }

        if (document == 0) {
            SGEdit clients = (SGEdit) view.findViewById(R.id.client_code);
            data = (SGEdit) view.findViewById(R.id.client_date);
            entrega = (SGEdit) view.findViewById(R.id.client_entrega);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date();
            data.setText(dateFormat.format(date));
            entrega.setText(dateFormat.format(date));
            SimpleDateFormat datehoraFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
            hora = (SGEdit) view.findViewById(R.id.client_hora);
            hora.setText(datehoraFormat.format(date));

            getCamps().setValue("state","F");

            postRead();

            clients.setText(client);
        }

    }

    // camps.add(new
    // FormField("familia",familia,R.id.articles_familia,"familia","select _id,descripcio from Filters"));

	/*
	 * wizard.setOnClickListener(new View.OnClickListener() { public void
	 * onClick(View v) { Intent ClientsPerRutaIntent = new Intent(getAct()
	 * .getBaseContext(), ExecTask.class);
	 * ClientsPerRutaIntent.putExtra("parametre1", client);
	 * ClientsPerRutaIntent.putExtra("programa", "Precomanda");
	 * getAct().startActivity(ClientsPerRutaIntent); } });
	 * 
	 * }
	 */

    @Override
    int getMantViewId() {
        return R.layout.cap;
    }

    @Override
    long getButtons() {
        return ICO_BACK | ICO_HELP | ICO_SAVE | ICO_DELETE;
    }

    @Override
    long getRowsLayout() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void haCanviat(Boolean b) {
        // TODO Auto-generated method stub

    }

}

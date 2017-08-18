package sgb.tasks;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Gps extends Activity implements LocationListener,OnClickListener {
	protected LocationManager locationManager;
	protected LocationListener locationListener;
	protected Context context;
	TextView txtLat;
	TextView txtDesc;
	ImageView img;
	Button but;
	Double lat,lng;
	String adr;
	String provider;
	Boolean swLocation = false;
	Boolean swFinded = false;
	protected String latitude, longitude;
	protected boolean gps_enabled, network_enabled;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps);
		txtLat = (TextView) findViewById(R.id.textview1);
		txtDesc = (TextView) findViewById(R.id.textDesc);
		img = (ImageView) findViewById(R.id.satellite);
		but = (Button) findViewById(R.id.button1);
		but.setOnClickListener(this);
		but.setVisibility(View.GONE);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			turnGPSOn();
		}

		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,	0, this);

		} catch (SecurityException e) {
			Utilitats.ShowModal(Gps.this, "No es pot inicialitzar GPS.\n Error de permisos");
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (swLocation == false) {
			swLocation = true;
			lat = location.getLatitude();
			lng = location.getLongitude();
			txtLat.setText("Lat:" + lat
						+ "\n,Lng:" + lng);
			adr = getCompleteAddressString(lat,lng);
			but.setVisibility(View.VISIBLE);  // Es posa aqui perqué en mobils antics mai surt l'adreça
			if (adr.length() <= 1)
				swLocation = false; // Perque torni a entrar
			else
			{
				txtDesc.setText(adr);
				Utilitats.so(this, R.raw.capella);
			}

		}

	}

	private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
		String strAdd = "";
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		try {
			List<Address> addresses = geocoder.getFromLocation(LATITUDE,
					LONGITUDE, 1);
			if (addresses != null) {
				Address returnedAddress = addresses.get(0);
				StringBuilder strReturnedAddress = new StringBuilder("");

				for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
					strReturnedAddress
							.append(returnedAddress.getAddressLine(i)).append(
									"\n");
				}
				strAdd = strReturnedAddress.toString();
//				Log.w("My Current loction address",						"" + strReturnedAddress.toString());
			} else {
//				Log.w("My Current loction address", "No Address returned!");
			}
		} catch (Exception e) {
			e.printStackTrace();
//			Log.w("My Current loction address", "Canont get Address!");
		}
		return strAdd;
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



	private void turnGPSOn() {

		startActivity(new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	}

	private void turnGPSOff() {
		startActivity(new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

	}

	@Override
	public void onClick(View v) {
		Intent returnIntent = new Intent();
		Utilitats.lat = lat;
		Utilitats.lng = lng;
		Utilitats.adr = adr;
		finish();
		
	}

}

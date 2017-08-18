package sgb.tasks;

import android.content.Context;
import android.content.SharedPreferences;

/* s un singleton per gestionar les variables globals i les preferncies */

public class Prefs {
	String name="orders"; // Fitxer de preferncies.
	SharedPreferences settings; 
	String PROGRAMA = "Prefs";

	private static Prefs prefs;
	Prefs() { settings = null; };
	public static Prefs getInstance(Context ctx) {
		if (prefs == null)
			prefs = new Prefs();
		prefs.open(ctx);
		return prefs;
	}
	
	void open(Context ctx) {
		settings = ctx.getSharedPreferences(
				name, 0);
	}

	void close() {
		settings = null;
	}
	
	String getString(String key,String defValue) {
		return settings.getString(key, defValue);
	}

	void  setString(String key,String value) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key,value);
		editor.commit();
	}
}

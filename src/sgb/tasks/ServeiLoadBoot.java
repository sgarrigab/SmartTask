package sgb.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServeiLoadBoot extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		// LANZAR SERVICIO
		Intent serviceIntent = new Intent(context, ServeiBoot.class);
		// startService(svc);
		// serviceIntent.setAction("sgb.service.tasks.Servei");
		context.startService(serviceIntent);

		// LANZAR ACTIVIDAD
/*		Intent i = new Intent(context, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i); */
	}
}
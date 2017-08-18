package sgb.tasks;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class LlistaLinies extends TPlantillaList {
	long document;
	String tipus;
	

	public LlistaLinies(Activity act, OrdersHelper helper, String tipus,String document,Boolean alta) {
		super(act, helper,true);
		PROGRAMA = "LlistaLinies";
		

		this.document = Long.parseLong(document);
		this.tipus = tipus;
		run();
		
		if (alta) /* Ja no es para a línies ja que no n'hi ha cap */
			executaPrecomandes();


	}

	public void OnClickBoto(Cursor c,View v)
	{
		Utilitats.so(act,R.raw.button);
		String _id = getCursor().getString(getCursor().getColumnIndex("_id"));
		double quant = getCursor().getDouble(getCursor().getColumnIndex("quant"));
		double servir = getCursor().getDouble(getCursor().getColumnIndex("servir"));
		servir = servir > 0 ? servir = 0 : quant;
		ContentValues cv = new ContentValues();
		cv.put("_id", _id);
		cv.put("servir", servir);
		try {
			helper.update("linia", "_id", cv);
			cursor.requery();
			adapter.notifyDataSetChanged();


		} catch (SQLiteConstraintException e) {
			Errors.appendLog(act, Errors.ERROR,
					PROGRAMA, "Error Updating linia", e,
					cv);
		}

	}



	public void OnPopulate(Cursor c, View v) {
		double  Imp = c.getDouble(c.getColumnIndex("servir"));
		TextView boto = (TextView) v.findViewById(R.id.listrow_boto_comptador);
		if (Imp > 0.0)
			boto.setBackgroundResource(R.drawable.imp_ok);
		else
			boto.setBackgroundResource(R.drawable.imp_ko);
		boto.setVisibility(View.VISIBLE);

	}

	public void executaPrecomandes()
	{

		/* Grava Document actual a preferències */

		Prefs prefs = Prefs.getInstance(getContext());
		prefs.setString("document", Long.toString(document));
		prefs.close();

		Intent intent = new Intent(getAct().getBaseContext(),
				ExecTask.class);
		intent.putExtra("parametre1", Long.toString(document));
		intent.putExtra("parametre2", Long.toString(0));
		intent.putExtra("Tipus", tipus);
		intent.putExtra("programa", "Linia");
		int requestCode = 0;
		getAct().startActivityForResult(intent, requestCode);
	}

	public void onLongClick(View v) {

		if (v == this || v.getId() == R.id.tplant_list_add)
		{

		}
	}

	
	public void onClick(View v) {

		if (v == this || v.getId() == R.id.tplant_list_add)
		{
			executaPrecomandes();
			cursor.requery();
			adapter.notifyDataSetChanged();

		}
		super.onClick(v);
	}

	public void build() throws Exception {
		list = (ListView) view.findViewById(R.id.tplantillalist_list);
		this.getCamps().setTable("Linies");
		this.getCamps().setKey("_id");
		String Sql = "select L.Tipus,L.Docum,L._id _id,L.descripcio,L.marca,L.model,L.matricula,L.article,quant,servir from Linia L LEFT OUTER JOIN Articles A ON A.article = L.article where L.docum ="
				+ document + " and Tipus = '"+ tipus+"' ";
		this.getCamps()
				.setSqlList(Sql);

/*		this.getCamps().getCamps()
				.add(new TFormField("A.descripcio", R.id.listrow_text5)); */
		this.getCamps().getCamps()
				.add(new TFormField("marca", R.id.listrow_text2));
		this.getCamps().getCamps()
				.add(new TFormField("model", R.id.listrow_text3));
		this.getCamps().getCamps()
				.add(new TFormField("matricula", R.id.listrow_text4));
		this.getCamps().getCamps()
				.add(new TFormField("descripcio", R.id.listrow_text1));
		this.getCamps().getCamps()
				.add(new TFormField("", R.id.listrow_boto_comptador));
		// this.getCamps().getCamps()
		// .add(new TFormField("quantitat", R.id.listrow_text4));
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				String taula="";

				Prefs prefs = Prefs.getInstance(getContext());
				prefs.setString("tipus", tipus);
				prefs.setString("document", Long.toString(document));
				prefs.close();


				DialogLinia dlg = new DialogLinia(getContext(),
						(ExecTask)act, taula, "",id,
						LlistaLinies.this.getHelper(),LlistaLinies.this.getCursor());
				dlg.setOnCanviaListener(LlistaLinies.this);
				dlg.show();
				/*
				Intent ClientsPerRutaIntent = new Intent(getAct()
						.getBaseContext(), ExecTask.class);
				Intent intent = new Intent(getAct().getBaseContext(),
						ExecTask.class);
				intent.putExtra("parametre1", Long.toString(document));
				intent.putExtra("parametre2", Long.toString(id));
				intent.putExtra("programa", "XLinia");
				int requestCode = 0;
				getAct().startActivityForResult(intent, requestCode);
				cursor.requery();
 				cursor.moveToPosition(position); */
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
										   int position, long id) {
				String _id = getCursor().getString(getCursor().getColumnIndex("_id"));
				double quant = getCursor().getDouble(getCursor().getColumnIndex("quant"));
				double servir = getCursor().getDouble(getCursor().getColumnIndex("servir"));
				servir = servir > 0 ? servir = 0 : quant;
				ContentValues cv = new ContentValues();
				cv.put("_id", _id);
				cv.put("servir", servir);
				try {
					helper.update("linia", "_id", cv);
					cursor.requery();
					adapter.notifyDataSetChanged();


				} catch (SQLiteConstraintException e) {
					Errors.appendLog(act, Errors.ERROR,
							PROGRAMA, "Error Updating linia", e,
							cv);
				}


				return true;
			}
		});
	}

	@Override
	int getRowViewId() {
		return R.layout.tplantillalist_row;

	}

	@Override
	long getButtons() {
		return ICO_BACK | ICO_HELP | ICO_ADD ;
	}

	@Override
	long getRowsLayout() {
		return ROW_TEXT1 | ROW_TEXT2| ROW_TEXT3 | ROW_TEXT4 | ROW_TEXT5;
	}


	@Override
	public void haCanviat(Boolean b) {
		cursor.requery();
		
	}


}

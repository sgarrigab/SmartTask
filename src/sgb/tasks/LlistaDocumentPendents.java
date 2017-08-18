package sgb.tasks;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LlistaDocumentPendents extends TPlantillaList {
	protected Calendar dataMov;
	protected String wDataMov;
	SimpleDateFormat format,formatMDY;
	String Situacio;
	Boolean DataDia = true;
	Spinner User;
	List<String> spinFields=null;
	String CurrentUser="";




	public LlistaDocumentPendents(Activity act, OrdersHelper helper,String Situacio,Boolean DataDia) {
		super(act, helper,true);
		this.DataDia = DataDia;
		this.Situacio = Situacio;
		PROGRAMA = "LlistaDocumentPendents";
		dataMov = Calendar.getInstance();



		incDate(0);
		run();
		data.setText(wDataMov);
		User = (Spinner) findViewById(R.id.spin_userdoc);

		spinFields = new ArrayList<String>();
		Cursor c = helper.execSQL("select subjecte _id,nomfiscal from operaris");
		SimpleCursorAdapter qc = new SimpleCursorAdapter(act,
				android.R.layout.simple_spinner_item, c,
				new String[] { "nomfiscal" },
				new int[] { android.R.id.text1 },
				CursorAdapter.IGNORE_ITEM_VIEW_TYPE);

		qc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		User.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				CurrentUser = spinFields.get(position);
				Utilitats.setCurrentUser(getContext(),CurrentUser);
				runSQL();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
			}

		});
		User.setAdapter(qc);
		int Current=-1;
		int Pos = 0;
		CurrentUser = Utilitats.getCurrentUser(act);
		if (c.moveToFirst()) {
			do {
				String sr = c.getString(0);
				spinFields.add(c.getString(0));
				if (c.getString(0).equalsIgnoreCase(CurrentUser))
					Current=Pos;
				Pos++;
			} while (c.moveToNext());
		}
		if (Current >= 0)
			User.setSelection(Current);



	}

	public void runSQL() {
		CurrentUser = Utilitats.getCurrentUser(getContext());
		String wDat = formatMDY.format(dataMov.getTime());
		String Sql = "select Cap.tipus,Cap.docum,Cap._id,data dataf,Cap.client,Cap.nom,Cap.state from Cap  left join Clients on (Clients.subjecte = Cap.client) " +
				" where "+Situacio + " and operari = '"+CurrentUser+"' ";
		if (DataDia)
			Sql +=  " and data = '"+wDat+"' ";

		Sql += "order by Data,hora ";

	//	Sql += " and "+Situacio ;
	//	Sql += " order by data desc ";
		this.getCamps().setSqlList(Sql);
		super.runSQL();


	}

	@Override
	int getPaint(Cursor cur, View row) {
		ImageView v = (ImageView) row.findViewById(R.id.listrow_icon);
		if (v != null) {
			String state = cur.getString(cur.getColumnIndex("state"));
			if (state == null || state == "A")
				v.setImageResource(R.drawable.ball_green);
			else if (state.equals("F"))
				v.setImageResource(R.drawable.send);
			else if (state.equals("E"))
				v.setImageResource(R.drawable.tick);
			else
				v.setImageResource(R.drawable.tick);
		}
		return 0;
	}

	@Override
	void build() throws Exception {
		list = (ListView) view.findViewById(R.id.tplantillalist_list);
		if (DataDia)
			dataLayout.setVisibility(View.VISIBLE);
		this.getCamps().setTable("rutes");
		this.getCamps().setKey("_id");
		this.getCamps()
				.setSqlList(
				// "select *,L.subjecte from Cap  LEFT JOIN Clients L ON L.subjecte = subjecte where State <> 'E' ");
//						"select strftime('%d-%m-%Y', data) dataf,* from Cap  left join Clients on (Clients.subjecte = Cap.client) where Cap.state <> 'E' order by data	desc ");
						"select data dataf,* from Cap  left join Clients on (Clients.subjecte = Cap.client) where Cap.state <> 'E' order by data	desc ");

		this.getCamps().getCamps()
		.add(new TFormField("nom", R.id.listrow_text1));

		this.getCamps().getCamps()
				.add(new TFormField("docum", R.id.listrow_text2));
		this.getCamps().getCamps()
				.add(new TFormField("dataf", R.id.listrow_text3));
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent ClientsPerRutaIntent = new Intent(getAct()
						.getBaseContext(), ExecTask.class);
				cursor.moveToPosition(position);
				String tipus = cursor.getString(cursor.getColumnIndex("tipus"));
				String param = cursor.getString(cursor.getColumnIndex("docum"));
				String client = cursor.getString(cursor.getColumnIndex("client"));
				ClientsPerRutaIntent.putExtra("parametre1", client);
				ClientsPerRutaIntent.putExtra("parametre2", param);
				ClientsPerRutaIntent.putExtra("tipus", tipus);
				ClientsPerRutaIntent.putExtra("programa", "Cap");
				getAct().startActivity(ClientsPerRutaIntent);
	

			}
		});
		runSQL();
	}

	@Override
	int getRowViewId() {
		return R.layout.tplantillalist_row;
	}

	void incDate(int inc)
	{
		format = new SimpleDateFormat("dd/MM/yy");
		formatMDY = new SimpleDateFormat("yyyy-MM-dd");
		dataMov.add(Calendar.DATE, inc);
		wDataMov = format.format(dataMov.getTime());


	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v == butDataInc) {
			incDate(1);
			runSQL();
			data.setText(wDataMov);
		}
		else
		if (v == butDataDec) {
			incDate(-1);
			runSQL();
			data.setText(wDataMov);
		}

	}

	@Override
	long getButtons() {
		return ICO_BACK | ICO_HELP;
	}

	@Override
	long getRowsLayout() {
		return ROW_TEXT1 | ROW_TEXT2 | ROW_TEXT3 | ROW_TEXT4 | ROW_ICON;
	}

	@Override
	public void haCanviat(Boolean b) {
		// TODO Auto-generated method stub
		
	}


	private class ArrayAdapter<T> {
	}
}

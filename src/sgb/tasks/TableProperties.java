package sgb.tasks;

import java.util.ArrayList;

import android.database.Cursor;

public class TableProperties {
	String name;
	ArrayList<TableFieldProperties> tableFieldProperties;

	TableProperties(OrdersHelper helper, String Sql) {
		this.name = name;

	//	Cursor cur = helper.execSQL("DROP TABLE view_tmp ");
		String Create = "CREATE TEMP TABLE  view_tmp1 AS "+Sql+ " ";
		helper.getWritableDatabase().execSQL(Create);

		String sql = "select * from view_tmp1 ";
		Cursor cur = helper.execSQL(sql);
		while (cur.moveToNext() == true) {
			int i=0;
			i++;
		}


		tableFieldProperties = new ArrayList<TableFieldProperties>();
	//		String sql = "PRAGMA TABLE_INFO(" + name + ")";
		sql = "PRAGMA TABLE_INFO(view_tmp1) ";
		cur = helper.execSQL(sql);
		while (cur.moveToNext() == true) {
			TableFieldProperties field = new TableFieldProperties(this);
			field.setId(cur.getInt(cur.getColumnIndex("cid")));
			field.setName(cur.getString(cur.getColumnIndex("name")));
			field.setType(cur.getString(cur.getColumnIndex("type")));
			field.setNotnull(cur.getInt(cur.getColumnIndex("notnull")));
			field.setDfltValue(cur.getString(cur.getColumnIndex("dflt_value")));
			field.setPk(cur.getInt(cur.getColumnIndex("pk")));
			tableFieldProperties.add(field);
		}

	}
	String getTableName()
	{
		return name;
	}
	public ArrayList<TableFieldProperties> getTableFieldProperties() {
		return tableFieldProperties;
	}
	}



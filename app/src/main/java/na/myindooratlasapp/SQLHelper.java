package na.myindooratlasapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.math.*;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class SQLHelper extends SQLiteOpenHelper {
    final static String DATABASE_NAME= "Locations";
    public SQLHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table Rooms " +
                        "(id varchar primary key, floor varchar,latitude varchar,longitude varchar)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS Rooms");
        onCreate(db);
    }
    public void insert(String id,String floor,String latitude,String longitude)
    {

        SQLiteDatabase s=this.getWritableDatabase();
        s.execSQL("insert into rooms values('"+id+"','"+floor+"','"+latitude+"','"+longitude+"')");

    }
    public Cursor show()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from rooms ", null );
        return res;

    }
    public Destination getTarget(String id)
    {   Destination destination;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from rooms where id='"+id+"';", null );
        if(res.getCount()==0)
        {
            destination = new Destination();
            return destination;
        }
        res.moveToFirst();
        destination= new Destination(res);
        return destination;
    }
    public String[] getarray()
    {   SQLiteDatabase db = this.getReadableDatabase();
    String[] rooms;
        ArrayList<String> ids = new ArrayList<String>();
        Cursor res =  db.rawQuery( "select id from rooms;", null );
        res.moveToFirst();
        if(res.getCount()!=0)
        {
            res.moveToNext();
            do {
                ids.add(res.getString(0));
            }while (res.moveToNext());
        }
        rooms= ids.toArray(new String[ids.size()]);
        return rooms;
    }
    public String Rgeocode(LatLng pt,int floor)
    {   SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select id,latitude,longitude from rooms where floor='"+String.valueOf(floor)+"';", null );
        res.moveToFirst();

        float minimum_distance,tmp;
        String name;
        res.moveToNext();
        double x1 = 0,y1,x2,y2;
        x1=pt.latitude;
        y1=pt.longitude;
        x2=Double.parseDouble(res.getString(1));
        y2=Double.parseDouble(res.getString(2));
        double dx=Math.pow((x2-x1),2);
        double dy=Math.pow((y2-y1),2);
        minimum_distance= (float) Math.sqrt(dy+dx);
        name= res.getString(0);
        do {x2=Double.parseDouble(res.getString(1));
            y2=Double.parseDouble(res.getString(2));
            dx=Math.pow((x2-x1),2);
            dy=Math.pow((y2-y1),2);
            tmp= (float) Math.sqrt(dy+dx);
            if(tmp<minimum_distance)
            {
                minimum_distance=tmp;
                name = res.getString(0);
            }
            //;
        }while (res.moveToNext());

        return name;
    }

}

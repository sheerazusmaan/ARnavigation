package na.myindooratlasapp;

import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;

public class Destination {
LatLng point;
String name ;
int floor ;
public Destination(Cursor res)
    {
        point = new LatLng(Double.parseDouble(res.getString(2)),Double.parseDouble(res.getString(3)));
        name = res.getString(0);
        floor= Integer.parseInt(res.getString(1));
    }
    public Destination()
    {
        point = new LatLng(0,0);
        name="";
        floor=0;
    }
public int getfloor()
{
    return floor ;
}
public String getName()
{
    return name;
}
public LatLng getPoint()
{
    return point;
}
}

package na.myindooratlasapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class DHelper {
    final static String file = "LOCATIONS.csv";
    static SQLiteDatabase sqLiteDatabase;
    static BufferedReader reader;
    static SQLHelper sqlHelper;
    public static String storeToDB(InputStream input,Context c)
    {

        String line1="no";
        try {

            reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            line1=line;
            String[] record;
            sqlHelper=new SQLHelper(c);

            //sqLiteDatabase=SQLiteDatabase.openOrCreateDatabase("Locations",null,null);
            while((line = reader.readLine())!= null){


                record= line.split(",");
                sqlHelper.insert(record[0],record[1],record[2],record[3]);
            }

            // byte buffer into a string

        } catch (IOException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            return "error";
        }
    return line1;
    }
}

package edu.uoregon.casls.aris_android.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.uoregon.casls.aris_android.GamePlayActivity;
import edu.uoregon.casls.aris_android.GamesListActivity;
import edu.uoregon.casls.aris_android.R;

/**
 * Created by smorison on 7/16/15.
 */
public class AppUtils {

	private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 22;

	public static boolean isNetworkAvailable(Context context) {
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if ("WIFI".equals(ni.getTypeName()))
				if (ni.isConnected())
					haveConnectedWifi = true;
			if ("MOBILE".equals(ni.getTypeName()))
				if (ni.isConnected())
					haveConnectedMobile = true;
		}
		return haveConnectedWifi || haveConnectedMobile;
	}


	public static Location getGeoLocation(Context context) {
		if ( ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions( (Activity) context, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
					MY_PERMISSION_ACCESS_COURSE_LOCATION );
		}

		if ( Build.VERSION.SDK_INT >= 23 &&
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
				&& ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
//								&& isLocationEnabled(context)
				) {
			return new Location("0");
		}
		// Get LocationManager object from System Service LOCATION_SERVICE
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getProviders(true);
		Location bestLocation = null;
		for (String provider : providers) {
			Location l = locationManager.getLastKnownLocation(provider);
			if (l == null) {
				continue;
			}
			if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
				// Found best last known location: %s", l);
				bestLocation = l;
			}
		}
		return bestLocation;
//		// Create a criteria object to retrieve provider
//		Criteria criteria = new Criteria();
//		// Get the name of the best provider
//		String provider = locationManager.getBestProvider(criteria, true);
//		// Return Current Location
//		Location l = locationManager.getLastKnownLocation(provider);
//		return l;
////		return locationManager.getLastKnownLocation(provider);

	}

	public static boolean isLocationEnabled(Context context) {
		int locationMode = 0;
		String locationProviders;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			try {
				locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

			} catch (Settings.SettingNotFoundException e) {
				e.printStackTrace();
			}

			return locationMode != Settings.Secure.LOCATION_MODE_OFF;

		}else{
			locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			return !TextUtils.isEmpty(locationProviders);
		}


	}
	public class deltaMap {
		Map<String, Map<String, Object>> deltas;
	}

	public static File gameStorageFile(GamePlayActivity gamePlayAct) {
		String gameFileName = String.valueOf(gamePlayAct.mGame.game_id) + "_game.json";
		File appDir = new File(gamePlayAct.getFilesDir().getPath());
		File gameFile = new File(appDir, gameFileName);
		return gameFile;
	}

	public static File gameStorageFile(Context context, Long gameId) {
		String gameFileName = gameId + "_game.json";
		File appDir = new File(context.getFilesDir().getPath());
		File gameFile = new File(appDir, gameFileName);
		return gameFile;
	}

//	public static boolean streamFileExists(Context context, File file) {
//
//	}

	public static void writeToFileStream(Context context, File file, String stringData) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file.getName(), Context.MODE_PRIVATE));
			outputStreamWriter.write(stringData);
			outputStreamWriter.close();
		}
		catch (IOException e) {
			Log.e("Exception", "File write failed: " + e.toString());
		}
	}

	public static String readFromFileStream(Context context, File file) {

		String ret = "";

		try {
			InputStream inputStream = context.openFileInput(file.getName());

			if (inputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();

				while ((receiveString = bufferedReader.readLine()) != null) {
					stringBuilder.append(receiveString);
				}

				inputStream.close();
				ret = stringBuilder.toString();
			}
		} catch (FileNotFoundException e) {
			Log.e("login activity", "File not found: " + e.toString());
		} catch (IOException e) {
			Log.e("login activity", "Can not read file: " + e.toString());
		}
		return ret;
	}

	/**
	 * Get a diff between two dates
	 * @param date1 the oldest date
	 * @param date2 the newest date
	 * @param timeUnit the unit in which you want the diff
	 * @return the diff value, in the provided unit
	 */
	public static long getTimeDiff(Date date1, Date date2, TimeUnit timeUnit) {
		long diffInMillies = date2.getTime() - date1.getTime();
		return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}

	/**
	 * Convert string of unknown case to first char capitalized string.
	 * @param uglyName String with unknown capitalization
	 * @return String with first character capitalized, with remaining characters in lower case.
	 */
	public static String prettyName(String uglyName) {
		return uglyName.substring(0, 1).toUpperCase() + uglyName.substring(1).toLowerCase();
	}

	public static String getArisJs(Activity act) {
		try {
			Resources res = act.getResources();
			InputStream inStream = res.openRawResource(R.raw.arisjs);

			byte[] b = new byte[inStream.available()];
			inStream.read(b);
			return new String(b);
		} catch (Exception e) {
			// e.printStackTrace();
			return "";
		}
	}

	//	public static String convertStreamToString(InputStream is) throws Exception {
//		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//		StringBuilder sb = new StringBuilder();
//		String line = null;
//		while ((line = reader.readLine()) != null) {
//			sb.append(line).append("\n");
//		}
//		reader.close();
//		return sb.toString();
//	}

//	public static String getArisJsFromFile () throws Exception {
//		File fl = new File();
//		FileInputStream fin = new FileInputStream(fl);
//		String ret = convertStreamToString(fin);
//		//Make sure you close all streams.
//		fin.close();
//		return ret;
//	}

	// from suggestion: http://stackoverflow.com/a/29862162
	public static Bitmap decodeImageFile(File f, int WIDTH, int HIGHT){
		try {
			//Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f),null,o);

			//The new size we want to scale to
			final int REQUIRED_WIDTH=WIDTH;
			final int REQUIRED_HIGHT=HIGHT;
			//Find the correct scale value. It should be the power of 2.
			int scale=1;
			while(o.outWidth/scale/2>=REQUIRED_WIDTH && o.outHeight/scale/2>=REQUIRED_HIGHT)
				scale*=2;

			//Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize=scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {}
		return null;
	}

}

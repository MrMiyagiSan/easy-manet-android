package es.upv.grc.easymanet.android.activities;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import es.upv.grc.easymanet.android.Node;
import es.upv.grc.easymanet.android.R;
import es.upv.grc.easymanet.android.User;
import es.upv.grc.easymanet.android.services.AnnounceAndDicoveryService;

/**
 * @author Salavador Morera i Soler
 *
 */
@TargetApi(9)
public class MainActivity extends Activity implements OnSharedPreferenceChangeListener{

	// Debugging
	private static final String TAG = "EasyMANET MainActivity";
	private static final boolean D = true;

	// Tipos de Message enviados desde el Handler en AnnounceAndDiscoveryService
	public static final int NEW_USER 				= 1;
	public static final int USER_DISCONNECTED 		= 2;
	public static final int USER_UPDATED 			= 3;
	public static final int USER_REQUESTS_SERVICE 	= 4;
	public static final int NEW_NODELIST_RECEIVED 	= 5;

	// Preferences
	private static SharedPreferences sharedPref;

	// Views
	private static TextView ipTV;
	private static TextView usrNickTV;
	private static TextView bcAddTV;
	private static TextView infoTV;
	private static ImageButton nodoIB;


	private static User user;
	private static InetAddress bcAddIA;


	// Intent Request codes
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int TAKE_PICTURE = 100;
	private static final int PICK_IMAGE = 101;

	private AnnounceAndDicoveryService mAnnounceAndDiscoveryService; 
	private Handler handler;
	private Callback mCallback;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "MainActivity creada");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Obtención de referencias a los elementos de la GUI
		ipTV = (TextView) findViewById(R.id.ipTextView);
		bcAddTV = (TextView) findViewById(R.id.broadcastTV);
		usrNickTV = (TextView) findViewById(R.id.usrNickTV);
		nodoIB = (ImageButton)findViewById(R.id.usuarioIB);
		infoTV = (TextView) findViewById(R.id.infoTV);
		infoTV.setMovementMethod(new ScrollingMovementMethod());

		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPref.registerOnSharedPreferenceChangeListener(this);


		//    	ownIP = retrieveOwnIP();
		//    	ipTV.setText(ownIP.getHostAddress());
		//    	usrNickTV.setText(sharedPref.getString("userName", getString(R.string.usr_nick_sp)));
		user = new User();
		user.setNode(new Node(getLocalIpAddress(), new Date(), new Date()));
		//    	usuario.setIP(getLocalIpAddress());
		bcAddIA = getBroadcastAddress();
		if (bcAddIA!=null)
			bcAddTV.setText(bcAddIA.toString());

		applyPreferences();

		nodoIB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				takePhoto(v);
			}
		});

		startBroadcast();
		//    	startServices();
	}


	private void startServices() {
		// TODO Auto-generated method stub
		// Por cada servicio ofertado, abrir un puerto de escucha TCP
		// O bien abrir uno sólo e iniciar un servicio o otro según tipo de petición
		// Reaccionar a cada petición: preguntando al usuario si quiere aceptar la conexión,
		// creando conexión para cada petición aceptada, y abriendo actividad si el usuario lo desea.
		// 
	}

	private void startBroadcast() {
		// TODO Auto-generated method stub
		// obtener lista de nodos conocidos
		// Broadcast lista de nodos conocidos a nodos conocidos
		// Sleep(millisconds)

		mCallback = new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				// TODO acciones en caso de recibir un mensaje del servicio 
				switch (msg.what) {
				case NEW_USER: 
					Node nodo = (Node)msg.obj;
					infoTV.append("\n Nuevo usuario encontrado: " + msg.arg1 + " IP: " + nodo.getIp());
					return true;
				case USER_DISCONNECTED:
					infoTV.append("Usuario desconectado!\n");
					return true;
				case USER_UPDATED:
					infoTV.append("Información de usuario actualizada.\n");
					return true;
				case USER_REQUESTS_SERVICE:
					infoTV.append("El usuario tal quiere iniciar un intercambio.");
					return true;
				case NEW_NODELIST_RECEIVED:
					infoTV.append("\nNueva lista de nodos recibida");
					return true;
				}			
				return false;
			}
		};
		handler = new Handler(mCallback);
		mAnnounceAndDiscoveryService = new AnnounceAndDicoveryService(this.getApplicationContext(), handler);
		mAnnounceAndDiscoveryService.start();
	}

	private InetAddress getBroadcastAddress() {
		WifiManager mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		WifiInfo info = mWifi.getConnectionInfo();
		if(D)Log.d(TAG,"\n\nWiFi Status: " + info.toString());

		DhcpInfo dhcp = mWifi.getDhcpInfo(); 
		if (dhcp == null) { 
			Log.d(TAG, "Could not get dhcp info"); 
			return null; 
		} 

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask; 
		byte[] quads = new byte[4]; 
		for (int k = 0; k < 4; k++) 
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		// Returns the InetAddress corresponding to the array of bytes. 
		try {
			// The high order byte is quads[0].
			return InetAddress.getByAddress(quads);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}  
	}  


	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Cambiar método onDestroy cuando el service sea un andorid.app.Service
		mAnnounceAndDiscoveryService.stop();
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//    	switch (item.getItemId()) {
		//    	case EDIT_ID:
		startActivity(new Intent(this, SettingsActivity.class));
		//    		return(true);
		//    	}

		return(super.onOptionsItemSelected(item));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		applyPreferences();
	}

	public void applyPreferences() {
		String newUserName = sharedPref.getString("userName", getString(R.string.usr_nick_sp));
		String newImgUriString = sharedPref.getString("urlUserImg", "");
		Log.i(TAG, "UserName: " + newUserName);
		if ( !(((usrNickTV.getText())).equals((CharSequence)newUserName)) ) {
			usrNickTV.setText(newUserName);
		}
		user.setNick(newUserName);
		ipTV.setText(user.getNode().getIp());
		if ( 	(sharedPref.contains("urlUserImg")) && 
				((newImgUriString = sharedPref.getString("urlUserImg", "")) != null )) {
			// TODO check si la imagen del usuario guardada en preferencias se ha borrado
			BitmapDrawable dBitmap = new BitmapDrawable(newImgUriString);
			user.setImgUri(Uri.fromFile(new File(newImgUriString)));
			// FIXME user.getImgBmp devuelve null porque aún no se le ha asignado
			nodoIB.setImageBitmap(user.getImgBmp());
			// TODO mirar documentación de setMaxHeight(int i)
			//			nodoIB.setMaxHeight(100);
			//nodoIB.setImageURI( Uri.fromFile(new File(newImgUriString)));
		}
	}

	public String getLocalIpAddress() {
		//		// TODO Comprobar que la wifi está iniciada. ¿Comprobar que la dirección es, efectivamente, local?
		//	    try {
		//	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
		//	            NetworkInterface intf = en.nextElement();
		//	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
		//	                InetAddress inetAddress = enumIpAddr.nextElement();
		//	                if (!inetAddress.isLoopbackAddress()) {
		//	                    return inetAddress.getHostAddress().toString();
		//	                }
		//	            }
		//	        }
		//	    } catch (SocketException ex) {
		//	        Log.e(TAG, ex.toString());
		//	    }
		//	    return null;	
		WifiManager mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		WifiInfo info = mWifi.getConnectionInfo();
		if(D)Log.d(TAG,"\n\nWiFi Status: " + info.toString());

		// TODO Usar la mac para distinguir entre cambios de configuración y 
		// cambios de usuario, guardar conocidos, avisar de que ha conocidos en la red, etc
		//		String mac = info.getMacAddress();

		DhcpInfo dhcp = mWifi.getDhcpInfo(); 
		if (dhcp == null) { 
			Log.d(TAG, "Could not get dhcp info"); 
			return null; 
		} 
		byte[] bytes = BigInteger.valueOf(dhcp.ipAddress).toByteArray();
		byte[] reversed = new byte[bytes.length];
		for (int i=0;i<bytes.length;i++)
		{
			reversed[bytes.length-i-1]=bytes[i];
		}

		InetAddress address;
		try {
			address = InetAddress.getByAddress(reversed);
			return address.toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
		//	        if (resultCode == RESULT_OK) {
		//	            // Image captured and saved to fileUri specified in the Intent
		//	            Toast.makeText(this, "Image saved to:\n" +
		//	                     data.getData(), Toast.LENGTH_LONG).show();
		//	        } else if (resultCode == RESULT_CANCELED) {
		//	            // User cancelled the image capture
		//	        } else {
		//	            // Image capture failed, advise user
		//	        }
		//	    }
		switch (requestCode) {
		case TAKE_PICTURE:
			Uri u;
			File fi = new File(Environment.getExternalStorageDirectory().getPath()+File.pathSeparator+"tmp.jpg");
			try {
				u = data.getData();
				//u = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), fi.getAbsolutePath(), null, null));
				Log.d(TAG, "Ruta:" + u.toString());
				if (!fi.delete()) {
					Log.i("logMarker", "Failed to delete " + fi);
				}
			} catch /*(FileNotFoundException e)*/ (Exception e) {
				e.printStackTrace();
			}

		case PICK_IMAGE:
			if (resultCode == RESULT_OK) {

				Uri _uri = data.getData();
				if (_uri != null) {
					//User had pick an image.
					Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
					cursor.moveToFirst();

					//Link to the image
					final String imageFilePath = cursor.getString(0);
					cursor.close();
					Editor editor = sharedPref.edit();
					editor.putString("urlUserImg", imageFilePath);
					//editor.commit();
					editor.apply();
				}
			}
		}
	}



	protected void takePhoto() {
		//		// create Intent to take a picture and return control to the calling application
		//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//
		//        //fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
		//        //intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
		//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()+File.pathSeparator+"tmp.jpg")));
		//
		//        // start the image capture Intent
		//        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE); // TODO I18N
	}

	private Uri imageUri;

	public void takePhoto(View view) {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
		intent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(photo));
		imageUri = Uri.fromFile(photo);
		startActivityForResult(intent, TAKE_PICTURE);
	}


	/////////////////////////////////////////////////
	// file storage
	/////////////////////////////////////////////////

	public static final int MEDIA_TYPE_IMAGE = 1;

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "EasyMANET");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_"+ timeStamp + ".jpg");
		} else {
			return null;
		}

		return mediaFile;
	}

	//	@Override
	//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	//		super.onActivityResult(requestCode, resultCode, data);
	//		switch (requestCode) {
	//		case TAKE_PICTURE:
	//			if (resultCode == Activity.RESULT_OK) {
	//				Uri selectedImage = imageUri;
	//				getContentResolver().notifyChange(selectedImage, null);
	//				//	            ImageView imageView = (ImageView) findViewById(R.id.ImageView);
	//				ContentResolver cr = getContentResolver();
	//				Bitmap bitmap;
	//				try {
	//					bitmap = android.provider.MediaStore.Images.Media
	//							.getBitmap(cr, selectedImage);
	//
	//					//	                imageView.setImageBitmap(bitmap);
	//					BitmapDrawable dBitmap = new BitmapDrawable(bitmap);
	//					nodoIB.setImageDrawable(dBitmap);
	//					Toast.makeText(this, selectedImage.toString(),
	//							Toast.LENGTH_LONG).show();
	//				} catch (Exception e) {
	//					Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
	//					.show();
	//					Log.e("Camera", e.toString());
	//				}
	//			}
	//		}
	//	}
	//
	//	private InetAddress retrieveOwnIP() {
	//		WifiManager wifiM = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	//		InetAddress ownIP;
	//
	//		if (!wifiM.setWifiEnabled(true)) {
	//			Log.e(TAG, "No se ha podido iniciar el wifi");
	//		}
	//		else {
	//			Log.i(TAG, "wifi iniciada");
	//		}
	//
	//		try {
	//			ownIP=InetAddress.getLocalHost();
	//			if (D) Log.i(TAG, "IP local := "+ownIP.getHostAddress());
	//			return ownIP;
	//		} catch (Exception e) {
	//			Log.e(TAG, e.getMessage());
	//			return null;
	//		}
	//	} 
}

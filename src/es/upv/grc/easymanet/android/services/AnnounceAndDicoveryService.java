/**
 * 
 */
package es.upv.grc.easymanet.android.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Locale;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.RegistryMatcher;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import es.upv.grc.easymanet.android.Node;
import es.upv.grc.easymanet.android.NodeList;
import es.upv.grc.easymanet.android.User;
import es.upv.grc.easymanet.android.activities.MainActivity;
import es.upv.grc.easymanet.android.utils.DateFormatTransformer;


/**
 * @author Salavador Morera i Soler
 *
 */
public class AnnounceAndDicoveryService { 
	// Debugging
	private static final String TAG = "AnnounceAndDiscoveryService";
	private static final String TAG_AN = "AnnounceThread";
	private static final String TAG_DI = "DiscoveryThread";
	private static final boolean D = true;
	// Member fields
	private final Handler mHandler;
	private DiscoveryThread mDiscoveryThread;
	private AnnounceThread mAnnounceThread;
	private DatagramSocket mSocket ;
	private Context mContext;
	private ArrayList<User> mUsersAL;
	private NodeList mNodeList;
	protected static InetAddress myBcastIA, myLocalIA;
	// Constants
	public static final int PORT = 12580;

	Serializer serializer;



	/**
	 * Constructor.
	 * @param context  El Context de la (main)Activity UI
	 * @param handler  El Handler para comunicarse con la Activity UI
	 */
	public AnnounceAndDicoveryService(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}


	/**
	 * Inicia el Service.
	 * Lanza un Thread de escucha (DiscoveryThread) para recibir las listas de usuarios(nodos) y
	 * un Thread que anuncia por broadcast peri�dicamente la lista propia de usuarios(nodos) conocidos.
	 * 
	 */
	public synchronized void start() {
		if (D) Log.d(TAG, "start");

		mNodeList = new NodeList();
		try {
			myLocalIA = getLocalAddress();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			myBcastIA = getBroadcastAddress();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// Se a�ade al serializador de SimpleXML la capacidad de usar fechas en el formato deseado.
		// Quitamos del formato el timezone, porque SimpleXML no es capaz de interpretar correctamente todas 
		// las posibles implementaciones. Esto no deber�a ser un problema, ya que en una red ad-hoc (o wifi)
		// podemos suponer que todos los dispositivos estar�n en la misma zona horaria.
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.US);
		RegistryMatcher m = new RegistryMatcher();
		m.bind(Date.class, new DateFormatTransformer(dateFormat));

		serializer = new Persister(m);

		mAnnounceThread = new AnnounceThread();
		mDiscoveryThread = new DiscoveryThread();
		mAnnounceThread.start();
		mDiscoveryThread.start();

	}


	/**
	 * Detiene el Service.
	 */
	public synchronized void stop() {
		if (D) Log.d(TAG, "stop");
		if (mDiscoveryThread != null) {
			mDiscoveryThread.cancel(); 
			mDiscoveryThread = null;
		}
		if (mAnnounceThread != null) {
			mAnnounceThread.cancel();
			mAnnounceThread = null;
		}
	}	


	/**
	 * Este Thread escucha para obtener las listas de nodos de otros usuarios,
	 * las compara con la lista propia actual, y si descubre nuevos nodos, obtiene
	 * los detalles de los usuarios, pas�ndoselos a la MainActivity.
	 */    
	private class DiscoveryThread extends Thread {

		public DiscoveryThread() {}

		public void run() {

			try {
				byte[] buf = new byte[1024]; 

				while (true) { 
					DatagramPacket packet = new DatagramPacket(buf, buf.length); 
					mSocket.receive(packet);

					InetAddress remoteIP = packet.getAddress();

					if(remoteIP.equals(myLocalIA))
						continue;

					String xmlData = new String(packet.getData(), 0, packet.getLength()); 
					if (D) {
						Log.d(TAG_DI, "Received  packet from " + remoteIP.getAddress().toString());
						Log.v(TAG_DI, "Contents of packet from " + remoteIP.getAddress().toString() + " :\n" + xmlData); 
					}
					mHandler.obtainMessage(MainActivity.NEW_NODELIST_RECEIVED,-1,-1, null).sendToTarget();

					// TODO: Analizar packet en busca de nueva informaci�n de usuarios.

					try {
						NodeList receivedNodeList = serializer.read(NodeList.class, xmlData, false);
						// FIXME DESCOMENTAR MERGE
//						mNodeList.merge(receivedNodeList);

						ListIterator<Node> it = receivedNodeList.mNodes.listIterator();
						while (it.hasNext()) {
							// TODO: Lanzar conexiones TCP para obtener los datos de los usuarios nuevos
							// Send the obtained bytes to the UI Activity
							mHandler.obtainMessage(MainActivity.NEW_USER, it.nextIndex(),-1, it.next()).sendToTarget();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						if (D) Log.e(TAG_DI, "Error obteniendo nodeList de xml recibido:\n" + xmlData + "\nError: " + e.getMessage()); 
						e.printStackTrace();
					}

				} 
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void cancel() {
			try {
				if (!mSocket.isClosed()) {
					mSocket.close();
				}
			} catch (Exception e) {
				Log.e(TAG, "close() of mSocket from DiscoveryThread failed", e);
			}
		}
	}    

	private class AnnounceThread extends Thread {

		Node selfNode;

		public AnnounceThread() {

			DateFormat dateFormat;
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.US);

			// Horrible e ineficiente hack para evitar errores en la lectura de los xml, debidos a que SimpleXML 
			// tiene un bug en la interpretaci�n del timezone tal y como lo expresa Android 4.2.2 (CET), no as� con
			// como lo expresa Android 2.3.6 (GMT+01:00), a pesar de que, supuestamente el formato "yyyy-MM-dd HH:mm:ss.S z"
			// lo acepta y ambas expresiones son v�lidas seg�n http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
			try {
				Date now = new Date(); 
				String nowStr = dateFormat.format(now);
				selfNode = Node.newNode(myLocalIA.toString(), nowStr, nowStr);
			} catch (ParseException e) {
				if (D) {
					Log.e(TAG_AN, "Error parseando una fecha.\n", e);
				}
			}

			// Se a�ade a s� mismo a la lista de nodos.
			mNodeList.add(selfNode);

			try { 
				if(D)Log.d(TAG_AN, "Entering AnnounceThread Constructor");

				mSocket = new DatagramSocket(PORT);
				mSocket.setBroadcast(true); 


			} catch (IOException e) { 
				Log.e(TAG, "Could not make socket", e); 
			} 
		}


		public void cancel() {
			try {
				if (!mSocket.isClosed()) {
					mSocket.close();
				}
			} catch (Exception e) {
				Log.e(TAG, "close() of mSocket from AnnounceThread failed", e);
			}
		}


		public void run() {

			try {

				byte[] buf; 

				// Serialize & Broadcast NodeList
				while (true) { 
					if(D)Log.d(TAG, "AnnunceThread.run()");
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					try {
						serializer.write(mNodeList, out);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					buf = out.toByteArray();

					DatagramPacket packet = new DatagramPacket(buf, buf.length, myBcastIA, PORT); 
					mSocket.send(packet);
					if(D) {
						Log.d(TAG_AN, "Datagram Sent:\n");
						Log.v(TAG_AN, "Datagram Content:\n" + out.toString());
					}
					try
					{
						AnnounceThread.sleep(2000);
					} catch (InterruptedException e){

					}
				} 
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** 
	 * Calculate the broadcast IP. 
	 */ 
	private InetAddress getBroadcastAddress() throws IOException {
		WifiManager mWifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

		WifiInfo info = mWifi.getConnectionInfo();
		if(D)Log.v(TAG,"\n\nWiFi Status: " + info.toString());

		// DhcpInfo  is a simple object for retrieving the results of a DHCP request
		DhcpInfo dhcp = mWifi.getDhcpInfo(); 
		if (dhcp == null) { 
			Log.e(TAG, "Could not get dhcp info"); 
			return null; 
		} 

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask; 
		byte[] quads = new byte[4]; 
		for (int k = 0; k < 4; k++) 
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

		return InetAddress.getByAddress(quads); // The high order byte is quads[0].
	}  


	/**
	 * 
	 * @return InetAddress The local address in ipV6
	 * @throws IOException
	 */

	private InetAddress getLocalAddress()throws IOException {

		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();

				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
						//return inetAddress.getHostAddress().toString();
						return inetAddress;
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(TAG, ex.toString());
		}
		return null;
	}


	/**
	 * @return the mNodeList
	 */
	public NodeList getNodeList() {
		return mNodeList;
	}


	/**
	 * @param mNodeList the mNodeList to set
	 */
	public void setNodeList(NodeList nodeList) {
		this.mNodeList = nodeList;
	}
}

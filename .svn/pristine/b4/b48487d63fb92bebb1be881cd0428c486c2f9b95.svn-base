package es.upv.grc.easymanet.android;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class User implements Serializable {

	static final long serialVersionUID = 1L;

	//	static AtomicInteger numNodos = new AtomicInteger(0);

	// Información básica
	private Node mNode;
	private String mNick;
	private Uri mImgUri;
	private Bitmap mImgBmp;
	//	boolean what;

	// Servicios
	private HashMap<String, Boolean> services;

	// Constants
	public static final int PORT = 12580;

	public User() {
		super();
		//		numNodos.getAndIncrement();
	}


	/**
	 * 
	 * @param mImgUri
	 * @param mNick
	 * @param iP
	 */
	public User(Uri userImgUri, String usrNick, Node node) {
		super();
		//		numNodos.getAndIncrement();
		this.mImgUri = userImgUri;
		this.mNick = usrNick;
		this.mNode = node;
	}

	/**
	 * @param mNode
	 * @param mNick
	 * @param mImgUri
	 * @param services
	 */
	public User(Node mNode, String mNick, Uri mImgUri, HashMap<String, Boolean> services) {
		super();
		this.mNode = mNode;
		this.mNick = mNick;
		this.mImgUri = mImgUri;
		this.services = services;
	}


	/**
	 * @return the mNode
	 */
	public Node getNode() {
		return mNode;
	}


	/**
	 * @param mNode the mNode to set
	 */
	public void setNode(Node mNode) {
		this.mNode = mNode;
	}


	/**
	 * @return the mNick
	 */
	public String getNick() {
		return mNick;
	}


	/**
	 * @param mNick the mNick to set
	 */
	public void setNick(String mNick) {
		this.mNick = mNick;
	}


	/**
	 * @return the mImgUri
	 */
	public android.net.Uri getImgUri() {
		return mImgUri;
	}


	/**
	 * @param mImgUri the mImgUri to set
	 */
	public void setImgUri(android.net.Uri mImgUri) {
		this.mImgUri = mImgUri;
	}


	public Bitmap getImgBmp() {
		return mImgBmp;
	}


	public void setImgBmp(Bitmap imgBmp) {
		this.mImgBmp = imgBmp;
	}


	/**
	 * @return the services
	 */
	public HashMap<String, Boolean> getServices() {
		return services;
	}


	/**
	 * @param services the services to set
	 */
	public void setServices(HashMap<String, Boolean> services) {
		this.services = services;
	}

	public boolean getUserImage() {
		GetUserImageTask getUserImageTask = new GetUserImageTask();
		getUserImageTask.doInBackground(this);
		return true;
	}


	private class GetUserImageTask extends AsyncTask<User, Integer, Long> { // 	android.os.AsyncTask<Params, Progress, Result>
		protected Long doInBackground(User... users) {
			int count = users.length;
			long totalSize = 0;
			for (int i = 0; i < count; i++) {
//				totalSize += Downloader.downloadFile(urls[i]);
//				publishProgress((int) ((i / (float) count) * 100));
				// Escape early if cancel() is called
				if (isCancelled()) break;
			}
			return totalSize;
		}

		protected void onProgressUpdate(Integer... progress) {
//			setProgressPercent(progress[0]);
		}

		protected void onPostExecute(Long result) {
//			showDialog("Downloaded " + result + " bytes");
		}
	}

	private void sendMessage(String command) {
		try {
			InetAddress serverAddr = InetAddress.getByName(mNode.getIp()); // FIXME Al InetAddress.getByName se le puede pasar 192.234...?
			Log.d("ClientActivity", "C: Connecting...");
			Socket socket = new Socket(serverAddr, PORT);
//			connected = true;
//			while (connected) {
				try {
					Log.d("ClientActivity", "C: Sending command.");
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
							.getOutputStream())), true);
					// WHERE YOU ISSUE THE COMMANDS
					out.println(command);
					Log.d("ClientActivity", "C: Sent.");
				} catch (Exception e) {
					Log.e("ClientActivity", "S: Error", e);
				}
//			}
			socket.close();
			Log.d("ClientActivity", "C: Closed.");
		} catch (Exception e) {
			Log.e("ClientActivity", "C: Error", e);
//			concected = false;
		}
	}


	//	Once created, a task is executed very simply:

	//	 new DownloadFilesTask().execute(url1, url2, url3);

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "User [mNode=" + mNode + ", mNick=" + mNick + ", mImgUri="
				+ mImgUri + ", services=" + services + "]";
	}

}

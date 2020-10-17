package es.upv.grc.easymanet.android;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import android.util.Log;


/**
 * @author Saoro
 *
 */
public class Node {

	//	static final long serialVersionUID = 2L;
	// Debugging
	private static final String TAG = "Node";
	private static final boolean D = true;

	@Attribute (name="ip")
	private String mIp;
	@Element  (name="timestamp")
	private Date mTimestamp;
	@Element (name="lastseen")
	private Date mLastSeen;



	public Node(@Attribute(name="ip") String ip, @Element(name="timestamp")Date timestamp, @Element(name="lastseen")Date lastSeen) {
		this.mIp = ip;
		this.mTimestamp = timestamp;
		this.mLastSeen = lastSeen;
	}

	public static Node newNode(String ip, String timeStamp, String lastSeen) throws ParseException{
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.US);
		try {
			Date timeStampDate = dateFormat.parse(timeStamp);
			Date lastSeenDate = dateFormat.parse(lastSeen);
			Node node = new Node(ip, timeStampDate, lastSeenDate);
			return node;
		} catch (ParseException e) {
			if (D) {
				Log.e(TAG, "Error en constructor parseando una fecha.", e);
			}
		}
		return new Node();
	}

	public Node() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mIp == null) ? 0 : mIp.hashCode());
		result = prime * result
				+ ((mTimestamp == null) ? 0 : mTimestamp.hashCode());
		return result;
	}



	/**
	 * Un nodo se considera equivalente a otro si tienen la misma ip y el mismo timestamp. No se compara el campo lastSeen.
	 * @param Object el nodo a comparar
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (mIp == null) {
			if (other.mIp != null)
				return false;
		} else if (!mIp.equals(other.mIp))
			return false;
		if (mTimestamp == null) {
			if (other.mTimestamp != null)
				return false;
		} else if (!mTimestamp.equals(other.mTimestamp))
			return false;
		return true;
	}

	public String getIp() {
		return mIp;
	}
	public void setIp(String ip) {
		this.mIp = ip;
	}
	public Date getTimestamp() {
		return mTimestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.mTimestamp= timestamp;
	}
	public Date getLastSeen() {
		return mLastSeen;
	}
	public void setLastSeen(Date lastSeen) {
		this.mLastSeen = lastSeen;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Node [mIp=" + mIp + ", mTimestamp=" + mTimestamp
				+ ", mLastSeen=" + mLastSeen + "]";
	}

}

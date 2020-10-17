/**
 * 
 */
package es.upv.grc.easymanet.android;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.simpleframework.xml.ElementList;

/**
 * @author Saoro
 *
 */

public class NodeList {
	
	@ElementList(name="NodeList")
	public List<Node> mNodes = new ArrayList<Node>();
	
	public NodeList merge(NodeList nodeList) {
		Node current;
		int indiceNodo;
		ListIterator<Node> it = nodeList.getNodes().listIterator();
		while(it.hasNext()) {
			current = it.next();
			indiceNodo = this.getNodes().indexOf(current);
			if (indiceNodo > -1) { // El nodo ya era conocido
				// Si el lastSeen del recibido es más reciente que el almacenado en local, éste se actualiza 
				if (this.getNodes().get(indiceNodo).getLastSeen().before(current.getLastSeen())) {
					this.getNodes().get(indiceNodo).setLastSeen(current.getLastSeen());
				}
			} else { // Es un nuevo nodo
				add(current);
			}
		}
		return this;
	}

	public List<Node> getNodes() {
		return mNodes;
	}
	
	public void add(Node node) {
		if (node != null) {
			this.mNodes.add(node);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "NodeList [mNodes=" + mNodes + "]";
	}
	
	
}

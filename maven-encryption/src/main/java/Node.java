import java.io.Serializable;

import lombok.Data;

public @Data class Node implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String algoName = "";
	int key ;
	public Node(int key,String algoName) {
		this.key = key;
		this.algoName = algoName;
	}
	
}

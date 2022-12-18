package name.dxliu.bfs;

import java.io.Serializable;

public class IntegerEdge implements Serializable{
	private static final long serialVersionUID = -8993684754091338898L;
	private int source;
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getEdge() {
		return edge;
	}
	public void setEdge(int edge) {
		this.edge = edge;
	}
	public int getTarget() {
		return target;
	}
	public void setTarget(int target) {
		this.target = target;
	}
	private int edge;
	private int target;
	public IntegerEdge(int s,int t,int e){
		source=s;target=t;edge=e;
	}
	public IntegerEdge(){}
	@Override
	public String toString() {
		return "IntegerEdge [source=" + source + ", edge=" + edge + ", target=" + target + "]";
	}
}

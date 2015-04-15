// Copyright (c) 2012 Kenzie Lane Mosaic, LLC. All rights reserved.
package textrank;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The Edge
 *
 * @author Sang Venkatraman
 */
public class Edge {

	private Node source;
	private Node dest;

	public Edge(Node source, Node dest) {
		this.source = source;
		this.dest = dest;
	}

	public Node getSource() {
		return source;
	}

	public void setSource(Node source) {
		this.source = source;
	}

	public Node getDest() {
		return dest;
	}

	public void setDest(Node dest) {
		this.dest = dest;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Edge)) return false;

		Edge edge = (Edge) o;

		if (dest != null ? !dest.equals(edge.dest) : edge.dest != null) return false;
		if (source != null ? !source.equals(edge.source) : edge.source != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = source != null ? source.hashCode() : 0;
		result = 31 * result + (dest != null ? dest.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("source", source)
				.append("dest", dest)
				.toString();
	}
}

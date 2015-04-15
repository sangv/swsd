/*
Copyright (c) 2009, ShareThis, Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

    * Neither the name of the ShareThis, Inc., nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package textrank;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implements a node in the TextRank graph, denoting some noun or
 * adjective morpheme.
 *
 * @author paco@sharethis.com
 */

public class
    Node
    implements Comparable<Node>
{
    // logging

    private final static Log log_ =
        LogFactory.getLog(Node.class.getName());


    public double rank = 0.0D;
    public String key = null;
    public boolean marked = false;
    public NodeValue value = null;


    /**
     * Private constructor.
     */

    private
	Node (final String key, final NodeValue value)
    {
	this.rank = 1.0D;
	this.key = key;
	this.value = value;
    }


    /**
     * Compare method for sort ordering.
     */

    public int
	compareTo (final Node that)
    {
        if (this.rank > that.rank) {
	    return -1;
	}
	else if (this.rank < that.rank) {
	    return 1;
	}
	else {
	    return this.value.text.compareTo(that.value.text);
	}
    }


    /**
     * Create a unique identifier for this node, returned as a hex
     * string.
     */

    public String
	getId ()
    {
	return Integer.toString(hashCode(), 16);
    }


    /**
     * Factory method.
     */

    public static Node
	buildNode (final Graph graph, final String key, final NodeValue value)
	throws Exception
    {
	Node n = graph.get(key);

	if (n == null) {
	    n = new Node(key, value);
	    graph.put(key, n);
	}

	if (log_.isDebugEnabled()) {
	    log_.debug(n.key);
	}

	return n;
    }


    /**
     * Search nearest neighbors in WordNet subgraph to find the
     * maximum rank of any adjacent SYNONYM synset.
     */

    public double
	maxNeighbor (final Graph graph, final double min, final double coeff)
    {
	double adjusted_rank = 0.0D;

	if (log_.isDebugEnabled()) {
	    log_.debug("neighbor: " + value.text + " " + value);
	    log_.debug("  edges:");

	    for (Node n : graph.edges(this)) {
		log_.debug(n.value);
	    }
	}

	if (graph.edges(this).size() > 1) {
	    // consider the immediately adjacent synsets

	    double max_rank = 0.0D;

	    for (Node n : graph.edges(this)) {
		if (n.value instanceof SynsetLink) {
		    max_rank = Math.max(max_rank, n.rank);
		}
	    }

	    if (max_rank > 0.0D) {
		// adjust it for scale [0.0, 1.0]
		adjusted_rank = (max_rank - min) / coeff;
	    }
	}
	else {
	    // consider the synsets of the one component keyword

	    for (Node n : graph.edges(this)) {
		if (n.value instanceof KeyWord) {
		    // there will only be one
		    adjusted_rank = n.maxNeighbor(graph, min, coeff);
		}
	    }
	}

	if (log_.isDebugEnabled()) {
	    log_.debug(adjusted_rank);
	}

	return adjusted_rank;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Node)) return false;

		Node node = (Node) o;

		if (!key.equals(node.key)) return false;
		if (!value.equals(node.value)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = key.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("rank", rank)
				.append("key", key)
				.append("marked", marked)
				.append("value", value)
				.toString();
	}
}

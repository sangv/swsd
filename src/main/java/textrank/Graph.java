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

import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.*;


/**
 * An abstraction for handling the graph as a data object.
 *
 * @author paco@sharethis.com
 */

public class Graph

{
    // logging

    private final static Log LOG =
        LogFactory.getLog(Graph.class.getName());

	Map<String,Node> nodeMap = new TreeMap<String,Node>();

	edu.uci.ics.jung.graph.Graph<Node,Edge> jungGraph = new UndirectedSparseGraph<Node,Edge>();


    /**
     * Public definitions.
     */

    public final static double INCLUSIVE_COEFF = 0.25D;
    public final static double KEYWORD_REDUCTION_FACTOR = 0.8D;
    public final static double TEXTRANK_DAMPING_FACTOR = 0.85D;
    public final static double STANDARD_ERROR_THRESHOLD = 0.005D;

    /**
     * Public members.
     */

    public SummaryStatistics dist_stats = new SummaryStatistics();


    /**
     * Run through N iterations of the TreeRank algorithm, or until
     * the standard error converges below a given threshold.
     */

    public void
	runTextRank ()
    {
		for(Node node : jungGraph.getVertices()){
			System.out.println(node);
		}
	final int max_iterations = this.jungGraph.getVertexCount();


	// iterate, then sort and mark the top results

	iterateGraph(max_iterations);
    }


    /**
     * Iterate through the graph, calculating rank.
     */

    protected void
	iterateGraph (final int max_iterations)
    {
	final double[] rank_list = new double[jungGraph.getVertexCount()];

	// either run through N iterations, or until the standard
	// error converges below a threshold

	for (int k = 0; k < max_iterations; k++) {
	    dist_stats.clear();

	    // calculate the next rank for each node
		Node[] node_list = jungGraph.getVertices().toArray(new Node[]{});
	    for (int i = 0; i < node_list.length; i++) {
		final Node n1 = node_list[i];
		double rank = 0.0D;

		for (Node n2 : this.edges(n1)) {
		    rank += n2.rank / (double) jungGraph.getOutEdges(n2).size();
		}

		rank *= TEXTRANK_DAMPING_FACTOR;
		rank += 1.0D - TEXTRANK_DAMPING_FACTOR;

		rank_list[i] = rank;
		dist_stats.addValue(Math.abs(n1.rank - rank));
	    }

	    final double standard_error =
		dist_stats.getStandardDeviation() / Math.sqrt((double) dist_stats.getN());

	    if (LOG.isInfoEnabled()) {
		LOG.info("iteration: " + k + " error: " + standard_error);
	    }

	    // swap in new rank values

	    for (int i = 0; i < node_list.length; i++) {
		node_list[i].rank = rank_list[i];
	    }

	    if (standard_error < STANDARD_ERROR_THRESHOLD) {
		break;
	    }
	}
    }


    /**
     * Sort results to identify potential keywords.
     */

    public void
	sortResults (final long max_results)
    {
		Node[] node_list = jungGraph.getVertices().toArray(new Node[]{});
	Arrays.sort(node_list,
		    new Comparator<Node>() {
			public int compare (Node n1, Node n2) {
			    if (n1.rank > n2.rank) {
				return -1;
			    }
			    else if (n1.rank < n2.rank) {
				return 1;
			    }
			    else {
				return 0;
			    }
			}
		    }
		    );

	// mark the top-ranked nodes

	dist_stats.clear();

	for (int i = 0; i < node_list.length; i++) {
	    final Node n1 = node_list[i];

	    if (i <= max_results) {
		n1.marked = true;
		dist_stats.addValue(n1.rank);
	    }

	    if (LOG.isDebugEnabled()) {
		LOG.debug("n: " + n1.key + " " + n1.rank + " " + n1.marked);

		for (Node n2 : edges(n1)) {
		    LOG.debug(" - " + n2.key);
		}
	    }
	}
    }


    /**
     * Calculate a threshold for the ranked results.
     */

    public double
	getRankThreshold ()
    {
	return dist_stats.getMean() +
	    (dist_stats.getStandardDeviation() * INCLUSIVE_COEFF)
	    ;
    }

	public Collection<Node> values() {
		return nodeMap.values();
	}

	public Node get(String key) {
		if(nodeMap.containsKey(key)){
			nodeMap.get(key);
		}
		return null;
	}

	public void put(String key, Node node) {
		if(!nodeMap.containsKey(key)) {
			nodeMap.put(key, node);
			jungGraph.addVertex(node);
		}
	}

	public void connect(Node n1, Node n2) {
		jungGraph.addEdge(new Edge(n1,n2),n1,n2);
	}

	public void disconnect(Node n1, Node n2) {
		Edge e = new Edge(n1,n2);
		jungGraph.removeEdge(e);
	}

	public int size() {
		return jungGraph.getVertexCount();
	}

	public List<Node> edges(Node n) {
		List<Node> nodes = new ArrayList<Node>();
		for (Edge edge : jungGraph.getOutEdges(n)){
			nodes.add(edge.getDest());
		}
		return nodes;
	}
}


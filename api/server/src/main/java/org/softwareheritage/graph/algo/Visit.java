package org.softwareheritage.graph.algo;

import java.util.ArrayList;
import java.util.Stack;

import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.bits.LongArrayBitVector;

import org.softwareheritage.graph.Graph;
import org.softwareheritage.graph.SwhId;

public class Visit {
  public class Path extends ArrayList<SwhId> {}

  Graph graph;
  boolean isTransposed;
  String allowedEdges;
  Stack<Long> currentPath;
  ArrayList<Path> paths;
  LongArrayBitVector visited;

  public Visit(Graph graph, SwhId start, String allowedEdges, String algorithm, String direction) {
    this.graph = graph;
    this.isTransposed = (direction == "backward");
    this.allowedEdges = allowedEdges;
    this.paths = new ArrayList<Path>();
    this.currentPath = new Stack<Long>();
    this.visited = LongArrayBitVector.ofLength(graph.getNbNodes());

    if (algorithm == "dfs") {
      dfs(graph.getNode(start));
    }
  }

  // Allow Jackson JSON to only serialize the 'paths' field
  public ArrayList<Path> getPaths() {
    return paths;
  }

  private void dfs(long currentNode) {
    visited.set(currentNode);
    currentPath.push(currentNode);

    long degree = graph.degree(currentNode, isTransposed);
    LazyLongIterator neighbors = graph.neighbors(currentNode, isTransposed);

    if (degree == 0) {
      Path path = new Path();
      for (long node : currentPath) {
        path.add(graph.getSwhId(node));
      }
      paths.add(path);
    }

    while (degree-- > 0) {
      long nextNode = neighbors.nextLong();
      if (isEdgeAllowed(currentNode, nextNode) && !visited.getBoolean(nextNode)) {
        dfs(nextNode);
      }
    }

    currentPath.pop();
  }

  private boolean isEdgeAllowed(long currentNode, long nextNode) {
    // TODO
    return true;
  }
}
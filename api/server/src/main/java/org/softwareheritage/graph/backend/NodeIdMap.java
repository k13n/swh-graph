package org.softwareheritage.graph.backend;

import java.io.IOException;

import org.softwareheritage.graph.SwhId;
import org.softwareheritage.graph.backend.MapFile;

public class NodeIdMap {
  public static final int SWH_ID_LENGTH = 50;
  public static final int NODE_ID_LENGTH = 20;

  String graphPath;
  long nbIds;
  MapFile swhToNodeMap;
  MapFile nodeToSwhMap;

  public NodeIdMap(String graphPath, long nbNodes) throws IOException {
    this.graphPath = graphPath;
    this.nbIds = nbNodes;

    // +1 are for spaces and end of lines
    int swhToNodeLineLength = SWH_ID_LENGTH + 1 + NODE_ID_LENGTH + 1;
    int nodeToSwhLineLength = SWH_ID_LENGTH + 1;
    this.swhToNodeMap = new MapFile(graphPath + ".swhToNodeMap.csv", swhToNodeLineLength);
    this.nodeToSwhMap = new MapFile(graphPath + ".nodeToSwhMap.csv", nodeToSwhLineLength);
  }

  // SWH id (string) -> WebGraph node id (long)
  // Each line in .swhToNode.csv is formatted as: swhId nodeId
  // The file is sorted by swhId, hence we can binary search on swhId to get corresponding nodeId
  public long getNodeId(SwhId swhId) {
    long start = 0;
    long end = nbIds - 1;

    while (start <= end) {
      long lineNumber = (start + end) / 2L;
      String[] parts = swhToNodeMap.readAtLine(lineNumber).split(" ");
      if (parts.length != 2) {
        break;
      }

      String currentSwhId = parts[0];
      long currentNodeId = Long.parseLong(parts[1]);

      int cmp = currentSwhId.compareTo(swhId.toString());
      if (cmp == 0) {
        return currentNodeId;
      } else if (cmp < 0) {
        start = lineNumber + 1;
      } else {
        end = lineNumber - 1;
      }
    }

    throw new IllegalArgumentException("Unknown SWH id: " + swhId);
  }

  // WebGraph node id (long) -> SWH id (string)
  // Each line in .nodeToSwh.csv is formatted as: swhId
  // The file is ordered by nodeId, meaning node0's swhId is at line 0, hence we can read the
  // nodeId-th line to get corresponding swhId
  public SwhId getSwhId(long nodeId) {
    if (nodeId < 0 || nodeId >= nbIds) {
      throw new IllegalArgumentException("Node id " + nodeId + " should be between 0 and " + nbIds);
    }

    String swhId = nodeToSwhMap.readAtLine(nodeId);
    return new SwhId(swhId);
  }

  public void close() throws IOException {
    swhToNodeMap.close();
    nodeToSwhMap.close();
  }
}

package org.softwareheritage.graph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import it.unimi.dsi.big.webgraph.BVGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.fastutil.objects.Object2LongFunction;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.io.LineIterator;
import it.unimi.dsi.lang.MutableString;

public class Dataset
{
    public enum Name {
        DIR_TO_DIR,
        DIR_TO_FILE,
        DIR_TO_REV,
        ORIGIN_TO_SNAPSHOT,
        RELEASE_TO_OBJ,
        REV_TO_DIR,
        REV_TO_REV,
        SNAPSHOT_TO_OBJ
    }

    BVGraph graph;
    String path;
    HashMap<String, Long> hashToNode;
    HashMap<Long, String> nodeToHash;

    public Dataset(String datasetPath)
    {
        try {
            this.graph = BVGraph.load(datasetPath);
            this.path = datasetPath;
            setupNodesMapping();
        } catch (Exception e) {
            System.out.println("[WARNING] Could not load dataset " + datasetPath + ": " + e);
        }
    }

    void setupNodesMapping() throws IOException, ClassNotFoundException
    {
        this.hashToNode = new HashMap<String, Long>();
        this.nodeToHash = new HashMap<Long, String>();

        // First mapping: SWH hexhash (strings) <=> WebGraph MPH (longs)
        HashMap<Long, String> mphToHash = new HashMap<Long, String>();
        Object2LongFunction<String> mphMap =
            (Object2LongFunction<String>) BinIO.loadObject(path + ".mph");

        InputStream nodeFile = new FileInputStream(path + ".nodes.csv.gz");
        Collection<MutableString> hashes =
            new LineIterator(
            new FastBufferedReader(
            new InputStreamReader(
            new GZIPInputStream(nodeFile), "UTF-8"))).allLines();

        for (MutableString h : hashes)
        {
            String hash = new String(h.toString());
            long mph = mphMap.getLong(hash);
            mphToHash.put(mph, hash);
        }

        // Second mapping: WebGraph MPH (longs) <=> BFS ordering (longs)
        long n = mphMap.size();
        long[][] bfsMap = LongBigArrays.newBigArray(n);
        long loaded = BinIO.loadLongs(path + ".order", bfsMap);
        if (loaded != n)
            throw new IllegalArgumentException("Graph contains " + n + " nodes, but read " + loaded);

        // Create final mapping: SWH hexhash (strings) <=> BFS ordering (longs)
        for (long id = 0; id < n; id++)
        {
            String hash = mphToHash.get(id);
            long node = LongBigArrays.get(bfsMap, id);

            hashToNode.put(hash, node);
            nodeToHash.put(node, hash);
        }
    }

    public String getPath()
    {
        return path;
    }

    public long getNode(String hash)
    {
        return hashToNode.get(hash);
    }

    public String getHash(long node)
    {
        return nodeToHash.get(node);
    }

    public long getNbNodes()
    {
        return graph.numNodes();
    }

    public long getNbEdges()
    {
        return graph.numArcs();
    }

    public LazyLongIterator successors(long node)
    {
        return graph.successors(node);
    }

    public long outdegree(long node)
    {
        return graph.outdegree(node);
    }
}
package graph;

import heap.Heap;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.File;
import java.io.FileNotFoundException;

/*
 * Author: Ben Fry-Holman
 * Date: 8/11/2024
 * Purpose: Paths and Graphs for A4
 */

/** Provides an implementation of Dijkstra's single-source shortest paths
 * algorithm.
 * Sample usage:
 *   Graph g = // create your graph
 *   ShortestPaths sp = new ShortestPaths();
 *   Node a = g.getNode("A");
 *   sp.compute(a);
 *   Node b = g.getNode("B");
 *   LinkedList<Node> abPath = sp.getShortestPath(b);
 *   double abPathLength = sp.getShortestPathLength(b);
 *   */
public class ShortestPaths {
    // stores auxiliary data associated with each node for the shortest
    // paths computation:
    private HashMap<Node, PathData> paths;

    /**
     * Compute the shortest path to all nodes from origin using Dijkstra's algorithm. Fill in the paths field, which
     * associates each Node with its PathData record, storing total distance from the source, and the backpointer to the
     * previous node on the shortest path. Precondition: origin is a node in the Graph.
     */
    public void compute(Node origin) {
        paths = new HashMap<Node, PathData>();
        Heap<Node, Double> toProcess = new Heap<>();
        // origin node
        paths.put(origin, new PathData(0, null));
        toProcess.add(origin, 0.0);
        // do each node in heap until all done
        while (toProcess.size() > 0) {
            Node current = toProcess.poll();
            double currentDist = paths.get(current).distance;
            // gp through all neighbors of current node
            for (Map.Entry<Node, Double> neighborEntry : current.getNeighbors().entrySet()) {
                Node neighbor = neighborEntry.getKey();
                double weight = neighborEntry.getValue();
                double newDist = currentDist + weight;
                // if shorter than known update path data
                if (!paths.containsKey(neighbor) || newDist < paths.get(neighbor).distance) {
                    paths.put(neighbor, new PathData(newDist, current));
                    if (toProcess.contains(neighbor)) {
                        toProcess.changePriority(neighbor, newDist);
                    } else {
                        toProcess.add(neighbor, newDist);
                    }
                }
            }
        }
    }
    
    /**
     * Returns the length of the shortest path from the origin to destination. If no path exists, return
     * Double.POSITIVE_INFINITY. Precondition: destination is a node in the graph, and compute(origin) has been called.
     */
    public double shortestPathLength(Node destination) {
        // path reached??? then return distance
        if (paths.containsKey(destination)) {
            return paths.get(destination).distance;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Returns a LinkedList of the nodes along the shortest path from origin to destination. This path includes the
     * origin and destination. If origin and destination are the same node, it is included only once. If no path to it
     * exists, return null. Precondition: destination is a node in the graph, and compute(origin) has been called.
     */
    public LinkedList<Node> shortestPath(Node destination) {
        // path exists???
        if (!paths.containsKey(destination)) {
            return null;
        }
        // remake the path
        LinkedList<Node> path = new LinkedList<>();
        Node current = destination;
        while (current != null) {
            path.addFirst(current); 
            // add node over and over to front to reverse the path
            current = paths.get(current).previous;
        }
        return path;
    }


    /**
     * Inner class representing data used by Dijkstra's algorithm in the process of computing shortest paths from a
     * given source node.
     */
    class PathData {
        double distance; // distance of the shortest path from source
        Node previous; // previous node in the path from the source

        /**
         * constructor: initialize distance and previous node
         */
        public PathData(double dist, Node prev) {
            distance = dist;
            previous = prev;
        }
    }


    /**
     * Static helper method to open and parse a file containing graph information. Can parse either a basic file or a
     * DB1B CSV file with flight data. See GraphParser, BasicParser, and DB1BParser for more.
     */
    protected static Graph parseGraph(String fileType, String fileName) throws
            FileNotFoundException {
        // create an appropriate parser for the given file type
        GraphParser parser;
        if (fileType.equals("basic")) {
            parser = new BasicParser();
        } else if (fileType.equals("db1b")) {
            parser = new DB1BParser();
        } else {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + fileType);
        }

        // open the given file
        parser.open(new File(fileName));

        // parse the file and return the graph
        return parser.parse();
    }

    public static void main(String[] args) {
        // read command line args
        String fileType = args[0];
        String fileName = args[1];
        String origCode = args[2];
    
        String destCode = null;
        if (args.length == 4) {
            destCode = args[3];
        }
    
        // parse a graph with the given type and filename
        Graph graph;
        try {
            graph = parseGraph(fileType, fileName);
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file " + fileName);
            return;
        }
        graph.report();
        // get shortest path from origin
        ShortestPaths sp = new ShortestPaths();
        Node origin = graph.getNode(origCode);
        sp.compute(origin);
        // if no destination, print shortest path to all nodes
        if (destCode == null) {
            System.out.println("Shortest paths from " + origCode + ":");
            for (Node node : graph.getNodes().values()) {  
                double dist = sp.shortestPathLength(node);
                if (dist < Double.POSITIVE_INFINITY) {
                    System.out.println(node.getId() + ": " + dist);
                }
            }
        } else {
        // else print shortest path and length to the dest node.
            Node destination = graph.getNode(destCode);
            LinkedList<Node> path = sp.shortestPath(destination);
            if (path == null) {
                System.out.println("No path exists from " + origCode + " to " + destCode);
            } else {
                for (Node node : path) {
                    System.out.print(node.getId() + " ");
                }
                System.out.println(sp.shortestPathLength(destination));
            }
        }
    }
}

package tsp.solver.test;

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.Graphs;
import org.graphstream.ui.view.Viewer;
import org.graphstream.algorithm.Prim;

import java.util.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
         System.setProperty("org.graphstream.ui", "swing");
        Graph graph = generateTree(10);
//        Viewer viewer = graph.display();
//        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT);
        graph.display();


        for(Node n:graph) {
            System.out.println(n.getId());
            System.out.println(n.getEdge(3));
        }
//        TSPSolver solver = new TSPSolver(graph);
//        List<Node> bestPath = solver.solve();

//        System.out.println("Beste Route:");
//        for (Node node : bestPath) {
//            System.out.println("Knoten " + node.getId());
//        }

        //visualizeGraph(graph, bestPath);
    }


    public static int createRandomNode(List<Integer> intList) {
        Random rn = new Random();
        return intList.get(rn.nextInt(intList.size()));
    }


    public static Graph generateTree(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        Graph graph = new SingleGraph("New TSP Graph");
    //    graph.addAttribute("ui.stylesheet", "url('style.css')");
        List<Integer> nodeList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nodeList.add(i + 1);
        }
        int randomFirstNode = createRandomNode(nodeList);
        graph.addNode(String.valueOf(randomFirstNode)).setAttribute("label", String.valueOf(randomFirstNode));
        nodeList.remove((Integer) randomFirstNode);
        while (nodeList.size() != 0) {
            int randomNode = createRandomNode(nodeList);
            int existingNode = (int) (Math.random() * graph.getNodeCount());
            //Schlingen vermeiden
            if (randomNode != existingNode) {
                Node node = graph.addNode(String.valueOf(randomNode));
                node.setAttribute("label", String.valueOf(randomNode));
                Node node1 = graph.getNode(existingNode);
                graph.addEdge(randomNode + "--" + node1.getId(), node, node1);
                nodeList.remove((Integer) randomNode);
            }
        }
        return graph;
    }


//    public static List<String> tspApproximation(Graph graph) {
//        // Schritt 1: MST mit Prim's Algorithmus berechnen
//        Prim prim = new Prim("ui.class", "intree");
//        prim.init(graph);
//        prim.compute();
//
//        // Schritt 2: Preorder-Traversierung des MST
//        List<String> preorderList = new ArrayList<>();
//        Node startNode = graph.getNode("1");
//        preorderTraversal(startNode, preorderList, new boolean[graph.getNodeCount()]);
//
//        // Schritt 3: Startpunkt am Ende hinzufügen, um die Tour zu vervollständigen
//        if (!preorderList.isEmpty()) {
//            preorderList.add(preorderList.get(0));
//        }
//
//        return preorderList;
//    }

    private static void addEdge(Graph graph, String node1, String node2, int weight) {
        String edgeId = node1 + "-" + node2;
        Edge edge = graph.addEdge(edgeId, node1, node2);
        edge.setAttribute("weight", weight);
        edge.setAttribute("ui.label", weight);
    }

//    private static void preorderTraversal(Node node, List<String> preorderList, boolean[] visited) {
//        int nodeId = Integer.parseInt(node.getId()) - 1;
//        visited[nodeId] = true;
//        preorderList.add(node.getId());
//
//        for (Edge edge : node.getLeavingEdge()) {
//            Node adjacent = edge.getTargetNode();
//            int adjacentId = Integer.parseInt(adjacent.getId()) - 1;
//            // Überprüfen, ob die Kante Teil des MST ist und ob sie zu einem unbesuchten Knoten führt
//            if (!visited[adjacentId] && edge.hasAttribute("intree")) {
//                preorderTraversal(adjacent, preorderList, visited);
//            }
//        }
//    }

//    private static void visualizeGraph(Graph graph, List<Node> bestPath) {
//        Graph g = new SingleGraph("TSP Graph");
//        for (Node node : graph.getNodes()) {
//            g.addNode(node.getId()).addAttribute("xy", node.getX(), node.getY());
//        }
//
//        for (int i = 0; i < bestPath.size(); i++) {
//            Node src = bestPath.get(i);
//            Node dest = bestPath.get((i + 1) % bestPath.size());
//            String edgeId = src.getId() + "-" + dest.getId();
//            g.addEdge(edgeId, src.getId(), dest.getId());
//        }
//
//        for (Node node : g) {
//            node.addAttribute("ui.label", node.getId());
//        }
//
//        g.display();
//    }
}

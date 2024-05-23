package tsp.solver.test;

import java.util.*;

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import org.graphstream.algorithm.Dijkstra;


public class TSPAntColony {

    private static final int NODES = 50;
    private static final double INITIAL_PHEROMONE = 1.0;
    private static final double EVAPORATION_RATE = 0.5;
    static double Q = 1.0;
    static double ALPHA = 1.0;

    private static double[][] distances = new double[NODES][NODES];
    private static double[][] pheromones = new double[NODES][NODES];

    public static void main(String[] args) {
        double[] QValues = {0.1, 1.0, 10.0};
        double[] ALPHAValues = {0.1, 1.0, 10.0};
        initializeDistances();
        initializePheromones();

        //mehrere ausführungen mit untschiedlichen Q und Alpha werten
        for (double Qs : QValues) {
            for (double ALPHAs : ALPHAValues) {
                Q = Qs;
                ALPHA= ALPHAs;
                System.out.println("Testing for Q = " + Q + ", ALPHA = " + ALPHA );

                List<List<Integer>> tours = new ArrayList<>();
                for (int iteration = 0; iteration < 100; iteration++) {
                    tours = simulateAnts();
                    updatePheromones(tours);
                    evaporatePheromones();
                }

                List<Integer> bestTour = findBestTour(tours);
                int bestTourCost = calculateTourCost(bestTour);

                //visualizeBestTourInGraph(bestTour);
                visualizeBestTour(bestTour);
                // printDistancesAndPheromones();
                System.out.println("Beste Tour: " + bestTour);
                System.out.println("Gesamtkosten: " + bestTourCost + "\n");
            }}

        //setzen der werte für einzelne ausführung
        Q = 1.0;
        ALPHA = 1.0;

        List<List<Integer>> tours = new ArrayList<>();
        for (int iteration = 0; iteration < 100; iteration++) {
            tours = simulateAnts();
            updatePheromones(tours);
            evaporatePheromones();
        }

        List<Integer> bestTour = findBestTour(tours);
        int bestTourCost = calculateTourCost(bestTour);

        //visualizeBestTourInGraph(bestTour);
        visualizeBestTour(bestTour);
        // printDistancesAndPheromones();
        System.out.println("Beste Tour: " + bestTour);
        System.out.println("Gesamtkosten: " + bestTourCost + "\n");

        visualizeGraph();
    }

    //random distanzen zwischen den knoten generieren
    static void initializeDistances() {
        Random random = new Random();
        for (int i = 0; i < NODES; i++) {
            for (int j = 0; j < NODES; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else {
                    distances[i][j] = 10 + random.nextDouble() * 90; // Random distance between 10 and 100
                    distances[j][i] = distances[i][j]; // Symmetric TSP
                }
            }
        }
        // System.out.println("distances: " + distances);
    }

    //anfängliche pheromonwerte initialisieren
    static void initializePheromones() {
        for (int i = 0; i < NODES; i++) {
            Arrays.fill(pheromones[i], INITIAL_PHEROMONE);
        }
    }

    //Ameisen simulieren.
    static List<List<Integer>> simulateAnts() {
        List<List<Integer>> tours = new ArrayList<>();

        for (int ant = 0; ant < NODES; ant++) {
            List<Integer> tour = new ArrayList<>();
            tour.add(ant);

            while (tour.size() < NODES) {
                int currentNode = tour.get(tour.size() - 1);
                int nextNode = chooseNextNode(currentNode, tour);
                tour.add(nextNode);
            }
            tour.add(tour.get(0));
            tours.add(tour);
            depositPheromones(tour);
        }
        return tours;
    }

    private static int chooseNextNode(int currentNode, List<Integer> tour) {
        double[] probabilities = calculateProbabilities(currentNode, tour);
        return selectNextNode(probabilities, tour);
    }

    private static double[] calculateProbabilities(int currentNode, List<Integer> tour) {
        double[] probabilities = new double[NODES];
        double totalProbability = 0.0;

        for (int nextNode = 0; nextNode < NODES; nextNode++) {
            if (!tour.contains(nextNode)) {
                double distance = distances[currentNode][nextNode];
                double pheromone = pheromones[currentNode][nextNode];

                probabilities[nextNode] = Math.pow(pheromone, Q) / Math.pow(distance, ALPHA);
                totalProbability += probabilities[nextNode];
            }
        }

        if (totalProbability == 0.0) {
            // If all probabilities are zero, distribute probabilities equally among unvisited nodes
            for (int nextNode = 0; nextNode < NODES; nextNode++) {
                if (!tour.contains(nextNode)) {
                    probabilities[nextNode] = 1.0 / (NODES - tour.size());
                }
            }
        } else {
            for (int i = 0; i < probabilities.length; i++) {
                probabilities[i] /= totalProbability;
            }
        }
        return probabilities;
    }

    private static int selectNextNode(double[] probabilities, List<Integer> tour) {
        double randomValue = Math.random();
        double cumulativeProbability = 0.0;

        for (int nextNode = 0; nextNode < NODES; nextNode++) {
            if (!tour.contains(nextNode)) {
                cumulativeProbability += probabilities[nextNode];
                if (cumulativeProbability >= randomValue) {
                    return nextNode;
                }
            }
        }

        // If no next node is selected, return a random unvisited node as a fallback
        List<Integer> unvisitedNodes = new ArrayList<>();
        for (int nextNode = 0; nextNode < NODES; nextNode++) {
            if (!tour.contains(nextNode)) {
                unvisitedNodes.add(nextNode);
            }
        }
        return unvisitedNodes.get(new Random().nextInt(unvisitedNodes.size()));
    }

    static void updatePheromones(List<List<Integer>> tours) {
        for (List<Integer> tour : tours) {
            int tourCost = calculateTourCost(tour);

            for (int i = 0; i < tour.size() - 1; i++) {
                int currentNode = tour.get(i);
                int nextNode = tour.get(i + 1);

                pheromones[currentNode][nextNode] += 1.0 / tourCost;
            }
        }
    }

    static void evaporatePheromones() {
        for (int i = 0; i < NODES; i++) {
            for (int j = 0; j < NODES; j++) {
                pheromones[i][j] *= (1 - EVAPORATION_RATE);
            }
        }
    }

    private static void depositPheromones(List<Integer> tour) {
        int tourCost = calculateTourCost(tour);

        for (int i = 0; i < tour.size() - 1; i++) {
            int currentNode = tour.get(i);
            int nextNode = tour.get(i + 1);

            pheromones[currentNode][nextNode] += 1.0 / tourCost;
        }
    }

    static int calculateTourCost(List<Integer> tour) {
        int totalCost = 0;

        for (int i = 0; i < tour.size() - 1; i++) {
            int currentNode = tour.get(i);
            int nextNode = tour.get(i + 1);

            totalCost += distances[currentNode][nextNode];
        }

        return totalCost;
    }

    static List<Integer> findBestTour(List<List<Integer>> tours) {
        int bestTourCost = Integer.MAX_VALUE;
        List<Integer> bestTour = null;

        for (List<Integer> tour : tours) {
            int tourCost = calculateTourCost(tour);
            if (tourCost < bestTourCost) {
                bestTourCost = tourCost;
                bestTour = tour;
            }
        }
        return bestTour;
    }


    private static void visualizeGraph() {
        System.setProperty("org.graphstream.ui", "swing");

        Graph graph = new SingleGraph("Graph");

        // Knoten hinzufügen
        for (int i = 0; i < NODES; i++) {
            Node node = graph.addNode(String.valueOf(i));
            node.setAttribute("ui.label", node.getId());
        }

        // Kanten hinzufügen
        for (int i = 0; i < NODES; i++) {
            for (int j = 0; j < NODES; j++) {
                if (i != j && distances[i][j] > 0) { // Keine Schleifen und nur positive Distanzen
                    String edgeId = i + "-" + j;
                    Edge edge = graph.addEdge(edgeId, String.valueOf(i), String.valueOf(j), true);
                    edge.setAttribute("ui.label", String.format("%.2f", distances[i][j]));
                }
            }
        }

        // Styling für den Graphen
        graph.setAttribute("ui.stylesheet",
                "node { text-size: 20px; text-color: black; fill-color: red; }" +
                        "edge { text-size: 15px; text-color: blue; }"
        );
        // Graph anzeigen
        graph.display();
    }



    private static void visualizeBestTour(List<Integer> bestTour) {
        System.setProperty("org.graphstream.ui", "swing");

        Graph graph = new SingleGraph("BestTourGraph");

        // Knoten hinzufügen
        for (int i = 0; i < NODES; i++) {
            Node node = graph.addNode(String.valueOf(i));
            node.setAttribute("ui.label", node.getId());
        }

        // Kanten des besten Rundreisewegs hinzufügen
        for (int i = 0; i < bestTour.size() - 1; i++) {
            int source = bestTour.get(i);
            int target = bestTour.get(i + 1);
            String edgeId = source + "-" + target;
            Edge edge = graph.addEdge(edgeId, String.valueOf(source), String.valueOf(target), true);
            edge.setAttribute("ui.label", String.format("%.2f", distances[source][target]));
        }

        // Styling für den Graphen
        graph.setAttribute("ui.stylesheet",
                "node { text-size: 20px; text-color: black; fill-color: red; }" +
                        "edge { text-size: 15px; text-color: blue; }"
        );

        // Graph anzeigen
        graph.display();
    }

    private static void visualizeBestTourInGraph(List<Integer> bestTour) {
        System.setProperty("org.graphstream.ui", "swing");

        Graph graph = new SingleGraph("Graph");

        // Knoten hinzufügen
        for (int i = 0; i < NODES; i++) {
            graph.addNode(String.valueOf(i));
        }

        // Kanten hinzufügen
        for (int i = 0; i < NODES; i++) {
            for (int j = 0; j < NODES; j++) {
                if (i != j && distances[i][j] > 0) { // Keine Schleifen und nur positive Distanzen
                    String edgeId = i + "-" + j;
                    Edge edge = graph.addEdge(edgeId, String.valueOf(i), String.valueOf(j), true);
                    edge.setAttribute("weight", distances[i][j]);
                }
            }
        }

        // Kanten der besten Rundreisewegs markieren
        for (int i = 0; i < bestTour.size() - 1; i++) {
            int source = bestTour.get(i);
            int target = bestTour.get(i + 1);
            String edgeId = source + "-" + target;
            Edge edge = graph.getEdge(edgeId);
            edge.setAttribute("ui.style", "fill-color: green;");
            edge.setAttribute("ui.label", String.format("%.2f", edge.getAttribute("weight")));
        }



        // Styling für den Graphen
        graph.setAttribute("ui.stylesheet",
                "node { text-size: 20px; text-color: black; fill-color: red; }" +
                        "edge { text-size: 15px; text-color: blue; }"
        );

        // Labels anzeigen
        for (Node node : graph) {
            node.setAttribute("ui.label", node.getId());
        }

        // Graph anzeigen
        graph.display();
    }


    private static void printDistancesAndPheromones() {
        for (int i = 0; i < NODES; i++) {
            for (int j = 0; j < NODES; j++) {
                System.out.printf("%8.2f", distances[i][j]); // Formatiere die Ausgabe auf zwei Dezimalstellen
            }
            System.out.println(); // Neue Zeile am Ende jeder Zeile des Arrays
        }
        System.out.println();
        for (int i = 0; i < NODES; i++) {
            for (int j = 0; j < NODES; j++) {
                System.out.printf("%8.2f", pheromones[i][j]); // Formatiere die Ausgabe auf zwei Dezimalstellen
            }
            System.out.println(); // Neue Zeile am Ende jeder Zeile des Arrays
        }

    }
}

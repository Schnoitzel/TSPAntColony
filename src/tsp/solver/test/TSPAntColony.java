package tsp.solver.test;

import java.util.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

public class TSPAntColony {
    private int nodes;
    private double initialPheromone;
    private double evaporationRate;
    private double q;
    private double alpha;

    private double[][] distances;
    private double[][] pheromones;


    public TSPAntColony(int nodes, double initialPheromone, double evaporationRate, double q, double alpha) {
        this.nodes = nodes;
        this.initialPheromone = initialPheromone;
        this.evaporationRate = evaporationRate;
        this.q = q;
        this.alpha = alpha;

        this.distances = new double[nodes][nodes];
        this.pheromones = new double[nodes][nodes];
    }

    public static void main(String[] args) {
        double[] qValues = {1, 10.0, 100};
        double[] alphaValues = {1, 10.0, 100};

        TSPAntColony tsp = new TSPAntColony(100, 1.0, 0.5, 1.0, 1.0);
        tsp.initializeDistancesWr();
        tsp.initializePheromones();

        for (double q : qValues) {
            for (double alpha : alphaValues) {
                tsp.q = q;
                tsp.alpha = alpha;
                System.out.println("Testing for Q = " + q + ", ALPHA = " + alpha);

                long totalDuration = 0;
                int totalBestTourCost = 0;

                for (int run = 0; run < 100; run++) {
                    List<List<Integer>> tours = new ArrayList<>();
                    tsp.initializePheromones(); // Reset Pheromones for each run

                    long startTime = System.nanoTime(); // Startzeit
                    for (int iteration = 0; iteration < 100; iteration++) {
                        tours = tsp.simulateAnts();
                        tsp.updatePheromones(tours);
                        tsp.evaporatePheromones();
                    }
                    long endTime = System.nanoTime(); // Endzeit
                    long duration = (endTime - startTime) / 1_000_000; // Dauer in Millisekunden

                    List<Integer> bestTour = tsp.findBestTour(tours);
                    int bestTourCost = tsp.calculateTourCost(bestTour);

                    totalDuration += duration;
                    totalBestTourCost += bestTourCost;
                }

                long averageDuration = totalDuration / 100;
                int averageBestTourCost = totalBestTourCost / 100;

                System.out.println("Durchschnittliche beste Tour Kosten: " + averageBestTourCost);
                System.out.println("Durchschnittliche Dauer: " + averageDuration + " ms\n");
            }
        }

        // tsp.visualizeGraph();
    }


    // Random distanzen zwischen den knoten generieren
    public void initializeDistancesWr() {
        Random random = new Random();
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else {
                    distances[i][j] = 10 + random.nextDouble() * 90; // Random distance between 10 and 100
                    distances[j][i] = distances[i][j]; // Symmetric TSP
                }
            }
        }
    }

    // Anfängliche Pheromon Werte initialisieren
    public void initializePheromones() {
        for (int i = 0; i < nodes; i++) {
            Arrays.fill(pheromones[i], initialPheromone);
        }
    }

    // Ameisen simulieren
    public List<List<Integer>> simulateAnts() {
        List<List<Integer>> tours = new ArrayList<>();

        // Für jede Ameise
        for (int ant = 0; ant < nodes; ant++) {
            List<Integer> tour = new ArrayList<>();
            tour.add(ant); //Ameise Startknoten zuweisen

            // Solange die Tour noch nicht alle Knoten umfasst
            while (tour.size() < nodes) {
                int currentNode = tour.get(tour.size() - 1); // Der aktuelle Knoten ist der zuletzt besuchte Knoten
                int nextNode = chooseNextNode(currentNode, tour); // Wähle den nächsten Knoten basierend auf Wahrscheinlichkeiten
                tour.add(nextNode); // Füge den gewählten Knoten zur Tour hinzu
            }
            tour.add(tour.get(0)); // Kehre zum Ausgangspunkt zurück, um die Rundreise abzuschließen
            tours.add(tour); // Füge die vollständige Tour zur Liste der Rundreisen hinzu
            depositPheromones(tour); // Hinterlasse Pheromone auf der aktuellen Tour
        }
        return tours; // Gib die Liste aller Rundreisen zurück
    }

    //Wählt nächsten Knoten
    private int chooseNextNode(int currentNode, List<Integer> tour) {
        double[] probabilities = calculateProbabilities(currentNode, tour);
        return selectNextNode(probabilities, tour);
    }

    //Berechnet Wahrscheinlichkeiten für nächsten Knoten
    public double[] calculateProbabilities(int currentNode, List<Integer> tour) {
        double[] probabilities = new double[nodes];
        double totalProbability = 0.0;

        //Überprüft ob Distanzen und Pheromone korrekt initialisiert wurden.
        if (distances == null || pheromones == null || distances.length != nodes || pheromones.length != nodes ||
                distances[0].length != nodes || pheromones[0].length != nodes) {
            throw new IllegalStateException("Distances and pheromones are not properly initialized.");
        }

        //wahrscheinlichkeit für alle nächsten Nodes ausrechnen
        for (int nextNode = 0; nextNode < nodes; nextNode++) {
            if (!tour.contains(nextNode)) {
                double distance = distances[currentNode][nextNode];
                double pheromone = pheromones[currentNode][nextNode];
                if (distance != 0) {
                    probabilities[nextNode] = Math.pow(pheromone, q) / Math.pow(distance, alpha);  //Kurze wege mit hohen Pheromon Wert, bekommen hohe Wahrscheinlichkeit
                    totalProbability += probabilities[nextNode];
                }
            }
        }
        //Wenn alle Wahrscheinlichkeiten null sind, werden die Wahrscheinlichkeiten gleichmäßig unter den unbesuchten Knoten verteilt.
        if (totalProbability == 0.0) {
            for (int nextNode = 0; nextNode < nodes; nextNode++) {
                if (!tour.contains(nextNode)) {
                    probabilities[nextNode] = 1.0 / (nodes - tour.size());
                }
            }
        } else {
            for (int i = 0; i < probabilities.length; i++) {
                probabilities[i] /= totalProbability;
            }
        }
        return probabilities;
    }

    //Knoten wählen anhand kumulierter Wahrscheinlichkeit, sobald sie einen random wert überschreitet -> wähle den Knoten
    public int selectNextNode(double[] probabilities, List<Integer> tour) {
        double randomValue = Math.random();
        double cumulativeProbability = 0.0;

        for (int nextNode = 0; nextNode < nodes; nextNode++) {
            if (!tour.contains(nextNode)) {
                cumulativeProbability += probabilities[nextNode];
                if (cumulativeProbability >= randomValue) {  //zur Vermeidung lokaler Optima
                    return nextNode;
                }
            }
        }

        // Wenn kein Knoten ausgewählt wurde, random Knoten wählen
        List<Integer> unvisitedNodes = new ArrayList<>();
        for (int nextNode = 0; nextNode < nodes; nextNode++) {
            if (!tour.contains(nextNode)) {
                unvisitedNodes.add(nextNode);
            }
        }
        return unvisitedNodes.get(new Random().nextInt(unvisitedNodes.size()));
    }

    //Pheromon Werte aktualisieren, für alle touren nach einer Iteration
    public void updatePheromones(List<List<Integer>> tours) {
        for (List<Integer> tour : tours) {
            int tourCost = calculateTourCost(tour);

            for (int i = 0; i < tour.size() - 1; i++) {
                int currentNode = tour.get(i);
                int nextNode = tour.get(i + 1);

                pheromones[currentNode][nextNode] += 1.0 / tourCost;
            }
        }
    }

    //Pheromone Werte aktualisieren, für eine einzige tour
    private void depositPheromones(List<Integer> tour) {
        int tourCost = calculateTourCost(tour);

        for (int i = 0; i < tour.size() - 1; i++) {
            int currentNode = tour.get(i);
            int nextNode = tour.get(i + 1);

            pheromones[currentNode][nextNode] += 1.0 / tourCost;
        }
    }

    //Pheromon Werte verdunsten lassen
    public void evaporatePheromones() {
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                pheromones[i][j] *= (1 - evaporationRate);
            }
        }
    }

    //berechnet die Gesamtkosten einer Tour
    public int calculateTourCost(List<Integer> tour) {
        int totalCost = 0;

        for (int i = 0; i < tour.size() - 1; i++) {
            int currentNode = tour.get(i);
            int nextNode = tour.get(i + 1);

            totalCost += distances[currentNode][nextNode];
        }

        return totalCost;
    }

    //Sucht die beste Tour aus einer Liste von Touren
    public List<Integer> findBestTour(List<List<Integer>> tours) {
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



    //__________________________________________________________________________________________________________________
    //Ab hier nur noch visualisierung
    //__________________________________________________________________________________________________________________



    private void visualizeGraph() {
        System.setProperty("org.graphstream.ui", "swing");

        Graph graph = new SingleGraph("Graph");

        for (int i = 0; i < nodes; i++) {
            Node node = graph.addNode(String.valueOf(i));
            node.setAttribute("ui.label", node.getId());
        }

        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (i != j && distances[i][j] > 0) {
                    String edgeId = i + "-" + j;
                    Edge edge = graph.addEdge(edgeId, String.valueOf(i), String.valueOf(j), true);
                    edge.setAttribute("ui.label", String.format("%.2f", distances[i][j]));
                }
            }
        }

        graph.setAttribute("ui.stylesheet",
                "node { text-size: 20px; text-color: black; fill-color: red; }" +
                        "edge { text-size: 15px; text-color: blue; }"
        );
        graph.display();
    }

    private void visualizeBestTour(List<Integer> bestTour) {
        System.setProperty("org.graphstream.ui", "swing");

        Graph graph = new SingleGraph("BestTourGraph");

        for (int i = 0; i < nodes; i++) {
            Node node = graph.addNode(String.valueOf(i));
            node.setAttribute("ui.label", node.getId());
        }

        for (int i = 0; i < bestTour.size() - 1; i++) {
            int source = bestTour.get(i);
            int target = bestTour.get(i + 1);
            String edgeId = source + "-" + target;
            Edge edge = graph.addEdge(edgeId, String.valueOf(source), String.valueOf(target), true);
            edge.setAttribute("ui.label", String.format("%.2f", distances[source][target]));
        }

        graph.setAttribute("ui.stylesheet",
                "node { text-size: 20px; text-color: black; fill-color: red; }" +
                        "edge { text-size: 15px; text-color: blue; }"
        );

        graph.display();
    }

    private void visualizeBestTourInGraph(List<Integer> bestTour) {
        System.setProperty("org.graphstream.ui", "swing");

        Graph graph = new SingleGraph("Graph");

        for (int i = 0; i < nodes; i++) {
            graph.addNode(String.valueOf(i));
        }

        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (i != j && distances[i][j] > 0) {
                    String edgeId = i + "-" + j;
                    Edge edge = graph.addEdge(edgeId, String.valueOf(i), String.valueOf(j), true);
                    edge.setAttribute("weight", distances[i][j]);
                }
            }
        }

        for (int i = 0; i < bestTour.size() - 1; i++) {
            int source = bestTour.get(i);
            int target = bestTour.get(i + 1);
            String edgeId = source + "-" + target;
            Edge edge = graph.getEdge(edgeId);
            edge.setAttribute("ui.style", "fill-color: green;");
            edge.setAttribute("ui.label", String.format("%.2f", edge.getAttribute("weight")));
        }

        graph.setAttribute("ui.stylesheet",
                "node { text-size: 20px; text-color: black; fill-color: red; }" +
                        "edge { text-size: 15px; text-color: blue; }"
        );

        for (Node node : graph) {
            node.setAttribute("ui.label", node.getId());
        }

        graph.display();
    }

    public void printDistancesAndPheromones() {
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                System.out.printf("%8.2f", distances[i][j]);
            }
            System.out.println();
        }
        System.out.println();
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                System.out.printf("%8.2f", pheromones[i][j]);
            }
            System.out.println();
        }
    }

    public double[][] getDistances() {
        return distances;
    }

    public int getNodes() {
        return nodes;
    }

    public double[][] getPheromones() {
        return pheromones;
    }

    public void setDistances(double[][] distances) {
        this.distances = distances;
    }

    public void initializeDistances(double random) {
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else {
                    distances[i][j] = 10 + random * 90;
                    distances[j][i] = distances[i][j];
                }
            }
        }
    }
}

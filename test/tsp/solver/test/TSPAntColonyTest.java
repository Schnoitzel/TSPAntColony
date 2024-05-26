//package tsp.solver.test;
//
//
//import static org.mockito.Mockito.when;
//import static org.testng.AssertJUnit.*;
//
//
//import org.junit.jupiter.api.BeforeEach;
//
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.testng.annotations.Test;
//
//import java.util.*;
//import java.util.stream.IntStream;
//
//
//public class TSPAntColonyTest {
//
//
//    @Test
//    public void testInitializeDistances() {
//        double mockRandom = 0.5;
//        TSPAntColony.initializeDistances(mockRandom);
//        double[][] distances = TSPAntColony.getDistances();
//        assertNotNull(distances);
//        assertEquals(TSPAntColony.getNodes(), distances.length);
//        for (int i = 0; i < TSPAntColony.getNodes(); i++) {
//            for (int j = 0; j < TSPAntColony.getNodes(); j++) {
//                if (i == j) {
//                    assertEquals(0.0, distances[i][j], 0.0001);
//                } else {
//                    assertEquals(55.0, distances[i][j], 0.0001); // Expected distance: 10 + 0.5 * 90
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testInitializePheromones() {
//        TSPAntColony.initializePheromones();
//
//        double[][] pheromones = TSPAntColony.getPheromones();
//        assertNotNull(pheromones);
//        assertEquals(TSPAntColony.getNodes(), pheromones.length);
//        for (int i = 0; i < TSPAntColony.getNodes(); i++) {
//            for (int j = 0; j < TSPAntColony.getNodes(); j++) {
//                assertEquals(1.0, pheromones[i][j], 0.0001);
//            }
//        }
//    }
//
//    @Test
//    public void testCalculateTourCost() {
//        double mockRandom = 0.5;
//        //.when(mockRandom.nextDouble()).thenReturn(0.5);
//        TSPAntColony.initializeDistances(mockRandom);
//        List<Integer> tour = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 0);
//        int cost = TSPAntColony.calculateTourCost(tour);
//        double expectedCost = 7 * (10 + 0.5 * 90); // 7 edges in the tour, each with the mocked distance
//        assertEquals(expectedCost, cost, 0.0001);
//    }
//
//    @Test
//    public void testFindBestTour() {
//        List<Integer> tour1 = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 0);
//        List<Integer> tour2 = Arrays.asList(0, 6, 5, 4, 3, 2, 1, 0);
//        List<List<Integer>> tours = Arrays.asList(tour1, tour2);
//        List<Integer> bestTour = TSPAntColony.findBestTour(tours);
//        assertNotNull(bestTour);
//        assertEquals(tour1, bestTour);
//    }
//
//    @Test
//    public void testCalculateProbabilities() {
//        // Test 1: Basic test with one node in the tour
//        int currentNode = 0;
//        List<Integer> tour = new ArrayList<>();
//        tour.add(1);
//
//        double mockRandom = 0.5;
//        TSPAntColony.initializeDistances(mockRandom);
//        TSPAntColony.initializePheromones();
//
//        double[] probabilities = TSPAntColony.calculateProbabilities(currentNode, tour);
//        assertEquals(TSPAntColony.getNodes(), probabilities.length);
//
//        double totalProbability = Arrays.stream(probabilities).sum();
//        assertEquals(1.0, totalProbability, 1e-6);
//        for (int i = 0; i < probabilities.length; i++) {
//            if (!tour.contains(i)) {
//                assertFalse(Double.isNaN(probabilities[i])); // Ensure no NaN values
//            }
//        }
//
//        // Test 2: Different start node
//        currentNode = 2;
//        tour = new ArrayList<>(Arrays.asList(0, 1));
//
//        probabilities = TSPAntColony.calculateProbabilities(currentNode, tour);
//        assertEquals(TSPAntColony.getNodes(), probabilities.length);
//
//        totalProbability = Arrays.stream(probabilities).sum();
//        assertEquals(1.0, totalProbability, 1e-6);
//        for (int i = 0; i < probabilities.length; i++) {
//            if (!tour.contains(i)) {
//                assertFalse(Double.isNaN(probabilities[i])); // Ensure no NaN values
//            }
//        }
//
//        // Test 3: Different tour
//        currentNode = 1;
//        tour = new ArrayList<>(Arrays.asList(0, 2, 3));
//
//        probabilities = TSPAntColony.calculateProbabilities(currentNode, tour);
//        assertEquals(TSPAntColony.getNodes(), probabilities.length);
//
//        totalProbability = Arrays.stream(probabilities).sum();
//        assertEquals(1.0, totalProbability, 1e-6);
//        for (int i = 0; i < probabilities.length; i++) {
//            if (!tour.contains(i)) {
//                assertFalse(Double.isNaN(probabilities[i])); // Ensure no NaN values
//            }
//        }
//
//        // Test 4: Different distances and pheromone values
//        TSPAntColony.initializeDistances(0.8);
//        TSPAntColony.initializePheromones();
//
//        currentNode = 0;
//        tour = new ArrayList<>(Arrays.asList(1, 3, 5));
//
//        probabilities = TSPAntColony.calculateProbabilities(currentNode, tour);
//        assertEquals(TSPAntColony.getNodes(), probabilities.length);
//
//        totalProbability = Arrays.stream(probabilities).sum();
//        assertEquals(1.0, totalProbability, 1e-6);
//        for (int i = 0; i < probabilities.length; i++) {
//            if (!tour.contains(i)) {
//                assertFalse(Double.isNaN(probabilities[i])); // Ensure no NaN values
//            }
//        }
//    }
//
//
//    @Test
//    public void testSelectNextNode() {
//
//        List<Integer> tour = new ArrayList<>(Arrays.asList(0, 5, 6 ,4 , 2 , 1));
//        double[] probabilities = {0.0, 0.0, 0.0, 0.0, 0.8235294117647058, 0.17647058823529405, 0.0};
//        int selectedNode = TSPAntColony.selectNextNode(probabilities, tour);
//        assertTrue(selectedNode >= 0 && selectedNode < TSPAntColony.getNodes());
//        assertFalse(tour.contains(selectedNode));
//    }
//}
//

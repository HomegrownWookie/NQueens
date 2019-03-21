import java.util.*;

public class NQueens {
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        double accuracy = 0.0;
        long runTime = 0;
        int searchCost = 0;
        Report report;
        double ATTEMPTS = 10.0;
        double averageAcc;
        long averageTime;
        double averageCost;

        System.out.println("Welcome to Ian's N-Queen Solver!");
        System.out.println("Which system would you like to use?");
        System.out.println("1) Simulated Annealing");
        System.out.println("2) Genetic Algorithm");
        int choice = scan.nextInt();
        switch (choice){
            case 1:
                System.out.print("How many queens would you like to use?");
                int n = scan.nextInt();
                for(int i = 0; i < ATTEMPTS; i++){
                    report = SimulatedAnnealing.solve(n);
                    if(report.isSolution())
                        accuracy++;
                    runTime += report.getTimeSpent();
                    searchCost += report.getCost();
                }
                averageAcc = (accuracy / ATTEMPTS) * 100;
                averageTime = runTime / (long)ATTEMPTS;
                averageCost = searchCost / ATTEMPTS;
                System.out.println("Percentage of Successful Attempts: " + averageAcc + "%");
                System.out.println("Average Time Spent: " + averageTime + "ms");
                System.out.println("Average Search Cost: " + averageCost);
                break;
            case 2:
                System.out.print("How many queens would you like to use?");
                int m = scan.nextInt();
                for(int i = 0; i < ATTEMPTS; i++){
                    report = GeneticAlgorithm.solve(m);
                    if(report.isSolution())
                        accuracy++;
                    runTime += report.getTimeSpent();
                    searchCost += report.getCost();
                }
                averageAcc = (accuracy / ATTEMPTS) * 100;
                averageTime = runTime / (long)ATTEMPTS;
                averageCost = searchCost / ATTEMPTS;
                System.out.println("Percentage of Successful Attempts: " + averageAcc + "%");
                System.out.println("Average Time Spent: " + averageTime + "ms");
                System.out.println("Average Search Cost: " + averageCost);
                break;
            default:
                System.out.println("Invalid Choice!");
                main(args);
        }
        System.out.println("Goodbye!");
    }
}

class SimulatedAnnealing {
    private static double TEMPERATURE = 120;
    private static int MAX_ITERATIONS = 1000;
    private static double COOLING_FACTOR = 0.95;

    static Report solve(int n){
        int[] board = Tools.generateRandomState(n);
        int costToBeat = Tools.getHeuristic(board);
        long startTime = System.currentTimeMillis();
        int totalCost = 0;

        for (int x = 0; x < MAX_ITERATIONS && costToBeat > 0; x++) {
            board = makeMove(board, costToBeat);
            costToBeat = Tools.getHeuristic(board);
            TEMPERATURE = Math.max(TEMPERATURE * COOLING_FACTOR, 0.01);
            totalCost++;
        }


        long endTime = System.currentTimeMillis();
        long timeSpent = endTime-startTime;
        boolean solution;
        solution = costToBeat == 0;

        return new Report(solution, totalCost, timeSpent);
    }

    private static int[] makeMove(int[] board, int costToBeat) {
        int n = board.length;

        while (true) {
            int col = (int) (Math.random() * n);
            int row = (int) (Math.random() * n);
            int tempRow = board[col];
            board[col] = row;

            int cost = Tools.getHeuristic(board);
            if (cost < costToBeat)
                return board;

            int deltaE = costToBeat - cost;
            double acceptProb = Math.min(1, Math.exp(deltaE / TEMPERATURE));

            if (Math.random() < acceptProb)
                return board;

            board[col] = tempRow;
        }


    }

}

class GeneticAlgorithm {
    private static int POPULATION_SIZE = 100;
    private static double MUTATION_PROB = 0.20;
    private static int NUM_GENERATIONS = 500000;

    static Report solve(int n) {
        long startTime = System.currentTimeMillis();
        long endTime, timeSpent = 0;
        int totalCost = 0;
        Report report;
        POPULATION_SIZE = POPULATION_SIZE - (POPULATION_SIZE % 2);
        int[][] population = generatePopulation(n, POPULATION_SIZE);
        int maxFitness = getMaxFitness(n);

        for (int x = 0; x < NUM_GENERATIONS; x++) {
            population = getSelectedPopulation(population);
            population = handleCrossovers(population, n);

            for (int i = 0; i < POPULATION_SIZE; i++) {
                if (getFitness(population[i]) == maxFitness) {
                    endTime = System.currentTimeMillis();
                    timeSpent = endTime - startTime;
                    report = new Report(true, totalCost, timeSpent);
                    return report;
                }
                population[i] = attemptMutation(population[i]);
                totalCost++;
                if (getFitness(population[i]) == maxFitness) {
                    endTime = System.currentTimeMillis();
                    timeSpent = endTime - startTime;
                    report = new Report(true, totalCost, timeSpent);
                    return report;
                }
            }
        }
        report = new Report(false, totalCost, timeSpent);
        return report;
    }

    private static int[][] handleCrossovers(int[][] population, int n) {
        for (int i = 0; i < population.length; i += 2) {
            int crossoverPos = (int) (Math.random() * n);
            for (int j = 0; j < crossoverPos; j++) {
                int temp = population[i][j];
                population[i][j] = population[i+1][j];
                population[i+1][j] = temp;
            }
        }
        return population;
    }

    private static int[][] getSelectedPopulation(int[][] population) {
        Arrays.sort(population, Comparator.comparingInt(GeneticAlgorithm::getFitness));
        return population;
    }

    private static int[] attemptMutation(int[] board) {
        if (satisfyProb(MUTATION_PROB))
            board[(int)(Math.random()*board.length)] = (int)(Math.random()*board.length);
        return board;
    }

    private static boolean satisfyProb(double prob) {
        return prob >= Math.random();
    }

    private static int getFitness(int[] board) {
        return getMaxFitness(board.length) - Tools.getHeuristic(board);
    }

    private static int getMaxFitness(int n) {
        return n*(n-1)/2;
    }

    private static int[] generateChild(int n) {
        return Tools.generateRandomState(n);
    }

    private static int[][] generatePopulation(int n, int populationSize) {
        int[][] population = new int[populationSize][];
        for (int i = 0; i < populationSize; i++)
            population[i] = generateChild(n);

        return population;
    }
}

class Tools{
    static int[] generateRandomState(int n) {
        int[] board = new int[n];

        for (int i = 0; i < board.length; i++)
            board[i] = (int) (Math.random() * board.length);

        return board;
    }

    static int getHeuristic(int[] board) {
        int h = 0;

        for (int i = 0; i < board.length; i++)
            for (int j = i + 1; j < board.length; j++)
                if (board[i] == board[j] || Math.abs(board[i] - board[j]) == j - i)
                    h += 1;

        return h;
    }
}

class Report{
    private boolean solution;
    private int cost;
    private long timeSpent;

    Report(boolean solution, int cost, long timeSpent) {
        this.cost = cost;
        this.solution = solution;
        this.timeSpent = timeSpent;
    }

    boolean isSolution() {
        return solution;
    }

    int getCost() {
        return cost;
    }

    long getTimeSpent() {
        return timeSpent;
    }
}
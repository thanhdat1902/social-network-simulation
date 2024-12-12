package networksim;

import java.util.HashSet;
import java.util.Set;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;

public class Infected {
    private static int infectedCount = 0;
    
    private double beta;   // Infection probability
    private double gamma;  // Recovery probability

    public Infected(double beta, double gamma) {
        this.beta = beta;
        this.gamma = gamma;
        incrementInfectedCount();

    }

    @ScheduledMethod(start = 1, interval = 1)
    public void step() {
        Context<Object> context = ContextUtils.getContext(this);
        Network<Object> network = (Network<Object>) context.getProjection("infection network");

        // Attempt to infect susceptible neighbors
        for (Object neighbor : network.getAdjacent(this)) {
            if (neighbor instanceof Susceptible) {
                infect((Susceptible) neighbor);
            }
        }

        // Attempt recovery
        recover();

        // Stop the simulation if no infected agents are left

        if (getInfectedCount() == 0) {
        	System.out.print("Stop Infected");
            stopSimulation();
        }
    }

    public static synchronized void incrementInfectedCount() {
        infectedCount++;
    }

    public static synchronized void decrementInfectedCount() {
        infectedCount--;
    }

    public static synchronized int getInfectedCount() {
        return infectedCount;
    }

    public void stopSimulation() {
        System.out.println("Stopping the simulation: no infected agents left");
        RunEnvironment.getInstance().endRun();
    }

    private void infect(Susceptible susceptible) {
        // Infect the susceptible neighbor with probability beta
        if (Math.random() < beta) {
            Context<Object> context = ContextUtils.getContext(susceptible);
            Network<Object> network = (Network<Object>) context.getProjection("infection network");

            // Get the edges connected to the susceptible node
            Set<Object> neighbors = new HashSet<>();
            for (Object neighbor : network.getAdjacent(susceptible)) {
                neighbors.add(neighbor);
            }
            
            // Remove the susceptible agent from context and network (edges are removed automatically)
            context.remove(susceptible);

            // Create and add the new infected agent
            Infected newInfected = new Infected(beta, gamma);
            context.add(newInfected);

            // Recreate the edges for the new infected agent
            for (Object neighbor : neighbors) {
                network.addEdge(newInfected, neighbor);
            }

        }
    }

    public void recover() {
        // Recover with probability gamma
        if (Math.random() < gamma) {
            Context<Object> context = ContextUtils.getContext(this);
            Network<Object> network = (Network<Object>) context.getProjection("infection network");

            // Get the edges connected to this infected node
            Set<Object> neighbors = new HashSet<>();
            for (Object neighbor : network.getAdjacent(this)) {
                neighbors.add(neighbor);
            }

            // Remove the infected agent from context and network (edges are removed automatically)
            context.remove(this);

            // Create and add the new recovered agent
            Recovered recovered = new Recovered();
            context.add(recovered);
            decrementInfectedCount();

            // Recreate the edges for the new recovered agent
            for (Object neighbor : neighbors) {
                network.addEdge(recovered, neighbor);
            }

        }
    }
}
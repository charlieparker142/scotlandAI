package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import uk.ac.bris.cs.gamekit.graph.Edge;

import java.util.Collections;
import java.util.HashMap;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.ai.ResourceProvider;
import uk.ac.bris.cs.scotlandyard.ai.Visualiser;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.MoveVisitor;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Spectator;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardGame;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardModel;
import uk.ac.bris.cs.scotlandyard.model.Transport;



@ManagedAI("DetectiveAI")
public class DetectiveAI implements PlayerFactory, MoveVisitor{

    public Integer score;
    public Integer destination;
    public Integer count;
    public Integer index;

    public DetectiveAI() {
        this.score = 0;
        this.destination = 0;
        this.count = 0;
        this.index = 0;
    }


	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {

		return new MyPlayer();
    }
    

	// TODO A sample player that selects a random move
	private class MyPlayer implements Player {

		private final Random random = new Random();

		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
				Consumer<Move> callback) {

            List<Move> bestmoves = new ArrayList<Move>();

            if(view.getCurrentPlayer().isDetective()) {
                
                int highestscore = highestScoreMoveDetective(view, location, moves);

                for (Move move : moves) {

                    if (scoreOfMove(move, view, location) == highestscore){
                        bestmoves.add(move);
                    }
                    
                }

                int randomInt = random.nextInt(bestmoves.size());
                System.out.println("Detective chooses " + bestmoves.get(randomInt));
                callback.accept(bestmoves.get(randomInt));   



            }
			else { 
                callback.accept(new ArrayList<>(moves).get(0));
            }

		}
    }
    

    @Override
    public List<Spectator> createSpectators(ScotlandYardView view) { 
        Spectator spectator = new Spectator() {
            
        };
        ArrayList<Spectator> listOfSpectators = new ArrayList<Spectator>();
        listOfSpectators.add(spectator);
        return listOfSpectators;

    }


    @Override
    public void ready(Visualiser visualiser, ResourceProvider provider) {

    }


    @Override
    public void finish() {

    }

    public Integer highestScoreMoveDetective(ScotlandYardView view, int location, Set<Move> moves){
        //this.moves.clear();
        Integer highestScore = 0;

        for (Move m : moves) {
            Integer move1 = scoreOfMove(m, view, location);
            if (move1 > highestScore) {
                highestScore = move1;
            } 
        }

        return highestScore;
    }

    public void setTheIndex(int index) {
        this.index = index;
    }

    public Integer scoreOfMove(Move move, ScotlandYardView view, int location) {
        if (move.getClass() == PassMove.class) return 100;
        move.visit(this);
        Integer a = howManyMovesToMrX(move, view, location, this.destination);
        Integer b = varietyOfRoutes(move, view, location, this.destination);
        return 3*a + b;
    }

    public Integer howManyMovesToMrX(Move move, ScotlandYardView view, int location, int destination) {
        // for the edges from the location...
        boolean MrXHere = false;
        Collection<Edge<Integer, Transport>> edges =  view.getGraph().getEdgesFrom(view.getGraph().getNode(destination));

        while (MrXHere == false) {

            // checking if any detectives are 1 moves away from potential move
		    for (Edge<Integer, Transport> edge : edges) {
                int destinationOfEdge = edge.destination().value();
                if (mrXisHere(destinationOfEdge, view)) {
                    this.count = 3;
                    MrXHere = true;
                } 
            }

            // checking if any detectives are 2 moves away from potential move
		    for (Edge<Integer, Transport> edge : edges) {
                Collection<Edge<Integer, Transport>> edges2 =  view.getGraph().getEdgesFrom(edge.destination());
                for (Edge<Integer, Transport> edge2 : edges2) {
                    int destinationOfEdge = edge2.destination().value();
                    if(mrXisHere(destinationOfEdge, view)) {
                        this.count = 2;
                        MrXHere = true;
                    }
                }
            }

            // checking if any detectives are 3 moves away from potential move
            for (Edge<Integer, Transport> edge : edges) {
                Collection<Edge<Integer, Transport>> edges2 =  view.getGraph().getEdgesFrom(edge.destination());
                for (Edge<Integer, Transport> edge2 : edges2) {
                    Collection<Edge<Integer, Transport>> edges3 =  view.getGraph().getEdgesFrom(edge.destination());
                    for (Edge<Integer, Transport> edge3 : edges2) {
                        int destinationOfEdge = edge3.destination().value();
                        if(mrXisHere(destinationOfEdge, view)) {
                            this.count = 1;
                            MrXHere = true;
                        }
                    }
                }
            }

            this.count = 0;
            MrXHere = true;
        }   

        return this.count;
    }

    public Integer varietyOfRoutes(Move move, ScotlandYardView view, int location, int destination) {
        Collection<Edge<Integer, Transport>> edges =  view.getGraph().getEdgesFrom(view.getGraph().getNode(destination));
        Integer numberOfRoutes = edges.size();
        return numberOfRoutes;
    }

    @Override
    public void visit(DoubleMove move){
        this.destination = move.finalDestination();
    }

    @Override
    public void visit(PassMove move){
        this.destination = -1;
    }    
    
    @Override
    public void visit(TicketMove move){
        this.destination = move.destination();
        
    }

    public boolean mrXisHere(int destination, ScotlandYardView view) {
        for ( Colour colour : view.getPlayers()) {
            if(colour.isMrX()) {
                if(view.getPlayerLocation(colour) == Optional.of(destination)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Integer findTheDestination(Move move) {
        move.visit(this);
        return this.destination;
    }

}

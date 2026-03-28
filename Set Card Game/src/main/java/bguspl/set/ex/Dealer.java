package bguspl.set.ex;

import bguspl.set.Env;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    //added fields :
    /**
     * The sets to test Queue (responsible for handling and testing the sets each player makes)
     */
    protected BlockingQueue<int[]> testSetsQueue;

    /**
     * an array of the players threads
     */
    public Thread[] playersThreads;


    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        this.testSetsQueue = new ArrayBlockingQueue<>(players.length , true);
        this.playersThreads = new Thread[players.length];
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        table.players=this.players;
        for (int i = 0 ; i < players.length ; i++){
            playersThreads[i] = new Thread(players[i],"player "+ players[i].id);
            playersThreads[i].start();
        }
        reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis;
        while (!shouldFinish()) {
            placeCardsOnTable();
            table.hints();
            timerLoop();
            updateTimerDisplay(false);
            removeAllCardsFromTable();
        }
        removealltokens();
        announceWinners();
        callPlayerThreadsForTerminating();
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            updateTimerDisplay(false);
            removeCardsFromTable();
            placeCardsOnTable();
        }
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        synchronized (this){
            notifyAll();
        }
        callPlayerThreadsForTerminating();
        terminate = true;
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return (terminate || env.util.findSets(deck, 1).size() == 0);
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeCardsFromTable() {
        if(!testSetsQueue.isEmpty()){
            try {
                int[] arrToTest = testSetsQueue.take();
                int playerId = arrToTest[0];
                for (int i = 1 ; i < arrToTest.length ; i ++ ){
                    if (table.tokensArray[arrToTest[i]][playerId] == 0){
                        players[playerId].hasLegalSet = -1;
                        playersThreads[playerId].interrupt();
                        return;
                    }
                }
                int[] sendToTest = Arrays.copyOfRange(arrToTest, 1 , arrToTest.length);
                for(int i = 0 ; i < sendToTest.length ; i ++){
                    if(table.slotToCard[sendToTest[i]] != null){
                    sendToTest[i] = table.slotToCard[sendToTest[i]];
                    }
                }
                boolean isLegalSet = env.util.testSet(sendToTest);
                if(isLegalSet){
                        for(int j : sendToTest) {
                            table.removeCard(table.cardToSlot[j]);
                        }
                    players[playerId].playerCards = new ArrayList<>();
                    updateTimerDisplay(true);
                    players[playerId].hasLegalSet = 1;
                }
                else if (!isLegalSet){
                    players[playerId].hasLegalSet = 0;
                }
                playersThreads[playerId].interrupt();
            } catch (InterruptedException ignored) {}
        }
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        // this list is added in order to pick random slot to place the card on
        ArrayList<Integer> pickRandSlot = new ArrayList<>();
        for(int i = 0 ; i < table.slotToCard.length ; i++){
            pickRandSlot.add(i);
        }
        // shuffles the lists then we pick a random slot to place a card on , and a random card from the deck
        Collections.shuffle(pickRandSlot);
        Collections.shuffle(deck);
        for (int i = 0 ;i < table.slotToCard.length ; i++){
            int slot = pickRandSlot.get(i);
            if(!deck.isEmpty()) {
                if (table.slotToCard[slot] == null) {
                    table.placeCard(deck.remove(0), slot);
                }
            }
        }
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
            long currentTime = System.currentTimeMillis();
        // if reset is true , we reset the time and start counting down again.
            if (reset) {
                reshuffleTime = currentTime + env.config.turnTimeoutMillis;
            }
            // if the countdown reaches 0 , then we call for the function itself with true to start counting again.
            else if (reshuffleTime <= currentTime) {
                updateTimerDisplay(true);
                return;
            }
        // update the time ...
            long remainingTime = reshuffleTime - currentTime;
            boolean isWarning = remainingTime <= env.config.turnTimeoutWarningMillis;
            env.ui.setCountdown(remainingTime, isWarning);
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // this list is added in order to pick random slot to place the card on
        ArrayList<Integer> arrayList = new ArrayList<>();
        for(int i = 0 ; i < table.slotToCard.length ; i++){
            arrayList.add(i);
        }
        // shuffles the list then we pick a random slot to place a card on , and a random card from the deck
        Collections.shuffle(arrayList);
        for(int i = 0 ; i < arrayList.size() ; i++) {
           Integer curslot = table.slotToCard[arrayList.get(i)];
           table.removeCard(arrayList.get(i));
           if(curslot != null) {
               deck.add(curslot);
           }
       }
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        int MaxPoints = 0;
        int[] Winners;
        int winCounter = 0;
        for (Player player : players) {
            if (player.score() > MaxPoints) {
                MaxPoints = player.score();
            }
        }

        for (Player player : players) {
            if (player.score() == MaxPoints) {
                winCounter++;
            }
        }

        Winners = new int[winCounter];
        int nextInd = 0;
        for (int i = 0 ; i < players.length  ; i++){
            if(players[i].score() == MaxPoints) {
                Winners[nextInd] = players[i].id;
                nextInd++;
            }
        }
        env.ui.announceWinner(Winners);
    }

    /**
     * we call this function for terminating all the players threads , and so for doing the bonus Task :
     * Terminating all threads (that you created) gracefully and in reverse order to the order they were created in.
     */
    private void callPlayerThreadsForTerminating() {
        for (int i = playersThreads.length - 1 ;  i >=  0 ;  i--) {
            try {
                players[i].terminate();
                players[i].playerThread.interrupt();
                playersThreads[i].join();
            } catch (InterruptedException ignored) {}
        }
    }

    private void removealltokens(){
        for (int j = 0 ; j < table.players.length; j++ ) {
            for (int i = 0; i < table.slotToCard.length; i++) {
                    table.removeToken(j,i);
            }
        }
    }

}

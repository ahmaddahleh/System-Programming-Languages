package bguspl.set.ex;

import bguspl.set.Env;

import java.util.ArrayList;

import java.util.Random;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The id of the player (starting from 0).
     */
    public final int id;
    /**
     * The game environment object.
     */
    private final Env env;
    /**
     * Game entities.
     */
    private final Table table;
    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;
    /**
     * The thread representing the current player. // changed the field to public
     */
    public Thread playerThread;
    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;
    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;
    /**
     * The current score of the player.
     */
    private int score;
    /**
     * The game dealer.
     */
    private Dealer dealer;


    // Added fields :
    /**
     * this field indicates if a set that have been sent to the dealer is legal or not (after checking it is updated )
     */
    protected volatile int hasLegalSet = -1;
    /**
     * this field indicates whether the thread is sleeping or not , so we can decide what to do then
     */
    protected volatile boolean threadIsSleppin;
     public volatile boolean sizeisfit = true;
    /**
     * holds the cards of each player(where a token is placed)
     */
    public volatile ArrayList<Integer> playerCards;
    /**
     * used to maintain a correct check of a set
     */
    public volatile boolean arrayChecked = false;
    /**
     * this field indicates whether the thread is penalized or not , so we can decide what to do then
     */
    protected volatile boolean GotPenalty;
    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.dealer = dealer;
        this.table = table;
        this.id = id;
        this.human = human;
        this.score = 0;
        this.playerCards = new ArrayList<>();
        this.GotPenalty = false;
    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        if (!human) createArtificialIntelligence();
        while (!terminate) {
            synchronized (this){
                notifyAll();
            }
            if (playerCards.size() == 3 && !GotPenalty && !arrayChecked) {
                try {
                    dealer.testSetsQueue.put(makeSet());
                } catch (InterruptedException ignored) {}
                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException ignored) {}
                if (hasLegalSet == 1) {
                    threadIsSleppin = true;
                    point();
                    hasLegalSet = -1;
                } else if (hasLegalSet == 0) {
                    GotPenalty = true;
                    threadIsSleppin = true;
                    penalty();
                    hasLegalSet = -1;
                }
                    arrayChecked = true;
            }
        }
        if (!human) try {
            aiThread.join();
        } catch (InterruptedException ignored) {
        }
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very, very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.info("thread " + Thread.currentThread().getName() + " starting.");

            while (!terminate) {
                Random random = new Random();
                int chosenSlot = random.nextInt(env.config.columns * env.config.rows);
                keyPressed(chosenSlot);
                if (playerCards.size() == 3) {
                    try {
                        synchronized (this) {
                            wait();
                        }
                    } catch (InterruptedException ignored) {}
                }
            }
            env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        terminate = true;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        if (!threadIsSleppin && sizeisfit) {
            if (!playerCards.contains(slot) && playerCards.size() < 3) {
                table.placeToken(id, slot);
                arrayChecked = false;
            } else if (playerCards.contains(slot)) {
                table.removeToken(id, slot);
                arrayChecked = false;
            }
            sizeisfit = playerCards.size() <= 3;
        }
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        env.ui.setScore(id, ++score);
        long remainingTime = env.config.pointFreezeMillis;
        while (remainingTime > 0) {
            env.ui.setFreeze(id, remainingTime);
            long sleepTime = Math.min(remainingTime, 1000);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ignored) {
            }
            remainingTime -= 1000;
        }
        threadIsSleppin = false;
        env.ui.setFreeze(id, 0);
        int ignored = table.countCards(); // this part is just for demonstration in the unit tests
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        long remainingTime = env.config.penaltyFreezeMillis;
        while (remainingTime > 0) {
            env.ui.setFreeze(id, remainingTime);
            long sleepTime = Math.min(remainingTime, 1000);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ignored) {
            }
            remainingTime -= 1000;
        }
        GotPenalty = false;
        threadIsSleppin = false;
        env.ui.setFreeze(id, 0);
    }

    public int score() {
        return score;
    }

    /**
     * this function is called in order to take a set from the list(player cards) and send it to the dealer to check
     */
    public int[] makeSet() {
        synchronized (playerCards) {
            int[] arr = new int[playerCards.size() + 1];
            arr[0] = id;
            for (int i = 0; i < playerCards.size(); i++) {
                if (playerCards.get(i) != null) {
                    int slot = playerCards.get(i);
                    arr[i + 1] = slot;
                }
            }
            return arr;
        }
    }

}

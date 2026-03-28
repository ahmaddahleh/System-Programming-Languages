package bguspl.set.ex;

import bguspl.set.Env;

import bguspl.set.UserInterfaceDecorator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class contains the data that is visible to the player.
 *
 * @inv slotToCard[x] == y iff cardToSlot[y] == x
 */
public class Table {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Mapping between a slot and the card placed in it (null if none).
     */
    protected final Integer[] slotToCard; // card per slot (if any)

    /**
     * Mapping between a card and the slot it is in (null if none).
     */
    protected final Integer[] cardToSlot; // slot per card (if any)

    /**
     *  indicates if a there is a token in the slot or not
     */
    protected int[][] tokensArray;

    /**
     * this is an array of the players in the game (it may contain both AI and human)
     */
    Player[] players;

    /**
     * this is an array of objects , each object represents a locker for a slot in the table
     */
    Object[] slotLockers;

    /**
     * Constructor for testing.
     *
     * @param env        - the game environment objects.
     * @param slotToCard - mapping between a slot and the card placed in it (null if none).
     * @param cardToSlot - mapping between a card and the slot it is in (null if none).
     */
    public Table(Env env, Integer[] slotToCard, Integer[] cardToSlot) {

        this.env = env;
        this.slotToCard = slotToCard;
        this.cardToSlot = cardToSlot;
        this.tokensArray = new int[env.config.columns * env.config.rows][env.config.players];
        this.slotLockers = new Object[slotToCard.length];

        for (int i = 0; i < env.config.columns * env.config.rows; i++) {
            for (int j = 0; j < env.config.players; j++) {
                tokensArray[i][j] = 0 ;
            }
        }

        for (int i = 0 ; i < slotToCard.length ; i++){
            slotLockers[i] = new Object();
        }
    }

    /**
     * Constructor for actual usage.
     *
     * @param env - the game environment objects.
     */
    public Table(Env env) {
        this(env, new Integer[env.config.tableSize], new Integer[env.config.deckSize]);
    }

    /**
     * This method prints all possible legal sets of cards that are currently on the table.
     */
    public void hints() {
        List<Integer> deck = Arrays.stream(slotToCard).filter(Objects::nonNull).collect(Collectors.toList());
        env.util.findSets(deck, Integer.MAX_VALUE).forEach(set -> {
            StringBuilder sb = new StringBuilder().append("Hint: Set found: ");
            List<Integer> slots = Arrays.stream(set).mapToObj(card -> cardToSlot[card]).sorted().collect(Collectors.toList());
            int[][] features = env.util.cardsToFeatures(set);
            System.out.println(sb.append("slots: ").append(slots).append(" features: ").append(Arrays.deepToString(features)));
        });
    }

    /**
     * Count the number of cards currently on the table.
     *
     * @return - the number of cards on the table.
     */
    public int countCards() {
        int cards = 0;
        for (Integer card : slotToCard)
            if (card != null)
                ++cards;
        return cards;
    }

    /**
     * Places a card on the table in a grid slot.
     * @param card - the card id to place in the slot.
     * @param slot - the slot in which the card should be placed.
     *
     * @post - the card placed is on the table, in the assigned slot.
     */
    public void placeCard(int card, int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}
        synchronized (slotLockers[slot]) {
            cardToSlot[card] = slot;
            slotToCard[slot] = card;
            // UPDATING THE USER INTERFACE :
            env.ui.placeCard(card, slot);
        }
    }

    /**
     * Removes a card from a grid slot on the table.
     * @param slot - the slot from which to remove the card.
     */
    public void removeCard(int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}
        synchronized (slotLockers[slot]) {
            if (slotToCard[slot] != null) {
                cardToSlot[slotToCard[slot]] = null;
                slotToCard[slot] = null;
                // each player that have token right there should be removed
                for (int i = 0; i < tokensArray[slot].length; i++) {
                    if (tokensArray[slot][i] == 1) {
                        removeToken(i, slot);
                    }
                }
                // UPDATING THE USER INTERFACE :
                env.ui.removeCard(slot);
            }
        }
    }

    /**
     * Places a player token on a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot on which to place the token.
     */
    public void placeToken(int player, int slot) {
        if (slotToCard[slot] != null) {
            synchronized (slotLockers[slot]) {
                tokensArray[slot][player] = 1;
                players[player].playerCards.add((Integer) slot);
                env.ui.placeToken(player, slot);
            }
        }
    }

    /**
     * Removes a token of a player from a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot from which to remove the token.
     * @return       - true iff a token was successfully removed.
     */
    public boolean removeToken(int player, int slot) {
        synchronized (slotLockers[slot]) {
            if (tokensArray[slot][player] == 1) {
                tokensArray[slot][player] = 0;
                players[player].playerCards.remove((Integer) slot);
                env.ui.removeToken(player, slot);
                return true;
            }
            return false;
        }
    }
}
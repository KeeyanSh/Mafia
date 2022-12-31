package com.company;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The type Player.
 */
abstract public class Player implements Serializable {
    private ArrayList<Player> players;
    /**
     * The Name.
     */
    protected String name;
    private int status;

    /**
     * Instantiates a new Player.
     *
     * @param name the name
     */
    public Player(String name) {
        this.name = name;
        players = new ArrayList<>();
        status = 1;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Shows act of the player.
     */
    abstract public void showAct();
}


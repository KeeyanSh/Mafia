package com.company;

import java.util.ArrayList;

/**
 * The type Mafia.
 */
public class Mafia extends Player {
    /**
     * The Mafias.
     */
    protected static ArrayList<Mafia> mafias;
    /**
     * The Citizens.
     */
    protected static ArrayList<Citizen> citizens;

    /**
     * Instantiates a new Mafia.
     *
     * @param name the name
     */
    public Mafia(String name) {
        super(name);
        mafias = new ArrayList<>();
        citizens = new ArrayList<>();
    }

    @Override
    public void showAct() {
    }

}



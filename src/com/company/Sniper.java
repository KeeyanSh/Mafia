package com.company;

/**
 * The type Sniper.
 */
public class Sniper extends Citizen {
    private int shots;

    /**
     * Instantiates a new Sniper.
     *
     * @param name the name
     */
    public Sniper(String name) {
        super(name);
        shots = 3;
    }

    @Override
    public void showAct() {
        System.out.println("\n\n\n+ Decide Who To Kill ?\n(Invalid numbers means nobody.)");
    }

    /**
     * Gets left shots number.
     *
     * @return the shots
     */
    public int getShots() {
        return shots;
    }

    /**
     * Shots.
     */
    public void shot() {
            shots--;
    }
}
package com.company;

/**
 * The type Die hard.
 */
public class DieHard extends Citizen {
    private int lives, beAware;

    /**
     * Instantiates a new Die hard.
     *
     * @param name the name
     */
    public DieHard(String name) {
        super(name);
        lives = 1;
        beAware = 2;
    }

    /**
     * Gets lives.
     *
     * @return the lives
     */
    public int getLives() {
        return lives;
    }


    /**
     * reduces lives num if being shot.
     */
    public void beShot() {
        lives--;
    }

    /**
     * Gets be aware.
     *
     * @return the be aware
     */
    public int getBeAware() {
        return beAware;
    }


    /**
     * Be aware of dead characters.
     */
    public void beAware() {
        beAware--;
    }

    @Override
    public void showAct() {
        System.out.println("\n\n\n+ Do you want to know the characters that are out tonight ?" +
                "\n\t1. Yes" + "\n\tOther nums. No");
    }
}

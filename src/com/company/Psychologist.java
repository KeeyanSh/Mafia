package com.company;

/**
 * The type Psychologist.
 */
public class Psychologist extends Citizen {
    /**
     * Instantiates a new Psychologist.
     *
     * @param name the name
     */
    public Psychologist(String name) {
        super(name);
    }

    @Override
    public void showAct() {
        System.out.println("+ \n\n\nWho do you want to be quiet the next day ?\n(Invalid numbers means nobody.)");
    }
}

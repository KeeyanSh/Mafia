package com.company;


/**
 * The type God father.
 */
public class GodFather extends Mafia {
    /**
     * Instantiates a new God father.
     *
     * @param name the name
     */
    public GodFather(String name) {
        super(name);
    }

    @Override
    public void showAct() {
        System.out.println("+ Decide Who Should Be Killed ?\n(Invalid numbers means nobody.)");
    }
}

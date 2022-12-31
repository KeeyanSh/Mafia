package com.company;

/**
 * The type Mayor.
 */
public class Mayor extends Citizen {
    /**
     * Instantiates a new Mayor.
     *
     * @param name the name
     */
    public Mayor(String name) {
        super(name);
    }

    @Override
    public void showAct() {
        System.out.println("\n\n\n+ Do you accept the vote results ?\n\t1.Yes\n\tOther nums. No");
    }
}

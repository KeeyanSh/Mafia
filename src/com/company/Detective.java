package com.company;

/**
 * The type Detective.
 */
public class Detective extends Citizen {
    /**
     * Instantiates a new Detective.
     *
     * @param name the name
     */
    public Detective(String name) {
        super(name);
    }

    @Override
    public void showAct() {
        System.out.println("\n\n\n+ Whoose character do you want to know ?\n(Invalid number means nobody.)");
    }
}

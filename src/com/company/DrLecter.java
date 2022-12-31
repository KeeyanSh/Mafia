package com.company;

/**
 * The type Dr lecter.
 */
public class DrLecter extends Mafia {
    private int saveYourself, saves;

    /**
     * Instantiates a new Dr lecter.
     *
     * @param name the name
     */
    public DrLecter(String name) {
        super(name);
        saveYourself = 1;
        saves = 3;
    }

    @Override
    public void showAct() {
        System.out.println("\n\n\n+ Which Mafia you want to save ?\n(Invalid numbers means nobody.)");
    }


    /**
     * Saves the player himself.
     */
    public void saveYourself() {
        if (saveYourself > 0 && saves > 0) {
            save();
            saveYourself--;
        } else System.out.println("you cant save yourself any more...");
    }

    /**
     * Gets left saves number.
     *
     * @return the saves
     */
    public int getSaves() {
        return saves;
    }

    /**
     * Save.
     */
    public void save() {
        saves--;
    }

    /**
     * Gets save yourself num.
     *
     * @return the save yourself
     */
    public int getSaveYourself() {
        return saveYourself;
    }
}

package com.company;

/**
 * The type City doctor.
 */
public class CityDoctor extends Citizen {
    private int saveYourself, saves;

    /**
     * Instantiates a new City doctor.
     *
     * @param name the name
     */
    public CityDoctor(String name) {
        super(name);
        saveYourself = 1;
        saves = 3;
    }

    @Override
    public void showAct() {
        System.out.println("\n\n\n+ Which Player you want to save ?\n(Invalid numbers means nobody.)");
    }

    /**
     * Saves the current player.
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
     * reduces Save number.
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

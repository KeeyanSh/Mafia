package com.company;

import java.io.Serializable;

/**
 * The enum Character.
 */
public enum Character implements Serializable {
    /**
     * The Godfather.
     */
    GODFATHER(new GodFather("")),
    /**
     * The Drlecter.
     */
    DRLECTER(new DrLecter("")),
    /**
     * The Mafia.
     */
    MAFIA(new Mafia("")),

    /**
     * The Citydoctor.
     */
    CITYDOCTOR(new CityDoctor("")),
    /**
     * The Detective.
     */
    DETECTIVE(new Detective("")),
    /**
     * The Sniper.
     */
    SNIPER(new Sniper("")),
    /**
     * The Diehard.
     */
    DIEHARD(new DieHard("")),
    /**
     * The Mayor.
     */
    MAYOR(new Mayor("")),
    /**
     * The Psychologist.
     */
    PSYCHOLOGIST(new Psychologist("")),
    /**
     * The Citizen.
     */
    CITIZEN(new Citizen(""));

    private Character(Player role) {
        this.player = role;
    }

    private Player player;

    /**
     * Gets player instance.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

}
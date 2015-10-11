package com.hacknc.database;

/**
 * Created by Gunnar on 10/10/2015.
 */
public class CrimeDataRow {

    private String countyName, stateName;
    private int population, violentCrime, propertyCrime;

    public CrimeDataRow() {
        countyName = "ERR";
        stateName = "ERR";
    }

    public CrimeDataRow(String countyName, String stateName, int population, int violentCrime, int propertyCrime) {
        this.countyName = countyName;
        this.stateName = stateName;
        this.population = population;
        this.violentCrime = violentCrime;
        this.propertyCrime = propertyCrime;
    }

    public String getCountyName() {
        return countyName;
    }

    public String getStateName() {
        return stateName;
    }

    public int getPopulation() {
        return population;
    }

    public int getViolentCrime() {
        return violentCrime;
    }

    public int getPropertyCrime() {
        return propertyCrime;
    }

    public int getTotalCrime() {
        return violentCrime + propertyCrime;
    }

    /**
     * Gets an arbitrary score for a county with violent crime rated more.
     * @return score for the county
     */
    public double getScore () {
        return (3.0 * violentCrime + 2.0 * propertyCrime) / population;
    }

    /**
     * Gets an arbitrary score for a county with violent crime rated more.
     * @param violent if false, violent does not affect score
     * @param property if false, property does not affect score
     * @return score for the county
     */
    public double getScore (boolean violent, boolean property) {
        return (3.0 * (violent ? violentCrime : 0) + 2.0 * (property ? propertyCrime : 0)) / population;
    }
}

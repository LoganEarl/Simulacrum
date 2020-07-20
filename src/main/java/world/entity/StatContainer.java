package main.java.world.entity;

import main.java.world.trait.Trait;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static main.java.world.entity.EntityTable.*;

public class StatContainer implements Entity.SqlExtender {
    private static final Random RND = new Random(System.currentTimeMillis());

    public static final String SIGNIFIER = "stats";
    private static final String[] HEADERS = new String[]{STR, DEX, INT, WIS, TOUGH, FIT};


    private int strength;
    private int dexterity;
    private int intelligence;
    private int wisdom;
    private int toughness;
    private int fitness;

    public StatContainer() {
        this(0,0,0,0,0,0);
    }

    public StatContainer(int strength, int dexterity, int intelligence, int wisdom, int toughness, int fitness) {
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.wisdom = wisdom;
        this.toughness = toughness;
        this.fitness = fitness;
    }

    public StatContainer(ResultSet readEntry) throws SQLException {
        strength = readEntry.getInt(STR);
        dexterity = readEntry.getInt(DEX);
        intelligence = readEntry.getInt(INT);
        wisdom = readEntry.getInt(WIS);
        toughness = readEntry.getInt(TOUGH);
        fitness = readEntry.getInt(FIT);
    }

    /**
     * performs a stat check on the specified stat and difficulty
     *
     * @param stat               the column name of the stat to roll for.
     * @param difficultyModifier the amount to add or subtract from the result
     * @return the net stat, with 0 and up being a success
     * @throws IllegalArgumentException if you pass in a column name for stat that is not recognized
     */
    public int preformStatCheck(String stat, int difficultyModifier, Set<Trait> augmentations) {
        int baseStat = getStat(stat, augmentations);

        return baseStat - RND.nextInt(100) + difficultyModifier;
    }

    public int getStat(String columnName, Set<Trait> augmentations) {
        int sum = augmentations.stream().mapToInt(trait -> trait.getStatModifiers().getStat(columnName, Collections.emptySet())).sum();

        switch (columnName) {
            case STR:
                sum += strength;
                break;
            case DEX:
                sum += dexterity;
                break;
            case INT:
                sum += intelligence;
                break;
            case WIS:
                sum += wisdom;
                break;
            case TOUGH:
                sum += toughness;
                break;
            case FIT:
                sum += fitness;
                break;
            default:
                throw new IllegalArgumentException("Column name " + columnName + " is not an entity stat");
        }

        return sum;
    }

    public double getWeightSoftLimit() {
        //15 to 100 kg based on str
        return strength / 100.0 * 85 + 15;
    }

    public double getWeightHardLimit() {
        //40 to 400 kg based on str
        return strength / 100.0 * 360 + 40;
    }

    public int calculateMaxHP() {
        return getToughness() * 2 + (getStrength() + getDexterity()) / 2;
    }

    public int calculateMaxStamina() {
        return getFitness() * 2 + (getStrength() + getDexterity()) / 2;
    }

    public int calculateMaxMP() {
        return getIntelligence() * 2 + getWisdom();
    }

    public int calculateMaxBurnout() {
        return getWisdom() * 2 + getIntelligence();
    }

    @Override
    public String getSignifier() {
        return SIGNIFIER;
    }

    @Override
    public Object[] getInsertSqlValues() {
        return new Object[]{strength, dexterity, intelligence, wisdom, toughness, fitness};
    }

    @Override
    public String[] getSqlColumnHeaders() {
        return HEADERS;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[STR:%d] [DEX:%d] [INT:%d] [WIS:%d] [FIT:%d] [TOUGH:%d]",
                strength, dexterity, intelligence, wisdom, fitness, toughness);
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getWisdom() {
        return wisdom;
    }

    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
    }

    public int getToughness() {
        return toughness;
    }

    public void setToughness(int toughness) {
        this.toughness = toughness;
    }

    public int getFitness() {
        return fitness;
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }
}

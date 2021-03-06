package main.java.world.item.weapon;

import main.java.world.item.DamageType;
import main.java.world.item.Item;
import main.java.world.item.ItemFactory;
import main.java.world.item.ItemType;
import main.java.world.meta.World;

import java.sql.ResultSet;
import java.util.Map;
import java.util.Random;

import static main.java.world.item.weapon.WeaponStatTable.*;

public class Weapon extends Item {
    private static final Random  rnd = new Random();

    public Weapon(ResultSet fromEntry, ItemFactory factory, String databaseName) throws Exception{
        super(fromEntry,factory, databaseName);
    }

    @Override
    public ItemType getItemType() {
        return ItemType.weapon;
    }

    @Override
    protected Map<String, String> getDerivedStats() {
        return WeaponStatTable.getStatsForWeapon(getItemName(),getDatabaseName());
    }

    @Override
    protected boolean compositeStatsExistInWorld(World targetWorld) {
        initStats();
        return WeaponStatTable.existsInWorld(getItemName(), targetWorld);
    }

    @Override
    protected boolean writeCompositeStatsToWorld(World targetWorld) {
        initStats();
        return WeaponStatTable.writeStatsToWorld(getDerivedStats(),targetWorld);
    }

    public double getStaminaUsage(int str, int dex){
        double weight = super.getWeight();
        double balance = getBalance();
        double base = 5 + weight * 4;
        double strengthReduction = 0.05 * str;
        double balanceReduction = (50-dex)/100.0*balance*weight;

        return (base - strengthReduction - balanceReduction) * 10;
    }

    public int rollDamage(int str, int dex, int intel, int wis){
        int dmgRange = getMaxBaseDamage() - getMinBaseDamage();

        int rawDmg = rnd.nextInt(dmgRange) + getMinBaseDamage();

        float strAddition = str * getStrScalar();
        float dexAddition = dex * getDexScalar();
        float intAddition = intel * getIntScalar();
        float wisAddition = wis * getWisScalar();

        rawDmg += Math.floor(strAddition + dexAddition + intAddition + wisAddition);
        return rawDmg;
    }

    public int rollHit(int str, int dex, int intel, int wis){
        int rawRoll = rnd.nextInt(99) + 1;
        rawRoll = modWithPrimaryStat(rawRoll,str,dex,intel,wis);

        if(rawRoll < 0){
            int simpleBonus = getSimpleBonus();
            if(rawRoll + simpleBonus > 0)
                rawRoll = 0;
            else
                rawRoll += simpleBonus;
        }

        rawRoll += getHitBonus();

        return rawRoll;
    }

    private int modWithPrimaryStat(int rawRoll, int str, int dex, int intel, int wis){
        int[] stats = {str, dex, intel, wis};
        float[] scalars = {getStrScalar(), getDexScalar(), getIntScalar(), getWisScalar()};
        int bestIndex = 0;
        float bestScalar = -1.0f;
        for(int i = 0; i < scalars.length; i++){
            if(scalars[i] > bestScalar || (scalars[i] == bestScalar && stats[i] > stats[bestIndex])){
                bestIndex = i;
                bestScalar = scalars[i];
            }
        }
        return stats[bestIndex] - rawRoll;
    }

    public int getMinBaseDamage(){
        initStats();
        return getCastInt(MIN_BASE_DAMAGE);
    }

    public int getMaxBaseDamage(){
        initStats();
        return getCastInt(MAX_BASE_DAMAGE);
    }

    public float getAttackSpeed(){
        initStats();
        return getCastFloat(ATTACK_SPEED);
    }

    public float getBalance(){
        initStats();
        return getCastFloat(BALANCE);
    }

    public float getStrScalar(){
        initStats();
        return getCastFloat(STR_SCALAR);
    }

    public float getDexScalar() {
        initStats();
        return getCastFloat(DEX_SCALAR);
    }

    public float getIntScalar() {
        initStats();
        return getCastFloat(INT_SCALAR);
    }

    public float getWisScalar() {
        initStats();
        return getCastFloat(WIS_SCALAR);
    }

    public int getHitBonus() {
        initStats();
        return getCastInt(HIT_BONUS);
    }

    public int getSimpleBonus() {
        initStats();
        return getCastInt(SIMPLE_BONUS);
    }

    public DamageType getDamageType() {
        initStats();
        try {
            return DamageType.valueOf(getCastString(DAMAGE_TYPE));
        }catch (Exception ignored){}
        return DamageType.slash;
    }

    private static ItemFactory.ItemParser parser = new ItemFactory.ItemParser() {
        @Override
        public ItemType getAssociatedType() {
            return ItemType.weapon;
        }

        @Override
        public Item parseFromResultSet(ResultSet fromEntry, ItemFactory sourceFactory, String databaseName) throws Exception {
            return new Weapon(fromEntry,sourceFactory,databaseName);
        }
    };

    public static ItemFactory.ItemParser factory(){
        return parser;
    }
}

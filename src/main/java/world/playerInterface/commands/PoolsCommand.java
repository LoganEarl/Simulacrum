package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.world.WorldModel;
import main.java.world.entity.pool.EntityPoolContainer;
import main.java.world.entity.pool.PoolValueContainer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static main.java.world.playerInterface.ColorTheme.*;

public class PoolsCommand extends EntityCommand {
    private static final int BAR_LENGTH = 15;
    private static Map<Client, PoolsCommand> registeredContinuingPools = new HashMap<>();

    private int lastHP = 0;
    private int lastMP = 0;
    private int lastBurn = 0;
    private int lastStam = 0;

    private boolean complete = false;
    private long nextUpdateTimestamp = -1;
    private boolean useBars;
    private double updateIntervalSeconds = -1;

    private boolean firstRun = true;

    public PoolsCommand(boolean useBars, Client sourceClient, WorldModel model) {
        this(useBars,-1,sourceClient, model);
    }

    public PoolsCommand(boolean useBars, double updateIntervalSeconds, Client sourceClient, WorldModel model) {
        super(sourceClient, model);
        this.updateIntervalSeconds = updateIntervalSeconds;
        nextUpdateTimestamp = System.currentTimeMillis();
        this.useBars = useBars;

        if(updateIntervalSeconds != -1){
            PoolsCommand.killUpdaterOfClient(sourceClient);
            registeredContinuingPools.put(sourceClient,this);
        }
    }

    @Override
    protected boolean requiresBalance() {
        return false;
    }

    @Override
    protected boolean entityCommandIsComplete() {
        return complete;
    }

    @Override
    protected void executeEntityCommand() {
        PoolValueContainer pools = getSourceEntity().getPools().getCurrentValues();
        if (lastHP != pools.getHp() || lastBurn != pools.getBurnout() ||
                lastMP != pools.getMp() || lastStam != pools.getStamina()) {
            lastHP = pools.getHp();
            lastBurn = pools.getBurnout();
            lastMP = pools.getMp();
            lastStam = pools.getStamina();

            getSourceClient().sendMessage(getDisplayText());
        }
        if (updateIntervalSeconds != -1) {
            if(firstRun){
                firstRun = false;
                getSourceClient().sendMessage("Now updating pools");
            }
            nextUpdateTimestamp = System.currentTimeMillis() + (long) (updateIntervalSeconds * 1000) - 100;
        }else
            complete = true;

    }

    private String getDisplayText() {
        PoolValueContainer pools = getSourceEntity().getPools().getCurrentValues();
        PoolValueContainer maxPools = getSourceEntity().getPools().getMaxValues();
        String hpBar = "", stamBar = "", mpBar = "", burnBar = "";
        if (useBars) {
            hpBar = getBarForRatio(pools.getHp() / (double) maxPools.getHp(), BAR_LENGTH);
            stamBar = getBarForRatio(pools.getStamina() / (double) maxPools.getStamina(), BAR_LENGTH);
            mpBar = getBarForRatio(pools.getMp() / (double) maxPools.getMp(), BAR_LENGTH);
            burnBar = getBarForRatio(pools.getBurnout() / (double) maxPools.getBurnout(), BAR_LENGTH);
        }

        return String.format(Locale.US,
                "HP:    %4d/%-4d\t %s\nSTAM:  %4d/%-4d\t %s\nMP:    %4d/%-4d\t %s\nBURN:  %4d/%-4d\t %s\n",
                pools.getHp(), maxPools.getHp(), getMessageInColor(hpBar, HP_COLOR),
                pools.getStamina(), maxPools.getStamina(), getMessageInColor(stamBar,STAMINA_COLOR),
                pools.getMp(), maxPools.getMp(), getMessageInColor(mpBar, MP_COLOR),
                pools.getBurnout(), maxPools.getBurnout(), getMessageInColor(burnBar,BURNOUT_COLOR));

    }

    private String getBarForRatio(double ratio, int length) {
        int completion = (int) (ratio * length);
        StringBuilder text = new StringBuilder("[");
        for (int i = 0; i < length; i++) {
            if (i < completion)
                text.append("I");
            else
                text.append("_");
        }
        text.append("]");
        return text.toString();
    }

    @Override
    public long getStartTimestamp() {
        return nextUpdateTimestamp;
    }

    @Override
    protected boolean canDoWhenDying() {
        return true;
    }

    public void stopUpdates() {
        complete = true;
    }

    public static void killUpdaterOfClient(Client client){
        if(registeredContinuingPools.containsKey(client)){
            registeredContinuingPools.get(client).stopUpdates();
            registeredContinuingPools.remove(client);
        }
    }
}

package me.justinjaques.earthspike;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.sun.org.apache.xerces.internal.impl.XMLEntityHandler;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class EarthSpike extends EarthAbility implements AddonAbility {

    private enum States{

        SOURCE_SELECTED, TRAVELLING, SPIKE

    }
    private static final String AUTHOR = ChatColor.GREEN + "Viridescent_";
    private static final String VERSION = ChatColor.GREEN + "1.0.0";
    private static final String NAME = "EarthSpike";
    private static long COOLDOWN;
    private static long DISTANCE_UNTIL_SPIKE;
    private static long SOURCE_RANGE;
    private static double SPEED;
    static String path = "ExtraAbilites.Viridescent_.Earth.EarthSpike.";

    private static double DAMAGE;


    private Location location;
    private Listener listener;
    private Permission perm;
    private Block sourceBlock;
    private Vector direction;
    private double distanceTravelled;




    private States state;

    private void setFields() {
        SPEED = ConfigManager.defaultConfig.get().getDouble(path+"SPEED");
        DISTANCE_UNTIL_SPIKE = ConfigManager.defaultConfig.get().getLong(path+"DISTANCE_UNTIL_SPIKE");
        COOLDOWN = ConfigManager.defaultConfig.get().getLong(path+"COOLDOWN");
        SOURCE_RANGE = ConfigManager.defaultConfig.get().getLong(path+"SOURCE_RANGE");



    }


    public EarthSpike(Player player) {


        super(player);

        Block block = getEarthSourceBlock(player, "EarthSpike",SOURCE_RANGE);

        if(block == null) {
            return;

        }

        distanceTravelled = 0;
        sourceBlock = block;
        location = block.getLocation().add(.5, .5, .5);
        setFields();


        state = States.SOURCE_SELECTED;
        if(!bPlayer.isOnCooldown(this)) {
            start();


        }




    }
    public void onClick(){
        if (state == States.SOURCE_SELECTED) {

            direction = GeneralMethods.getDirection(location, GeneralMethods.getTargetedLocation(player, SOURCE_RANGE * SOURCE_RANGE)).normalize().multiply(SPEED);
            direction.multiply(0.5);
            this.direction.setY(0);
            state = States.TRAVELLING;


        }


    }


    private void progressSourceSelected() {

       // playFocusEarthEffect(sourceBlock);

        if(sourceBlock.getLocation().distanceSquared(player.getLocation()) > SOURCE_RANGE * SOURCE_RANGE || !isEarthbendable(player, sourceBlock))  {

            remove();
        }

    }

    private void progressTravelling() {

        location.add(direction);


        distanceTravelled += SPEED;
        System.out.println(distanceTravelled);

        if (distanceTravelled >= DISTANCE_UNTIL_SPIKE) {
            state = States.SPIKE;
            distanceTravelled = 0;

        } else {
            ParticleEffect.REDSTONE.display(location, 10, 0, 0.5, 0, new Particle.DustOptions(Color.fromRGB(165, 80 ,42), (float) 1.2));


        }


    }

    private void affectTargets() {
        List<Entity> targets  = GeneralMethods.getEntitiesAroundPoint(location, 1);
        for (Entity target : targets) {
            if (target.getUniqueId() == player.getUniqueId()) {
                continue;
            }

            DamageHandler.damageEntity(target, DAMAGE, this);

        }

    }

    private void progressSpike() {

        System.out.println(location);
        new RaiseEarth(player, location, 3);
        affectTargets();
        removeWithCooldown();
    }






    @Override
    public void progress() {
        if(!bPlayer.canBend(this)) {

            removeWithCooldown();
            return;
        }

        switch(state) {
            case SOURCE_SELECTED:
                progressSourceSelected();
                break;

            case TRAVELLING:
                progressTravelling();
                break;

            case SPIKE:
                progressSpike();
                break;

        }

    }




    private void removeWithCooldown() {
        bPlayer.addCooldown(this);
        remove();


    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return COOLDOWN;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void load() {
        perm = new Permission("bending.ability.EarthSpike");
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
        listener = new EarthSpikeListener();
        ConfigManager.defaultConfig.get().addDefault(path+"COOLDOWN", 5000);
        ConfigManager.defaultConfig.get().addDefault(path+"DISTANCE_UNTIL_SPIKE", 8);
        ConfigManager.defaultConfig.get().addDefault(path+"SOURCE_RANGE", 4);
        ConfigManager.defaultConfig.get().addDefault(path+"SPEED", 3);
        ConfigManager.defaultConfig.get().addDefault(path+"DAMAGE", 2);

        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);


    }

    @Override
    public void stop() {
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(perm);
        HandlerList.unregisterAll(listener);
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getInstructions() {

        return ChatColor.GREEN + "Press SNEAK on an Earthbendable block near you, and then click on a desired target to send a spike of Earth at them.";
    }

    @Override
    public String getDescription() {

        return ChatColor.GREEN + "EarthSpike is an Earthbending technique that allows it's user to send a spike of Earth from under the ground to any unsuspecting target in turn dealing damage and shooting them into the air.";
    }

}

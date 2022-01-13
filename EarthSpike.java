package me.justinjaques.earthspike;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.sun.org.apache.xerces.internal.impl.XMLEntityHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
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
    private static final String AUTHOR = "&2Viridescent_";
    private static final String VERSION = "&21.0.0";
    private static final String NAME = "EarthSpike";
    private static final long COOLDOWN = 5000;
    private static final long DISTANCE_UNTIL_SPIKE = 8;
    private static final long SOURCE_RANGE = 4;
    private static final long SPIKE_WIDTH = 1;
    private static final long SPIKE_HEIGHT = 6;
    private static final double HITBOX = 1.5;
    private static final double SPEED = 3;

    private double damage = 3;


    private Location location;
    private Listener listener;
    private Permission perm;
    private Block sourceBlock;
    private Vector direction;
    private double distanceTravelled;




    private States state;



    public EarthSpike(Player player) {


        super(player);

        Block block = getEarthSourceBlock(player, "EarthSpike",SOURCE_RANGE);

        if(block == null) {
            System.out.println("Block Null");
            return;

        }

        distanceTravelled = 0;
        sourceBlock = block;
        location = block.getLocation().add(.5, .5, .5);


        state = States.SOURCE_SELECTED;
        start();



    }
    public void onClick(){
        if (state == States.SOURCE_SELECTED) {
            ProjectKorra.log.info("Clicked");
            direction = GeneralMethods.getDirection(location, GeneralMethods.getTargetedLocation(player, SOURCE_RANGE * SOURCE_RANGE)).normalize().multiply(SPEED);
            direction.multiply(0.5);
            this.direction.setY(0);
            state = States.TRAVELLING;


        }


    }


    private void progressSourceSelected() {
        ProjectKorra.log.info("Source Selected");
       // playFocusEarthEffect(sourceBlock);

        if(sourceBlock.getLocation().distanceSquared(player.getLocation()) > SOURCE_RANGE * SOURCE_RANGE || !isEarthbendable(player, sourceBlock))  {
            ProjectKorra.log.info("Source not in range");
            remove();
        }

    }

    private void progressTravelling() {
        ProjectKorra.log.info("Progress Travelling");
        location.add(direction);


        distanceTravelled += SPEED;
        System.out.println(distanceTravelled);

        if (distanceTravelled >= DISTANCE_UNTIL_SPIKE) {
            state = States.SPIKE;
            distanceTravelled = 0;

        } else {
            ParticleEffect.SQUID_INK.display(location, 3, direction.getX(), direction.getY() + 3, direction.getZ());

        }


    }

    private void affectTargets() {
        List<Entity> targets  = GeneralMethods.getEntitiesAroundPoint(location, 1);
        for (Entity target : targets) {
            if (target.getUniqueId() == player.getUniqueId()) {
                continue;
            }

            DamageHandler.damageEntity(target, damage, this);

        }

    }

    private void progressSpike() {
        ProjectKorra.log.info("Spike Generated");
        System.out.println(location);
        new RaiseEarth(player, location, 3);
        affectTargets();
        removeWithCooldown();
    }






    @Override
    public void progress() {
        //STAGE 1 (Travelling state):
        //Vector in direction of ability, get the blocks along that vector and apply the particle effect. Use DISTANCE_UNTIL_SPIKE to determine how many blocks before the spike occurs


        //STAGE 2 (Spike state): Physically raise the spike, afterwards using an affecttarget() function to apply damage and force



        //Raise the pillar at the final stage of the attack

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
        perm = new Permission("bending.ability" + NAME);
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
        listener = new EarthSpikeListener();
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
}


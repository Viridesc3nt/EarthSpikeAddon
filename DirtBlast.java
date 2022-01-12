package me.justinjaques.earthspike;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
    private static final long DISTANCE_UNTIL_SPIKE = 4;
    private static final long SOURCE_RANGE = 3;
    private static final long SPIKE_WIDTH = 1;
    private static final long SPIKE_HEIGHT = 6;
    private static final double HITBOX = 1.5;
    private static final double SPEED = 3;


    private Location location;
    private Listener listener;
    private Permission perm;
    private Block sourceBlock;
    private Vector direction;
    private double distanceTravelled;
    private List<TempBlock> tempBlocks;




    private States state;



    public EarthSpike(Player player) {


        super(player);

        Block block = getEarthSourceBlock(player, "EarthSpike",SOURCE_RANGE);

        if(block == null) return;

        EarthSpike existing = getAbility(player, getClass());

        if(existing != null) {
            remove();
        }

        sourceBlock = block;
        location = block.getLocation().add(.5, .5, .5);
        tempBlocks = new LinkedList<>();
        state = States.SOURCE_SELECTED;
        start();



    }

    private List<Block> getBlocksAlongLine(Location from, Location to) {
        from = from.clone();
        Vector between = GeneralMethods.getDirection(from, to).normalize();
        List<Block> result = new ArrayList<>();
        while(from.distanceSquared(to) > DISTANCE_UNTIL_SPIKE) {
            if (!result.contains(from.getBlock())) {
                result.add(from.getBlock());
            }

            from.add(between);
        }
        result.add(to.getBlock());
        return result;

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
        //if distanceTravelled == BLOCKS_UNTIL_SPIKE

        List<Block> line = getBlocksAlongLine(sourceBlock.getLocation().add(.5, .5, .5), location);
        for(int i = tempBlocks.size(); i < line.size(); i++) {
            Block blockOnLine = line.get(i);
            tempBlocks.add(new TempBlock(blockOnLine, Material.GRASS_BLOCK));

        }
        state = States.SPIKE;

    }


    private void progressSpike() {
        new RaiseEarth(player, location, 6);
    }



    public void onClick(){
        if (state == States.SOURCE_SELECTED) {
            state = States.TRAVELLING;
            direction = GeneralMethods.getDirection(location, GeneralMethods.getTargetedLocation(player, DISTANCE_UNTIL_SPIKE).multiply(SPEED));
        }


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

            case TRAVELLING:
                progressTravelling();

            case SPIKE:
                progressSpike();

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

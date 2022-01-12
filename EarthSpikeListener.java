package me.justinjaques.earthspike;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import static org.bukkit.event.block.Action.LEFT_CLICK_AIR;
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;

public class EarthSpikeListener implements Listener {

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if(event.isSneaking()) {
            BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

            if(bPlayer.canBend(CoreAbility.getAbility(EarthSpike.class))) {
                new EarthSpike(player);

            }

        }

    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {

        if(event.getAction() != LEFT_CLICK_AIR || event.getAction() != LEFT_CLICK_BLOCK) {
            return;

        }

        Player player = event.getPlayer();
        EarthSpike earthSpike = CoreAbility.getAbility(player, EarthSpike.class);

        if(earthSpike != null) {
            earthSpike.onClick();
        }

    }

}

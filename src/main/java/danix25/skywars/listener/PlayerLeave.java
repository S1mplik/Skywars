package danix25.skywars.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import danix25.skywars.Skywars;
import danix25.skywars.object.Game;
import danix25.skywars.object.GamePlayer;

public class PlayerLeave implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.setQuitMessage(null);

        Game game = Skywars.getInstance().getGame(player);
        if (game != null && game.getGamePlayer(player) != null) {
            GamePlayer gamePlayer = game.getGamePlayer(player);

            if (gamePlayer.isTeamClass()) {
                if (gamePlayer.getTeam().isPlayer(player)) {
                    player.damage(player.getMaxHealth()); // Kill player to make game process this as a death
                }
            } else {
                if (gamePlayer.getPlayer() == player) {
                    player.damage(player.getMaxHealth()); // Kill player to make game process this as a death
                }
            }
        }
    }

}

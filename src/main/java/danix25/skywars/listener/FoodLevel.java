package danix25.skywars.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import danix25.skywars.Skywars;
import danix25.skywars.object.Game;
import danix25.skywars.object.GamePlayer;

public class FoodLevel implements Listener {

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            Game game = Skywars.getInstance().getGame(player);
            if (game != null && game.getGamePlayer(player) != null) {
                GamePlayer gamePlayer = game.getGamePlayer(player);

                if (!(game.isState(Game.GameState.ACTIVE) || game.isState(Game.GameState.DEATHMATCH))) {
                    if (gamePlayer.isTeamClass()) {
                        if (gamePlayer.getTeam().isPlayer(player)) {
                            event.setFoodLevel(25);
                            event.setCancelled(true);
                        }
                    } else {
                        if (gamePlayer.getPlayer() == player) {
                            event.setFoodLevel(25);
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

}

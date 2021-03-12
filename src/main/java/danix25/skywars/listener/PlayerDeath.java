package danix25.skywars.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import danix25.skywars.Skywars;
import danix25.skywars.object.Game;
import danix25.skywars.object.GamePlayer;

public class PlayerDeath implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Game game = Skywars.getInstance().getGame(player);
        player.teleport(Skywars.getInstance().getLobbyPoint());
        if (game != null && game.getGamePlayer(player) != null) {
            GamePlayer gamePlayer = game.getGamePlayer(player);

            if (gamePlayer.isTeamClass()) {
                if (gamePlayer.getTeam().isPlayer(player)) {
                    handle(event, game);
                }
            } else {
                if (gamePlayer.getPlayer() == player) {
                    handle(event, game);
                }
            }
        }
    }

    private void handle(PlayerDeathEvent event, Game game) {

        Player player = event.getEntity();

        if (!game.isState(Game.GameState.ACTIVE) && !game.isState(Game.GameState.DEATHMATCH)) {
            return;
        }

        event.setDeathMessage(null);
        game.activateSpectatorSettings(player);

        if (game.getPlayers().size() <= 1) {
            try {
                GamePlayer winner = game.getPlayers().get(0);
                if (winner.isTeamClass()) {
                    game.sendMessage("&7[&e&lSkywars&7] &a&l "); // Broadcast team win
                } else {
                    game.sendMessage("&7[&e&lSkywars&7]&a&l " + winner.getName() + " Vyhral hruDanix25"); // Indiv win
                    player.getAllowFlight();




                }

                game.setState(Game.GameState.ENDING);
            } catch (IndexOutOfBoundsException ignored) {}
        }
    }

}

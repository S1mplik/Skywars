package danix25.skywars.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import danix25.skywars.Skywars;
import danix25.skywars.object.Game;
import danix25.skywars.object.GamePlayer;
import danix25.skywars.utility.ChatUtil;

public class JoinCommand extends SubCommand {
    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (args.length == 0) {
                player.sendMessage(ChatUtil.format("&7[&e&lSkywars&7] &r &c/skywars join <jmeno_hry> "));
            } else {
                for (Game game : Skywars.getInstance().getGames()) {
                    for (GamePlayer gamePlayer : game.getPlayers()) {
                        if (gamePlayer.isTeamClass()) {
                            if (gamePlayer.getTeam().isPlayer(player)) {
                                player.sendMessage(ChatUtil.format("&7[&e&lSkywars&7] &r &cJsi ve hre."));
                                return;
                            }
                        } else {
                            if (gamePlayer.getPlayer() == player) {
                                player.sendMessage(ChatUtil.format("&7[&e&lSkywars&7] &r &cJsi ve hre."));
                                return;
                            }
                        }
                    }
                }

                Game game = Skywars.getInstance().getGame(args[0]);
                if (game == null) {
                    player.sendMessage(ChatUtil.format("&7[&e&lSkywars&7] &r &cTato hra neexistuje."));
                    return;
                }

                game.joinGame(new GamePlayer(player));
            }
        } else {
            commandSender.sendMessage(ChatUtil.format("&7[&e&lSkywars&7] &r &cNejsi hrac!"));
        }
    }
}

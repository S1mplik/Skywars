package danix25.skywars.commands;
import org.bukkit.command.CommandSender;
import danix25.skywars.Skywars;
import danix25.skywars.object.Game;
import danix25.skywars.utility.ChatUtil;

public class ListCommand extends SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        for (Game game : Skywars.getInstance().getGames()) {
            sender.sendMessage(ChatUtil.format("&7[&e&lSkywars&7] &r &f" + game.getDisplayName() + " - " + game.getPlayers().size() + " (zivi) hraci"));
        }
    }
}

package danix25.skywars.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import danix25.skywars.Skywars;
import danix25.skywars.object.Game;

public class GameCountdownTask extends BukkitRunnable {

    private int time = 20;
    private Game game;

    public GameCountdownTask(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        time -= 1;

        if (time == 0) {
            // Start
            cancel();

            new GameRunTask(game).runTaskTimer(Skywars.getInstance(), 0, 20);
//            Skywars.getInstance().getServer().getScheduler().runTask(Skywars.getInstance(), new GameRunTask(game)); // Deprecated
        } else {
            if (time == 15 || time == 10 || time == 5) {
                game.sendMessage("&7[&e&lServer&7] &cTeleportace za " + time + " sekund");
            }
        }
    }

}

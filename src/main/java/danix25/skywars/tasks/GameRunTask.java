package danix25.skywars.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import danix25.skywars.object.Game;

public class GameRunTask extends BukkitRunnable {

    private Game game;
    private int startIn = 10;

    public GameRunTask(Game game) {
        this.game = game;
        this.game.setState(Game.GameState.PREPARATION);
        this.game.assignSpawnPositions();
        this.game.sendMessage("&7[&e&lServer&7]&c Byl si teleportovan.");
        this.game.sendMessage("&7[&e&lServer&7]&c Hra zacne za: " + this.startIn + " sekund");
        this.game.setMovementFrozen(true);
    }

    @Override
    public void run() {
        if (startIn <= 1) {
            this.cancel();
            this.game.setState(Game.GameState.ACTIVE);
            this.game.sendMessage("&7[&e&lServer&7] &aHra zacala");
            this.game.setMovementFrozen(false);
        } else {
            startIn -= 1;
            this.game.sendMessage("&7[&e&lServer&7] &cHra zacne za " + startIn + " sekund" + (startIn == 1 ? "" : "s"));
        }
    }
}

package danix25.skywars.object;


import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import danix25.skywars.RollbackHandler;
import danix25.skywars.Skywars;
import danix25.skywars.Data.DataHandler;
import danix25.skywars.tasks.GameCountdownTask;

import java.util.*;

public class Game {

    // Basic config options
    private String displayName;
    private int maxPlayers;
    private int minPlayers;
    private World world;
    private List<Location> spawnPoints;
    private boolean isTeamGame;
    private Location lobbyPoint;
    private List<ItemStack> normalItems;
    private List<ItemStack> rareItems;

    // Active game information
    private List<GamePlayer> players;
    private Set<GamePlayer> spectators;
    private GameState gameState = GameState.LOBBY;
    private Map<GamePlayer, Location> gamePlayerToSpawnPoint = new HashMap<>();
    private Set<Chest> opened;
    private boolean movementFrozen = false;

    public Game(String gameName) {
        FileConfiguration fileConfiguration = DataHandler.getInstance().getGameInfo();

        this.displayName = fileConfiguration.getString("games." + gameName + ".displayName");
        this.maxPlayers = fileConfiguration.getInt("games." + gameName + ".maxPlayers");
        this.minPlayers = fileConfiguration.getInt("games." + gameName + ".minPlayers");

        RollbackHandler.getRollbackHandler().rollback(fileConfiguration.getString("games." + gameName + ".worldName"));

        this.world = Bukkit.createWorld(new WorldCreator(fileConfiguration.getString("games." + gameName + ".worldName") + "_active"));

        try {
            String[] values = fileConfiguration.getString("games." + gameName + ".lobbyPoint").split(","); // [X:0, Y:0, Z:0]
            double x = Double.parseDouble(values[0].split(":")[1]); // X:0 -> X, 0 -> 0
            double y = Double.parseDouble(values[1].split(":")[1]);
            double z = Double.parseDouble(values[2].split(":")[1]);
            lobbyPoint = new Location(world, x, y, z);
        } catch (Exception ex) {
            Skywars.getInstance().getLogger().severe("Chyba nenacetl se spawn " + fileConfiguration.getString("games." + gameName + ".lobbyPoint") + " for gameName: '" + gameName + "'. ExceptionType: " + ex);
        }

        this.spawnPoints = new ArrayList<>();

        for (String point : fileConfiguration.getStringList("games." + gameName + ".spawnPoints")) {
            // X:0,Y:0,Z:0
            try {
                String[] values = point.split(","); // [X:0, Y:0, Z:0]
                double x = Double.parseDouble(values[0].split(":")[1]); // X:0 -> X, 0 -> 0
                double y = Double.parseDouble(values[1].split(":")[1]);
                double z = Double.parseDouble(values[2].split(":")[1]);
                Location location = new Location(world, x, y, z);
                spawnPoints.add(location);
            } catch (Exception ex) {
                Skywars.getInstance().getLogger().severe("Chyba nenacetl se spawn " + point + " jmeno hry: '" + gameName + "'. ExceptionType: " + ex);
            }
        }

        this.opened = new HashSet<>();

        this.normalItems = new ArrayList<>();
        this.rareItems = new ArrayList<>();

        for (String item : fileConfiguration.getStringList("games." + gameName + ".normalItems")) {
            try {
                Material material = Material.valueOf(item);
                int count = 1;
                if (material == Material.ARROW) {
                    count = 5;
                } else if (material == Material.COBBLESTONE) {
                    count = 64;
                } else if (material == Material.GOLD_BLOCK) {
                    count = 6;
                }
                this.normalItems.add(new ItemStack(material, count));
            } catch (Exception ex) {
                Skywars.getInstance().getLogger().severe(gameName + " zkousim nacist normalni predmety: " + item);
            }
        }

        for (String item : fileConfiguration.getStringList("games." + gameName + ".rareItems")) {
            try {
                Material material = Material.valueOf(item);
                int count = 1;
                if (material == Material.ARROW) {
                    count = 15;
                } else if (material == Material.COBBLESTONE) {
                    count = 64;
                } else if (material == Material.GOLD_BLOCK) {
                    count = 6;
                }
                this.rareItems.add(new ItemStack(material, count));
            } catch (Exception ex) {
                Skywars.getInstance().getLogger().severe(gameName + " zkousim nacist vzacne predmety: " + item);
            }
        }

        this.isTeamGame = fileConfiguration.getBoolean("games." + gameName + ".isTeamGame");
        this.players = new ArrayList<>();
        this.spectators = new HashSet<>();
    }

    public boolean joinGame(GamePlayer gamePlayer) {
        if (gamePlayer.isTeamClass() && !isTeamGame) {
            return false;
        }

        if (isState(GameState.LOBBY) || isState(GameState.STARTING)) {
            if (getPlayers().size() == getMaxPlayers()) {
                gamePlayer.sendMessage("&7[&e&lServer&7] &cHra je plna!");
                return false;
            }

            getPlayers().add(gamePlayer);
            gamePlayer.teleport(isState(GameState.LOBBY) ? lobbyPoint : null);
            sendMessage("&7[&a+&7] " + gamePlayer.getName() + " &7(" + getPlayers().size() + "/&7" + getMaxPlayers() + ")");

            gamePlayer.getPlayer().getInventory().clear();
            gamePlayer.getPlayer().getInventory().setArmorContents(null);
            gamePlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
            gamePlayer.getPlayer().setHealth(gamePlayer.getPlayer().getMaxHealth());

            if (getPlayers().size() == getMinPlayers() && !isState(GameState.STARTING)) {
                setState(GameState.STARTING);
                sendMessage("&7[&e&lServer&7] &cHra zacne za 20 sekund");
                startCountdown();
            }

            Skywars.getInstance().setGame(gamePlayer.getPlayer(), this);
            return true;
        } else {
            activateSpectatorSettings(gamePlayer.getPlayer());
            Skywars.getInstance().setGame(gamePlayer.getPlayer(), this);
            return true;
        }
    }

    public void activateSpectatorSettings(Player player) {
        GamePlayer gamePlayer = getGamePlayer(player);

        player.setMaxHealth(20);
        player.setHealth(player.getMaxHealth());
        player.setGameMode(GameMode.SPECTATOR);

        if (gamePlayer != null) {
            switchToSpectator(gamePlayer);
        }
    }

    public void startCountdown() {
        new GameCountdownTask(this).runTaskTimer(Skywars.getInstance(), 0, 20);
    }

    public void assignSpawnPositions() {
        int id = 0;
        for (GamePlayer gamePlayer : getPlayers()) {
            try {
                gamePlayerToSpawnPoint.put(gamePlayer, spawnPoints.get(id));
                gamePlayer.teleport(spawnPoints.get(id));
                id += 1;
                gamePlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
                gamePlayer.getPlayer().setHealth(gamePlayer.getPlayer().getMaxHealth());
            } catch (IndexOutOfBoundsException ex) {
                Skywars.getInstance().getLogger().severe("Nenasel jsem spawn lokaci (Hra " + getDisplayName() + ")");
            }
        }
    }

    public boolean isState(GameState state) {
        return getGameState() == state;
    }

    public void setState(GameState gameState) {
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<GamePlayer> getPlayers() {
        return players;
    }

    public Set<GamePlayer> getSpectators() {
        return spectators;
    }

    public boolean isTeamGame() {
        return isTeamGame;
    }

    public GamePlayer getGamePlayer(Player player) {
        for (GamePlayer gamePlayer : getPlayers()) {
            if (gamePlayer.isTeamClass()) {
                // Handle
            } else {
                if (gamePlayer.getPlayer() == player) {
                    return gamePlayer;
                }
            }
        }

        for (GamePlayer gamePlayer : getSpectators()) {
            if (gamePlayer.isTeamClass()) {
                // Handle
            } else {
                if (gamePlayer.getPlayer() == player) {
                    return gamePlayer;
                }
            }
        }

        return null;
    }

    public void sendMessage(String message) {
        for (GamePlayer gamePlayer : getPlayers()) {
            gamePlayer.sendMessage(message);
        }
    }

    public void switchToSpectator(GamePlayer gamePlayer) {
        getPlayers().remove(gamePlayer);
        getSpectators().add(gamePlayer);
    }

    public Set<Chest> getOpened() {
        return opened;
    }

    public List<ItemStack> getRareItems() {
        return rareItems;
    }

    public List<ItemStack> getNormalItems() {
        return normalItems;
    }

    public void setMovementFrozen(boolean movementFrozen) {
        this.movementFrozen = movementFrozen;
    }

    public boolean isMovementFrozen() {
        return movementFrozen;
    }

    public World getWorld() {
        return world;
    }

    public enum GameState {
        LOBBY, STARTING, PREPARATION, ACTIVE, DEATHMATCH, ENDING
    }

}

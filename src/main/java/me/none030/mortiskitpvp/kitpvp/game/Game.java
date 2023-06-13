package me.none030.mortiskitpvp.kitpvp.game;

import me.none030.mortiskitpvp.kitpvp.arenas.Arena;
import me.none030.mortiskitpvp.kitpvp.kits.Kit;
import me.none030.mortiskitpvp.kitpvp.kits.KitManager;
import me.none030.mortiskitpvp.kitpvp.kits.KitMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class Game {

    private final Arena arena;
    private final World world;
    private final Set<GamePlayer> gamePlayers;
    private long time;
    private long endTime;
    private TeamType winner;
    private boolean started;
    private boolean ended;

    public Game(KitManager kitManager, Arena arena, Set<GamePlayer> gamePlayers) {
        this.arena = arena;
        this.world = arena.create();
        this.gamePlayers = gamePlayers;
        this.time = 0;
        this.endTime = 0;
        this.winner = TeamType.NONE;
        this.started = false;
        this.ended = false;
        teleport();
        openKitSelector(kitManager);
    }

    private void teleport() {
        arena.teleportRed(new ArrayList<>(getRedPlayers()), world);
        arena.teleportBlue(new ArrayList<>(getBluePlayers()), world);
    }

    private void openKitSelector(KitManager kitManager) {
        for (GamePlayer gamePlayer : gamePlayers) {
            Player player = gamePlayer.getPlayer();
            KitMenu menu = new KitMenu(kitManager, player);
            menu.open(player);
        }
    }

    private void closeKitSelector() {
        for (GamePlayer gamePlayer : gamePlayers) {
            Player player = gamePlayer.getPlayer();
            if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof KitMenu)) {
                continue;
            }
            player.closeInventory();
        }
    }

    public void giveKits(KitManager kitManager) {
       Set<GamePlayer> gamePlayers = new HashSet<>(getAliveRedGamePlayers());
        gamePlayers.addAll(getAliveBlueGamePlayers());
        for (GamePlayer gamePlayer : gamePlayers) {
            Player player = gamePlayer.getPlayer();
            Kit kit = kitManager.getKit(player);
            if (kit == null) {
                kit = kitManager.getRandomKit(player);
                if (kit == null) {
                    return;
                }
            }
            kit.give(player);
        }
    }

    public GamePlayer getGamePlayer(Player player) {
        for (GamePlayer gamePlayer : gamePlayers) {
            if (player.equals(gamePlayer.getPlayer())) {
                return gamePlayer;
            }
        }
        return null;
    }

    public Set<Player> getRedPlayers() {
        Set<Player> redPlayers = new HashSet<>();
        for (GamePlayer gamePlayer : getRedGamePlayers()) {
            redPlayers.add(gamePlayer.getPlayer());
        }
        return redPlayers;
    }

    public Set<Player> getBluePlayers() {
        Set<Player> bluePlayers = new HashSet<>();
        for (GamePlayer gamePlayer : getBlueGamePlayers()) {
            bluePlayers.add(gamePlayer.getPlayer());
        }
        return bluePlayers;
    }

    public Set<GamePlayer> getRedGamePlayers() {
        Set<GamePlayer> redGamePlayers = new HashSet<>();
        for (GamePlayer gamePlayer : gamePlayers) {
            if (gamePlayer.isTeam(TeamType.RED)) {
                redGamePlayers.add(gamePlayer);
            }
        }
        return redGamePlayers;
    }

    public Set<GamePlayer> getBlueGamePlayers() {
        Set<GamePlayer> blueGamePlayers = new HashSet<>();
        for (GamePlayer gamePlayer : gamePlayers) {
            if (gamePlayer.isTeam(TeamType.BLUE)) {
                blueGamePlayers.add(gamePlayer);
            }
        }
        return blueGamePlayers;
    }

    public Set<GamePlayer> getAliveRedGamePlayers() {
        Set<GamePlayer> redGamePlayers = new HashSet<>();
        for (GamePlayer gamePlayer : getRedGamePlayers()) {
            if (gamePlayer.isSpectating()) {
                continue;
            }
            redGamePlayers.add(gamePlayer);
        }
        return redGamePlayers;
    }

    public Set<GamePlayer> getAliveBlueGamePlayers() {
        Set<GamePlayer> blueGamePlayers = new HashSet<>();
        for (GamePlayer gamePlayer : getBlueGamePlayers()) {
            if (gamePlayer.isSpectating()) {
                continue;
            }
            blueGamePlayers.add(gamePlayer);
        }
        return blueGamePlayers;
    }

    public void checkGamePlayers() {
        for (GamePlayer gamePlayer : gamePlayers) {
            switch (gamePlayer.getTeam()) {
                case NONE:
                    if (!world.equals(gamePlayer.getPlayer().getWorld())) {
                        arena.teleportSpectator(gamePlayer.getPlayer(), world);
                    }
                    if (!gamePlayer.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                        gamePlayer.getPlayer().setGameMode(GameMode.SPECTATOR);
                    }
                case BLUE:
                    if (gamePlayer.isSpectating()) {
                        if (!world.equals(gamePlayer.getPlayer().getWorld())) {
                            arena.teleportSpectator(gamePlayer.getPlayer(), world);
                        }
                        if (!gamePlayer.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                            gamePlayer.getPlayer().setGameMode(GameMode.SPECTATOR);
                        }
                    }else {
                        if (!world.equals(gamePlayer.getPlayer().getWorld())) {
                            arena.teleportBlue(gamePlayer.getPlayer(), world);
                        }
                        if (!gamePlayer.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
                            gamePlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
                        }
                    }
                case RED:
                    if (gamePlayer.isSpectating()) {
                        if (!world.equals(gamePlayer.getPlayer().getWorld())) {
                            arena.teleportSpectator(gamePlayer.getPlayer(), world);
                        }
                        if (!gamePlayer.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                            gamePlayer.getPlayer().setGameMode(GameMode.SPECTATOR);
                        }
                    }else {
                        if (!world.equals(gamePlayer.getPlayer().getWorld())) {
                            arena.teleportRed(gamePlayer.getPlayer(), world);
                        }
                        if (!gamePlayer.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
                            gamePlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
                        }
                    }
            }
        }
    }

    public void showCountdown(GameManager gameManager) {
        long timeLeft = gameManager.getStartTime() - time;
        Component title = Component.text(gameManager.getMessage("COUNTDOWN_TITLE").replace("%time%", String.valueOf(timeLeft)));
        Component subTitle = Component.text(gameManager.getMessage("COUNTDOWN_SUBTITLE").replace("%time%", String.valueOf(timeLeft)));
        for (GamePlayer gamePlayer : gamePlayers) {
            gamePlayer.getPlayer().showTitle(Title.title(title, subTitle));
            gamePlayer.getPlayer().sendMessage(gameManager.getMessage("COUNTDOWN").replace("%time%", String.valueOf(timeLeft)));
        }
    }

    public void showResult(GameManager gameManager) {
        switch (winner) {
            case NONE:
                List<GamePlayer> gamePlayers = new ArrayList<>(getRedGamePlayers());
                gamePlayers.addAll(getBlueGamePlayers());
                for (GamePlayer gamePlayer : gamePlayers) {
                    Component title = Component.text(gameManager.getMessage("TIE_TITLE").replace("%team_name%", gamePlayer.getTeamName()));
                    Component subTitle = Component.text(gameManager.getMessage("TIE_SUBTITLE").replace("%team_name%", gamePlayer.getTeamName()));
                    gamePlayer.getPlayer().showTitle(Title.title(title, subTitle));
                }
            case RED:
                for (GamePlayer gamePlayer : getRedGamePlayers()) {
                    Component title = Component.text(gameManager.getMessage("WIN_TITLE").replace("%team_name%", gamePlayer.getTeamName()));
                    Component subTitle = Component.text(gameManager.getMessage("WIN_SUBTITLE").replace("%team_name%", gamePlayer.getTeamName()));
                    gamePlayer.getPlayer().showTitle(Title.title(title, subTitle));
                }
                for (GamePlayer gamePlayer : getBlueGamePlayers()) {
                    Component title = Component.text(gameManager.getMessage("LOSE_TITLE").replace("%team_name%", gamePlayer.getTeamName()));
                    Component subTitle = Component.text(gameManager.getMessage("LOSE_SUBTITLE").replace("%team_name%", gamePlayer.getTeamName()));
                    gamePlayer.getPlayer().showTitle(Title.title(title, subTitle));
                }
            case BLUE:
                for (GamePlayer gamePlayer : getBlueGamePlayers()) {
                    Component title = Component.text(gameManager.getMessage("WIN_TITLE").replace("%team_name%", gamePlayer.getTeamName()));
                    Component subTitle = Component.text(gameManager.getMessage("WIN_SUBTITLE").replace("%team_name%", gamePlayer.getTeamName()));
                    gamePlayer.getPlayer().showTitle(Title.title(title, subTitle));
                }
                for (GamePlayer gamePlayer : getRedGamePlayers()) {
                    Component title = Component.text(gameManager.getMessage("LOSE_TITLE").replace("%team_name%", gamePlayer.getTeamName()));
                    Component subTitle = Component.text(gameManager.getMessage("LOSE_SUBTITLE").replace("%team_name%", gamePlayer.getTeamName()));
                    gamePlayer.getPlayer().showTitle(Title.title(title, subTitle));
                }
        }
    }

    public void sendResultMessage(GameManager gameManager) {
        switch (winner) {
            case NONE: {
                List<GamePlayer> gamePlayers = new ArrayList<>(getRedGamePlayers());
                gamePlayers.addAll(getBlueGamePlayers());
                for (GamePlayer gamePlayer : gamePlayers) {
                    gamePlayer.getPlayer().sendMessage(gameManager.getMessage("TIE").replace("%team_name%", gamePlayer.getTeamName()));
                }
            }
            case RED: {
                for (GamePlayer gamePlayer : getRedGamePlayers()) {
                    gamePlayer.getPlayer().sendMessage(gameManager.getMessage("WIN").replace("%team_name%", gamePlayer.getTeamName()));
                }
                for (GamePlayer gamePlayer : getBlueGamePlayers()) {
                    gamePlayer.getPlayer().sendMessage(gameManager.getMessage("LOSE").replace("%team_name%", gamePlayer.getTeamName()));
                }
            }
            case BLUE: {
                for (GamePlayer gamePlayer : getBlueGamePlayers()) {
                    gamePlayer.getPlayer().sendMessage(gameManager.getMessage("WIN").replace("%team_name%", gamePlayer.getTeamName()));
                }
                for (GamePlayer gamePlayer : getRedGamePlayers()) {
                    gamePlayer.getPlayer().sendMessage(gameManager.getMessage("LOSE").replace("%team_name%", gamePlayer.getTeamName()));
                }
            }
        }
    }

    public void end(GameManager gameManager) {
        Iterator<GamePlayer> gamePlayerList = gamePlayers.iterator();
        while (gamePlayerList.hasNext()) {
            GamePlayer gamePlayer = gamePlayerList.next();
            gamePlayerList.remove();
            removePlayer(gameManager, gamePlayer);
        }
        arena.delete(world);
    }

    public void removePlayer(GameManager gameManager, GamePlayer gamePlayer) {
        gameManager.getGameByPlayer().remove(gamePlayer.getPlayer());
        gameManager.getBattlefieldManager().getBattlefield().teleport(gamePlayer.getPlayer());
    }

    public void check(GameManager gameManager) {
        Set<GamePlayer> redGamePlayers = getAliveRedGamePlayers();
        Set<GamePlayer> blueGamePlayers = getAliveBlueGamePlayers();
        if (redGamePlayers.size() == 0 && blueGamePlayers.size() == 0) {
            setEnded(true);
            setWinner(TeamType.NONE);
            sendResultMessage(gameManager);
            showResult(gameManager);
            return;
        }
        if (redGamePlayers.size() == 0) {
            setEnded(true);
            setWinner(TeamType.BLUE);
            sendResultMessage(gameManager);
            showResult(gameManager);
            return;
        }
        if (blueGamePlayers.size() == 0) {
            setEnded(true);
            setWinner(TeamType.RED);
            sendResultMessage(gameManager);
            showResult(gameManager);
            return;
        }
        if (time >= gameManager.getLength()) {
            if (redGamePlayers.size() == blueGamePlayers.size()) {
                setEnded(true);
                setWinner(TeamType.NONE);
                sendResultMessage(gameManager);
                showResult(gameManager);
                return;
            }
            if (redGamePlayers.size() < blueGamePlayers.size()) {
                setEnded(true);
                setWinner(TeamType.BLUE);
                sendResultMessage(gameManager);
                showResult(gameManager);
            }else {
                setEnded(true);
                setWinner(TeamType.RED);
                sendResultMessage(gameManager);
                showResult(gameManager);
            }
        }
    }

    public Arena getArena() {
        return arena;
    }

    public World getWorld() {
        return world;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public TeamType getWinner() {
        return winner;
    }

    public void setWinner(TeamType winner) {
        this.winner = winner;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
        closeKitSelector();
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }
}

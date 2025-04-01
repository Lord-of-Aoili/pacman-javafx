package pacman.model.level;

import org.json.simple.JSONObject;

import javafx.application.Platform;
import pacman.ConfigurationParseException;
import pacman.model.engine.observer.GameState;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.DynamicEntity;
import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.ghost.GhostManager;
import pacman.model.entity.dynamic.ghost.GhostMode;
import pacman.model.entity.dynamic.physics.PhysicsEngine;
import pacman.model.entity.dynamic.player.Controllable;
import pacman.model.entity.dynamic.player.Pacman;
import pacman.model.entity.staticentity.StaticEntity;
import pacman.model.entity.staticentity.collectable.Collectable;
import pacman.model.entity.staticentity.collectable.Pellet;
import pacman.model.level.iterator.GhostCollection;
import pacman.model.level.iterator.Iterator;
import pacman.model.level.observer.LevelStateObserver;
import pacman.model.maze.Maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Concrete implement of Pac-Man level
 */
public class LevelImpl implements Level {

    private static final int START_LEVEL_TIME = 100;
    private final Maze maze;
    private final List<LevelStateObserver> observers;
    private List<Renderable> renderables;
    private Controllable player;
    private List<Ghost> ghosts;
    private int tickCount;
    private Map<GhostMode, Integer> modeLengths;
    private int numLives;
    private int points;
    private GameState gameState;
    private List<Renderable> collectables;
    private GhostMode currentGhostMode;
    private GhostCollection ghostCollection;
    private int frightenedGhostCount = 0;
    private Map<Ghost, Integer> respawnDelays = new HashMap<>();

    public LevelImpl(JSONObject levelConfiguration,
                     Maze maze) {
        this.renderables = new ArrayList<>();
        this.maze = maze;
        this.tickCount = 0;
        this.observers = new ArrayList<>();
        this.modeLengths = new HashMap<>();
        this.gameState = GameState.READY;
        this.currentGhostMode = GhostMode.SCATTER;
        this.points = 0;

        initLevel(new LevelConfigurationReader(levelConfiguration));
    }

    private void initLevel(LevelConfigurationReader levelConfigurationReader) {
        // Fetch all renderables for the level
        this.renderables = maze.getRenderables();

        // Set up player
        if (!(maze.getControllable() instanceof Controllable)) {
            throw new ConfigurationParseException("Player entity is not controllable");
        }
        this.player = (Controllable) maze.getControllable();
        this.player.setSpeed(levelConfigurationReader.getPlayerSpeed());
        setNumLives(maze.getNumLives());

        // Set up ghosts
        this.ghosts = maze.getGhosts().stream()
                .map(element -> (Ghost) element)
                .collect(Collectors.toList());
        Map<GhostMode, Double> ghostSpeeds = levelConfigurationReader.getGhostSpeeds();
        for (Ghost ghost : this.ghosts) {
            GhostManager.getInstance().registerGhost(ghost.getGhostType(), ghost);
            player.registerObserver(ghost);
            ghost.setSpeeds(ghostSpeeds);
            ghost.setGhostMode(this.currentGhostMode);
        }
        this.modeLengths = levelConfigurationReader.getGhostModeLengths();

        this.ghostCollection = new GhostCollection(this.ghosts);

        // Set up collectables
        this.collectables = new ArrayList<>(maze.getPellets());

    }

    @Override
    public List<Renderable> getRenderables() {
        return this.renderables;
    }

    private List<DynamicEntity> getDynamicEntities() {
        return renderables.stream().filter(e -> e instanceof DynamicEntity).map(e -> (DynamicEntity) e).collect(
                Collectors.toList());
    }

    private List<StaticEntity> getStaticEntities() {
        return renderables.stream().filter(e -> e instanceof StaticEntity).map(e -> (StaticEntity) e).collect(
                Collectors.toList());
    }

    @Override
    public void tick() {
        if (this.gameState != GameState.IN_PROGRESS) {

            if (tickCount >= START_LEVEL_TIME) {
                setGameState(GameState.IN_PROGRESS);
                tickCount = 0;
            }

        } else {

            if (tickCount == modeLengths.get(currentGhostMode)) {
                // update ghost mode
                if (this.currentGhostMode == GhostMode.FRIGHTENED) {
                    this.currentGhostMode = GhostMode.SCATTER;
                } else {
                    this.currentGhostMode = GhostMode.getNextGhostMode(currentGhostMode);
                }
                Iterator<Ghost> ghostIterator = ghostCollection.createIterator();
                while (ghostIterator.hasNext()) {
                    Ghost ghost = ghostIterator.next();
                    ghost.setGhostMode(this.currentGhostMode);
                }

                tickCount = 0;
            }

            if (tickCount % Pacman.PACMAN_IMAGE_SWAP_TICK_COUNT == 0) {
                this.player.switchImage();
            }

            // Update the dynamic entities
            updateRespawnGhosts();

            List<DynamicEntity> dynamicEntities = getDynamicEntities();

            for (DynamicEntity dynamicEntity : dynamicEntities) {
                maze.updatePossibleDirections(dynamicEntity);
                dynamicEntity.update();
            }

            for (int i = 0; i < dynamicEntities.size(); ++i) {
                DynamicEntity dynamicEntityA = dynamicEntities.get(i);

                // handle collisions between dynamic entities
                for (int j = i + 1; j < dynamicEntities.size(); ++j) {
                    DynamicEntity dynamicEntityB = dynamicEntities.get(j);

                    if (dynamicEntityA.collidesWith(dynamicEntityB) ||
                            dynamicEntityB.collidesWith(dynamicEntityA)) {
                        if (isPlayer(dynamicEntityA)) {
                            Ghost ghost = (Ghost) dynamicEntityB;
                            if (ghost.getGhostMode() == GhostMode.FRIGHTENED) {
                                eatGhost(ghost);
                            } else {
                                dynamicEntityA.collideWith(this, dynamicEntityB);
                                dynamicEntityB.collideWith(this, dynamicEntityA);
                            }
                        } else if (isPlayer(dynamicEntityB)) {
                            Ghost ghost = (Ghost) dynamicEntityA;
                            if (ghost.getGhostMode() == GhostMode.FRIGHTENED) {
                                eatGhost(ghost);
                            } else {
                                dynamicEntityA.collideWith(this, dynamicEntityB);
                                dynamicEntityB.collideWith(this, dynamicEntityA);
                            }
                        }
                    }
                }

                // handle collisions between dynamic entities and static entities
                for (StaticEntity staticEntity : getStaticEntities()) {
                    if (dynamicEntityA.collidesWith(staticEntity)) {
                        if (isCollectable(staticEntity) && isPlayer(dynamicEntityA) && staticEntity instanceof Pellet) {
                            Pellet pellet = (Pellet) staticEntity;
                            consumePowerPellet(pellet);
                        }
                        dynamicEntityA.collideWith(this, staticEntity);
                        PhysicsEngine.resolveCollision(dynamicEntityA, staticEntity);   
                    }
                }
            }
        }

        tickCount++;
    }

    @Override
    public boolean isPlayer(Renderable renderable) {
        return renderable == this.player;
    }

    @Override
    public boolean isCollectable(Renderable renderable) {
        return maze.getPellets().contains(renderable) && ((Collectable) renderable).isCollectable();
    }

    @Override
    public void collect(Collectable collectable) {
        this.points += collectable.getPoints();
        notifyObserversWithScoreChange(collectable.getPoints());
        this.collectables.remove(collectable);
    }

    private void eatGhost(Ghost ghost) {
        frightenedGhostCount++;
        int pointsAwarded = (int) Math.pow(2, frightenedGhostCount - 1) * 200;
        this.points += pointsAwarded;
        notifyObserversWithScoreChange(pointsAwarded);

        renderables.remove(ghost);
        respawnDelays.put(ghost, 30);
    }

    private void updateRespawnGhosts() {
        List<Ghost> readyToRespawn = new ArrayList<>();
        respawnDelays.forEach((ghost, ticks) -> {
            if (ticks > 1) {
                respawnDelays.put(ghost, ticks - 1);
            } else {
                readyToRespawn.add(ghost);
            }
        });

        for (Ghost ghost : readyToRespawn) {
            ghost.reset();
            ghost.setGhostMode(GhostMode.SCATTER);
            renderables.add(ghost);
            respawnDelays.remove(ghost);
        }
    }
    
    private void consumePowerPellet(Pellet pellet) {
        if (pellet.isPowerPellet()) {
            currentGhostMode = GhostMode.FRIGHTENED;
            frightenedGhostCount = 0;
            Iterator<Ghost> ghostIterator = ghostCollection.createIterator();
            while (ghostIterator.hasNext()) {
                Ghost ghost = ghostIterator.next();
                ghost.setGhostMode(this.currentGhostMode);
            }
            tickCount = 0;
        }
    }

    @Override
    public void handleLoseLife() {
        if (gameState == GameState.IN_PROGRESS) {
            for (DynamicEntity dynamicEntity : getDynamicEntities()) {
                dynamicEntity.reset();
            }
            for (Ghost ghost : ghosts) {
                if (!renderables.contains(ghost)) {
                    renderables.add(ghost);
                }
                this.currentGhostMode = GhostMode.SCATTER;
                ghost.setGhostMode(currentGhostMode);
            }
            respawnDelays.clear();
            setNumLives(numLives - 1);
            setGameState(GameState.READY);
            tickCount = 0;
        }
    }

    @Override
    public void moveLeft() {
        player.left();
    }

    @Override
    public void moveRight() {
        player.right();
    }

    @Override
    public void moveUp() {
        player.up();
    }

    @Override
    public void moveDown() {
        player.down();
    }

    @Override
    public boolean isLevelFinished() {
        if (collectables.isEmpty()) {
            for (Ghost ghost : ghosts) {
                if (!renderables.contains(ghost)) {
                    renderables.add(ghost);
                }
                this.currentGhostMode = GhostMode.SCATTER;
                ghost.setGhostMode(currentGhostMode);
            }
            respawnDelays.clear();
        }
        return collectables.isEmpty();
    }

    @Override
    public void registerObserver(LevelStateObserver observer) {
        this.observers.add(observer);
        observer.updateNumLives(this.numLives);
        observer.updateGameState(this.gameState);
    }

    @Override
    public void removeObserver(LevelStateObserver observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObserversWithNumLives() {
        for (LevelStateObserver observer : observers) {
            observer.updateNumLives(this.numLives);
        }
    }

    private void setGameState(GameState gameState) {
        this.gameState = gameState;
        notifyObserversWithGameState();
    }

    @Override
    public void notifyObserversWithGameState() {
        for (LevelStateObserver observer : observers) {
            observer.updateGameState(gameState);
        }
    }

    /**
     * Notifies observer of change in player's score
     */
    public void notifyObserversWithScoreChange(int scoreChange) {
        for (LevelStateObserver observer : observers) {
            observer.updateScore(scoreChange);
        }
    }

    @Override
    public int getPoints() {
        return this.points;
    }

    @Override
    public int getNumLives() {
        return this.numLives;
    }

    private void setNumLives(int numLives) {
        this.numLives = numLives;
        notifyObserversWithNumLives();
    }

    @Override
    public void handleGameEnd() {
        this.renderables.removeAll(getDynamicEntities());
    }
}

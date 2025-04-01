package pacman.model.entity.dynamic.ghost;

import javafx.scene.image.Image;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.ghost.state.*;
import pacman.model.entity.dynamic.ghost.strategy.BlinkyChaseStrategy;
import pacman.model.entity.dynamic.ghost.strategy.ClydeChaseStrategy;
import pacman.model.entity.dynamic.ghost.strategy.FrightenedStrategy;
import pacman.model.entity.dynamic.ghost.strategy.GhostStrategy;
import pacman.model.entity.dynamic.ghost.strategy.InkyChaseStrategy;
import pacman.model.entity.dynamic.ghost.strategy.PinkyChaseStrategy;
import pacman.model.entity.dynamic.ghost.strategy.ScatterStrategy;
import pacman.model.entity.dynamic.physics.*;
import pacman.model.level.Level;
import pacman.model.maze.Maze;

import java.util.*;

/**
 * Concrete implementation of Ghost entity in Pac-Man Game
 */
public class GhostImpl implements Ghost {

    private static final int minimumDirectionCount = 8;
    private final Layer layer = Layer.FOREGROUND;
    private Image image;
    private Image normalImage;
    private Image frightenedImage = new Image("maze/ghosts/frightened.png");
    private final BoundingBox boundingBox;
    private final Vector2D startingPosition;
    private final Vector2D targetCorner;
    private KinematicState kinematicState;
    private GhostMode ghostMode;
    private Vector2D targetLocation;
    private KinematicState player;
    private Direction currentDirection;
    private Set<Direction> possibleDirections;
    private Map<GhostMode, Double> speeds;
    private int currentDirectionCount = 0;
    private GhostStrategy currentStrategy;
    private GhostState currentState;
    public char ghostType; 

    public GhostImpl(Image image, BoundingBox boundingBox, KinematicState kinematicState, GhostMode ghostMode, Vector2D targetCorner, char ghostType) {
        this.image = image;
        this.normalImage = image;
        this.boundingBox = boundingBox;
        this.kinematicState = kinematicState;
        this.startingPosition = kinematicState.getPosition();
        this.ghostMode = ghostMode;
        setState();
        setStrategy();
        this.possibleDirections = new HashSet<>();
        this.targetCorner = targetCorner;
        this.targetLocation = currentStrategy.getTargetLocation(this);
        this.currentDirection = null;
        this.ghostType = ghostType;
    }

    @Override
    public void setSpeeds(Map<GhostMode, Double> speeds) {
        this.speeds = speeds;
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public void update() {
        this.updateDirection();
        this.kinematicState.update();
        this.boundingBox.setTopLeft(this.kinematicState.getPosition());
    }

    private void updateDirection() {
        // Ghosts update their target location when they reach an intersection
        if (Maze.isAtIntersection(this.possibleDirections)) {
            this.targetLocation = getTargetLocation();
        }

        Direction newDirection = selectDirection(possibleDirections);

        // Ghosts have to continue in a direction for a minimum time before changing direction
        if (this.currentDirection != newDirection) {
            this.currentDirectionCount = 0;
        }
        this.currentDirection = newDirection;

        switch (currentDirection) {
            case LEFT -> this.kinematicState.left();
            case RIGHT -> this.kinematicState.right();
            case UP -> this.kinematicState.up();
            case DOWN -> this.kinematicState.down();
        }
    }

    public char getGhostType() {
        return this.ghostType;
    }

    @Override
    public void setFrightenedImage() {
        this.image = frightenedImage;
    }

    @Override
    public void setNormalImage() {
        this.image = normalImage;
    }

    @Override
    public Vector2D getTargetCorner() {
        return this.targetCorner;
    }

    @Override
    public Vector2D getPlayerPosition() {
        return this.player.getPosition();
    }

    @Override
    public Direction getPlayerDirection() {
        return this.player.getDirection();
    }
    
    @Override
    public KinematicState getKinematicState() {
        return this.kinematicState;
    }

    @Override
    public GhostMode getGhostMode() {
        return this.ghostMode;
    }

    private Vector2D getTargetLocation() {
        return currentStrategy.getTargetLocation(this);
    }

    private Direction selectDirection(Set<Direction> possibleDirections) {
        if (possibleDirections.isEmpty()) {
            return currentDirection;
        }

        // ghosts have to continue in a direction for a minimum time before changing direction
        if (currentDirection != null && currentDirectionCount < minimumDirectionCount) {
            currentDirectionCount++;
            return currentDirection;
        }

        Map<Direction, Double> distances = new HashMap<>();

        for (Direction direction : possibleDirections) {
            // ghosts never choose to reverse travel
            if (currentDirection == null || direction != currentDirection.opposite()) {
                distances.put(direction, Vector2D.calculateEuclideanDistance(this.kinematicState.getPotentialPosition(direction), this.targetLocation));
            }
        }

        // only go the opposite way if trapped
        if (distances.isEmpty()) {
            return currentDirection.opposite();
        }

        // select the direction that will reach the target location fastest
        return Collections.min(distances.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private void setState() {
        switch (ghostMode) {
            case CHASE:
                break;
            case SCATTER:
                this.currentState = new NormalState();
                break;
            case FRIGHTENED:
                this.currentState = new FrightenedState();
                break;
            default:
                this.currentState = new NormalState();
                break;
        }
        currentState.enter(this);
    }
    
    private void setStrategy() {
        switch (ghostMode) {
            case CHASE:
                if (this.ghostType == 'b') {
                    this.currentStrategy = new BlinkyChaseStrategy();
                } else if (this.ghostType == 's') {
                    this.currentStrategy = new PinkyChaseStrategy();
                } else if (this.ghostType == 'i') {
                    this.currentStrategy = new InkyChaseStrategy();
                } else if (this.ghostType == 'c') {
                    this.currentStrategy = new ClydeChaseStrategy();
                }
                break;
            case SCATTER:
                this.currentStrategy = new ScatterStrategy();
                break;
            case FRIGHTENED:
                this.currentStrategy = new FrightenedStrategy();
                break;
            default:
                this.currentStrategy = new ScatterStrategy();
                break;
        }
    }
    
    @Override
    public void setGhostMode(GhostMode ghostMode) {
        this.ghostMode = ghostMode;
        setState();
        setStrategy();
        this.kinematicState.setSpeed(speeds.get(ghostMode));
        // ensure direction is switched
        this.currentDirectionCount = minimumDirectionCount;
    }

    @Override
    public boolean collidesWith(Renderable renderable) {
        return boundingBox.collidesWith(kinematicState.getSpeed(), kinematicState.getDirection(), renderable.getBoundingBox());
    }

    @Override
    public void collideWith(Level level, Renderable renderable) {
        if (level.isPlayer(renderable)) {
            level.handleLoseLife();
        }
    }

    @Override
    public void update(KinematicState player) {
        this.player = player;
    }

    @Override
    public Vector2D getPositionBeforeLastUpdate() {
        return this.kinematicState.getPreviousPosition();
    }

    @Override
    public double getHeight() {
        return this.boundingBox.getHeight();
    }

    @Override
    public double getWidth() {
        return this.boundingBox.getWidth();
    }

    @Override
    public Vector2D getPosition() {
        return this.kinematicState.getPosition();
    }

    @Override
    public void setPosition(Vector2D position) {
        this.kinematicState.setPosition(position);
    }

    @Override
    public Layer getLayer() {
        return this.layer;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public void reset() {
        // return ghost to starting position
        this.kinematicState = new KinematicStateImpl.KinematicStateBuilder()
                .setPosition(startingPosition)
                .build();
        this.boundingBox.setTopLeft(startingPosition);
        this.ghostMode = GhostMode.SCATTER;
        this.currentDirectionCount = minimumDirectionCount;
    }

    @Override
    public void setPossibleDirections(Set<Direction> possibleDirections) {
        this.possibleDirections = possibleDirections;
    }

    @Override
    public Direction getDirection() {
        return this.kinematicState.getDirection();
    }

    @Override
    public Vector2D getCenter() {
        return new Vector2D(boundingBox.getMiddleX(), boundingBox.getMiddleY());
    }
}
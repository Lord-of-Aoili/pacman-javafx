package pacman.model.entity.dynamic.ghost;

import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.DynamicEntity;
import pacman.model.entity.dynamic.physics.Direction;
import pacman.model.entity.dynamic.physics.KinematicState;
import pacman.model.entity.dynamic.physics.Vector2D;
import pacman.model.entity.dynamic.player.observer.PlayerPositionObserver;

import java.util.Map;

/**
 * Represents Ghost entity in Pac-Man Game
 */
public interface Ghost extends DynamicEntity, PlayerPositionObserver {

    /***
     * Sets the speeds of the Ghost for each GhostMode
     * @param speeds speeds of the Ghost for each GhostMode
     */
    void setSpeeds(Map<GhostMode, Double> speeds);

    /**
     * Sets the mode of the Ghost used to calculate target position
     *
     * @param ghostMode mode of the Ghost
     */
    void setGhostMode(GhostMode ghostMode);

    /**
     * Sets the Ghost image to frightened
     */
    void setFrightenedImage();

    /**
     * Sets Ghost image to normal state
     */
    void setNormalImage();

    /**
     * Sets Ghost image to normal state
     */
    Vector2D getTargetCorner();

    /**
     * Gets pacman's position
     */
    Vector2D getPlayerPosition();

    /**
     * Gets pacman's direction
     */
    Direction getPlayerDirection();

    /**
     * Gets own Kinematic State
     */
    KinematicState getKinematicState();

    /**
     * Gets the type of ghost, b, s, i, c
     */
    char getGhostType();

    /**
     * Sets Ghost image to normal state
     */
    GhostMode getGhostMode();
}

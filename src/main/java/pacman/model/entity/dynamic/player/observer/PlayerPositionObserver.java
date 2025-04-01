package pacman.model.entity.dynamic.player.observer;

import pacman.model.entity.dynamic.physics.KinematicState;

/***
 * Observer for PlayerPositionObserver
 */
public interface PlayerPositionObserver {

    /**
     * Updates observer with the new position of the player
     *
     * @param position the player's position
     */
    void update(KinematicState position);
}

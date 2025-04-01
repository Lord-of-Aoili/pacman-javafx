package pacman.model.entity.dynamic.ghost.strategy;

import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.physics.Vector2D;

public class ClydeChaseStrategy implements GhostStrategy{

    @Override
    public Vector2D getTargetLocation(Ghost ghost) {
            double distanceThreshold = 16 * 8;
            Vector2D position = ghost.getPlayerPosition();
            Vector2D bottomLeftCorner = ghost.getTargetCorner();

            double distanceToPacMan = Vector2D.calculateEuclideanDistance(ghost.getKinematicState().getPosition(), position);

            if (distanceToPacMan > distanceThreshold) {
                return position;  // Target is Pac-Man
            } else {
                return bottomLeftCorner;  // Target is the bottom-left corner
            }
        }
}

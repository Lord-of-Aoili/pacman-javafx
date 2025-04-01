package pacman.model.entity.dynamic.ghost.strategy;

import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.physics.Vector2D;

public class PinkyChaseStrategy implements GhostStrategy {
    @Override
    public Vector2D getTargetLocation(Ghost ghost) {
        int spaceAhead = 16 * 4; // Chase 4 spaces ahead
        Vector2D position = ghost.getPlayerPosition();
        switch (ghost.getPlayerDirection()) {
            case LEFT:
                return new Vector2D(position.getX() - spaceAhead, position.getY());
            case RIGHT:
                return new Vector2D(position.getX() + spaceAhead, position.getY());
            case UP:
                return new Vector2D(position.getX(), position.getY() - spaceAhead);
            case DOWN:
                return new Vector2D(position.getX(), position.getY() + spaceAhead);
            default:
                return position;  // Default to current position if direction is unknown
        }
    }    
}

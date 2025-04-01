package pacman.model.entity.dynamic.ghost.strategy;

import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.ghost.GhostManager;
import pacman.model.entity.dynamic.physics.Vector2D;

public class InkyChaseStrategy implements GhostStrategy {

    @Override
    public Vector2D getTargetLocation(Ghost ghost) {

        Vector2D blinkyPosition = GhostManager.getInstance().getGhost('b').getPosition();
        Vector2D twoSpacesAheadOfPacman = getTwoSpacesAhead(ghost); // Calculate two spaces ahead of Pac-Man

        double deltaX = twoSpacesAheadOfPacman.getX() - blinkyPosition.getX();
        double deltaY = twoSpacesAheadOfPacman.getY() - blinkyPosition.getY();

        Vector2D doubledVector = new Vector2D(deltaX * 2, deltaY * 2);

        // Calculate Inky's target by adding the doubled vector to Blinky's position
        return new Vector2D(blinkyPosition.getX() + doubledVector.getX(), blinkyPosition.getY() + doubledVector.getY());
    }

    private Vector2D getTwoSpacesAhead(Ghost ghost) {
        int spaceAhead = 16 * 2;
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

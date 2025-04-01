package pacman.model.entity.dynamic.ghost.strategy;

import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.physics.Vector2D;

public class BlinkyChaseStrategy implements GhostStrategy{

    @Override
    public Vector2D getTargetLocation(Ghost ghost) {
        return ghost.getPlayerPosition(); // Chase Pac-Man
    }
}

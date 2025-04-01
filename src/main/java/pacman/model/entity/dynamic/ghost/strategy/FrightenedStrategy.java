package pacman.model.entity.dynamic.ghost.strategy;

import pacman.model.entity.dynamic.ghost.Ghost;
import pacman.model.entity.dynamic.physics.Vector2D;

import java.util.*;

public class FrightenedStrategy implements GhostStrategy{
    private static final Random random = new Random();
    List<Vector2D> targetCorners = Arrays.asList(
            new Vector2D(0, 16 * 3),
            new Vector2D(448, 16 * 3),
            new Vector2D(0, 16 * 34),
            new Vector2D(448, 16 * 34)
    );

    @Override
    public Vector2D getTargetLocation(Ghost ghost) {
        int index = random.nextInt(targetCorners.size());
        return targetCorners.get(index);
    }
}
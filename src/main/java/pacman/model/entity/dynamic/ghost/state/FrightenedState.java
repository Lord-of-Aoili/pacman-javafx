package pacman.model.entity.dynamic.ghost.state;

import pacman.model.entity.dynamic.ghost.Ghost;

public class FrightenedState implements GhostState {
    @Override
    public void update(Ghost ghost) {
    }

    @Override
    public void enter(Ghost ghost) {
        ghost.setFrightenedImage();
    }

    @Override
    public void exit(Ghost ghost) {
    }
}
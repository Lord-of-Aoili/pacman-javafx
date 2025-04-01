package pacman.model.entity.dynamic.ghost.state;

import pacman.model.entity.dynamic.ghost.Ghost;

public class NormalState implements GhostState {

    @Override
    public void update(Ghost ghost) {
        // ghost.setGhostMode(GhostMode.SCATTER);
    }

    @Override
    public void enter(Ghost ghost) {
        ghost.setNormalImage();
    }

    @Override
    public void exit(Ghost ghost) {
    }
}
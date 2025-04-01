package pacman.model.entity.dynamic.ghost.state;

import pacman.model.entity.dynamic.ghost.Ghost;

public interface GhostState {
    void enter(Ghost ghost);
    void update(Ghost ghost);
    void exit(Ghost ghost);
}
package pacman.model.entity.dynamic.ghost;

import java.util.HashMap;
import java.util.Map;

public class GhostManager {
    private static GhostManager instance;
    private Map<Character, Ghost> ghosts;

    private GhostManager() {
        ghosts = new HashMap<>();
    }

    public static GhostManager getInstance() {
        if (instance == null) {
            synchronized (GhostManager.class) {
                if (instance == null) {
                    instance = new GhostManager();
                }
            }
        }
        return instance;
    }

    public void registerGhost(char name, Ghost ghost) {
        ghosts.put(name, ghost);
    }

    public Ghost getGhost(char name) {
        return ghosts.get(name);
    }

    public void removeGhost(char name) {
        ghosts.remove(name);
    }
}

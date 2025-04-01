package pacman.model.level.iterator;

import java.util.*;

import pacman.model.entity.dynamic.ghost.Ghost;

public class GhostCollection implements Aggregate {
    private List<Ghost> ghosts;

    public GhostCollection(List<Ghost> ghosts) {
        this.ghosts = new ArrayList<>(ghosts);
    }

    @Override
    public Iterator<Ghost> createIterator() {
        return new GhostIterator();
    }
    
    private class GhostIterator implements Iterator<Ghost> {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return currentIndex < ghosts.size();
        }

        @Override
        public Ghost next() {
            return hasNext() ? ghosts.get(currentIndex++) : null;
        }

        @Override
        public void remove() {
            if (currentIndex <= ghosts.size() && currentIndex > 0) {
                ghosts.remove(--currentIndex);
            }
        }
    }
}

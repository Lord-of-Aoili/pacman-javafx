package pacman.model.level.iterator;

public interface Iterator<T> {
    boolean hasNext();
    T next();
    void remove();
}
package pacman.model.factories;

import javafx.scene.image.Image;
import pacman.ConfigurationParseException;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.ghost.GhostImpl;
import pacman.model.entity.dynamic.ghost.GhostMode;
import pacman.model.entity.dynamic.physics.*;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Concrete renderable factory for Ghost objects
 */
public class GhostFactory implements RenderableFactory {

    private static final int RIGHT_X_POSITION_OF_MAP = 448;
    private static final int TOP_Y_POSITION_OF_MAP = 16 * 3;
    private static final int BOTTOM_Y_POSITION_OF_MAP = 16 * 34;
    private final char ghostType;
    private Vector2D targetCorner;
    List<Vector2D> targetCorners = Arrays.asList(
            new Vector2D(0, TOP_Y_POSITION_OF_MAP),
            new Vector2D(RIGHT_X_POSITION_OF_MAP, TOP_Y_POSITION_OF_MAP),
            new Vector2D(0, BOTTOM_Y_POSITION_OF_MAP),
            new Vector2D(RIGHT_X_POSITION_OF_MAP, BOTTOM_Y_POSITION_OF_MAP)
    );
    private Image GHOST_IMAGE;
    private static final Map<Character, Image> IMAGES = new HashMap<>();

    static {
        IMAGES.put(RenderableType.BLINKY, new Image("maze/ghosts/blinky.png"));
        IMAGES.put(RenderableType.INKY, new Image("maze/ghosts/inky.png"));
        IMAGES.put(RenderableType.CLYDE, new Image("maze/ghosts/clyde.png"));
        IMAGES.put(RenderableType.PINKY, new Image("maze/ghosts/pinky.png"));
    }

    public GhostFactory(char renderableType) {
        this.GHOST_IMAGE = IMAGES.get(renderableType);
        this.ghostType = renderableType;
        switch (renderableType) {
            case 'b' -> this.targetCorner = targetCorners.get(1); // Blinky targets top-right corner
            case 's' -> this.targetCorner = targetCorners.get(0); // Pinky targets top-left corner
            case 'i' -> this.targetCorner = targetCorners.get(3); // Inky targets bottom-right corner
            case 'c' -> this.targetCorner = targetCorners.get(2); // Clyde targets bottom-left corner
        }
    }

    @Override
    public Renderable createRenderable(
            Vector2D position
    ) {
        try {
            position = position.add(new Vector2D(4, -4));

            BoundingBox boundingBox = new BoundingBoxImpl(
                    position,
                    GHOST_IMAGE.getHeight(),
                    GHOST_IMAGE.getWidth()
            );

            KinematicState kinematicState = new KinematicStateImpl.KinematicStateBuilder()
                    .setPosition(position)
                    .build();

            return new GhostImpl(
                    GHOST_IMAGE,
                    boundingBox,
                    kinematicState,
                    GhostMode.SCATTER,
                    targetCorner, 
                    ghostType);
        } catch (Exception e) {
            throw new ConfigurationParseException(
                    String.format("Invalid ghost configuration | %s ", e));
        }
    }


}

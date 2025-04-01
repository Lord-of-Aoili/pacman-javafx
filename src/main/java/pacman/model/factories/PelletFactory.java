package pacman.model.factories;

import javafx.scene.image.Image;
import pacman.ConfigurationParseException;
import pacman.model.entity.Renderable;
import pacman.model.entity.dynamic.physics.BoundingBox;
import pacman.model.entity.dynamic.physics.BoundingBoxImpl;
import pacman.model.entity.dynamic.physics.Vector2D;
import pacman.model.entity.staticentity.collectable.Pellet;

/**
 * Concrete renderable factory for Pellet objects
 */
public class PelletFactory implements RenderableFactory {
    private static final Image PELLET_IMAGE = new Image("maze/pellet.png");
    private static final int NUM_POINTS = 10;
    private static final int POWER_POINTS = 50;
    private final Renderable.Layer layer = Renderable.Layer.BACKGROUND;
    private boolean isPowerPellet;

    public PelletFactory(char renderableType) {
        if (renderableType == RenderableType.POWER_PELLET) {
            this.isPowerPellet = true;
        }
    }

    @Override
    public Renderable createRenderable(
            Vector2D position
    ) {
        try {
            double sizeMultiplier = isPowerPellet ? 2.0 : 1.0;
            int points = isPowerPellet ? POWER_POINTS : NUM_POINTS;
            Vector2D offset = isPowerPellet ? new Vector2D(-8, -8) : new Vector2D(0, 0);

            Vector2D adjustedPosition = position.add(offset);

            BoundingBox boundingBox = new BoundingBoxImpl(
                adjustedPosition,
                PELLET_IMAGE.getHeight() * sizeMultiplier,
                PELLET_IMAGE.getWidth() * sizeMultiplier
            );

            return new Pellet(
                    boundingBox,
                    layer,
                    PELLET_IMAGE,
                    points
            );

        } catch (Exception e) {
            throw new ConfigurationParseException(
                    String.format("Invalid pellet configuration | %s", e));
        }
    }
}

package core;

import tileengine.*;

public class WorldRenderer {
    private TERenderer ter;

    public WorldRenderer(int width, int height) {
        ter = new TERenderer();
        ter.initialize(width, height);
    }

    public void drawWorld(TETile[][] tiles) {
        ter.renderFrame(tiles);
    }

}

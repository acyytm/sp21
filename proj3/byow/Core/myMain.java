package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

public class myMain {

    public static void main(String[] args) {
        Engine e = new Engine();
        TERenderer renderer = new TERenderer();
        String command = "n5197880843569031643s";
        TETile[][] world = e.interactWithInputString(command);
        renderer.initialize(80, 30);
        renderer.renderFrame(world);
    }
}

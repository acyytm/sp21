package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Random;

public class World {


    /** Width of the canvas. */
    private static final int WIDTH = 80;

    /** Height of the canvas. */
    private static final int HEIGHT = 30;

    private TETile[][] world;

    private Node[][] nodes;

    private TERenderer renderer;

    private final long SEED;

    private final Random RANDOM;

    /** A list of  point positions to represent rooms, the point is a random point in rooms. */
    private ArrayList<Node> rooms;

    private class Node {
        /** Type of node. */
        private TETile tile;

        private int x;
        private int y;

        public Node(TETile tile, int x, int y) {
            this.tile = tile;
            this.x = x;
            this.y = y;
        }

        public void changeTo(TETile newTile) {
            this.tile = newTile;
        }

        public void toTile() {
            world[x][y] = tile;

        }

        public boolean isWall() {
            return tile.description().equals(Tileset.WALL.description());
        }

        public boolean isFloor() {
            return tile.description().equals(Tileset.FLOOR.description());
        }

        public boolean isNothing() {
            return tile.description().equals(Tileset.NOTHING.description());
        }
    }

    /**
     * Generate a world with every tile is nothing
     */
    public World(int seed) {
        renderer = new TERenderer();
        renderer.initialize(WIDTH, HEIGHT);
        world = new TETile[WIDTH][HEIGHT];
        nodes = new Node[WIDTH][HEIGHT];
        rooms = new ArrayList<>();
        SEED = seed;
        RANDOM = new Random(SEED);

        for(int i = 0; i < WIDTH; i ++) {
            for(int j = 0; j < HEIGHT; j ++) {
                nodes[i][j] = new Node(Tileset.NOTHING, i, j);
            }
        }

        generateWorld();

        for(int i = 0; i < WIDTH; i ++) {
            for(int j = 0; j < HEIGHT; j ++) {
                nodes[i][j].toTile();
            }
        }
    }

    /**
     * Generate a world with random rooms and the number of rooms is from 12 to 18
     */
    public void generateWorld() {
        int MIN = 10;
        int MAX = 20;

        int roomsNum = RandomUtils.uniform(RANDOM, MIN, MAX);

        for(int i = 0; i < roomsNum;) {
            int randomX = RandomUtils.uniform(RANDOM, WIDTH);
            int randomY = RandomUtils.uniform(RANDOM, HEIGHT);
            if(randomY >= HEIGHT - 2 || randomX >= WIDTH - 2)
                continue;
            generateRoom(randomX, randomY);
            i++;
        }

        generateHall();
        generateDoor();
    }

    /** Genarate room with given x and y. */
    private boolean generateRoom(int x, int y) {
        int MIN = 3;
        int MAX = 6;

        int randomWidth = RandomUtils.uniform(RANDOM, MIN, MAX);
        int randomHeight = RandomUtils.uniform(RANDOM, MIN, MAX);

        if(hasRoom(x, y, x + randomWidth, y + randomHeight)) {
            return false;
        }

        // generate the room
        for(int i = x; i <= x + randomWidth; i++) {
            for(int j = y; j <= y + randomHeight; j ++) {
                if(validatePos(i, j) && isEdge(i, j, x, y, x + randomWidth, y + randomHeight)) {
                    nodes[i][j].changeTo(Tileset.WALL);
                }
                else if(validatePos(i, j)) {
                    nodes[i][j].changeTo(Tileset.FLOOR);
                }
            }
        }

        int posX = x + 1;
        int posY = y + 1;

        Node represent = new Node(nodes[posX][posY].tile, posX, posY);

        rooms.add(represent);
        return true;
    }

    /** Generate halls to connect rooms. */
    private void generateHall() {
        for(int i = 0; i < rooms.size() - 1; i++) {
            Node start = rooms.get(i);
            Node end = rooms.get(i + 1);

            if(start.x - end.x == 0) {
                generateHallWithNegSlope(start, end);
            }
            else if(getSlope(start.x, start.y, end.x, end.y) < 0) {
                generateHallWithNegSlope(start, end);
            }else {
                generateHallWithPosSlope(start, end);
            }

        }
    }

    private double getSlope(int x1, int y1, int x2, int y2) {
        if(x1 == x2) {
            throw new RuntimeException();
        }
        return 1.0 * (y1 - y2) / (x1 - x2);
    }

    private void generateHallWithNegSlope(Node start, Node end) {
        generateL(start.x - 1, start.y, end.x, end.y - 1, Tileset.WALL);
        generateL(start.x, start.y, end.x, end.y, Tileset.FLOOR);
        generateL(start.x + 1, start.y, end.x, end.y + 1, Tileset.WALL);
    }

    private void generateHallWithPosSlope(Node start, Node end) {
        generateL(start.x - 1, start.y, end.x, end.y + 1, Tileset.WALL);
        generateL(start.x, start.y, end.x, end.y, Tileset.FLOOR);
        generateL(start.x + 1, start.y, end.x, end.y - 1, Tileset.WALL);
    }

    /** Generate "L" line. */
    private void generateL(int startX, int startY, int endX, int endY, TETile tile) {
        boolean changed = false;
        if(startY > endY) {
            int temp = startY;
            startY = endY;
            endY = temp;
            changed  = true;
        }

        for(int i = startY; i <= endY; i++) {
            if(nodes[startX][i].isNothing() || tile.description().equals(Tileset.FLOOR.description())) {
                nodes[startX][i].changeTo(tile);
            }
        }

        if(changed) {
            endY = startY;
        }

        if(startX > endX) {
            int temp = startX;
            startX = endX;
            endX = temp;
        }

        for(int i = startX; i <= endX; i++) {
            if(nodes[i][endY].isNothing() || tile.description().equals(Tileset.FLOOR.description())) {
                nodes[i][endY].changeTo(tile);
            }
        }

    }

    private void generateDoor() {
        int door = RandomUtils.uniform(RANDOM, 0, rooms.size());
        int x = rooms.get(door).x;
        int y = rooms.get(door).y;
        nodes[x][y].changeTo(Tileset.LOCKED_DOOR);
    }

    public TETile[][] getWorld() {
        return world;
    }

    /**
     * Get if room has conflict with another room
     * @param x x position of current room
     * @param y y position of current room
     * @param width width of current room
     * @param height height of current room
     * @return true if there is no room conflict with current room, false if there has been a room in that position
     */
    private boolean hasRoom(int x, int y, int width, int height) {
        for(int i = x; i <= x + width; i++) {
            for(int j = y; j <= y + height; j ++) {
                if(validatePos(i, j) && !nodes[i][j].isNothing()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Judge if a tile is edge tile. Contains two condition. */
    private boolean isEdge(int x, int y, int x1, int y1, int x2, int y2) {
        return x == WIDTH - 1 || y == HEIGHT - 1 || x == x1 || x == x2 || y == y1 || y == y2;
    }

    /** Return true if x, y in canvas. */
    private boolean validatePos(int x, int y) {
        return x < WIDTH && y < HEIGHT;
    }
}

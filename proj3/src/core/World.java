package core;
import edu.princeton.cs.algs4.StdDraw;

import tileengine.TETile;

import edu.princeton.cs.algs4.QuickUnionUF;

import java.awt.*;
import java.util.*;
import java.util.List;


public class World {

    // build your own world!
    // goal: generates tile worlds for autograderbuddy and
    // the GUI taking in user input for main

    Random random;
    Long seed; // using long bc int is too restrictive
    private TETile[][] tiles; // creating grid of tiles
    static WorldRenderer wr;
    int size;
    int maxX;
    int maxY;
    int numRooms;
    int bounds; // used for random x and y generation
    boolean gameIsOver;
    boolean loadedGame;
    QuickUnionUF uf;
    static final int RAND1 = 11;
    static final int RAND2 = 20;
    static final int TIME = 3000;
    static final int MAX_ATTEMPTS = 1000;

    private static TETile FLOOR = new TETile('.', Color.LIGHT_GRAY, Color.DARK_GRAY, "floor", 1);
    public static final TETile WALL = new TETile('^', Color.BLUE, Color.MAGENTA, "wall", 2);
    public static final TETile NOTHING = new TETile('$', Color.PINK, Color.lightGray, "nothing", 3);
    public static final TETile AVATAR = new TETile('@', Color.WHITE, Color.DARK_GRAY, "avatar", 4);
    public static final TETile BOMB = new TETile('0', Color.WHITE, Color.BLACK, "BOMB", 5);
    public  static final TETile GOLD = new TETile('*', Color.YELLOW, Color.DARK_GRAY, "GOLD", 6);

    Avatar avatar;
    Avatar2 avatar2;
    List<Room> listOfRooms = new ArrayList<>();


    public World(int width, int height, Long seed, boolean loadedGame) {
        gameIsOver = false;
        this.seed = seed;
        random = new Random(seed);
        size = width * height;
        tiles = new TETile[width][height];
        wr = new WorldRenderer(width, height);
        this.loadedGame = loadedGame;

        // Initialize grid with NOTHING tiles.
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = NOTHING;
            }
        }

        maxX = width;
        maxY = height;


        numRooms = random.nextInt(5) + RAND1;
        bounds = height / 4; // 1/4 height of world

        generateRooms();
        connectedRooms();
        displayWorld();

        if (!loadedGame) {
            avatarCreate();
        }


        int numBombs = random.nextInt(5) + RAND2;
        placeBombs(numBombs);

        placeGold();
    }

    // Helper functions and other classes

    public class Room {
        private int x;
        private int y;
        private int width;
        private int height;


        // defining coordinates
        List<Integer> xCord = new ArrayList<>();
        List<Integer> yCord = new ArrayList<>();

        public Room(int x, int y, int width, int height, int num) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;

        }
    }

    public void generateRooms() {
        int roomAttempts = 0; // To limit the number of attempts to place rooms.
        int buffer = 2;

        while (listOfRooms.size() < numRooms && roomAttempts < MAX_ATTEMPTS) {
            int roomWidth = random.nextInt(8) + 4; // Rooms will be between 4 and 10 tiles wide.
            int roomHeight = random.nextInt(8) + 4; // Rooms will be between 4 and 10 tiles tall.
            int roomX = random.nextInt(maxX - roomWidth - 2 * buffer - 2) + buffer + 1;
            int roomY = random.nextInt(maxY - roomHeight - 2 * buffer - 2) + buffer + 1;

            if (maxX - roomWidth - 2 * buffer <= 0 || maxY - roomHeight - 2 * buffer <= 0) {
                continue; // Skip this attempt if there's insufficient space
            }

            // Create a new room.
            Room newRoom = new Room(roomX, roomY, roomWidth, roomHeight, listOfRooms.size());

            boolean overlaps = false;
            for (Room existingRoom : listOfRooms) {
                if (roomsOverlap(existingRoom, newRoom, buffer)) {
                    overlaps = true;
                    break;
                }
            }

            // If no overlap, add the room
            if (!overlaps) {
                listOfRooms.add(newRoom);
                drawRoom(newRoom);
            }

            roomAttempts++;

        }

        uf = new QuickUnionUF(numRooms);


    }

    private boolean roomsOverlap(Room r1, Room r2, int buffer) {
        // Check if r1 and r2 overlap, considering the buffer
        return !(r1.x + r1.width + buffer < r2.x
                || r2.x + r2.width + buffer < r1.x
                || r1.y + r1.height + buffer < r2.y
                || r2.y + r2.height + buffer < r1.y);
    }

    // Helper method to draw a room on the tiles grid.
    private void drawRoom(Room room) {
        // Draw walls and floors
        for (int x = room.x - 1; x <= room.x + room.width; x++) {
            for (int y = room.y - 1; y <= room.y + room.height; y++) {
                // Determine if the current tile should be a wall or floor
                if (x >= room.x && x < room.x + room.width && y >= room.y && y < room.y + room.height) {
                    tiles[x][y] = FLOOR; // Inside the room is FLOOR
                } else if (x >= 0 && x < maxX && y >= 0 && y < maxY) {
                    tiles[x][y] = WALL; // Surrounding tiles are WALL
                }
            }
        }
    }

    private void connectedRooms() {

        for (int r1Index = 0; r1Index < listOfRooms.size(); r1Index++) {

            Room r1 = listOfRooms.get(r1Index);
            List<Room> neighbors = findClosestNeighbors(r1);

            for (Room neigbor : neighbors) {
                int r2Index = listOfRooms.indexOf(neigbor);

                if (!uf.connected(r1Index, r2Index)) {

                    if (!space(r1, neigbor)) {
                        connectUsingHallways(r1, neigbor);
                        uf.union(r1Index, r2Index);
                        break;
                    }
                }
            }
        }
        wrapHallwaysWithWalls();
    }

    private boolean space(Room r1, Room r2) {
        return r1.x + r1.width > r2.x && r1.x < r2.x + r2.width
                && r1.y + r1.height > r2.y && r1.y < r2.y + r2.height;
    }


    private  List<Room> findClosestNeighbors(Room room) {
        List<Room> neighbors = new ArrayList<>();

        for (Room other : listOfRooms) {
            if (room != other) {
                neighbors.add(other);
            }
        }
        neighbors.sort(Comparator.comparingDouble(
                r -> Math.sqrt(Math.pow(room.x - r.x, 2) + Math.pow(room.y - r.y, 2))));
        return neighbors;
    }

    private void connectUsingHallways(Room r1, Room r2) {


        // calculate center of each room - get coordinates
        int centerX1 = r1.x + r1.width / 2;
        int centerY1 = r1.y + r1.height / 2;
        int centerX2 = r2.x + r2.width / 2;
        int centerY2 = r2.y + r2.height / 2;

        horizontalHallwayCreator(centerX1, centerX2, centerY1);
        verticalHallwayCreator(centerY1, centerY2, centerX2);

    }

    private  void horizontalHallwayCreator(int x1, int x2, int y) {
        int xStart = Math.min(x1, x2);
        int xEnd = Math.max(x1, x2);

        // Loop through and mark the horizontal path as a floor
        for (int x = xStart; x <= xEnd; x++) {
            if (x >= 0 && x < maxX && y >= 0 && y < maxY) {
                tiles[x][y] = FLOOR; // Set the tile to be a hallway floor
            }
        }
    }

    private  void verticalHallwayCreator(int y1, int y2, int x) {
        int yStart = Math.min(y1, y2);
        int yEnd = Math.max(y1, y2);

        // Loop through and mark the vertical path as a floor (hallway)
        for (int y = yStart; y <= yEnd; y++) {
            if (x >= 0 && x < maxX && y >= 0 && y < maxY) {
                tiles[x][y] = FLOOR; // Set the tile to be a floor (hallway)
            }
        }
    }

    public void wrapHallwaysWithWalls() {
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                if (tiles[x][y] == FLOOR) {
                    for (int dirx = -1; dirx <= 1; dirx++) {
                        for (int diry = -1; diry <= 1; diry++) {

                            if (dirx == 0 && diry == 0) {
                                continue;
                            }
                            // skip checking center tile bc want to be floor

                            int wallx = x + dirx;
                            int wally = y + diry;

                            // checking if in bound and if tile is not floor
                            if (wallx >= 0 && wallx < maxX && wally >= 0
                                    && wally < maxY && tiles[wallx][wally] == NOTHING) {
                                tiles[wallx][wally] = WALL;
                            }
                        }
                    }
                }
            }
        }
    }

    public TETile[][] getTiles() {
        return tiles;
    }

    public void displayWorld() {
        wr.drawWorld(tiles);
    }

    public class Avatar {
        int avatarX;
        int avatarY;

        public Avatar(int x, int y) {
            avatarX = x;
            avatarY = y;

        }

        // moving the avatar
        public void move(char d) {

            if (d == 'w') { // moving up/foward command
                if (tiles[avatarX][avatarY + 1] == FLOOR) {
                    tiles[avatarX][avatarY] = FLOOR;
                    avatarY++;
                    tiles[avatarX][avatarY] = AVATAR;
                } else if (tiles[avatarX][avatarY + 1] == BOMB) {
                    System.out.println("GAME OVER!! You ran into a BOMB");
                    System.exit(0);
                } else if (tiles[avatarX][avatarY + 1] == GOLD) {
                    avatarY++;
                    System.out.println("YOU FOUND THE GOLD");
                    StdDraw.pause(TIME);
                    System.exit(0);
                }

            } else if (d == 's') { // moving down/backward command
                if (tiles[avatarX][avatarY - 1] == FLOOR) {
                    tiles[avatarX][avatarY] = FLOOR;
                    avatarY--;
                    tiles[avatarX][avatarY] = AVATAR;
                } else if (tiles[avatarX][avatarY - 1] == BOMB) {
                    System.out.println("GAME OVER!! You ran into a BOMB");
                    System.exit(0);
                } else if (tiles[avatarX][avatarY - 1] == GOLD) {
                    avatarY--;
                    System.out.println("YOU FOUND THE GOLD");
                    StdDraw.pause(TIME);
                    System.exit(0);
                }

            } else if (d == 'a') { // moving left command
                if (tiles[avatarX - 1][avatarY] == FLOOR) {
                    tiles[avatarX][avatarY] = FLOOR;
                    avatarX--;
                    tiles[avatarX][avatarY] = AVATAR;
                } else if (tiles[avatarX - 1][avatarY] == BOMB) {
                    System.out.println("GAME OVER!! You ran into a BOMB");
                    System.exit(0);
                } else if (tiles[avatarX - 1][avatarY] == GOLD) {
                    avatarX--;
                    System.out.println("YOU FOUND THE GOLD");
                    StdDraw.pause(TIME);
                    System.exit(0);
                }

            } else if (d == 'd') {
                if (tiles[avatarX + 1][avatarY] == FLOOR) {
                    tiles[avatarX][avatarY] = FLOOR;
                    avatarX++;
                    tiles[avatarX][avatarY] = AVATAR;
                } else if (tiles[avatarX + 1][avatarY] == BOMB) {
                    System.out.println("GAME OVER!! You ran into a BOMB");
                    System.exit(0);
                } else if (tiles[avatarX + 1][avatarY] == GOLD) {
                    avatarX++;
                    System.out.println("YOU FOUND THE GOLD");
                    StdDraw.pause(TIME);
                    System.exit(0);
                }
            }

            System.out.println(avatarX + " " + avatarY);
        }
    }

    public class Avatar2 {
        int avatarX2;
        int avatarY2;

        public Avatar2(int x, int y) {
            avatarX2 = x;
            avatarY2 = y;

        }

        // moving the avatar
        public void move(char d) {
            if (d == 'h') { // moving up/foward command
                if (tiles[avatarX2][avatarY2 + 1] == FLOOR) {
                    tiles[avatarX2][avatarY2] = FLOOR;
                    avatarY2++;
                    tiles[avatarX2][avatarY2] = AVATAR;
                } else if (tiles[avatarX2][avatarY2 + 1] == BOMB) {
                    System.out.println("GAME OVER!! You ran into a BOMB");
                    System.exit(0);
                } else if (tiles[avatarX2][avatarY2 + 1] == GOLD) {
                    avatarY2++;
                    System.out.println("YOU FOUND THE GOLD");
                    StdDraw.pause(TIME);
                    System.exit(0);
                }

            } else if (d == 'n') { // moving down/backward command
                if (tiles[avatarX2][avatarY2 - 1] == FLOOR) {
                    tiles[avatarX2][avatarY2] = FLOOR;
                    avatarY2--;
                    tiles[avatarX2][avatarY2] = AVATAR;

                } else if (tiles[avatarX2][avatarY2 - 1] == BOMB) {
                    System.out.println("GAME OVER!! You ran into a BOMB");
                    System.exit(0);
                } else if (tiles[avatarX2][avatarY2 - 1] == GOLD) {
                    avatarY2--;
                    System.out.println("YOU FOUND THE GOLD");
                    FLOOR = new TETile('.', Color.LIGHT_GRAY, Color.YELLOW, "floor", 1);
                    StdDraw.pause(TIME);
                    System.exit(0);
                }

            } else if (d == 'b') { // moving left command
                if (tiles[avatarX2 - 1][avatarY2] == FLOOR) {
                    tiles[avatarX2][avatarY2] = FLOOR;
                    avatarX2--;
                    tiles[avatarX2][avatarY2] = AVATAR;

                } else if (tiles[avatarX2 - 1][avatarY2] == BOMB) {
                    System.out.println("GAME OVER!! You ran into a BOMB");
                    System.exit(0);
                } else if (tiles[avatarX2 - 1][avatarY2] == GOLD) {
                    avatarX2--;
                    System.out.println("YOU FOUND THE GOLD");
                    FLOOR = new TETile('.', Color.LIGHT_GRAY, Color.YELLOW, "floor", 1);
                    StdDraw.pause(TIME);
                    System.exit(0);
                }

            } else if (d == 'm') {
                if (tiles[avatarX2 + 1][avatarY2] == FLOOR) {
                    tiles[avatarX2][avatarY2] = FLOOR;
                    avatarX2++;
                    tiles[avatarX2][avatarY2] = AVATAR;
                } else if (tiles[avatarX2 + 1][avatarY2] == BOMB) {
                    System.out.println("GAME OVER!! You ran into a BOMB");
                    System.exit(0);
                } else if (tiles[avatarX2 + 1][avatarY2] == GOLD) {
                    avatarX2++;
                    System.out.println("YOU FOUND THE GOLD");

                    StdDraw.pause(TIME);
                    System.exit(0);
                }

            }

            System.out.println(avatarX2 + " " + avatarY2);
        }
    }

    private void avatarCreate() {
        Room one = listOfRooms.get(0); // return first room that is generated
        int x = one.x + one.width / 2;
        int y = one.y + one.height / 2;

        Room two = listOfRooms.get(1);
        int x2 = two.x + two.width / 2;
        int y2 = two.y + two.height / 2;

        avatar = new Avatar(x, y);
        avatar2 = new Avatar2(x2, y2);
        tiles[x][y] = AVATAR;
    }


    public void avatarLoad(Integer x, Integer y) {
        avatar = new Avatar(x, y);
        tiles[x][y] = AVATAR;
    }

    public void avatarLoad2(Integer x, Integer y) {
        avatar2 = new Avatar2(x, y);
        tiles[x][y] = AVATAR;
    }

    public int getAvatarX() {
        return avatar.avatarX;
    }

    public int getAvatarY() {
        return avatar.avatarY;
    }

    public int getAvatarX2() {
        return avatar2.avatarX2;
    }

    public int getAvatarY2() {
        return avatar2.avatarY2;
    }

    public void placeBombs(int numBombs) {
        int attempt = 0;

        int placedBombs = 0;

        while (placedBombs < numBombs && attempt < MAX_ATTEMPTS) {
            int bombX = random.nextInt(maxX);
            int bombY = random.nextInt(maxY);

            if (tiles[bombX][bombY] == FLOOR) {
                tiles[bombX][bombY] = BOMB; // replacing floor tile with bomb randomly
                placedBombs++;
            }
            attempt++;
        }
        if (placedBombs < numBombs) {
            System.out.println("Unable to place all bombs");
        }
    }

    public void placeGold() {
        int gold = 1;
        int placed = 0;

        while (placed < gold) {
            int goldX = random.nextInt(maxX);
            int goldY = random.nextInt(maxY);

            if (tiles[goldX][goldY] == FLOOR) {
                tiles[goldX][goldY] = GOLD; // replacing floor tile with bomb randomly
                placed++;

            }
        }
    }


}





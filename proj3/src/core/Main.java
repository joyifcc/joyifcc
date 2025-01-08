package core;
import edu.princeton.cs.algs4.StdDraw;
import tileengine.*;
import java.util.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

public class Main {

    private static final String TITLE = "CS61B: BOMBS AND GOLD";
    private static final String NEW = "(N) New Game";
    private static final String LOAD = "(L) Load Game";
    private static final String QUIT = "(Q) Quit Game";

    private static final int WORLD_WIDTH = 60;
    private static final int WORLD_HEIGHT = 40;

    private boolean qPressed = false; // for saving game logic
    private boolean isGameOver;
    private char lastInput = '\0';
    private String prevTileType = "";

    private final TERenderer teRenderer = new TERenderer();
    private World world; // all the tiles in our world

    static final int NUM1 = 255;
    static final int NUM2 = 39;
    static final int NUM3 = 10;
    static final double NUM4 = 0.8;
    static final double NUM5 = 0.6;
    static final double NUM6 = 0.4;
    static final int NUM7 = 100;
    static final int NUM8 = 20;
    static final double NUM9 = 0.6;
    static final double NUM10 = 0.3;
    static final double NUM11 = 0.05;
    static final int NUM12 = 10;


    public static void main(String[] args) {
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.clear();

        Main game = new Main();
        game.mainMenuDisplay();
    }

    public static boolean gameisOver(World world) {
        return world.gameIsOver;
    }

    private void rendingHUD() {
        // displays HUD at fixed position in game
        String curTileType = getTileType();

        if (curTileType != prevTileType) {
            clearingHUD();
        }

        String description = "Tile: " + curTileType;

        prevTileType = curTileType;


        StdDraw.setPenColor(NUM1, NUM1, NUM1); // white

        StdDraw.text(3, NUM2, description); // x = 3, y = 39 -- score display

        StdDraw.show();
    }

    private void clearingHUD() {

        // renderingHUD helper function
        StdDraw.setPenColor(StdDraw.BLACK); // use to erase
        StdDraw.filledRectangle(3, NUM2, NUM3, 1);
        StdDraw.show();
    }

    private String getTileType() {
        // get mouse X and Y position and cast it as an int
        int clickX = (int) Math.floor(StdDraw.mouseX());
        int clickY = (int) Math.floor(StdDraw.mouseY());

        // return our world tiles
        if (clickX >= 0 && clickX < world.getTiles().length && clickY >= 0 && clickY < world.getTiles()[0].length) {
            TETile tile = world.getTiles()[clickX][clickY];
            if (tile != null) {
                return tile.description();
            }
        }
        return "nothing";
    }

    private void mainMenuDisplay() {
        boolean quit = false;

        // loading our menu and displaying the screen content
        StdDraw.clear();


        StdDraw.text(0.5, NUM4, TITLE); // coordinates for placing each selection
        StdDraw.text(0.5, NUM5, NEW); // title on top then new load and quit all in center
        StdDraw.text(0.5, 0.5, LOAD);
        StdDraw.text(0.5, NUM6, QUIT);
        StdDraw.show();

        while (!quit) {

            // based on user input, perform function in main menu
            if (StdDraw.hasNextKeyTyped()) {
                char userInput = StdDraw.nextKeyTyped();

                // switch input into all lower case to process BOTH upper and lower case
                switch (Character.toLowerCase(userInput)) {
                    case 'n':
                        newWorldGeneration();
                        break;
                    case 'l':
                        loadWorld();
                        break;
                    case 'q':
                        quit = true;
                        System.exit(0);
                        break;
                    default: // handling unexpected inputs
                        System.out.println("Invalid Option Detected");
                        break;

                }
            }

            StdDraw.pause(NUM7);
        }
    }


    private void newWorldGeneration() {
        String seedValue = userSeed();
        long seed = Long.parseLong(seedValue); // using Long bc integer too restrictive

        world = new World(WORLD_WIDTH, WORLD_HEIGHT, seed, false); // Use class field directly

        teRenderer.initialize(WORLD_WIDTH, WORLD_HEIGHT);
        teRenderer.renderFrame(world.getTiles());

        gameRun();
    }

    private void gameRun() {
        while (!isGameOver) {
            rendingHUD();
            updatingWorld();
            renderWorld(world.getTiles());
        }
    }

    private void renderWorld(TETile[][] tiles) {
        teRenderer.renderFrame(tiles);
    }

    private void updatingWorld() {
        // key pressing requirements based on spec
        if (StdDraw.hasNextKeyTyped()) {
            char input = Character.toLowerCase(StdDraw.nextKeyTyped());
            System.out.println("Key pressed: " + input);

            if (lastInput == ':' && input == 'q') {
                saveAndQuit();
            } else {
                world.avatar.move(input); // initiate avatar movement
                world.avatar2.move(input);
                teRenderer.renderFrame(world.getTiles());
            }
            lastInput = input;
        }

        StdDraw.pause(NUM8);
    }

    private void saveAndQuit() {
        saveWorld();
        System.exit(0);
    }

    private static String userSeed() {
        StringBuilder seedBuilder = new StringBuilder();


        StdDraw.clear();
        StdDraw.text(0.5, NUM9, "Enter Seed Number:"); // main menu word display
        StdDraw.show();

        // based on user input, get seed and implement into world generation
        char userInput = '\0';

        do {
            if (StdDraw.hasNextKeyTyped()) {
                userInput = StdDraw.nextKeyTyped();

                // if input is valid digit -> append to our seedBuilder
                if (Character.isDigit(userInput)) {
                    seedBuilder.append(userInput);

                    // hiding previously rendered seed
                    StdDraw.setPenColor(StdDraw.WHITE);
                    StdDraw.filledRectangle(0.5, NUM9, NUM10, NUM11);

                    // rendering updated seed text
                    StdDraw.setPenColor(StdDraw.BLACK);
                    StdDraw.text(0.5, NUM9, "Seed: " + seedBuilder.toString());
                    StdDraw.show();
                }
            }
        } while (userInput != 's' && userInput != 'S');

        return seedBuilder.toString();
    }


    private void loadWorld() {
        Long seed = null;
        Integer avatarX = null;
        Integer avatarY = null;
        Integer avatarX2 = null;
        Integer avatarY2 = null;



        try (BufferedReader reader = new BufferedReader(new FileReader("save.txt"))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Seed:")) {
                    // Extract seed value
                    seed = Long.parseLong(line.substring(6).trim());
                } else if (line.startsWith("Avatar X:")) {
                    // Extract avatar X position
                    avatarX = Integer.parseInt(line.substring(NUM12).trim());
                } else if (line.startsWith("Avatar Y:")) {
                    // Extract avatar Y position
                    avatarY = Integer.parseInt(line.substring(NUM12).trim());
                } else if (line.startsWith("Avatar X2:")) {
                    avatarX2 = Integer.parseInt(line.substring(NUM12).trim());
                } else if (line.startsWith("Avatar Y2:")) {
                    avatarY2 = Integer.parseInt(line.substring(NUM12).trim());
                } else {
                    System.err.println("Warning: Unrecognized line format: " + line);
                }
            }

            if (seed == null || avatarX == null || avatarY == null || avatarX2 == null || avatarY2 == null) {
                throw new IllegalStateException("Error: Missing required data in save file.");
            }

            world = new World(WORLD_WIDTH, WORLD_HEIGHT, seed, true);
            world.avatarLoad(avatarX, avatarY);
            world.avatarLoad2(avatarX2, avatarY2);

            teRenderer.initialize(WORLD_WIDTH, WORLD_HEIGHT);
            teRenderer.renderFrame(world.getTiles());

            gameRun();

        } catch (FileNotFoundException e) {
            // This catches the case where the file is not found
            System.err.println("Error: The file save.txt was not found.");
        } catch (IOException e) {
            // This catches any other IO issues
            e.printStackTrace();
            System.err.println("Error loading game: " + e.getMessage());
        }
    }

    public void saveWorld() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("save.txt"))) {
            // Save the seed
            writer.write("Seed: " + world.seed + "\n");

            // Save the avatar's position
            writer.write("Avatar X: " + world.getAvatarX() + "\n");
            writer.write("Avatar Y: " + world.getAvatarY() + "\n");
            writer.write("Avatar X2: " + world.getAvatarX2() + "\n");
            writer.write("Avatar Y2: " + world.getAvatarY2() + "\n");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error saving game: " + e.getMessage());
        }
    }
}





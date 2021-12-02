/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainFiles;

import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Ethan
 */
public class CavedMain {

    /*
        Make tools do something
            - turn block data into an array of values [durability, type, x, y]
                - this not only benifits not having to retrofit all code with this new data type but also makes for future expansion easier
                - makes doors, mob check, world gen and everything that relates to block interactions easier (everything)
            - this makes all map data accessable at random 
            - removes the need for map data extraction (a slow inefficient process)
        Make enemies
            - if delta is bigger than 5 dont find player
            - when player not found pick a random place on the map to move to
            - if player found at any point go to player and cause damage when beside player
            - have the delta of x and y of player become which moves go next
            - if both deltas the same go down
            - whichever delta is bigger go that way
            - this creates super pathfinding
            - eventually make enemies not able to move through blocks
            - if somehow the enemy moves onto the same place as the player make the player die
        Make world save & load
            - save to editable file
            - text file has cd data stored, player inventory stored and enemy data saved
            - save to save to to file located in documents with name specified
            - load to load that file located in documents with name specified
        Caves
            - have box with inpenetrable shell around that cant be broken appear on screen with exit in middle
            - caves will include stone that is mineable and ores
            - cave data will be stored in a seperate data set
            - caves may include enemies
        Player only door
            - item in which only a player can pass through acting as an automatic door
        Change characters on screen to easier to read characters
            - search through ascii tables for this
        
        ### KNOWN BUGS ####
     */
    // BLACK "\033[0;30m"
    // RED "\033[0;31m"
    // GREEN "\033[0;32m"
    // YELLOW "\033[0;33m"
    // BLUE "\033[0;34m"
    // PURPLE "\033[0;35m"
    // CYAN "\033[0;36m"
    // WHITE "\033[0;37m"

    // TODO Add JColor for combined highlighting and foregrounding

    // For background use 4 infront of color id instead of 3
    /**
     * @param args the command line arguments
     */
    static Scanner input = new Scanner(System.in);

    // map related variables
    static String[] chars = { "W", "D", "C", "V", "S", "#" };
    static int[] amount = { 0, 0, 0, 0, 0, 0 };
    static String[] charColors = { "\u001B[33m", "\u001B[33m", "\u001B[34m", "\u001B[37m", "\u001B[37m", "\u001B[30m" };
    static Boolean[] breakable = { true, true, true, false, true, false };
    static String reset = "\u001B[0m";
    static String playerC = "\u001B[36m";
    static String health = "\u001B[31m";

    static Random random = new Random();

    static BlockData bound = new BlockData(5, 0, 0, 1, amount[5]++);

    static BlockData[][] caveTemplate = {
            { bound, bound, bound, bound, bound, bound, bound, bound, bound },
            { bound, null, null, null, null, null, null, null, bound },
            { bound, null, null, null, null, null, null, null, bound },
            { bound, null, null, null, null, null, null, null, bound },
            { bound, null, null, null, null, null, null, null, bound },
            { bound, null, null, null, null, null, null, null, bound },
            { bound, null, null, null, null, null, null, null, bound },
            { bound, null, null, null, null, null, null, null, bound },
            { bound, bound, bound, bound, bound, bound, bound, bound, bound }
    };

    static boolean inCave;
    static int enterX;
    static int enterY;
    static int caveIn = 0;

    static int chunkX = 0;
    static int chunkY = 0;

    // general player related variables
    static int playerX = 0;
    static int playerY = 0;

    static int playerHp = 3;
    // inventory player related variables
    static String[] tb = { "  ", "  ", "  " };
    static int[] inv = { 0, 0, 0, 0, 0, 0 };
    static int invPos = 0;

    // general game variables
    static boolean run = true;

    static int size = inputInt("Enter desired size of map to generate in multiples of 9");
    static BlockData[][] map = new BlockData[size][size];

    public static void main(String[] args) {
        map = genMap(size);
        BlockData[][][] caves = new BlockData[amount[3]][9][9];
        System.out.println(amount[3]);
        caves = genCaves(caves);
        printMap(map);
        while (run) {
            topUI();
            if (inCave) {
                printChunk(caves[caveIn], "\u001B[37m", 0, 0);
            } else {
                printChunk(map, "\u001B[32m", chunkX, chunkY);
            }
            bottomUI();
            userInput();

            // System.out.print("\033[H\033[2J");
            // System.out.flush();
        }
        input.close();
    }

    public static void userInput() {
        String instruction;
        instruction = inputString("");
        instruction = instruction.toUpperCase();

        /*
        
        To add an if statement put this sequence after the last condition
        
         if (instruction.equals("")) {
        
        } else
        
         */
        // movement
        if (instruction.equals("W")) {
            move(0, -1);
        } else if (instruction.equals("A")) {
            move(-1, 0);
        } else if (instruction.equals("S")) {
            move(0, 1);
        } else if (instruction.equals("D")) {
            move(1, 0);
        }

        // mining
        if (instruction.charAt(0) == 'M') {
            char secondChar = instruction.charAt(1);
            if (secondChar == 'R') {
                mineBlock(1, 0);
            } else if (secondChar == 'L') {
                mineBlock(-1, 0);
            } else if (secondChar == 'U') {
                mineBlock(0, -1);
            } else if (secondChar == 'D') {
                mineBlock(0, 1);
            }
        }

        // placing
        if (instruction.charAt(0) == 'P') {
            char secondChar = instruction.charAt(1);
            if (secondChar == 'R') {
                placeBlock(1, 0);
            } else if (secondChar == 'L') {
                placeBlock(-1, 0);
            } else if (secondChar == 'U') {
                placeBlock(0, -1);
            } else if (secondChar == 'D') {
                placeBlock(0, 1);
            }
        }

        // inv checker
        if (instruction.equals("INV")) {
            for (int i = 0; i < inv.length; i++) {
                System.out.println(inv[i] + "" + chars[i] + " ");
            }
        }

        // exit cave
        if (instruction.equals("R") && inCave) {
            inCave = false;
            playerX = enterX;
            playerY = enterY;
        }

        // map print
        if (instruction.equals("MAP")) {
            printMap(map);
        }

        // print amount of every type
        if (instruction.equals("SUM")) {
            printAmount();
        }

        // game force quit
        if (instruction.equals("STOP") || instruction.equals("EXIT")) {
            System.out.println("Hope to see you again");
            System.out.println("Have a good day user");
            run = false;
        }

        // scrolling of hotbar
        if (instruction.equals("Q")) {
            if (invPos > 0) {
                invPos--;
            }
        } else if (instruction.equals("E")) {
            if (invPos < inv.length - 2) {
                invPos++;
            }
        }

        // interacting / crafting
        if (instruction.charAt(0) == 'E') {
            String subString = instruction.substring(1);
            if (subString.equals("U")) {
                interact(0, -1);
            } else if (subString.equals("R")) {
                interact(1, 0);
            } else if (subString.equals("D")) {
                interact(0, 1);
            } else if (subString.equals("L")) {
                interact(-1, 0);
            } else if (subString.equals("I")) {
                if (inv[0] >= 4) {
                    inv[2]++;
                    System.out.println("Crafting bench crafted");
                } else {
                    System.out.println("Not enough wood to make crafting bench");
                }
            }
        }
    }

    public static void interact(int x, int y) {
        if (map[playerY + y][playerX + x] != null) {
            if (map[playerY + y][playerX + x].id == 2) {
                // the item to the offset space is a crafting bench
                System.out.println("What item do you want to craft?");
                System.out.println("Possible items include: dirt cake, axe, ct(crafting table)");
                String item = inputString("Type name of item to craft");
                item = item.toUpperCase();
                if (item.equals("DIRT")) {
                    if (inv[1] >= 1) {
                        playerHp++;
                        System.out.println("A dirt cake was crafted and consumed");
                        inv[1]--;
                    } else {
                        System.out.println("Not enough dirt to make dirt cake");
                    }
                } else if (item.equals("AXE")) {
                    if (inv[0] >= 2) {
                        tb[0] = "AX";
                        System.out.println("An axe was crafted");
                        inv[0] = inv[0] - 2;
                    } else {
                        System.out.println("Not enough wood to make an axe");
                    }
                } else {
                    System.out.println(item);
                }
            } else if (map[playerY + y][playerX + x].id == 3) {
                enterX = playerX;
                enterY = playerY;
                caveIn = map[playerY + y][playerX + x].num;
                inCave = true;
                playerX = 4;
                playerY = 4;
            }
        } else {
            System.out.println("No interactable here");
        }
    }

    public static void placeBlock(int x, int y) {
        System.out.println("What block do you want to place?");
        System.out.println("For wood enter 0, for dirt enter 1 and for a crafting bench enter 2");
        System.out.println("Amounts of each block are as the following");
        int block = inputInt("Wood: " + inv[0] + " / Dirt: " + inv[1] + " / CB: " + inv[2]);

        // if block to the offset doesnt exist then place block
        if (!checkBlock(playerX + x, playerY + y)) {
            // if enough blocks in inv exists
            if (inv[block] > 0) {
                // remove block
                inv[block]--;
                map[playerY + y][playerX + x] = new BlockData(block, playerX + x, playerY + y, 2, amount[block]++);
            } else {
                System.out.println("Not enough blocks of that type");
            }
        }
    }

    public static void mineBlock(int x, int y) {
        // if block to the offset exists mine it
        if (checkBlock(playerX + x, playerY + y)) {
            if (breakable[map[playerY + y][playerX + x].id]) {
                // add block id to the right to inventory
                inv[map[playerY + y][playerX + x].id]++;
                // set id of block to the right to null
                map[playerY + y][playerX + x] = null;
            } else {
                System.out.println("This block is not breakable.");
            }
        }
    }

    public static boolean checkBlock(int x, int y) {
        if (map[y][x] != null) {
            return true;
        }
        return false;
    }

    // user input methods
    public static void move(int x, int y) {
        int highX = (chunkX + 1) * 9;
        int lowX = (chunkX + 1) * 9 - 9;

        int highY = (chunkY + 1) * 9;
        int lowY = (chunkY + 1) * 9 - 9;

        if (playerX + x >= lowX && playerX + x < highX) {
            // if within bounds of chunk move normally
            playerX = playerX + x;
        } else if (playerX == lowX || playerX == highX - 1) {
            // if at edge of chunk check if not at edge of map
            // if not at map offset chunk and player
            if (chunkX + x >= 0 && chunkX + x <= (size / 9) - 1) {
                chunkX = chunkX + x;
                playerX = playerX + x;
            }
        }

        if (playerY + y < highY && playerY + y >= lowY) {
            // if within bounds of chunk move normally
            playerY = playerY + y;
        } else if (playerY == lowY || playerY == highY - 1) {
            // if at edge of chunk check if not at edge of map
            // if not at map offset chunk and player
            if (chunkY + y <= (size / 9) - 1 && chunkY + y >= 0) {
                chunkY = chunkY + y;
                playerY = playerY + y;
            }
        }
    }

    // map and user interface methods

    // print whole map
    public static void printMap(BlockData[][] map) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] != null) {
                    System.out.print(charColors[map[y][x].id] + chars[map[y][x].id] + " " + reset);
                } else if (y == playerY && x == playerX) {
                    System.out.print(playerC + "P " + reset);
                } else {
                    System.out.print("\u001B[32m" + "O " + reset);
                }
            }
            System.out.println("");
        }
    }

    // print current chunk
    public static void printChunk(BlockData[][] chunk, String backgroundColor, int chunkX, int chunkY) {
        for (int y = (((chunkY + 1) * 9) - 9); y <= (((chunkY + 1) * 9) - 1); y++) {
            System.out.print("[|      ");
            for (int x = (((chunkX + 1) * 9) - 9); x <= (((chunkX + 1) * 9) - 1); x++) {
                if (chunk[y][x] != null) {
                    System.out.print(charColors[chunk[y][x].id] + chars[chunk[y][x].id] + " " + reset);
                } else if (y == playerY && x == playerX) {
                    System.out.print(playerC + "P " + reset);
                } else {
                    System.out.print(backgroundColor + "O " + reset);
                }
            }
            System.out.print("     |]");
            System.out.println("");
        }
    }

    // print top section of user interface
    public static void topUI() {
        // prints health tag
        System.out.print("[HEA:]");

        // prints full hearts
        for (int i = 0; i < playerHp && i < 3; i++) {
            System.out.print("[" + health + "<3" + reset + "]");
        }

        // prints empty hearts
        if (playerHp < 3) {
            for (int i = 0; i < 3 - playerHp; i++) {
                System.out.print("[<>]");
            }
        }
        System.out.println("[LOC:][00" + playerX + ",00" + playerY + "]");
        System.out.println("[+----- 1 2 3 4 5 6 7 8 9 -----+]");
        System.out.print("[|                             |]");
        System.out.println("");
    }

    // print botton section of user interface
    public static void bottomUI() {
        System.out.println("[|                             |]");
        System.out.println("[+----- 1 2 3 4 5 6 7 8 9 -----+]");
        // prints toolbar and itembar
        System.out.print("[TO:]" + "[" + tb[0] + "]" + "[" + tb[1] + "]" + "[" + tb[2] + "]" + "[ITE:]" + "[");
        if (inv[0] < 10) {
            System.out.print("0" + inv[invPos] + chars[invPos]);
        }
        System.out.print("][");
        if (inv[1] < 10) {
            System.out.print("0" + inv[invPos + 1] + chars[invPos + 1] + "]");
        }
        System.out.println("");
    }

    // print map distribution
    public static void printAmount() {
        for (int i = 0; i < amount.length; i++) {
            System.out.print(amount[i] + ", ");
        }
        System.out.println();
    }

    // generate the map
    public static BlockData[][] genMap(int size) {

        // up to 18 blocks per size / 3 ( up to 18 blocks per chunk)
        int lim = 18 * (int) Math.pow(size / 9, 2);
        int rand = randomInt(0, lim);
        for (int i = 0; i < rand; i++) {
            // create a random x, y and id to place;
            int x = randomInt(0, 9 * size / 9);
            int y = randomInt(0, 9 * size / 9);
            int id = randomInt(0, 81);
            if (id % 17 == 0) {
                id = 3;
            } else {
                id = randomInt(0, 2);
            }
            int dur = 2;
            // System.out.println(x + "" + y + "" + id);
            // put block down using block data with those random numbers
            map[y][x] = new BlockData(id, x, y, dur, amount[id]++);
        }

        return map;
    }

    // generate the caves
    public static BlockData[][][] genCaves(BlockData[][][] caves) {
        for (int n = 0; n < caves.length; n++) {
            // for each cave apply the template
            caves[n] = caveTemplate;

            /*
            int rand = randomInt(0, 9);
            for (int i = 0; i < rand; i++) {
                int y = randomInt(1, 8);
                int x = randomInt(1, 8);
                caves[n][y][x] = new BlockData(4, x, y, 3, amount[4]++);
            }
            
            for (int y = 0; y < caves[n].length; y++) {
                for (int x = 0; x < caves[n][y].length; x++) {
                    if (caves[n][y][x] != null) {
                        int id = caves[n][y][x].id;
                        System.out.print(charColors[id] + chars[id] + " " + reset);
                    } else {
                        System.out.print("  ");
                    }
                }
                System.out.println("");
            }
            */
        }

        return caves;
    }

    // general purpose methods

    // get a random int 
    public static int randomInt(int min, int max) {
        int rndInt = (random.nextInt((max - min))) + min;
        return rndInt;
    }

    // input a message, get back an int
    public static int inputInt(String message) {
        if (message != "") {
            System.out.println(message);
        }
        int num = input.nextInt();
        return num;
    }

    // input a message, get back a string
    public static String inputString(String message) {
        if (message != "") {
            System.out.println(message);
        }
        String str = input.next();
        return str;
    }
}

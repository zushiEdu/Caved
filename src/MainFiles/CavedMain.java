package MainFiles;

import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Ethan
 */
public class CavedMain {

    /*
        - Make enemies 
            - if delta is bigger than 5 dont find player
            - when player not found pick a random place on the map to move to
            - if player found at any point go to player and cause damage when beside player
            - have the delta of x and y of player become which moves go next
            - if both deltas the same go down
            - whichever delta is bigger go that way
            - this creates super pathfinding
            - eventually make enemies not able to move through blocks
            - if somehow the enemy moves onto the same place as the player make the player die
        - Make world save & load
            - save to editable file
            - text file has cd data stored, player inventory stored and enemy data saved
            - save to save to to file located in documents with name specified
            - load to load that file located in documents with name specified
        - Change characters on screen to easier to read characters
            - search through ascii tables for this
     */

    /**
     * @param args the command line arguments
     */
    static Scanner input = new Scanner(System.in);

    // map related variables
    static String[] ct = { "AX", "SH", "AX", null, "PI", null };
    static String[] chars = { "W", "D", "C", "V", "S", "#" };
    static int[] amount = { 0, 0, 0, 0, 0, 0 };
    // static String[] charColors = { "\u001B[43m", "\u001B[43m", "\u001B[44m", "\u001B[47m", "\u001B[47m", "\u001B[40m" };
    static String[] charColors = { "\u001B[33m", "\u001B[33m", "\u001B[34m", "\u001B[37m", "\u001B[37m", "\u001B[30m" };
    static Boolean[] breakable = { true, true, true, false, true, false };
    static String reset = "\u001B[0m";
    // \u001b[35m magenta
    // \u001b[36m cyan
    // \u001b[34m blue
    static String playerC = "\u001B[34m";
    static String health = "\u001B[31m";

    static BlockData bound = new BlockData(5, 0, 0, 1, amount[5]++);

    /*
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
    */

    static boolean inCave;
    static int enterX;
    static int enterY;
    static int caveIn = 0;

    static int tempChunkX;
    static int tempChunkY;

    static int playerHp = 3;
    // inventory player related variables
    static String[] tb = { "  ", "  ", "  " };
    static int[] inv = { 0, 0, 0, 0, 0, 0 };
    static int invPos = 0;

    // general game variables
    static boolean run = true;

    static int size = inputInt(
            "Enter desired size of map to generate, map will work best if size entered is a multiple of 9");
    static String secret = generating();
    static BlockData[][] map = new BlockData[size][size];

    // general player related variables
    static int playerX = centerPlayer(size);
    static int playerY = centerPlayer(size);

    static int chunkX = posToChunk(centerPlayer(size));
    static int chunkY = posToChunk(centerPlayer(size));

    public static int posToChunk(int pos) {
        return pos / 9;
    }

    public static int centerPlayer(int size) {
        if ((size / 9) % 2 == 0) {
            return (size / 2) + 4;
        } else {
            return size / 2;
        }
    }

    public static String generating() {
        System.out.println("Generating...");
        return "06/07/20";
    }

    public static void main(String[] args) {
        map = genMap(size);
        // BlockData[][][] caves = new BlockData[amount[3]][9][9];
        // System.out.println(amount[3]);
        // caves = genCaves(caves);
        // printMap(map);
        System.out.println("Type 'help' for help menu");
        while (run) {
            topUI();
            if (inCave) {
                // printChunk(caves[caveIn], "\u001B[37m", 0, 0);
            } else {
                printChunk(map, "\u001B[32m", chunkX, chunkY);
            }
            bottomUI();
            if (inCave) {
                // caves[caveIn] = userInput(caves[caveIn]);
            } else {
                map = userInput(map);
            }

            // System.out.print("\033[H\033[2J");
            // System.out.flush();

            /*
            for (int i = 0; i < caves.length; i++) {
                for (int j = 0; j < caves[i].length; j++) {
                    for (int k = 0; k < caves[i][j].length; k++) {
                        System.out.println(caves[i][j][k]);
                    }
                }
            }
            
            for (int i = 0; i < map.length; i++) {
                System.out.println(map[i]);
            }
            */
        }
        input.close();
    }

    public static BlockData[][] userInput(BlockData[][] map) {
        String instruction;
        instruction = inputString("");
        instruction = instruction.toUpperCase();

        /*
        
        To add an if statement put this sequence after the last condition
        
         if (instruction.equals("")) {
        
        } else
        
         */

        // interacting / crafting
        if (instruction.charAt(0) == 'E') {
            String subString = instruction.substring(1);
            if (subString.equals("U")) {
                map = interact(0, -1, map);
            } else if (subString.equals("R")) {
                map = interact(1, 0, map);
            } else if (subString.equals("D")) {
                map = interact(0, 1, map);
            } else if (subString.equals("L")) {
                map = interact(-1, 0, map);
            } else if (subString.equals("I")) {
                if (inv[0] >= 4) {
                    inv[2]++;
                    System.out.println("Crafting bench crafted");
                    inv[0] = inv[0] - 4;
                } else {
                    System.out.println("Not enough wood to make crafting bench");
                }
            } else if (subString.equals("")) {
                map = interact(0, 0, map);
            }
        }

        // movement
        if (inCave) {
            if (instruction.equals("W")) {
                move(0, -1, 1);
            } else if (instruction.equals("A")) {
                move(-1, 0, 1);
            } else if (instruction.equals("S")) {
                move(0, 1, 1);
            } else if (instruction.equals("D")) {
                move(1, 0, 1);
            }
        } else {
            if (instruction.equals("W")) {
                move(0, -1, 0);
            } else if (instruction.equals("A")) {
                move(-1, 0, 0);
            } else if (instruction.equals("S")) {
                move(0, 1, 0);
            } else if (instruction.equals("D")) {
                move(1, 0, 0);
            }
        }

        // mining
        if (instruction.charAt(0) == 'M') {
            String suffix = instruction.substring(1, instruction.length());
            if (suffix.equals("R")) {
                map = mineBlock(1, 0, map);
            } else if (suffix == "L") {
                map = mineBlock(-1, 0, map);
            } else if (suffix.equals("U")) {
                map = mineBlock(0, -1, map);
            } else if (suffix.equals("D")) {
                map = mineBlock(0, 1, map);
            } else {
                map = mineBlock(0, 0, map);
            }
        }

        // placing
        if (instruction.charAt(0) == 'P') {
            String suffix = instruction.substring(1, instruction.length());
            if (suffix.equals("R")) {
                map = placeBlock(1, 0, map);
            } else if (suffix.equals("L")) {
                map = placeBlock(-1, 0, map);
            } else if (suffix.equals("U")) {
                map = placeBlock(0, -1, map);
            } else if (suffix.equals("D")) {
                map = placeBlock(0, 1, map);
            } else {
                map = placeBlock(0, 0, map);
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
            chunkX = tempChunkX;
            chunkY = tempChunkY;
        }

        // map print
        if (instruction.equals("MAP")) {
            printMap(map);
        }

        // print amount of every type
        if (instruction.equals("SUM")) {
            printAmount();
        }

        if (instruction.equals("REGEN")) {
            // clear map then regenerate
            for (int y = 0; y < map.length; y++) {
                for (int x = 0; x < map.length; x++) {
                    map[y][x] = null;
                }
            }
            genMap(size);
        }

        if (instruction.equals("HELP")) {
            System.out.println("");
            System.out.println("--- Help Menu ---");
            System.out.println("Movement - W Up, A Left, S Down, D Right");
            System.out.println("Scrolling of inventory - E Right, Q Left");
            System.out.println("Mining - M Standalone for location of player or combined with the directional keys");
            System.out.println("Placing - P Standalone for location of player or combined with the directional keys");
            System.out
                    .println("Interacting - E Standalone for location of player or combined with the directional keys");
            System.out.println("Directional Keys - U for up 1, R for right 1, L for left 1, D for down 1");
            System.out.println(
                    "Crafting - Type 'Ei' to craft a crafting bench, 4 wood required. Otherwise interact with a crafting bench with E +/ Directional Keys");
            System.out.println("Map view - 'Map' to view map");
            System.out.println("Block atlas, P Player, S Stone, W Wood, D Dirt, M Monster, C Crafting Bench");
            System.out.println("Regeneration - 'Regen' to regenerate the map");
            System.out.println("Block distribution - 'Sum' to view amounts of each block on the map");
            System.out.println("End game - 'Stop' or 'Exit' to exit / stop game");
            System.out.println("");
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
        return map;
    }

    public static BlockData[][] interact(int x, int y, BlockData[][] map) {
        if (map[playerY + y][playerX + x] != null) {
            if (map[playerY + y][playerX + x].id == 2) {
                // the item to the offset space is a crafting bench
                System.out.println("What item do you want to craft?");
                System.out.println("1. Dirt Cake 'dirt', 1 dirt");
                System.out.println("2. Axe 'axe', 2 wood");
                System.out.println("3. Pickaxe 'pickaxe', 3 wood");
                System.out.println("4. Shovel 'shovel', 1 wood");
                String item = inputString("Type name of item in quotation marks to craft");
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
                } else if (item.equals("PICKAXE")) {
                    if (inv[0] >= 3) {
                        tb[1] = "PI";
                        System.out.println("A pickaxe was crafted");
                        inv[0] = inv[0] - 3;
                    } else {
                        System.out.println("Not enough wood to make a pickaxe");
                    }
                } else if (item.equals("SHOVEL")) {
                    if (inv[0] >= 1) {
                        tb[2] = "SH";
                        System.out.println("A shovel was crafted");
                        inv[0] = inv[0] - 2;
                    } else {
                        System.out.println("Not enough wood to make a shovel");
                    }
                } else if (item.equals("")) {

                } else {
                    System.out.println(item + "could not be crafted.");
                }
            } else if (map[playerY + y][playerX + x].id == 3) {
                /*
                caveIn = map[playerY + y][playerX + x].num;
                tempChunkX = chunkX;
                tempChunkY = chunkY;
                enterX = playerX;
                enterY = playerY;
                inCave = true;
                playerX = 4;
                playerY = 4;
                */
            }
        } else {
            System.out.println("No interactable here");
        }

        return map;
    }

    public static BlockData[][] placeBlock(int x, int y, BlockData[][] map) {
        System.out.println("What block do you want to place?");
        System.out.println("1. For wood enter 0, Amount: " + inv[0]);
        System.out.println("2. For dirt enter 1, Amount: " + inv[1]);
        System.out.println("3. For a crafting bench enter 2, Amount: " + inv[2]);
        System.out.println("4. For stone enter 4, Amount: " + inv[4]);

        int block = inputInt("");

        // if block to the offset doesnt exist then place block
        if (!checkBlock(playerX + x, playerY + y, map)) {
            // if enough blocks in inv exists
            if (inv[block] > 0) {
                // remove block
                inv[block]--;
                map[playerY + y][playerX + x] = new BlockData(block, playerX + x, playerY + y, 3, amount[block]++);
            } else {
                System.out.println("Not enough blocks of that type");
            }
        }
        return map;
    }

    public static boolean checkTool(int id) {
        for (int i = 0; i < tb.length; i++) {
            if (ct[id].equals(tb[i])) {
                return true;
            }
        }
        return false;
    }

    public static BlockData[][] mineBlock(int x, int y, BlockData[][] map) {
        if (map[playerY + y][playerX + x] == null) {
            return map;
        }
        int id = map[playerY + y][playerX + x].id;
        Boolean ct = checkTool(id);
        int dmg;
        if (ct) {
            dmg = 4;
        } else {
            dmg = 1;
        }
        // if block to the offset exists mine it

        if (checkBlock(playerX + x, playerY + y, map)) {
            if (map[playerY + y][playerX + x].dur >= 1) {
                map[playerY + y][playerX + x].dur = map[playerY + y][playerX + x].dur - dmg;
            }
            if (breakable[map[playerY + y][playerX + x].id] && map[playerY + y][playerX + x].dur <= 0) {
                // add block id to the right to inventory
                inv[map[playerY + y][playerX + x].id]++;
                // set id of block to the right to null
                map[playerY + y][playerX + x] = null;
            }
        } else {
            System.out.println("This block is not breakable.");
        }
        return map;
    }

    public static boolean checkBlock(int x, int y, BlockData[][] map) {
        if (map[y][x] != null) {
            return true;
        }
        return false;
    }

    // user input methods
    public static void move(int x, int y, int borderOffset) {
        // subtract offset to high values
        // add offset to low values

        if (inCave) {
            chunkX = 0;
            chunkY = 0;
        }
        int highX = (chunkX + 1) * 9 - 1 - borderOffset;
        int lowX = (chunkX + 1) * 9 - 9 + borderOffset;

        int highY = (chunkY + 1) * 9 - 1 - borderOffset;
        int lowY = (chunkY + 1) * 9 - 9 + borderOffset;

        // System.out.println(highX + " " + lowX);
        // System.out.println(highY + " " + lowY);

        if (playerX + x >= lowX && playerX + x <= highX) {
            // if within bounds of chunk move normally
            playerX = playerX + x;
        } else if (!inCave) {
            if (playerX == lowX || playerX == highX) {
                // if at edge of chunk check if not at edge of map
                // if not at map offset chunk and player
                if (chunkX + x >= 0 && chunkX + x <= (size / 9) - 1) {
                    chunkX = chunkX + x;
                    playerX = playerX + x;
                }
            }
        }

        if (playerY + y <= highY && playerY + y >= lowY) {
            // if within bounds of chunk move normally
            playerY = playerY + y;
        } else if (!inCave) {
            if (playerY == lowY || playerY == highY) {
                // if at edge of chunk check if not at edge of map
                // if not at map offset chunk and player
                if (chunkY + y <= (size / 9) - 1 && chunkY + y >= 0) {
                    chunkY = chunkY + y;
                    playerY = playerY + y;
                }
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
        // System.out.println(caveIn);
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

        String amt = inv[invPos] + "";

        if (inv[0] < 10) {
            System.out.print("0" + amt + chars[invPos]);
        } else if (inv[0] <= 99) {
            amt = amt.substring(amt.length() - 2, amt.length());
            System.out.print(amt + chars[invPos]);
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
                if (y - 1 >= 0 && y + 1 < size && x - 1 >= 0 && x + 1 < size) {
                    // i know this is terrible code dont bug me about it
                    if (randomBool()) {
                        map[y + 1][x + 1] = new BlockData(4, x + 1, y + 1, 4, amount[4]++);
                    }
                    if (randomBool()) {
                        map[y + 1][x - 1] = new BlockData(4, x - 1, y + 1, 4, amount[4]++);
                    }
                    if (randomBool()) {
                        map[y - 1][x + 1] = new BlockData(4, x + 1, y - 1, 4, amount[4]++);
                    }
                    if (randomBool()) {
                        map[y - 1][x - 1] = new BlockData(4, x - 1, y - 1, 4, amount[4]++);
                    }
                    map[y + 1][x] = new BlockData(4, x, y + 1, 4, amount[4]++);
                    map[y - 1][x] = new BlockData(4, x, y - 1, 4, amount[4]++);
                    map[y][x + 1] = new BlockData(4, x + 1, y, 4, amount[4]++);
                    map[y][x - 1] = new BlockData(4, x - 1, y, 4, amount[4]++);
                }
                id = 4;
            } else {
                id = randomInt(0, 2);
            }
            int dur = 3;
            // System.out.println(x + "" + y + "" + id);
            // put block down using block data with those random numbers

            if (map[y][x] == null) {
                map[y][x] = new BlockData(id, x, y, dur, amount[id]++);
            }
        }

        return map;
    }

    // generate the caves
    public static BlockData[][][] genCaves(BlockData[][][] cave) {
        for (int n = 0; n < amount[3]; n++) {
            // for each cave apply the template

            // cave[n] = caveTemplate;

            // TODO issue seems like each interaction to the caves affect all other caves

            /*
            int x = randomInt(1, 8);
            int y = randomInt(1, 8);
            cave[n][y][x] = new BlockData(4, x, y, 5, amount[4]++);
            */
        }

        return cave;
    }

    // general purpose methods

    // get a random int 
    public static int randomInt(int min, int max) {
        Random random = new Random();
        int rndInt = (random.nextInt((max - min))) + min;
        return rndInt;
    }

    public static boolean randomBool() {
        if (randomInt(0, 2) == 1) {
            return true;
        } else {
            return false;
        }
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
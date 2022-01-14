package MainFiles;

import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ReadOnlyBufferException;

/**
 *
 *  Runs best in VSCode, netbeans has a slow console
 *  
 *  I did play around with drawing and had graphics finished 
 *  ( no screenshot you'll have to take my word it was quite cool) 
 *  I didn't like how the game looked and felt at that point
 * 
 * @author Ethan Huber
 */
public class CavedMain {

    /**
     * @param args the command line arguments
     */
    static Scanner input = new Scanner(System.in);

    // -- Dev Note --
    // i am aware of the un-used block found on lines 24-28, i removed barrier blocks used for a deprecated feature
    // instead of removing the block and changing the id of some other blocks, i kept it for compatability
    // --------------
    // map related variables
    static String[] matchingTools = { "AX", "SH", "AX", null, "PI", "AX" };
    static String[] blockCharacters = { "W", "D", "C", "V", "S", "B" };
    static int[] blockAmount = { 0, 0, 0, 0, 0, 0 };
    static String[] blockColors = { "\u001B[33m", "\u001B[33m", "\u001B[34m", "\u001B[37m", "\u001B[37m",
            "\u001B[30m" };
    static Boolean[] breakableBlocks = { true, true, true, false, true, true };
    static String resetColor = "\u001B[0m";
    static String playerColor = "\u001B[34m";
    static String healthColor = "\u001B[31m";
    static String mobColor = "\u001B[31m";

    static int tempChunkX;
    static int tempChunkY;

    static int size;

    static BlockData[][] map;
    static MobData[] mobs;

    // inventory related variables
    static String[] toolBar = { "  ", "  ", "  " };
    static int[] inventory = { 0, 0, 0, 0, 0, 0 };
    static int inventorySelectorPos = 0;

    // player related variables
    static int playerHp = 3;

    static int playerX;
    static int playerY;

    static int chunkX;
    static int chunkY;

    static int spawnX;
    static int spawnY;

    // general game variables
    static boolean run = true;

    public static void main(String[] args) {
        setup();
        while (run) {
            moveMobs();
            printTopUI();
            printChunk(map, "\u001B[32m", chunkX, chunkY);
            printBottomUI();
            userInput();
            checkPlayerHealth();
        }
        input.close();
    }

    public static void setup() {
        size = inputInt("Enter desired map size, size will work best if multiple of 9");
        message("Generating...");
        playerX = centerPlayer(size);
        playerY = centerPlayer(size);
        chunkX = posToChunk(centerPlayer(size));
        chunkY = posToChunk(centerPlayer(size));
        spawnX = playerX;
        spawnY = playerY;
        map = new BlockData[size][size];
        mobs = new MobData[size];
        map = genMap(size);
        genMobs(size);
        System.out.println("Type 'help' for help menu");
    }

    public static void checkPlayerHealth() {
        if (playerHp <= 0) {
            System.out.println("You died.");

            playerX = spawnX;
            playerY = spawnY;

            chunkX = posToChunk(spawnX);
            chunkY = posToChunk(spawnY);
            for (int i = 0; i < inventory.length; i++) {
                inventory[i] = inventory[i] / 5;
            }
            playerHp = 3;
        }
    }

    public static void moveMobs() {
        // removes any dead monsters
        for (int i = 0; i < mobs.length; i++) {
            if (mobs[i] != null) {
                if (mobs[i].health <= 0) {
                    mobs[i] = null;
                }
            }
        }
        // moves mobs while no block is in the way
        for (int i = 0; i < mobs.length; i++) {
            if (mobs[i] != null) {
                // moves mobs with random chance (1 in 4) while checking which direction it should move in
                if (randomBool(0, 4) && mobs[i].x > playerX && map[mobs[i].y][mobs[i].x - 1] == null) {
                    if ((mobs[i].x - 1) - playerX >= 1) {
                        mobs[i].x--;
                    }
                }
                if (randomBool(0, 4) && mobs[i].x < playerX && map[mobs[i].y][mobs[i].x + 1] == null) {
                    if ((mobs[i].x + 1) - playerX <= -1) {
                        mobs[i].x++;
                    }
                }
                if (randomBool(0, 4) && mobs[i].y < playerY && map[mobs[i].y + 1][mobs[i].x] == null) {
                    if ((mobs[i].y + 1) - playerY <= -1) {
                        mobs[i].y++;
                    }
                }
                if (randomBool(0, 4) && mobs[i].y > playerY && map[mobs[i].y - 1][mobs[i].x] == null) {
                    if ((mobs[i].y - 1) - playerY >= 1) {
                        mobs[i].y--;
                    }
                }

                // damage player if in a 1 wide boundry around the monster
                // System.out.println((mobs[i].x - playerX) + " " + (mobs[i].y - playerY));
                if (randomBool(0, 4) && mobs[i].x - playerX >= -1 && mobs[i].x - playerX <= 1
                        && mobs[i].y - playerY <= 1
                        && mobs[i].y - playerY >= -1) {
                    playerHp--;
                }

            }
        }
    }

    // place mobs around the map
    public static void genMobs(int size) {
        for (int i = 0; i < mobs.length; i++) {
            if (mobs[i] == null) {
                mobs[i] = new MobData(2, randomInt(0, size), randomInt(0, size));
            }
            // System.out.println(mobs[y][x].x + " " + mobs[y][x].y);
        }
    }

    // get any possible user input
    public static void userInput() {
        String instruction;
        instruction = inputString("");
        instruction = instruction.toUpperCase();

        // interacting / crafting
        if (instruction.charAt(0) == 'R') {
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
                if (inventory[0] >= 4) {
                    inventory[2]++;
                    System.out.println("Crafting bench crafted");
                    inventory[0] = inventory[0] - 4;
                } else {
                    System.out.println("Not enough wood to make crafting bench");
                }
            } else if (subString.equals("")) {
                interact(0, 0);
            }
        }

        // movement
        if (instruction.equals("W")) {
            move(0, -1, 0);
        } else if (instruction.equals("A")) {
            move(-1, 0, 0);
        } else if (instruction.equals("S")) {
            move(0, 1, 0);
        } else if (instruction.equals("D")) {
            move(1, 0, 0);
        }

        // mining
        if (instruction.charAt(0) == 'M' && !instruction.equals("MAP")) {
            String suffix = instruction.substring(1, instruction.length());
            if (suffix.equals("R")) {
                mineBlock(1, 0);
            } else if (suffix.equals("L")) {
                mineBlock(-1, 0);
            } else if (suffix.equals("U")) {
                mineBlock(0, -1);
            } else if (suffix.equals("D")) {
                mineBlock(0, 1);
            } else {
                mineBlock(0, 0);
            }
        }

        // placing
        if (instruction.charAt(0) == 'P') {
            String suffix = instruction.substring(1, instruction.length());
            if (suffix.equals("R")) {
                placeBlock(1, 0);
            } else if (suffix.equals("L")) {
                placeBlock(-1, 0);
            } else if (suffix.equals("U")) {
                placeBlock(0, -1);
            } else if (suffix.equals("D")) {
                placeBlock(0, 1);
            } else {
                placeBlock(0, 0);
            }
        }

        // killing

        if (instruction.charAt(0) == 'K') {
            String suffix = instruction.substring(1, instruction.length());
            if (suffix.equals("R")) {
                damage(1, 0);
            } else if (suffix.equals("U")) {
                damage(0, -1);
            } else if (suffix.equals("D")) {
                damage(0, 1);
            } else if (suffix.equals("L")) {
                damage(-1, 0);
            } else if (suffix.equals("UR")) {
                damage(1, -1);
            } else if (suffix.equals("DR")) {
                damage(1, 1);
            } else if (suffix.equals("DL")) {
                damage(-1, -1);
            } else if (suffix.equals("UL")) {
                damage(-1, 1);
            } else {
                damage(0, 0);
            }
        }

        // inv checker
        if (instruction.equals("INV")) {
            for (int i = 0; i < inventory.length; i++) {
                System.out.println(inventory[i] + "" + blockCharacters[i] + " ");
            }
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
            for (int i = 0; i < mobs.length; i++) {
                mobs[i] = null;
            }
            genMobs(size);
            genMap(size);
        }

        // help menu
        if (instruction.equals("HELP")) {
            System.out.println("");
            System.out.println("--- Help Menu ---");
            System.out.println("Movement - W Up, A Left, S Down, D Right");
            System.out.println("Scrolling of inventory - E Right, Q Left");
            System.out.println("Mining - M Standalone for location of player or combined with the directional keys");
            System.out.println("Placing - P Standalone for location of player or combined with the directional keys");
            System.out
                    .println("Interacting - R Standalone for location of player or combined with the directional keys");
            System.out.println("Directional Keys - U for up 1, R for right 1, L for left 1, D for down 1");
            System.out.println(
                    "Crafting - Type 'Ri' to craft a crafting bench, 4 wood required. Otherwise interact with a crafting bench with E +/ Directional Keys");
            System.out.println("Map view - 'Map' to view map");
            System.out.println("Block atlas, P Player, S Stone, W Wood, D Dirt, M Monster, C Crafting Bench");
            System.out.println("Regeneration - 'Regen' to regenerate the map");
            System.out.println("Block distribution - 'Sum' to view amounts of each block on the map");
            System.out.println("End game - 'Stop' or 'Exit' to exit / stop game");
            System.out.println("Press 'X to go back to spawn/bed");
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
            if (inventorySelectorPos > 0) {
                inventorySelectorPos--;
            }
        } else if (instruction.equals("E")) {
            if (inventorySelectorPos < inventory.length - 2) {
                inventorySelectorPos++;
            }
        }

        // teleport to spawn
        if (instruction.equals("X")) {
            if (map[spawnY][spawnX] != null) {
                if (map[spawnY][spawnX].id == 5) {
                    playerX = spawnX;
                    chunkX = posToChunk(spawnX);
                    playerY = spawnY;
                    chunkY = posToChunk(spawnY);
                }
            } else {
                System.out.println("Spawnpoint missing");
                playerX = centerPlayer(size);
                playerY = centerPlayer(size);
                chunkX = posToChunk(centerPlayer(size));
                chunkY = posToChunk(centerPlayer(size));
            }

        }

        // save / load
        if (instruction.equals("SAVE") || instruction.equals("WRITE")) {
            String fileName = inputString("Enter desired name of save");
            System.out.println("Saving...");
            saveMap(map, fileName + ".map");
            saveMobs(mobs, fileName + ".mobs");
        }
        if (instruction.equals("LOAD") || instruction.equals("READ")) {
            String fileName = inputString("Enter desired name to read from");
            System.out.println("Loading...");
            readMap(fileName + ".map");
            readMobs(fileName + ".mobs");
        }
    }

    // test if a mob is in a location
    public static boolean tryMob(int x, int y) {
        for (int i = 0; i < mobs.length; i++) {
            if (mobs[i] != null) {
                if (mobs[i].x == x && mobs[i].y == y) {
                    return true;
                }
            }
        }
        return false;
    }

    // interaction with blocks
    public static void interact(int x, int y) {
        if (map[playerY + y][playerX + x] != null) {
            if (map[playerY + y][playerX + x].id == 2) {
                // crafting
                // the item to the offset space is a crafting bench
                System.out.println("What item do you want to craft?");
                System.out.println("1. Dirt Cake 'dirt', 1 dirt");
                System.out.println("2. Axe 'axe', 2 wood");
                System.out.println("3. Pickaxe 'pickaxe', 3 wood");
                System.out.println("4. Shovel 'shovel', 1 wood");
                System.out.println("5. Bed 'bed', 5 wood");
                String item = inputString("Type name of item in quotation marks to craft");
                item = item.toUpperCase();
                if (item.equals("DIRT")) {
                    if (inventory[1] >= 1) {
                        playerHp++;
                        System.out.println("A dirt cake was crafted and consumed");
                        inventory[1]--;
                    } else {
                        System.out.println("Not enough dirt to make dirt cake");
                    }
                } else if (item.equals("AXE")) {
                    if (inventory[0] >= 2) {
                        toolBar[0] = "AX";
                        System.out.println("An axe was crafted");
                        inventory[0] = inventory[0] - 2;
                    } else {
                        System.out.println("Not enough wood to make an axe");
                    }
                } else if (item.equals("PICKAXE")) {
                    if (inventory[0] >= 3) {
                        toolBar[1] = "PI";
                        System.out.println("A pickaxe was crafted");
                        inventory[0] = inventory[0] - 3;
                    } else {
                        System.out.println("Not enough wood to make a pickaxe");
                    }
                } else if (item.equals("SHOVEL")) {
                    if (inventory[0] >= 1) {
                        toolBar[2] = "SH";
                        System.out.println("A shovel was crafted");
                        inventory[0] = inventory[0] - 2;
                    } else {
                        System.out.println("Not enough wood to make a shovel");
                    }
                } else if (item.equals("BED")) {
                    if (inventory[0] >= 5) {
                        inventory[5]++;
                        System.out.println("A bed was crafted");
                        inventory[0] = inventory[0] - 5;
                    }
                } else {
                    System.out.println(item + "could not be crafted.");
                }
            } else if (map[playerY + y][playerX + x].id == 5) {
                // interacting with bed
                spawnX = map[playerY + y][playerX + x].x;
                spawnY = map[playerY + y][playerX + x].y;
                System.out.println("Spawn point set.");
            }
        } else {
            System.out.println("No interactable here");
        }
    }

    // placing of blocks
    public static void placeBlock(int x, int y) {
        System.out.println("What block do you want to place?");
        System.out.println("1. For wood enter 0, Amount: " + inventory[0]);
        System.out.println("2. For dirt enter 1, Amount: " + inventory[1]);
        System.out.println("3. For a crafting bench enter 2, Amount: " + inventory[2]);
        System.out.println("4. For stone enter 4, Amount: " + inventory[4]);
        System.out.println("5. For a bed enter 5, Amount: " + inventory[5]);

        int block = inputInt("");

        // if block to the offset doesnt exist then place block
        if (!checkBlock(playerX + x, playerY + y)) {
            // if enough blocks in inv exists
            if (inventory[block] > 0) {
                // remove block
                inventory[block]--;
                map[playerY + y][playerX + x] = new BlockData(block, playerX + x, playerY + y, 3, blockAmount[block]++);
            } else {
                System.out.println("Not enough blocks of that type");
            }
        }
    }

    // check if a tool is present in inventory
    public static boolean checkTool(int id) {
        for (int i = 0; i < toolBar.length; i++) {
            if (matchingTools[id].equals(toolBar[i])) {
                return true;
            }
        }
        return false;
    }

    // mine blocks
    public static void mineBlock(int x, int y) {
        if (map[playerY + y][playerX + x] != null) {

            int id = map[playerY + y][playerX + x].id;
            Boolean correctTool = checkTool(id);
            int dmg;
            if (correctTool) {
                dmg = 4;
            } else {
                dmg = 1;
            }
            // if block to the offset exists mine it

            if (checkBlock(playerX + x, playerY + y)) {
                if (map[playerY + y][playerX + x].dur >= 1) {
                    map[playerY + y][playerX + x].dur = map[playerY + y][playerX + x].dur - dmg;
                }
                if (map[playerY + y][playerX + x].dur <= 0) {
                    // add block id to the right to inventory
                    inventory[map[playerY + y][playerX + x].id]++;
                    // set id of block to the right to null
                    map[playerY + y][playerX + x] = null;
                }
            } else {
                System.out.println("This block is not breakable.");
            }
        }
    }

    // check if a block in a given location exists
    public static boolean checkBlock(int x, int y) {
        if (map[y][x] != null) {
            return true;
        }
        return false;
    }

    // user input methods
    public static void move(int x, int y, int borderOffset) {
        // subtract offset to high values
        // add offset to low values
        int highX = (chunkX + 1) * 9 - 1 - borderOffset;
        int lowX = (chunkX + 1) * 9 - 9 + borderOffset;

        int highY = (chunkY + 1) * 9 - 1 - borderOffset;
        int lowY = (chunkY + 1) * 9 - 9 + borderOffset;

        if (playerX + x >= lowX && playerX + x <= highX) {
            // if within bounds of chunk move normally
            playerX = playerX + x;
        } else if (playerX == lowX || playerX == highX) {
            // if at edge of chunk check if not at edge of map
            // if not at map offset chunk and player
            if (chunkX + x >= 0 && chunkX + x <= (size / 9) - 1) {
                chunkX = chunkX + x;
                playerX = playerX + x;
            }

        }

        if (playerY + y <= highY && playerY + y >= lowY) {
            // if within bounds of chunk move normally
            playerY = playerY + y;
        } else if (playerY == lowY || playerY == highY) {
            // if at edge of chunk check if not at edge of map
            // if not at map offset chunk and player
            if (chunkY + y <= (size / 9) - 1 && chunkY + y >= 0) {
                chunkY = chunkY + y;
                playerY = playerY + y;
            }
        }

    }

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

    public static String message(String message) {
        System.out.println(message);
        return "06/07/20";
    }

    public static void saveMap(BlockData[][] mapData, String name) {
        // create a new empty file with given file name
        File save = new File(name);

        try {
            // try to save the given map to the file in zmf format
            save.createNewFile();

            FileWriter writer = new FileWriter(save);

            for (int y = 0; y < mapData.length; y++) {
                for (int x = 0; x < mapData[y].length; x++) {
                    if (mapData[y][x] != null) {
                        writer.write(
                                mapData[y][x].id + "," + mapData[y][x].x + "," + mapData[y][x].y + " ");
                    } else {
                        writer.write("e ");
                    }

                }
                writer.write("\n");
            }
            writer.flush();
            writer.close();
            // error destruction
        } catch (IOException e) {
        }
    }

    public static void saveMobs(MobData[] mobData, String name) {
        // create a new empty file with given file name
        File save = new File(name);

        try {
            // try to save the given map to the file in zmf format
            save.createNewFile();

            FileWriter writer = new FileWriter(save);

            for (int i = 0; i < mobData.length; i++) {
                if (mobData[i] != null) {
                    writer.write(
                            mobData[i].health + "," + mobData[i].x + "," + mobData[i].y + " ");
                } else {
                    writer.write("e ");
                }

                writer.write("\n");
            }
            writer.flush();
            writer.close();
            // error destruction
        } catch (IOException a) {
        } catch (NullPointerException b) {

        }
    }

    public static void readMap(String name) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                map[y][x] = null;
            }
        }
        try {
            // creates reader
            FileReader reader = new FileReader(name);

            File file = new File(name);
            // creates character array of text file with text file length
            char[] c = new char[(int) file.length()];

            // read file to character array and convert to string
            reader.read(c);
            String a = new String(c);

            // split file at spaces to isolate blocks
            String spl[] = a.split(" ");

            // remove any empty values in map
            for (int i = 0; i < spl.length; i++) {
                if (spl[i].contains("e") || spl[i].isEmpty()) {
                    spl[i] = null;
                }
            }

            // search through map, while ignoring empty blocks
            for (int i = 0; i < spl.length; i++) {
                if (!(spl[i] == null)) {
                    // split blocks with values between commas
                    String mod[] = spl[i].split(",");
                    // send each number into their corrosponding spot in a new block
                    int id = Integer.parseInt(mod[0].trim());
                    int x = Integer.parseInt(mod[1].trim());
                    int y = Integer.parseInt(mod[2].trim());
                    // set block to corrosponding position in the readMap
                    BlockData block = new BlockData(id, x, y, 2, blockAmount[id]--);
                    map[y][x] = block;
                }
            }

            reader.close();
            // land of error destruction
        } catch (IOException a) {
        } catch (NullPointerException b) {
        } catch (ReadOnlyBufferException c) {
        } catch (NumberFormatException d) {
        }

        // return the read map
    }

    public static void readMobs(String name) {
        for (int i = 0; i < mobs.length; i++) {
            mobs[i] = null;
        }
        try {
            // creates reader
            FileReader reader = new FileReader(name);

            File file = new File(name);
            // creates character array of text file with text file length
            char[] c = new char[(int) file.length()];

            // read file to character array and convert to string
            reader.read(c);
            String a = new String(c);

            // split file at spaces to isolate blocks
            String spl[] = a.split(" ");

            // remove any empty values in map
            for (int i = 0; i < spl.length; i++) {
                if (spl[i].contains("e") || spl[i].isEmpty()) {
                    spl[i] = null;
                }
            }

            // search through map, while ignoring empty blocks
            for (int i = 0; i < spl.length; i++) {
                if (!(spl[i] == null)) {
                    // split blocks with values between commas
                    String mod[] = spl[i].split(",");
                    // send each number into their corrosponding spot in a new block
                    int health = Integer.parseInt(mod[0].trim());
                    int x = Integer.parseInt(mod[1].trim());
                    int y = Integer.parseInt(mod[2].trim());
                    // set block to corrosponding position in the readMap
                    MobData mob = new MobData(health, x, y);
                    mobs[i] = mob;
                }
            }

            reader.close();
            // land of error destruction
        } catch (IOException a) {
        } catch (NullPointerException b) {
        } catch (ReadOnlyBufferException c) {
        } catch (NumberFormatException d) {
        }

        // return the read map
    }

    // print whole map
    public static void printMap(BlockData[][] map) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] != null) {
                    System.out.print(blockColors[map[y][x].id] + blockCharacters[map[y][x].id] + " " + resetColor);
                } else if (y == playerY && x == playerX) {
                    System.out.print(playerColor + "P " + resetColor);
                } else if (tryMob(x, y)) {
                    System.out.print(mobColor + "M " + resetColor);
                } else {
                    System.out.print("\u001B[32m" + "O " + resetColor);
                }

            }

            System.out.println("");
        }
    }

    // print current chunk
    public static void printChunk(BlockData[][] chunk, String backgroundColor, int chunkX, int chunkY) {
        try {
            for (int y = (((chunkY + 1) * 9) - 9); y <= (((chunkY + 1) * 9) - 1); y++) {
                System.out.print("[|      ");
                for (int x = (((chunkX + 1) * 9) - 9); x <= (((chunkX + 1) * 9) - 1); x++) {
                    if (chunk[y][x] != null) {
                        System.out.print(
                                blockColors[chunk[y][x].id] + blockCharacters[chunk[y][x].id] + " " + resetColor);
                    } else if (y == playerY && x == playerX) {
                        System.out.print(playerColor + "P " + resetColor);
                    } else if (tryMob(x, y)) {
                        System.out.print(mobColor + "M " + resetColor);
                    } else {

                        System.out.print(backgroundColor + "O " + resetColor);
                    }
                }
                System.out.print("     |]");
                System.out.println("");
            }
        } catch (NullPointerException a) {
            a.printStackTrace();
        }

    }

    // damage mob if at given position of player
    public static void damage(int x, int y) {
        for (int i = 0; i < mobs.length; i++) {
            if (mobs[i] != null) {
                if (playerX + x == mobs[i].x && playerY + y == mobs[i].y) {
                    if (checkTool(0)) {
                        mobs[i].health = 0;
                    } else {
                        mobs[i].health--;
                        System.out.println("That mob has " + mobs[i].health + "hp left");
                    }
                }
            }
        }
    }

    // print top section of user interface
    public static void printTopUI() {
        // prints health tag

        System.out.print("[HEA:]");

        // prints full hearts
        for (int i = 0; i < playerHp && i < 3; i++) {
            System.out.print("[" + healthColor + "<3" + resetColor + "]");
        }

        if (playerHp < 0) {
            playerHp = 0;
        }
        // prints empty hearts
        if (playerHp < 3) {
            for (int i = 0; i < 3 - playerHp; i++) {
                System.out.print("[<>]");
            }
        }

        String stringPlayerX = playerX + "";
        String stringPlayerY = playerY + "";

        if (stringPlayerX.length() > 3) {
            stringPlayerX = stringPlayerX.substring(stringPlayerX.length() - 2, stringPlayerX.length());
        }

        if (stringPlayerY.length() > 3) {
            stringPlayerY = stringPlayerY.substring(stringPlayerY.length() - 2, stringPlayerY.length());
        }

        char[] splitStringPlayerX = stringPlayerX.toCharArray();
        char[] splitStringPlayerY = stringPlayerY.toCharArray();

        for (int i = 0; i < 3 - splitStringPlayerX.length; i++) {
            stringPlayerX = '0' + stringPlayerX;
        }

        for (int i = 0; i < 3 - splitStringPlayerY.length; i++) {
            stringPlayerY = '0' + stringPlayerY;
        }

        System.out.println("[LOC:][" + stringPlayerX + "," + stringPlayerY + "]");
        System.out.println("[+----- 1 2 3 4 5 6 7 8 9 -----+]");
        System.out.print("[|                             |]");
        System.out.println("");
    }

    // print botton section of user interface
    public static void printBottomUI() {
        System.out.println("[|                             |]");
        System.out.println("[+----- 1 2 3 4 5 6 7 8 9 -----+]");
        // prints toolbar and itembar
        System.out.print(
                "[TO:]" + "[" + toolBar[0] + "]" + "[" + toolBar[1] + "]" + "[" + toolBar[2] + "]" + "[ITE:]" + "[");

        String amt0 = inventory[inventorySelectorPos] + "";
        String amt1 = inventory[inventorySelectorPos + 1] + "";

        if (inventory[0] < 10) {
            System.out.print("0" + amt0 + blockCharacters[inventorySelectorPos]);
        } else if (inventory[0] >= 10) {
            amt0 = amt0.substring(amt0.length() - 2, amt0.length());
            System.out.print(amt0 + blockCharacters[inventorySelectorPos]);
        }
        System.out.print("][");
        if (inventory[1] < 10) {
            System.out.print("0" + amt1 + blockCharacters[inventorySelectorPos + 1]);
        } else if (inventory[1] >= 10) {
            amt1 = amt1.substring(amt1.length() - 2, amt1.length());
            System.out.print(amt1 + blockCharacters[inventorySelectorPos + 1]);
        }
        System.out.print("]");
        System.out.println("");
    }

    // print map distribution
    public static void printAmount() {
        for (int i = 0; i < blockAmount.length; i++) {
            System.out.print(blockAmount[i] + ", ");
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
                    if (randomBool(0, 2)) {
                        map[y + 1][x + 1] = new BlockData(4, x + 1, y + 1, 4, blockAmount[4]++);
                    }
                    if (randomBool(0, 2)) {
                        map[y + 1][x - 1] = new BlockData(4, x - 1, y + 1, 4, blockAmount[4]++);
                    }
                    if (randomBool(0, 2)) {
                        map[y - 1][x + 1] = new BlockData(4, x + 1, y - 1, 4, blockAmount[4]++);
                    }
                    if (randomBool(0, 2)) {
                        map[y - 1][x - 1] = new BlockData(4, x - 1, y - 1, 4, blockAmount[4]++);
                    }
                    map[y + 1][x] = new BlockData(4, x, y + 1, 4, blockAmount[4]++);
                    map[y - 1][x] = new BlockData(4, x, y - 1, 4, blockAmount[4]++);
                    map[y][x + 1] = new BlockData(4, x + 1, y, 4, blockAmount[4]++);
                    map[y][x - 1] = new BlockData(4, x - 1, y, 4, blockAmount[4]++);
                }
                id = 4;
            } else {
                id = randomInt(0, 2);
            }
            int dur = 3;
            // System.out.println(x + "" + y + "" + id);
            // put block down using block data with those random numbers

            if (map[y][x] == null) {
                map[y][x] = new BlockData(id, x, y, dur, blockAmount[id]++);
            }
        }

        return map;
    }

    // get a random int 
    public static int randomInt(int min, int max) {
        Random random = new Random();
        int rndInt = (random.nextInt((max - min))) + min;
        return rndInt;
    }

    public static boolean randomBool(int min, int max) {
        if (randomInt(min, max) == 1) {
            return true;
        } else {
            return false;
        }
    }

    // input a message, get back an int
    public static int inputInt(String message) {
        int num = 0;
        if (message != "") {
            System.out.println(message);
        }
        try {
            num = input.nextInt();

        } catch (InputMismatchException a) {
        }
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
package MainFiles;

import java.util.Random;
import java.util.Scanner;

import java.nio.ReadOnlyBufferException;
import java.io.*;

/**
 *
 * @author Ethan
 */
public class CavedMain {

    /*
        - Make world save & load
            - save to editable file
            - save to save to to file located in documents with name specified
            - load to load that file located in documents with name specified
    
        Short form version
        - Save / Loading
        - GUI?
     */

    /**
     * @param args the command line arguments
     */
    static Scanner input = new Scanner(System.in);

    // map related variables
    static String[] ct = { "AX", "SH", "AX", null, "PI", "AX" };
    static String[] chars = { "W", "D", "C", "V", "S", "B" };
    static int[] amount = { 0, 0, 0, 0, 0, 0 };
    static String[] charColors = { "\u001B[33m", "\u001B[33m", "\u001B[34m", "\u001B[37m", "\u001B[37m", "\u001B[30m" };
    static Boolean[] breakable = { true, true, true, false, true, true };
    static String reset = "\u001B[0m";
    static String playerC = "\u001B[34m";
    static String health = "\u001B[31m";

    static String mobC = "\u001B[31m";

    static int tempChunkX;
    static int tempChunkY;

    static int playerHp = 3;
    // inventory player related variables
    static String[] tb = { "  ", "  ", "  " };
    static int[] inv = { 99, 0, 0, 0, 0, 0 };
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

    static int spawnX = playerX;
    static int spawnY = playerY;

    static MobData[] mobs = new MobData[size];

    public static BlockData[][] read(String name) {
        BlockData[][] readMap = new BlockData[size][size];
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
                    BlockData block = new BlockData(id, x, y, 2, amount[id]--);
                    readMap[y][x] = block;
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
        return readMap;
    }

    public static void save(BlockData[][] mapData, String name) {
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
        genMobs(size);
        System.out.println("Type 'help' for help menu");

        // save(map, "map.emf");
        // printChunk(read("map.emf"), "\u001B[32m", chunkX, chunkY);
        while (run) {
            moveMobs();
            topUI();
            printChunk(map, "\u001B[32m", chunkX, chunkY);
            bottomUI();
            map = userInput(map);
            checkPlayerHealth();
        }
        input.close();
    }

    public static void checkPlayerHealth() {
        if (playerHp <= 0) {
            System.out.println("You died.");

            playerX = spawnX;
            playerY = spawnY;

            chunkX = posToChunk(spawnX);
            chunkY = posToChunk(spawnY);
            for (int i = 0; i < inv.length; i++) {
                inv[i] = inv[i] / 5;
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
    public static BlockData[][] userInput(BlockData[][] map) {
        String instruction;
        instruction = inputString("");
        instruction = instruction.toUpperCase();

        // interacting / crafting
        if (instruction.charAt(0) == 'R') {
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
        if (instruction.charAt(0) == 'M') {
            String suffix = instruction.substring(1, instruction.length());
            if (suffix.equals("R")) {
                map = mineBlock(1, 0, map);
            } else if (suffix.equals("L")) {
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
            for (int i = 0; i < inv.length; i++) {
                System.out.println(inv[i] + "" + chars[i] + " ");
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
            save(map, fileName + ".emf");
        }
        if (instruction.equals("LOAD") || instruction.equals("READ")) {
            String fileName = inputString("Enter desired name to read from");
            System.out.println("Loading...");
            map = read(fileName + ".emf");
        }

        return map;
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
    public static BlockData[][] interact(int x, int y, BlockData[][] map) {
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
                } else if (item.equals("BED")) {
                    if (inv[0] >= 5) {
                        inv[5]++;
                        System.out.println("A bed was crafted");
                        inv[0] = inv[0] - 5;
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

        return map;
    }

    // placing of blocks
    public static BlockData[][] placeBlock(int x, int y, BlockData[][] map) {
        System.out.println("What block do you want to place?");
        System.out.println("1. For wood enter 0, Amount: " + inv[0]);
        System.out.println("2. For dirt enter 1, Amount: " + inv[1]);
        System.out.println("3. For a crafting bench enter 2, Amount: " + inv[2]);
        System.out.println("4. For stone enter 4, Amount: " + inv[4]);
        System.out.println("5. For a bed enter 5, Amount: " + inv[5]);

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

    // check if a tool is present in inventory
    public static boolean checkTool(int id) {
        for (int i = 0; i < tb.length; i++) {
            if (ct[id].equals(tb[i])) {
                return true;
            }
        }
        return false;
    }

    // mine blocks
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

    // check if a block in a given location exists
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

    // map and user interface methods

    // print whole map
    public static void printMap(BlockData[][] map) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] != null) {
                    System.out.print(charColors[map[y][x].id] + chars[map[y][x].id] + " " + reset);
                } else if (y == playerY && x == playerX) {
                    System.out.print(playerC + "P " + reset);
                } else if (tryMob(x, y)) {
                    System.out.print(mobC + "M " + reset);
                } else {
                    System.out.print("\u001B[32m" + "O " + reset);
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
                        System.out.print(charColors[chunk[y][x].id] + chars[chunk[y][x].id] + " " + reset);
                    } else if (y == playerY && x == playerX) {
                        System.out.print(playerC + "P " + reset);
                    } else if (tryMob(x, y)) {
                        System.out.print(mobC + "M " + reset);
                    } else {

                        System.out.print(backgroundColor + "O " + reset);
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
    public static void topUI() {
        // prints health tag

        System.out.print("[HEA:]");

        // prints full hearts
        for (int i = 0; i < playerHp && i < 3; i++) {
            System.out.print("[" + health + "<3" + reset + "]");
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

        String amt0 = inv[invPos] + "";
        String amt1 = inv[invPos + 1] + "";

        if (inv[0] < 10) {
            System.out.print("0" + amt0 + chars[invPos]);
        } else if (inv[0] >= 10) {
            amt0 = amt0.substring(amt0.length() - 2, amt0.length());
            System.out.print(amt0 + chars[invPos]);
        }
        System.out.print("][");
        if (inv[1] < 10) {
            System.out.print("0" + amt1 + chars[invPos + 1]);
        } else if (inv[1] >= 10) {
            amt1 = amt1.substring(amt1.length() - 2, amt1.length());
            System.out.print(amt1 + chars[invPos + 1]);
        }
        System.out.print("]");
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
                    if (randomBool(0, 2)) {
                        map[y + 1][x + 1] = new BlockData(4, x + 1, y + 1, 4, amount[4]++);
                    }
                    if (randomBool(0, 2)) {
                        map[y + 1][x - 1] = new BlockData(4, x - 1, y + 1, 4, amount[4]++);
                    }
                    if (randomBool(0, 2)) {
                        map[y - 1][x + 1] = new BlockData(4, x + 1, y - 1, 4, amount[4]++);
                    }
                    if (randomBool(0, 2)) {
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

    // general purpose methods

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
package MainFiles;

import java.util.Arrays;
import java.util.Scanner;
import MainFiles.BlockData;

/**
 *
 * @author ethan
 */
public class CavedMainOld {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        /* TODO
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
            - sometimes when mining one of the last blocks in chunk array it doesn't dissapear the first time
         */
        Scanner input_Int = new Scanner(System.in);
        Scanner input_String = new Scanner(System.in);

        int mapSize = 9;

        // sets default location
        int playerX = 1;
        int playerY = 1;

        int playerHealth = 1;

        // sets character and map data
        String[] chars = { "", "W", "D", "C" };
        String[] charColors = { "", "\u001B[33m", "\u001B[33m", "\u001B[34m" };

        String reset = "\u001B[0m";
        String player = "\u001B[34m";

        // new map data \/
        int[][][] cd = { { { 188, 233, 243, 184, 182, 355, 295 }, { 121, 131 }, { 0 } }, { { 111, 121 }, { 0 }, { 0 } },
                { { 0 }, { 0 }, { 0 } } };

        BlockData chunks[][][] = new BlockData[3][3][81];

        // whole map gen should go in BlockData
        // option to load random map or load from file
        // init goes in CavedMain
        // chunks[0][0][0].id);
        //        r  c  b
        // r = row c[1]
        // c = collum c[0]
        // b = block i
        int[] c = { 0, 0 };

        int lowX;
        int lowY;
        int highX;
        int highY;

        String[] tb = { "  ", "  ", "  " };
        int[] inv = { 0, 0, 0 };

        int invPos = 0;

        int in = 0;

        int bI = cd[c[1]][c[0]][in] / 100;
        int bX = cd[c[1]][c[0]][in] / 10 - (cd[c[1]][c[0]][in] / 100) * 10;
        int bY = cd[c[1]][c[0]][in] - cd[c[1]][c[0]][in] / 10 * 10;

        boolean stopState;

        String instruction;

        boolean run = true;

        // map generation
        //        Make world gen
        //    - 1 in 9 chance of dirt spawn
        //        out of 9 random generated numbers one should match 9
        //    - 1 in 27 chance of wood spawn
        //      out of 27 random generated numbers one should match 27
        //    - 1 in 81 chance of cave spawn
        //      out of 81 random generated numbers one should match 81
        //      ids follow as: 1, 2, 3
        // i is row 
        for (int y = 0; y < 3; y++) {
            // j is collum
            for (int x = 0; x < 3; x++) {
                // blocks within chunk
                int i = 0;
                for (int b = 0; b < 81; b++) {
                    int randX = (int) Math.floor(Math.random() * 9);
                    int randY = (int) Math.floor(Math.random() * 9);
                    int dur = 2;
                    //
                    int chance = (int) Math.floor(Math.random() * 100);
                    if (chance % 9 == 0) {
                        // dirt
                        chunks[y][x][i] = new BlockData(1, randX, randY, dur);
                        i++;
                    } else if (chance % 7 == 0) {
                        // wood
                        chunks[y][x][i] = new BlockData(2, randX, randY, dur);
                        i++;
                    } else if (chance % 12 == 0) {
                        // crafting table
                        chunks[y][x][i] = new BlockData(3, randY, randY, dur);
                        i++;
                    }
                }
            }
        }

        for (int y = 1; y <= 9; y++) {
            for (int x = 1; x <= 9; x++) {
                in = 0;
                stopState = false;
                while (stopState == false && in < chunks[0][0].length) {
                    // extracts map data
                    if (chunks[0][0][in] != null) {
                        bI = chunks[0][0][in].id;
                        bX = chunks[0][0][in].x;
                        bY = chunks[0][0][in].y;
                    }

                    if (bX == x && bY == y) {
                        stopState = true;
                    } else {
                        in++;
                    }
                }

                // print block with corrosponding information
                if (bX == x && bY == y) {
                    System.out.print(charColors[bI] + chars[bI] + " " + reset);
                } else if (playerX == x && playerY == y) {
                    System.out.print(player + "P " + reset);
                } else {
                    System.out.print("\u001B[32m" + "O " + reset);
                }
            }
            System.out.println("");
        }

        // master game loop
        while (run) {
            lowX = 9 * (c[0] + 1) - 8;
            lowY = 9 * (c[1] + 1) - 8;
            highX = mapSize * (c[0] + 1);
            highY = mapSize * (c[1] + 1);

            // prints health tag
            System.out.print("[HEA:]");

            // prints full hearts
            for (int i = 0; i < playerHealth; i++) {
                System.out.print("[<3]");
            }

            // prints empty hearts
            if (playerHealth < 3) {
                for (int i = 0; i < 3 - playerHealth; i++) {
                    System.out.print("[<>]");
                }
            }

            // prints location of player and some of the user interface
            System.out.print("[LOC:]");
            System.out.println("[00" + playerX + ",00" + playerY + "]");
            System.out.println("[+----- 1 2 3 4 5 6 7 8 9 -----+]");
            System.out.print("[|                             |]");
            System.out.println("");

            // print mapSize rows
            for (int y = lowY; y <= highY; y++) {

                System.out.print("[" + y + "      ");
                // print mapSize collums
                for (int x = lowX; x <= highX; x++) {

                    in = 0;
                    stopState = false;
                    while (stopState == false && in < cd[c[1]][c[0]].length) {
                        // extracts map data
                        bI = cd[c[1]][c[0]][in] / 100;
                        bX = (cd[c[1]][c[0]][in] / 10 - (cd[c[1]][c[0]][in] / 100) * 10) + (c[0] * 9);
                        bY = (cd[c[1]][c[0]][in] - cd[c[1]][c[0]][in] / 10 * 10) + (c[1] * 9);

                        if (bX == x && bY == y) {
                            stopState = true;
                        } else {
                            in++;
                        }
                    }

                    // print block with corrosponding information
                    if (bX == x && bY == y) {
                        System.out.print(charColors[bI] + chars[bI] + " " + reset);
                    } else if (playerX == x && playerY == y) {
                        System.out.print(player + "P " + reset);
                    } else {
                        System.out.print("\u001B[32m" + "O " + reset);
                    }
                }
                // increase row, reset collum and shift to next line
                System.out.print("     " + y + "]");
                System.out.println("");
            }
            // end of mapSize rows

            System.out.println("[|                             |]");
            System.out.println("[+----- 1 2 3 4 5 6 7 8 9 -----+]");

            // prints toolbar and itembar
            System.out.print("[TO:]" + "[" + tb[0] + "]" + "[" + tb[1] + "]" + "[" + tb[2] + "]" + "[ITE:]" + "[");
            if (inv[0] < 10) {
                System.out.print("0" + inv[invPos] + chars[invPos + 1]);
            }
            System.out.print("][");
            if (inv[1] < 10) {
                System.out.print("0" + inv[invPos + 1] + chars[invPos + 2] + "]");
            }
            System.out.println("");

            // START OF CONTROLS
            //
            // record next instruction
            instruction = input_String.nextLine();

            // change location of player but stops player before it goes off the edge
            if (instruction.equals("d")) {
                // command to move player right
                if (playerX < highX) {
                    playerX++;
                } else if (playerX == highX) {
                    if (c[0] < 2) {
                        c[0]++;
                        playerX++;
                    }
                }
            } else if (instruction.equals("a")) {
                // command to move player left
                if (playerX > lowX) {
                    playerX--;
                } else if (playerX == lowX) {
                    if (c[0] > 0) {
                        c[0]--;
                        playerX--;
                    }
                }
            } else if (instruction.equals("w")) {
                // command to move player up
                if (playerY > lowY) {
                    playerY--;
                } else if (playerY == lowY) {
                    if (c[1] > 0) {
                        c[1]--;
                        playerY--;
                    }
                }
            } else if (instruction.equals("s")) {
                // command to move player down
                if (playerY < highY) {
                    playerY++;
                } else if (playerY == highY) {
                    if (c[1] < 2) {
                        c[1]++;
                        playerY++;
                    }
                }
            } else if (instruction.equals("stop") || instruction.equals("exit")) {
                run = false;
            } else if (instruction.equals("m")) {
                int offset = 0;
                boolean stop = false;
                System.out.println("Which direction? Type r or l only");
                instruction = input_String.nextLine();
                if (instruction.equals("l")) {
                    offset = -1;
                } else if (instruction.equals("r")) {
                    offset = 1;
                } else {
                    System.out.println("This is not a valid input");
                    stop = true;
                }

                while (stop == false) {
                    // mine block to the right
                    for (int index = 0; index < cd[c[1]][c[0]].length; index++) {
                        // search through map data
                        if (cd[c[1]][c[0]][index] - cd[c[1]][c[0]][index] / 100 * 100 == (playerX + offset) * 10
                                + playerY) {
                            // found matching block to the right or left depending on offset

                            // add the matching block to the inventory
                            int type = cd[c[1]][c[0]][index] / 100 - 1;
                            inv[type]++;

                            // removes block from database
                            // if data is at the top change it to zero
                            for (int loop = 0; loop < cd[c[1]][c[0]].length; loop++) {
                                if (index + loop < cd[c[1]][c[0]].length) {
                                    cd[c[1]][c[0]][index] = cd[c[1]][c[0]][index + loop];
                                } else {
                                    cd[c[1]][c[0]][index] = 0;
                                }
                            }

                            stop = true;
                        }
                    }
                }
            } else if (instruction.equals("er")) {
                // use block to the right
                int index = 0;
                boolean stopConditon = false;
                while (index < cd[c[1]][c[0]].length && stopConditon == false) {
                    if (cd[c[1]][c[0]][index] - cd[c[1]][c[0]][index] / 100 * 100 == (playerX + 1) * 10 + playerY) {
                        // if there is a block to the right
                        if (cd[c[1]][c[0]][index] / 100 == 3) {
                            //if block to the right is a crafting bench
                            System.out.println("Type item to craft");
                            String item = input_String.nextLine();
                            if (item.equals("axe")) {
                                if (inv[0] >= 2) {
                                    tb[0] = "AX";
                                    System.out.println("Axe Was Crafted.");
                                    inv[0] = inv[0] - 2;
                                    stopConditon = true;
                                } else {
                                    System.out.println("Not enough wood.");
                                    stopConditon = true;
                                }
                            } else if (item.equals("dirt cake")) {
                                if (inv[1] >= 1) {
                                    playerHealth++;
                                    System.out.println("A Dirt Cake Was Crafted and Consumed.");
                                    inv[1] = inv[1] - 1;
                                    stopConditon = true;
                                } else {
                                    System.out.println("Not enough dirt.");
                                    stopConditon = true;
                                }
                            } else {
                                System.out.println("Invalid Item Code.");
                                stopConditon = true;
                            }
                        } else {
                            System.out.println("Was not crafting bench");
                            stopConditon = true;
                        }
                    }
                    index++;
                }
            } else if (instruction.equals("e")) {
                // incrase invPos by one if not at max scroll
                if (invPos < inv.length - 2) {
                    invPos++;
                    System.out.println(invPos);
                }
            } else if (instruction.equals("q")) {
                // decrease invPos by one if not at max scroll
                if (invPos > 0) {
                    invPos--;
                }
            } else if (instruction.equals("p")) {
                // code to place block

                int offset = 0;
                boolean validInput = true;
                System.out.println("Which direction? Type r or l only");
                instruction = input_String.nextLine();
                if (instruction.equals("l")) {
                    offset = -1;
                } else if (instruction.equals("r")) {
                    offset = 1;
                } else {
                    System.out.println("This is not a valid input");
                    validInput = false;
                }

                while (validInput) {
                    System.out.println("Which block to place?");
                    instruction = input_String.nextLine();
                    if (instruction.equals("dirt")) {
                        if (inv[1] >= 1) {
                            // place block to the right
                            // makes a copy of map data into newArr with an extra value at the end                
                            int[] newArr = Arrays.copyOf(cd[c[1]][c[0]], cd[c[1]][c[0]].length + 1);
                            cd[c[1]][c[0]] = newArr;
                            cd[c[1]][c[0]][cd[c[1]][c[0]].length - 1] = 200 + (playerX + offset) * 10 + playerY * 1;
                            inv[1]--;
                            validInput = false;
                        } else {
                            System.out.println("Not enough dirt");
                            validInput = false;
                        }
                    } else if (instruction.equals("ct")) {
                        if (inv[2] >= 1) {
                            // place block to the right
                            // makes a copy of map data into newArr with an extra value at the end                
                            int[] newArr = Arrays.copyOf(cd[c[1]][c[0]], cd[c[1]][c[0]].length + 1);
                            cd[c[1]][c[0]] = newArr;
                            cd[c[1]][c[0]][cd[c[1]][c[0]].length - 1] = 300 + (playerX + offset) * 10 + playerY * 1;
                            inv[2]--;
                            validInput = false;
                        } else {
                            System.out.println("Not enough crafting tables");
                            validInput = false;
                        }
                    } else if (instruction.equals("wood")) {
                        if (inv[0] >= 1) {
                            // place block to the right
                            // makes a copy of map data into newArr with an extra value at the end                
                            int[] newArr = Arrays.copyOf(cd[c[1]][c[0]], cd[c[0]][c[1]].length + 1);
                            cd[c[1]][c[0]] = newArr;
                            cd[c[1]][c[0]][cd[c[1]][c[0]].length - 1] = 100 + (playerX + offset) * 10 + playerY * 1;
                            inv[0]--;
                            validInput = false;
                        } else {
                            System.out.println("Not enough wood");
                            validInput = false;
                        }
                    }
                }
            } else {
                // prints invalid command when command entered is invalid
                System.out.println("Invalid Command");
            }

            // clears console after every loop but doesn't work in netbeans for unknown reason
            System.out.print("\033[H\033[2J");
            System.out.flush();
            // command to print current chunk data
            // for (int i = 0; i < cd[c[1]][c[0]].length; i++) {
            //     System.out.println(cd[c[1]][c[0]][i]);
            // }
        }
        input_Int.close();
        input_String.close();
    }
}

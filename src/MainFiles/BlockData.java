package MainFiles;

/**
 *
 * @author Ethan
 */
public class BlockData {
    int id;
    int x;
    int y;
    int dur;
    int num;

    // Constructor for blocks
    public BlockData(int identification, int x_coordinate, int y_coordinate, int durability, int number) {
        id = identification;
        x = x_coordinate;
        y = y_coordinate;
        dur = durability;
        num = number;
    }
}
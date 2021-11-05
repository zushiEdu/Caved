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
    
    public BlockData(int identification, int x_coordinate, int y_coordinate, int durability){
        id = identification;
        x = x_coordinate;
        y = y_coordinate;
        dur = durability;
    }
}

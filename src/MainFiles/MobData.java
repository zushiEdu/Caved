package MainFiles;

/**
 *
 * @author Ethan
 */
public class MobData {
    int health;
    int x;
    int y;

    // Constructor for mobs
    public MobData(int hp, int x_coordinate, int y_coordinate) {
        health = hp;
        x = x_coordinate;
        y = y_coordinate;
    }
}
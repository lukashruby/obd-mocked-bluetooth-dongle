
package cz.blahami2.cardashboardadapter;

/**
 *
 * @author Michael Blaha
 */
public class SpeedRpmStruct {
    public final float speed;
    public final float rpm;

    public SpeedRpmStruct( float speed, float rpm ) {
        this.speed = speed * 4;
        this.rpm = rpm;
    }

    @Override
    public String toString() {
        return "SpeedRpmStruct{" +
                "speed=" + speed +
                ", rpm=" + rpm +
                '}';
    }
}

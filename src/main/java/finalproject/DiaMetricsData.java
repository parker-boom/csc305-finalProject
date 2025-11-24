package finalproject;

/**
 * DIA metrics for a single source file.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 2.0
 */
public class DiaMetricsData {
    private final String name;
    private final double abstractness;
    private final double instability;
    private final double distance;
    private final int incoming;
    private final int outgoing;

    public DiaMetricsData(String name, double abstractness, double instability, double distance, int incoming, int outgoing) {
        this.name = name;
        this.abstractness = abstractness;
        this.instability = instability;
        this.distance = distance;
        this.incoming = incoming;
        this.outgoing = outgoing;
    }

    public String getName() {
        return name;
    }

    public double getAbstractness() {
        return abstractness;
    }

    public double getInstability() {
        return instability;
    }

    public double getDistance() {
        return distance;
    }

    public int getIncoming() {
        return incoming;
    }

    public int getOutgoing() {
        return outgoing;
    }

    public String getSimpleName() {
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        String fileName = slash >= 0 ? name.substring(slash + 1) : name;
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(0, dot) : fileName;
    }
}

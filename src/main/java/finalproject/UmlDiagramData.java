package finalproject;

/**
 * PlantUML payload for the diagram view.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 1.0
 */
public class UmlDiagramData {
    private final String plantUmlText;

    public UmlDiagramData(String plantUmlText) {
        this.plantUmlText = plantUmlText;
    }

    public String getPlantUmlText() {
        return plantUmlText;
    }
}

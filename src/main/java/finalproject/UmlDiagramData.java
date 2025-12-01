package finalproject;

/**
 * ROLE: Data.
 * Holds the PlantUML text produced from analysis for rendering the UML diagram.
 * Constructed by UmlBuilder and consumed by DiagramTab for display.
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

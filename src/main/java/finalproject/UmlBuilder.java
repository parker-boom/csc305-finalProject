package finalproject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ROLE: Data/Service.
 * Builds PlantUML text from parsed sources with precedence among implements, extends, and association types.
 * Consumes ParsedSource relationships produced by GitFetch and emits UmlDiagramData for DiagramTab.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 1.0
 */
public class UmlBuilder {

    public UmlDiagramData build(List<ParsedSource> files) {
        StringBuilder builder = new StringBuilder();
        builder.append("@startuml\n");

        // Declare nodes
        for (ParsedSource file : files) {
            if (file.isInterface()) {
                builder.append("interface ").append(file.getClassName()).append("\n");
            } else if (file.isAbstract()) {
                builder.append("abstract class ").append(file.getClassName()).append("\n");
            } else {
                builder.append("class ").append(file.getClassName()).append("\n");
            }
        }

        Set<String> strongest = new HashSet<>();

        // In order of highest precedence to lowest:
        // 1) implements (..|>)
        for (ParsedSource file : files) {
            for (String iface : file.getImplementedInterfaces()) {
                String key = file.getClassName() + "->" + iface;
                strongest.add(key);
                builder.append(file.getClassName()).append(" ..|> ").append(iface).append("\n");
            }
        }

        // 2) extends (--|>)
        for (ParsedSource file : files) {
            if (file.getParentClass() == null) {
                continue;
            }
            String key = file.getClassName() + "->" + file.getParentClass();
            if (strongest.add(key)) {
                builder.append(file.getClassName()).append(" --|> ").append(file.getParentClass()).append("\n");
            }
        }

        // 3) composition (*--)
        for (ParsedSource file : files) {
            for (String target : file.getCompositions()) {
                String key = file.getClassName() + "->" + target;
                if (strongest.add(key)) {
                    builder.append(file.getClassName()).append(" *-- ").append(target).append("\n");
                }
            }
        }

        // 4) aggregation (o--)
        for (ParsedSource file : files) {
            for (String target : file.getAggregations()) {
                String key = file.getClassName() + "->" + target;
                if (strongest.add(key)) {
                    builder.append(file.getClassName()).append(" o-- ").append(target).append("\n");
                }
            }
        }

        // 5) association (-->)
        for (ParsedSource file : files) {
            for (String target : file.getAssociations()) {
                if (target.equals(file.getClassName())) {
                    continue;
                }
                String key = file.getClassName() + "->" + target;
                if (strongest.add(key)) {
                    builder.append(file.getClassName()).append(" --> ").append(target).append("\n");
                }
            }
        }

        // 6) dependency (..>)
        for (ParsedSource file : files) {
            for (String target : file.getDependencies()) {
                if (target.equals(file.getClassName())) {
                    continue;
                }
                String key = file.getClassName() + "->" + target;
                if (strongest.add(key)) {
                    builder.append(file.getClassName()).append(" ..> ").append(target).append("\n");
                }
            }
        }

        builder.append("@enduml");
        return new UmlDiagramData(builder.toString());
    }

}

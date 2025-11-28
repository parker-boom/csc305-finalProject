package finalproject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds PlantUML text with richer associations.
 *
 * @version 1.0
 */
public class UmlBuilder {

    public UmlDiagramData build(List<SourceFile> files) {
        StringBuilder builder = new StringBuilder();
        builder.append("@startuml\n");

        // Declare nodes
        for (SourceFile file : files) {
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
        for (SourceFile file : files) {
            for (String iface : file.getImplementedInterfaces()) {
                String key = file.getClassName() + "->" + iface;
                strongest.add(key);
                builder.append(file.getClassName()).append(" ..|> ").append(iface).append("\n");
            }
        }

        // 2) extends (--|>)
        for (SourceFile file : files) {
            if (file.getParentClass() == null) {
                continue;
            }
            String key = file.getClassName() + "->" + file.getParentClass();
            if (strongest.add(key)) {
                builder.append(file.getClassName()).append(" --|> ").append(file.getParentClass()).append("\n");
            }
        }

        // 3) composition (*--)
        for (SourceFile file : files) {
            for (String target : file.getCompositions()) {
                String key = file.getClassName() + "->" + target;
                if (strongest.add(key)) {
                    builder.append(file.getClassName()).append(" *-- ").append(target).append("\n");
                }
            }
        }

        // 4) aggregation (o--)
        for (SourceFile file : files) {
            for (String target : file.getAggregations()) {
                String key = file.getClassName() + "->" + target;
                if (strongest.add(key)) {
                    builder.append(file.getClassName()).append(" o-- ").append(target).append("\n");
                }
            }
        }

        // 5) association (-->)
        for (SourceFile file : files) {
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
        for (SourceFile file : files) {
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

    // Helper holder for parsed source metadata (kept here to keep GitFetch lean).
    public static final class SourceFile {
        final String path;
        final String content;
        final String className;
        final Set<String> dependencies = new HashSet<>();
        final Set<String> associations = new HashSet<>();
        final Set<String> aggregations = new HashSet<>();
        final Set<String> compositions = new HashSet<>();
        Set<String> implementedInterfaces = new HashSet<>();
        String parentClass;
        boolean isInterface;
        boolean isAbstract;
        int incomingCount;

        public SourceFile(String path, String content) {
            this.path = path;
            this.content = content;
            this.className = extractClassName(path);
        }

        private static String extractClassName(String path) {
            int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
            String fileName = slash >= 0 ? path.substring(slash + 1) : path;
            int dot = fileName.lastIndexOf('.');
            return dot >= 0 ? fileName.substring(0, dot) : fileName;
        }

        public String getClassName() {
            return className;
        }

        public boolean isInterface() {
            return isInterface;
        }

        public boolean isAbstract() {
            return isAbstract;
        }

        public String getParentClass() {
            return parentClass;
        }

        public Set<String> getImplementedInterfaces() {
            return implementedInterfaces;
        }

        public Set<String> getDependencies() {
            return dependencies;
        }

        public Set<String> getAssociations() {
            return associations;
        }

        public Set<String> getAggregations() {
            return aggregations;
        }

        public Set<String> getCompositions() {
            return compositions;
        }

        public Set<String> allOutgoing() {
            Set<String> all = new HashSet<>();
            all.addAll(dependencies);
            all.addAll(associations);
            all.addAll(aggregations);
            all.addAll(compositions);
            if (parentClass != null) {
                all.add(parentClass);
            }
            all.addAll(implementedInterfaces);
            return all;
        }
    }
}

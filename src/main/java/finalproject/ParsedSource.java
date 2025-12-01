package finalproject;

import java.util.HashSet;
import java.util.Set;

/**
 * ROLE: Data.
 * Captures basic parse info (class name, relationships) for a Java source file.
 * Built in GitFetch and passed to UmlBuilder and DIA calculations to link classes.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 1.0
 */
public class ParsedSource {

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

    public ParsedSource(String path, String content) {
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

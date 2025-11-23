# Assignment 03 – GitHub Complexity Analysis

## Setup

- Java 21 JDK and Maven must be installed and available on the PATH.
- Project structure follows standard Maven layout (`pom.xml` in the root, source under `src/main/java`).
- A GitHub personal access token is hard-coded in `Controller` for simplicity; no extra configuration is required for this assignment.

## Design Notes

- Swing UI is split into modular panels tied together through a `Blackboard` singleton that broadcasts data, selection, and filter changes.
- Files are fetched with the Tulip `GitHubHandler`; analysis runs in `GitFetch` on a background thread to keep the UI responsive.
- The grid view reflects file complexity (color) and relative size (alpha), and now filters when a folder is selected from the tree.
- DIA metrics only consider dependencies among repository classes, preventing external libraries from skewing instability.

## To Run:

```bash
mvn -q compile
mvn exec:java -Dexec.mainClass=lab9.Main
```

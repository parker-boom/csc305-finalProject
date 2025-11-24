# Final Project CSC 305 — GitHub Code Analysis & UML Viewer

Built by Parker Jones and Ashley Aring for CSC 305. This desktop app fetches a GitHub folder, analyzes Java files for simple complexity and dependency metrics, and renders both a visual grid and a UML diagram.

## What this project is about

At a high level, the app pulls `.java` files from a GitHub URL, measures line counts and basic cyclomatic complexity, computes DIA metrics (abstractness/instability/distance) across in-repo classes, and produces a PlantUML diagram showing class dependency arrows. The UI is Swing-based: the left tree filters files, the grid view colors files by complexity, the metrics tab plots instability vs. abstractness, and the diagram tab shows the generated UML. Note that analysis is done within a package, and not between packages, as this is meant for smaller scale analysis.

## How it’s organized

At a high level, we follow a simple MVC-ish pattern: `Controller` wires user actions to a background fetch (`GitFetch`), while `Blackboard` (a singleton) broadcasts shared data (grid stats, DIA metrics, UML text) to the views. Each UI panel focuses on one concern (search, browser, grid, metrics, diagram, status bar). Fetching is one worker that stages: list GitHub files, download sources, build grid data, compute DIA, and generate PlantUML. Views listen to the blackboard to stay in sync without tight coupling.

## Setup

- Install Java 21 JDK and Maven on your PATH.
- Clone the repo and create an `.env` at `src/main/java/finalproject/.env` with:
  ```
  GH_ACCESS_TOKEN="your_token_here"
  ```
  (Personal access token needs repo read permissions.)

## Running

Once adding the above Git Hub access token, then
From the project root:

```bash
mvn -q compile
mvn exec:java
```

## Using the app

1. Launch the app.
2. Enter a GitHub folder URL (e.g., `https://github.com/org/repo/tree/main/src`).
3. Click “Analyze.” Status shows progress; tabs update when done.
4. Browse files on the left to filter the grid; switch tabs to see metrics or the UML diagram.

Logs: console plus JSON logs in `logs/app.log`. Generated artifacts live in `target/`
NOTE: /target & /logs are .gitignored

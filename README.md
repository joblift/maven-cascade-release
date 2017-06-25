A groovy-applicationw which helps release a tree of dependent maven-projects, increasing the referenced dependencies and parent along the ways thru the found project-graph.

### Process
- The application will search in the projects-dir for all maven projects.
- It traverses the given project and all dependents found in the graph
- First releases the start-project and stores the version
- continous on the leafs, until all projects are released. Stores those versions and replaces/updates them as well
- if an error occours the project will exit.

### Requirements
- Groovy, git and maven are in the PATH.
- All projects are located in the same directory (PROJECTS_DIR)
- multi-module projects are not supported

### Usage

    ./cascade-release.sh <PROJECTS_DIR> <START-PROJECT>

### Example

    ./cascade-release.sh $HOME/workspace my-lib


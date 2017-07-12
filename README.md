A groovy-applicationw which helps release a tree of dependent maven-projects, increasing the referenced dependencies and parent along the ways thru the found project-graph.

### Process
- The application will search in the projects-dir for all maven projects.
- It traverses the given project and all dependents found in the graph which are affected
- First releases the start-project and stores the version
- continous on the leafs, until all projects are released. Stores those versions and replaces/updates them as well
- if an error occours the project will exit.

### Requirements & Assumptions
- git, maven and java are in the PATH.
- All projects are located in the same directory (`-p` argument)
- Project Object Model file is named `pom.xml`
- Each xml-tag from the pom.xml is written in its own line (eg. `<dependencies>` or `<version>1.2.3</version>`)
- parent-hierarchies only have limited support (eg. when derived parents depend on other dependencies which depend on other parents is not supported)
- maven submodules/multi-module projects are not supported
- Version follows [Semver](http://semver.org)

### Basic usage

    ./cascade-release -p <PROJECTS_DIR> -s <START-PROJECT>
    
### Additional arguments

* `-i` Define the version increment, possible values are: major, minor, patch (default)
* `-f` will filter projects with the given groupId. Multiple groups can be separated by `,`
* `-u` will not release projects with the given groupId, put update the dependencies in the pom file
* `-h` print the usage help

### Example

    ./cascade-release -p $HOME/workspace -s my-lib

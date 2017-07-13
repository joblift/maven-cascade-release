A groovy-application which helps to release a tree of dependent maven-projects, updating the referenced dependencies and parent along the ways thru the found project-graph.

### Process
- The application will search in the projects-dir for all maven projects.
- It traverses the given project and all dependents found in the graph which are affected
- Determines the order of projects to be released, depending on the start-project
- Verifies that each project is up-to-date with git, in the master branch and not dirty
- Releases the projects. Updates the dependencies of released projects upfront. Stores the progress to continue if it breaks.

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
* `-g` Only projects with the groupId of the start-project will be analyzed, additional groupIds can be passed comma-separated using this argument.
* `-u` will not release projects with the given groupId, put update the dependencies in the pom file
* `-h` print the usage help

### Example

    ./cascade-release -p $HOME/workspace -s my-lib

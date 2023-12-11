# ![C Thing Software](https://www.cthing.com/branding/CThingSoftware-57x60.png "C Thing Software") molinillo4j

A port of the [Molinillo](https://github.com/CocoaPods/Molinillo/) generic dependency resolution algorithm to Java.

### Usage
Perform the following steps to use the dependency solver.

1. The dependency solver must be provided information about dependencies. This is accomplished by providing
   an implementation of the `SpecificationProvider` interface. For convenience, the implementation can extend
   the `AbstractSpecificationProvider` class.
2. The resolver can be configured to provide output during the resolution process. An instance of `DebugUI` can
   be specified to the `Resolver` constructor to provide debugging output. For customized output, an
   implementation of the `UI` interface can be provided. For convenience, the custom implementation can extend
   the `DefaultUI` class.
3. The resolver requires a set dependencies to be resolved. The type for these dependencies must be the same
   as specified in the specification provider.
4. If desired, a base set of locked dependencies can be provided to force the selection of those dependencies.
5. Perform the resolution using the following code:
   ```java
   Resolver<R, S> resolver = new Resolver<>(specificationProvider, ui);
   DependencyGraph<S, R> results = resolver.resolve(directDependencies);
   ```
6. The resolved dependencies are provided as a graph. The vertices contain a payload which is a resolved dependency,
   which is referred to as a specification. The type of the specified is the same as specified in the specification
   provider. The edges of the graph contain a requirement which represents the constraint satisfied by the dependency.
   The complete set of resolved dependencies can be obtained using the following code:
   ```java
   List<Optional<S>> resolved = results.getVertices()
                                       .values()
                                       .stream()
                                       .map(Vertex::getPayload)
                                       .toList();
   ```

### Additional Information
The Molinillo repository contains a detailed
[architecture document](https://github.com/CocoaPods/Molinillo/blob/master/ARCHITECTURE.md) that explains the algorithm.

### Glossary
* **Action** - Encapsulates a set of commands to modify the dependency graph. An action is added to a log so that
  its effects can be undone.
* **Activated** - The dependency graph representing the currently resolved dependencies. The resolution process
  constructs this graph while it attempts to select compatible dependencies. During the resolution process, the
  graph is often undone during backtracking to resolve conflicts. When resolution succeeds, the activated dependency
  graph reflects the resolved dependencies.
* **Conflict** - An incompatibility between selected dependencies. The occurrence of a conflict will result in the
  resolution process having to backtrack to a previous state so that it can try a difference set of dependencies.
  Typically, a conflict is the result of a version constraint being violated and backtracking involves selecting
  a different version of a dependency. If backtracking fails to resolve the conflict, the resolution process fails.
* **Dependency** - An entity that must be resolved. Typically, a dependency represents a software package or
  library and consists of its name and a constraint for its allowed versions. The term dependency and requirement
  are generally interchangeable.
* **Index** - A collection of specifications. Typically, an index is a software repository (e.g. Maven Central)
  which contains multiple packages and multiple versions of those packages.
* **Log** - A sequential list of actions that modify the dependency graph and tags to mark key points during the
  modifications. The log can be rewound to a specific tag thereby undoing all actions performed after that tag.
* **Payload** - Information attached to a vertex of the dependency graph. For the resolved dependency graph, the
  payload is the resolved dependency information.
* **Possibility** - A specification that may satisfy a dependency.
* **Requirement** - See dependency.
* **Resolution** - The act of performing dependency solving. Essentially, this is the process of going from
  version constraints to a specific compatible version for each direct dependency and their transitive dependencies.
* **Specification** - A resolved entity. Typically, a specification represents a software package or library and
  consists of its name, version and its direct dependencies.
* **Specification Provider** - The adapter between the resolver and the user-defined dependency system. Typically,
  the dependency system consists of a software repository, a versioning scheme and a version constraint notation.
* **State** - Classes that represent the state of the resolution process.
* **Tag** - An object inserted into the dependency graph modification log. The log can be then be rewound to that
  tag thereby undoing all actions performed after the tag.

### Building
The library is compiled for Java 17. If a Java 17 toolchain is not available, one will be downloaded.

Gradle is used to build the library:
```bash
./gradlew build
```
The Javadoc for the library can be generated by running:
```bash
./gradlew javadoc
```

# Compilers Project


## Students

| Name             | Number        | Contribution | 
| ---------------- | ------------- | ------------ |
| Inês Cardoso     |  up202005435  | 33%          |
| Joana Santos     |  up202006279  | 33%          |
| Mariana Carvalho |  up202007620  | 33%          |

### Self-assessment

Throughout the development of this project we received positive feedback about the delivery of each checkpoint, both in terms of code quality and logic.
For this final delivery, we maintained the quality presented previously. As we've successfully fulfilled all the required points, we believe our project should be graded 20/20.


## Checkpoint 1
In checkpoint 1 we added more operators to the grammar and added tests to check their priority and precedence.

| Operatores            | Precedence               |
|-----------------------|--------------------------|
| postfix	              | expr++ expr--            |
| unary                 | 	++expr --expr +expr -expr |
| multiplicative        | 	* / %                   |
| additive              | 	+ -                     |
| shift                 | 	<< >>                   |
| relational	           | < > <= >=                |
| equality              | 	== !=                   |
| bitwise AND	          | &                        |
| bitwise exclusive OR	 | ^                        |
| bitwise  inclusive OR | &#124;                   |
| logical AND              | 	&&                      |
| logical OR	              | &#124;&#124;             |

We also added the following objets types to type Declaration: 'byte','short','int','long','float','double','boolean','char'

## Checkpoint 3


### Optimizations

#### AST
* Constant Propagation
* Constant Folding
* Dead code Elimination

#### Ollir
* Register Allocation

#### Jasmin
* Use of iload_x, istore_x, astore_x, aload_x (e.g., instead of iload x)
* Use of iconst_0, bipush, sipush, ldc (load constants to the stack with the approriate instruction)
* Use of iinc
* Compare against zero, instead of two values - Use of iflt, ifne, etc



## Project Requirements

For this project, you need to install [Java](https://jdk.java.net/), [Gradle](https://gradle.org/install/), and [Git](https://git-scm.com/downloads/) (and optionally, a [Git GUI client](https://git-scm.com/downloads/guis), such as TortoiseGit or GitHub Desktop). Please check the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.

## Project Setup

There are some import folders in the repository. Your development source code is inside the subfolder named ``src/main``. Specifically, your initially application is in the folder ``src/main/pt/up/fe/comp2023``, and the grammar is in the subfolder ``src/main/antlr/comp2023/grammar``. Finally, the subfolder named ``test`` contains your unit tests.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/jmm/bin``. For convenience, there are two script files in the root folder, one for Windows (``jmm.bat``) and another for Linux (``jmm``), that call this launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.


## Tests

The base repository comes with two classes that contains unitary tests in the package ``pt.up.fe.comp``, ``TutorialTest`` and `` GrammarTest``. The tests in ``TutorialTest`` should all pass just using the provided code. ``GrammarTest`` contains tests for the complete Java-- grammar, and most should fail. By the end of Checkpoint 1, all tests should pass.

The class ``GrammarTest`` contains several static String variables at the beginning of the class where you should put the name of your rules for each type of rule that appears there. You have to set these variables to pass all tests.

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).

You can also see a test report by opening the file ``./build/reports/tests/test/index.html``.


### Reports
We also included in this project the class ``pt.up.fe.comp.jmm.report.Report``. This class is used to generate important reports, including error and warning messages, but also can be used to include debugging and logging information. E.g. When you want to generate an error, create a new Report with the ``Error`` type and provide the stage in which the error occurred.

### Parser Interface

We have included the interface ``pt.up.fe.comp.jmm.parser.JmmParser``, for which we already provide an example implementation in the file ``src/main/pt/up/fe/comp2023/SimpleParser.java``.

To configure the name of the class of the JmmParser implementation that should be automatically used for tests, use the file ``config.properties`` (more details below).

### Compilation Stages

The project is divided in four compilation stages, that you will be developing during the semester. The stages are Parser, Analysis, Optimization and Backend, and for each of these stages there is a corresponding Java interface that you will have to implement (e.g. for the Parser stage, you have to implement the interface JmmParser).


### config.properties

The testing framework, which uses the class ``pt.up.fe.comp.TestUtils``, has methods to test each of the four compilation stages (e.g., ``TestUtils.parse()`` for testing the Parser stage).

In order for the test class to find your implementations for the stages, it uses the file ``config.properties`` that is in root of your repository. It has four fields, one for each stage (i.e. ``ParserClass``, ``AnalysisClass``, ``OptimizationClass``, ``BackendClass``), and initially it only has one value, ``pt.up.fe.comp2023.SimpleParser``, associated with the first stage.

During the development of your compiler you will update this file in order to setup the classes that implement each of the compilation stages.

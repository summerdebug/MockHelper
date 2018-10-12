# MockHelper

## Requirements
* JVM Version: 1.8
* IntelliJ Idea version: 2018.2
* The test class should exist and have name `<TestedClass>Test`
* Test method should exist as a last method in the test class
* Test method should contain `// Act` comment
* Invocation of tested method should go right after `// Act` comment
* Mockito should be in classpath
* If static mocking is required, then PowerMockito should be in classpath
* If static mocking is required, `PowerMockRunner` should be specified as a test runner.

## Purpose
Automates routine actions while developing unit tests for Java application.

## Usage
### Mock non static method
- Put breakpoint on method invocation and debug application till that breakpoint
- Put mouse pointer to the method and press Alt + M
- Breakpoint should be in tested class

The method will be automatically mocked in the test, considering actual values of the parameter during debug session.
The mocked method will return either existing test field of required type or newly created field, if it does not exist.

### Mock class statically
- Put mouse pointer to the name of the class to be mocked in tested class
- Press Alt + K

The class to be mocked will be added to `PrepareForTest` annotation and to `PowerMockito.mockStatic` method. If the
annotation of method do not exist in the test yet, then they will be created.

### Mock static method
- Put breakpoint on method invocation and debug application till that breakpoint
- Put mouse pointer to the method and press Alt + O
- Breakpoint should be in tested class

The class will be mocked statically, as if the user clicked Ait + K. Also the static method under the mouse pointer
will be mocked like mocking of non static method on clicking Alt + M.

### Substitute method or constructor parameters
- Put mouse pointer to method or constructor invocation in test
- Click Alt + P

Parameter values will be automatically filled. For primitives and strings default values will be used.
For classes existing mocked fields will be used. If mocked field of required type does not exist, it will be created.

### Rearrange test fields
- Put mouse pointer inside test class
- Press Alt + L

Fields, not annotated with `@Rule` will be marked as private.
Fields will be arranged in the following order:
1. Constants
2. Non mocked fields
3. JUnit rules
4. Mocked fields

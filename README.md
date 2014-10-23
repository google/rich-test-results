The Rich Test Results project aims to represent most of the data that
can be produced by static and dynamic program analysis. This includes
static analysis tools, compilers, and various flavors of automated tests,
including browser and mobile tests.

The project is motivated by the lack of any standard, either proposed or
adopted, for testing tools to produce output that the user-facing tools
can understand and visualize.

Since any standard starts nascent, we also must interoperate with existing
formats. The xUnit XML format originally created by the Ant Junit task is
the de-facto industry standard today, so we provide a robust parser for it.
We intend to add more parsers as we determine that other formats merit
support.

The Rich Test Results project aims to represent most of the data that
can be produced by static and dynamic program analysis. This includes
static analysis tools, compilers, and various flavors of automated tests,
including browser and mobile tests.

Since this format will start out without adoption, we must interoperate with
existing formats. The xUnit XML format originally created by the Ant Junit
task is the de-facto industry standard today, so we provide a robust parser
for it. We intend to add more parsers as we determine that other formats merit
support.

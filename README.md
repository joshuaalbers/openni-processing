## OpenNI for Processing

This is a wrapper for the OpeniNI2 library so it can be used in Processing.
Primary development is done in OSX, but hopefully someday it will be tested with Linux and Windows.
Don't hold your breath.

## Notes for me
Use install_name_tool on libOpenNI2.jni.dylib so that libOpenNI2.dylib can be found when you run use this in a sketch.

`install_name_tool -change libOpenNI2.dylib @loader_path/libOpenNI2.dylib libOpenNI2.jni.dylib`

## Installation Instructions

TBD

## Build Instructions

TBD
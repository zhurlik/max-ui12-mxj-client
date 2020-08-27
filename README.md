![Java CI with Gradle](https://github.com/zhurlik/max-ui12-mxj-client/workflows/Java%20CI%20with%20Gradle/badge.svg)
# max-ui12-mxj-client
The Java based component for working with [Ui12 mixer](https://www.soundcraft.com/en/products/ui12) in the [Max8](https://cycling74.com/).

# Remote control via WebSocket
Ui12 is 12-input Remote-Controlled Digital Mixer that has own WebSocket server.  
Max8 has [mxj component](https://docs.cycling74.com/max8/refpages/mxj?q=mxj) to be able to execute Java in Max. 

# Requirements
For building this project you have to install JDK.

# How to install under Max8
* build fat jar  
`./gradlew clean customFatJar`
* copy to the Max8  
`cp ./build/libs/max8-ui12.jar -> C:\Program Files\Cycling '74\Max 8\resources\packages\max-mxj\java-classes\lib`

# How to use
Here is [a simple example](./sample) how it can be used in Max8.

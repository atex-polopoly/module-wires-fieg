# module-wires-fieg


dm.desk
=============================

This is the code used to feed dm.desk with wires in italian FIEG format.

For more information on dm.desk, please see the online documentation.


**Please note that this example can only be used in combination with the dm.desk project, and has only been verified to work with the corresponding release versions (of both dm.desk and Polopoly).**

## Installation

### 1. Clone the repository

Clone the repository into your project:

```
cd ~/gong
~/gong $ git clone https://github.com/atex-polopoly/module-wires-fieg.git

```

### 2. Amend the root pom.xml
Edit the root pom.xml (GONG pom.xml) to add the new module
```
<module>module-wires-fieg</module>
```
### 3. Amend the server-integration pom.xml
Edit the server-integration  pom.xml to add dependecy to the new module
```
    <dependency>
      <artifactId>module-wires-fieg</artifactId>
      <groupId>com.atex</groupId>
      <version>2.0</version>
    </dependency>
```

### 4. Configure integration server routes
The purpose of the module is to use parsers to process incoming wire texts, coming from different providers.
Each provider is supposed to have its own route in Camel/Integration Server. 
A sample configuration can be found in the dm.desk official documentation page (http://support.atex.com/confluence/display/AdamMaster/Importing+Content#ImportingContent-WiresinFIEG%28orotherflattext%29format)



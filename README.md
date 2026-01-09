### About
MWU (Mainstream web utility) is a HTTP server framework for Java, written in Java. This project right now is just a hobby. I started this project in Decemeber and is still being worked on.

## Example Code

This is an example server which can also be found in /src/main/java/com/server/Server.java

```java
MWU mwu = new MWU();

mwu.port().setPort(8080);
mwu.dir().setPublicDirectory("public");

try {
    mwu.start();
} catch (Exception e) {
    e.printStackTrace();
}

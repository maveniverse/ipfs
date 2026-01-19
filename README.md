# Maven @ IPFS

Adds IPFS transport support to Maven Resolver. Load this suite by adding in project `.mvn/extensions.xml`, or if Maven 4 user, one can add user-wide extension with `~/.m2/extensions.xml`. This extension adds IPFS transport support to Maven 3/4:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<extensions>
    <extension>
        <groupId>eu.maveniverse.maven.ipfs</groupId>
        <artifactId>extension3</artifactId>
        <version>${maveniverse.ipfs.version}</version>
    </extension>
</extensions>
```

Once extension loaded, remote repositories using IPFS can be declared with URLs in form of `ipfs:/name[/subpath]`. Parts of URI:
* protocol is fixed, must be `ipfs:/`
* `name` part may be: any IPNS resolvable name (key hash or DNSLink enabled domain) or CID
* `/subpath` part is optional, may point to some sub-path of name

Example setup (using Maven string representation of remote repositories in form of `id::url`) is shown at https://ipfs.maveniverse.eu/
* the `maveniverse-ipfs::ipfs:/ipfs.maveniverse.eu/repository-ipfs` contains all IPFS related artifacts to resolve `com.github.ipfs:java-ipfs-http-client:1.4.4`
* the `maveniverse-releases::ipfs:/ipfs.maveniverse.eu/releases` is example repository containing MIMA release

Note: same IPFS repositories content may be reached via IPFS HTTP Gateways using plain Maven HTTP transport as `maveniverse-ipfs::https://ipfs.io/ipns/ipfs.maveniverse.eu/repository-ipfs`. In this case you would use public goods, so it is recommended to use
own node + IPFS transport instead.

## Requirements

Run time requirement:
* Java 11+
* Maven 3.9+

Build time requirements:
* Java 21
* Maven 3.9.12+

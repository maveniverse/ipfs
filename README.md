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

## IPFS node

By default, transport will connect to IPFS node running on localhost. It is recommended for users to run their own node. Simplest solution is offered by [IPFS Desktop](https://docs.ipfs.tech/install/ipfs-desktop/).

## Consuming

For consumption nothing special is needed, just a locally running IPFS node. Recommended setup (aside of just installing and starting up node, either as daemon or via IPFS Desktop):
* Enable IPNS over PubSub (daemon `--enable-namesys-pubsub` daemon parameter)
* Note: in case of using IPFS in data centers (like GH CI is), recommended way to init node before starting is `ipfs init --profile server`. See [documentation](https://docs.ipfs.tech/how-to/command-line-quick-start/).

## Deploying

The deployment part expectation is that your local node has _same named key as `name` you use in repository stanza_. So, for examples above, node publishing to must have a key with name `ipfs.maveniverse.eu`. In case same name space has multiple publishers, the private key should be shared (in some sane, discreet way) among publishers. Ideally, to make publishing more human friendly, if the `name` is a domain, publishing the domain via [DNSLink](https://dnslink.dev/) is recommended. In this case you have human friendly `domain -> IPNS -> IPFS CID` indirection in place. Trust is derived similarly as in case of Maven publishing: owner of the domain tells the IPNS, and publishing to given IPNS is possible only in possesion of `name` named private key. Impostors may publish same named artifacts, but they are never able to publish those "as you would", as private key protects you from this happening. In case key is compromised, just generate new private key, and update your DNSLink record with it.

## Requirements

Run time requirement:
* Java 11+
* Maven 3.9+

Build time requirements:
* Java 21
* Maven 3.9.12+

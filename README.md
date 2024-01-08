# Extism Java SDK

Java SDK for the [Extism](https://extism.org/) WebAssembly Plugin-System.

[![maven](https://img.shields.io/maven-central/v/org.extism.sdk/extism)](https://search.maven.org/artifact/org.extism.sdk/extism)
[![javadoc](https://javadoc.io/badge2/org.extism.sdk/extism/javadoc.svg)](https://javadoc.io/doc/org.extism.sdk/extism)

## Installation

### Install the Extism Runtime Dependency

For this library, you first need to install the Extism Runtime. You can [download the shared object directly from a release](https://github.com/extism/extism/releases) or use the [Extism CLI](https://github.com/extism/cli) to install it:

```bash
sudo extism lib install latest

#=> Fetching https://github.com/extism/extism/releases/download/v0.5.2/libextism-aarch64-apple-darwin-v0.5.2.tar.gz
#=> Copying libextism.dylib to /usr/local/lib/libextism.dylib
#=> Copying extism.h to /usr/local/include/extism.h
```

### Install Jar

To use the Extism java-sdk you need to add the `org.extism.sdk` dependency to your dependency management system.

#### Maven

To use the Extism java-sdk with maven you need to add the following dependency to your `pom.xml` file:
```xml
<dependency>
    <groupId>org.extism.sdk</groupId>
    <artifactId>extism</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle

To use the Extism java-sdk with maven you need to add the following dependency to your `build.gradle` file:

```
implementation 'org.extism.sdk:extism:1.0.0'
```

## Getting Started

This guide should walk you through some of the concepts in Extism and this java library.

### Creating A Plug-in

The primary concept in Extism is the [plug-in](https://extism.org/docs/concepts/plug-in). You can think of a plug-in as a code module stored in a `.wasm` file.
Since you may not have an Extism plug-in on hand to test, let's load a demo plug-in from the web:

```java
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.wasm.UrlWasmSource;
import org.extism.sdk.Plugin;

var url = "https://github.com/extism/plugins/releases/latest/download/count_vowels.wasm";
var manifest = new Manifest(List.of(UrlWasmSource.fromUrl(url)));
var plugin = new Plugin(manifest, false, null);
```

> **Note**: See [the Manifest docs](https://www.javadoc.io/doc/org.extism.sdk/extism/latest/org/extism/sdk/manifest/Manifest.html) as it has a rich schema and a lot of options.

### Calling A Plug-in's Exports

This plug-in was written in Rust and it does one thing, it counts vowels in a string. As such, it exposes one "export" function: `count_vowels`. We can call exports using [Plugin#call](https://www.javadoc.io/doc/org.extism.sdk/extism/latest/org/extism/sdk/Plugin.html#call(java.lang.String,byte[]))

```java
var output = plugin.call("count_vowels", "Hello, World!");
System.out.println(output);
// => "{"count": 3, "total": 3, "vowels": "aeiouAEIOU"}"
```

All exports have a simple interface of bytes-in and bytes-out.
This plug-in happens to take a string and return a JSON encoded string with a report of results.

### Plug-in State

Plug-ins may be stateful or stateless. Plug-ins can maintain state b/w calls by the use of variables.
Our count vowels plug-in remembers the total number of vowels it's ever counted in the "total" key in the result.
You can see this by making subsequent calls to the export:

```java
var output = plugin.call("count_vowels", "Hello, World!");
System.out.println(output);
// => "{"count": 3, "total": 6, "vowels": "aeiouAEIOU"}"

var output = plugin.call("count_vowels", "Hello, World!");
System.out.println(output);
// => "{"count": 3, "total": 9, "vowels": "aeiouAEIOU"}"
```

These variables will persist until this plug-in is freed or you initialize a new one.

### Configuration

Plug-ins may optionally take a configuration object. This is a static way to configure the plug-in.
Our count-vowels plugin takes an optional configuration to change out which characters are considered vowels. Example:

```java
var plugin = new Plugin(manifest, false, null);
var output = plugin.call("count_vowels", "Yellow, World!");
System.out.println(output);
// => {"count": 3, "total": 3, "vowels": "aeiouAEIOU"}

// Let's change the vowels config it uses to determine what is a vowel:
var config = Map.of("vowels", "aeiouyAEIOUY");
var manifest2 = new Manifest(List.of(UrlWasmSource.fromUrl(url)), null, config);
var plugin = new Plugin(manifest2, false, null);
var output = plugin.call("count_vowels", "Yellow, World!");
System.out.println(output);
// => {"count": 4, "total": 4, "vowels": "aeiouyAEIOUY"}
// ^ note count changed to 4 as we configured Y as a vowel this time
```

### Host Functions

Let's extend our count-vowels example a little bit: Instead of storing the `total` in an ephemeral plug-in var,
let's store it in a persistent key-value store!

Wasm can't use our app's KV store on its own. This is where [Host Functions](https://extism.org/docs/concepts/host-functions) come in.

[Host functions](https://extism.org/docs/concepts/host-functions) allow us to grant new capabilities to our plug-ins from our application.
They are simply some java methods you write which can be passed down and invoked from any language inside the plug-in.

Let's load the manifest like usual but load up this `count_vowels_kvstore` plug-in:

```java
var url = "https://github.com/extism/plugins/releases/latest/download/count_vowels_kvstore.wasm";
var manifest = new Manifest(List.of(UrlWasmSource.fromUrl(url)));
var plugin = new Plugin(manifest, false, null);
```

> *Note*: The source code for this plug-in is [here](https://github.com/extism/plugins/blob/main/count_vowels_kvstore/src/lib.rs)
> and is written in rust, but it could be written in any of our PDK languages.

Unlike our previous plug-in, this plug-in expects you to provide host functions that satisfy its import interface for a KV store.
We want to expose two functions to our plugin, `kv_write(String key, Bytes value)` which writes a bytes value to a key and `Bytes kv_read(String key)` which reads the bytes at the given `key`.

```java
// Our application KV store
// Pretend this is redis or a database :)
var kvStore = new HashMap<String, byte[]>();

ExtismFunction kvWrite = (plugin, params, returns, data) -> {
    System.out.println("Hello from kv_write Java Function!");
    var key = plugin.inputString(params[0]);
    var value = plugin.inputBytes(params[1]);
    System.out.println("Writing to key " +  key);
    kvStore.put(key, value);
};

ExtismFunction kvRead = (plugin, params, returns, data) -> {
    System.out.println("Hello from kv_read Java Function!");
    var key = plugin.inputString(params[0]);
    System.out.println("Reading from key " +  key);
    var value = kvStore.get(key);
    if (value == null) {
        // default to zeroed bytes
        var zero = new byte[]{0,0,0,0};
        plugin.returnBytes(returns[0], zero);
    } else {
        plugin.returnBytes(returns[0], value);
    }
};

HostFunction kvWriteHostFn = new HostFunction<>(
    "kv_write",
    new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64, LibExtism.ExtismValType.I64},
    new LibExtism.ExtismValType[0],
    kvWrite,
    Optional.empty()
);

HostFunction kvReadHostFn = new HostFunction<>(
    "kv_read",
    new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
    new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
    kvRead,
    Optional.empty()
);

```

> *Note*: In order to write host functions you should get familiar with the methods on the [ExtismCurrentPlugin](https://www.javadoc.io/doc/org.extism.sdk/extism/latest/org/extism/sdk/ExtismCurrentPlugin.html) class.
> The `plugin` parameter is an instance of this class.

Now we just need to pass in these function references when creating the plugin:.

```java
HostFunction[] functions = {kvWriteHostFn, kvReadHostFn};
var plugin = new Plugin(manifest, false, functions);
var output = plugin.call("count_vowels", "Hello, World!");
// => Hello from kv_read Java Function!
// => Reading from key count-vowels
// => Hello from kv_write Java Function!
// => Writing to key count-vowels
System.out.println(output);
// => {"count": 3, "total": 3, "vowels": "aeiouAEIOU"}
```

## Development

# Build

To build the Extism java-sdk run the following command:

```
mvn clean verify
```


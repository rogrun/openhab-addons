# Telenot Binding

_The Telenot binding connects the Telenot Complex 400 to openhab._

_It is able to read the states of the contacts and the security areas._

## Supported Things

The binding supports the following thing types:

* `ipbridge` - Supports TCP connection to the serial tcp adapter.
* `mb` - Reports the "Meldebereiche".
* `mp` - Reports the inputs (contacts).
* `sb` - Reports the security area "Sicherungsbereiche".
* `emaState` - Reports the state of the system.

## Discovery

_Discovery is currently not available._

## Thing Configuration

### ipbridge

The `ipbridge` thing supports a TCP/IP connection to an RS323 to LAN adapter.

* `hostname` (required) The hostname or IP address of the serial to LAN adapter
* `tcpPort` (default = 4116) TCP port number for the serial to LAN adapter connection
* `reconnect` (1-60, default = 2) The period in minutes that the handler will wait between connection checks and connection attempts
* `timeout` (0-60, default = 5) The period in minutes after which the connection will be reset if no valid messages have been received. Set to 0 to disable.

Thing config file example:

```
Bridge telenot:ipbridge:device [ hostname="xxx.xxx.xxx.xxx", tcpPort=4116 ] {
  Thing ...
  Thing ...
}
```

### mb

The `mb` thing provides the state of each single reporting area (Meldebereich).

* `address` (required) The number of reporting area.

### mp

The `mp` thing provides the state of each single reporting point (Meldepunkt).

* `address` (required) The number of reporting ponit.

### sb

The `sb` thing provides the state of each single security area (Sicherungsbereich).

* `address` (required) The number of security area.


### emaState

The `emaState` thing currently provides the state of the system.

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| channel  | type   | description                  |
|----------|--------|------------------------------|
| control  | Switch | This is the control channel  |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_

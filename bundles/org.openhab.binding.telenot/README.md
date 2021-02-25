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

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

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

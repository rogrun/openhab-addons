# AquaTemp Binding

<img src="org.openhab.binding.aquatemp/doc/AquaTempLogo.png" width="140"/>

This binding connects AquaTemp heat pumps through the AquaTemp cloud services.

## Compatibility

This binding version requires openHAB 5.0.0 or newer and is intended for installation through the openHAB Marketplace.
The bundle is built against the openHAB 5.1 API with dependencies compatible with openHAB 5.0 and newer.
The current revision has been positively tested with openHAB 5.2.

Both AquaTemp cloud services are supported:

* `cloud.linked-go.com` for accounts using the legacy API
* `cloud.go-heating.com` for accounts used by current AquaTemp app versions

## Supported Things

The binding supports the following thing types:

* `bridge` - Connects to the selected AquaTemp cloud API.
* `device` - Represents a heat pump discovered in the configured account.

## Discovery

Discovery is supported for devices connected to the configured account.
After configuring and enabling the bridge, start a scan to add devices from the inbox.

## Binding Configuration

The `bridge` thing supports the connection to the AquaTemp API.
 
* `user` (required) The email address registered in the AquaTemp app
* `password` (required) The password registered in the AquaTemp app
* `cloudApi` (default = `linkedGo`) Selects the cloud service used by the account:
  * `linkedGo` for the existing `cloud.linked-go.com` API
  * `goHeating` for the `cloud.go-heating.com` API used by current AquaTemp app versions
* `productIds` (advanced) Comma-separated product IDs sent when discovering devices through the `goHeating` API
* `apiCallLimit` (default = 1450) Maximum number of API calls per day (*)
* `bufferApiCommands` (default = 450) Number of API calls reserved for commands (*)
* `pollingInterval` (default = 0) Interval in seconds at which available devices are queried (**)
* `disablePolling` (default = OFF) Disables automatic polling so refreshes can be triggered manually


(*) Used to calculate refresh time in seconds.
(**) If set to 0, then the interval will be calculated by the binding.

The `goHeating` integration is based on captured traffic from AquaTemp Android app version 2.2.1.
It supports discovery, device data, device status, fault information and control of power, silent mode,
operating mode and the mode-specific target temperature.

The automatic polling interval is calculated from the API-call limit when `pollingInterval` is `0`.
Sending the `REFRESH` command to a linked device item requests an immediate device refresh without sending a control command to the heat pump.

## Thing Configuration

_All configurations are made in the UI_

## Channels

### `bridge`

| channel             | type   | RO/RW | description                                |
|---------------------|--------|-------|--------------------------------------------|
| `count-api-calls`   | Number | RO    | How often the API is called this day       |


### `device`

| channel                            | type               | RO/RW | description                              |
|------------------------------------|--------------------|-------|------------------------------------------|
| `power-state`                      | Switch             | RW    | Power switch                             |
| `inlet-temperature`                | Number:Temperature | RO    | Water temperature of the inlet           |
| `outlet-temperature`               | Number:Temperature | RO    | Water temperature of the outlet          |
| `ambient-temperature`              | Number:Temperature | RO    | Ambient temperature                      |
| `target-temperature`               | Number:Temperature | RW    | Target temperature                       |
| `silent-mode`                      | Switch             | RW    | Turns the silent mode on/off             |
| `mode`                             | String             | RW    | Operating mode                           |
| `consumption`                      | Number:Power       | RO    | Current power consumption                |
| `actual-power`                     | Number:Power       | RO    | Actual heat pump power                    |
| `electricity-consumption-per-hour` | Number:Energy      | RO    | Electricity consumption per hour         |
| `compressor-output-frequency`      | Number:Frequency   | RO    | Compressor output frequency              |
| `compressor-current`               | Number:ElectricCurrent | RO | Compressor current                       |
| `operating-state`                  | String             | RO    | Operating state reported by the device   |
| `flow-switch`                      | Switch             | RO    | Water flow switch state                  |
| `error-message`                    | String             | RO    | Active device fault message              |
| `error-is-active`                  | Switch             | RO    | Whether a device fault is active         |

The `mode` channel contains the mode selected by the user, such as `Auto`, `Heat` or `Cool`.
The `operating-state` channel reports what the heat pump is currently doing within that mode, such as `Heating`, `Cooling` or `Idle`.
For example, a heat pump can have `mode` set to `Auto` while `operating-state` is `Heating`.

## Upgrading from an Earlier Marketplace Version

Remove the installed AquaTemp Marketplace binding while openHAB is running before installing this test or release version.
A restart is not required.
Existing bridge and device Things can be retained.

After the new version has been installed, verify that the bridge and its devices are online and run a refresh.
New channels might need to be linked to Items manually when existing Things are retained.

## Release Notes

This version adds support for the current `cloud.go-heating.com` API while retaining the legacy `cloud.linked-go.com` integration.
The following functionality was added or updated:

* discovery, polling and manual refresh through the current API
* control of power, silent mode, operating mode and mode-specific target temperature
* dynamic target-temperature limits reported by the device
* power, energy, compressor, water-flow, operating-state and fault diagnostic channels
* compatibility with openHAB 5.0, 5.1 and 5.2

The automatic transition of `operating-state` between heating, cooling and idle has not yet been observed across a complete operating season.
The channel mapping follows the values returned by the API; further real-world validation is planned.

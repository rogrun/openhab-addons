# AquaTemp Binding

<img src="org.openhab.binding.aquatemp/doc/AquaTempLogo.png" width="140"/>

This binding connects AquaTemp Devices.

## Supported Things

The binding supports the following thing types:

* `bridge` - Supports connection AquaTemp API.
* `device` - Provides a device which is connected (Discovery)

## Discovery

Discovery is supported for all devices connected in your account.

## Binding Configuration

The `bridge` thing supports the connection to the AquaTemp API.
 
* `user` (required) The E-Mail address which is registered for the AquaTemp App
* `password` (required) The password which is registered for the AquaTemp App
* `cloudApi` (default = `linkedGo`) Selects the cloud service used by the account:
  * `linkedGo` for the existing `cloud.linked-go.com` API
  * `goHeating` for the `cloud.go-heating.com` API used by current AquaTemp app versions
* `productIds` (advanced) Comma-separated product IDs sent when discovering devices through the `goHeating` API
* `apiCallLimit` (default = 1450) The limit how often call the API (*) 
* `bufferApiCommands` (default = 450) The buffer for commands (*)
* `pollingInterval` (default = 0) How often the available devices should be queried in seconds (**) 
* `disablePolling` (default = OFF) Deactivates the polling to carry out the manual poll using an item


(*) Used to calculate refresh time in seconds.
(**) If set to 0, then the interval will be calculated by the binding.

The `goHeating` integration is based on captured traffic from AquaTemp Android app version 2.2.1.
It supports discovery, device data, device status, fault information and control of power, silent mode,
operating mode and the mode-specific target temperature.

## Thing Configuration

_All configurations are made in the UI_

## Channels

### `bridge`

| channel             | type   | RO/RW | description                                |
|---------------------|--------|-------|--------------------------------------------|
| `count-api-calls`   | Number | RO    | How often the API is called this day       |


### `device`

| channel              | type               | RO/RW | description                     |
|----------------------|--------------------|-------|---------------------------------|
| `power-state`        | Switch             | RW    | Power switch                    |
| `inlet-temperature`  | Number:Temperature | RO    | Water-temperature of the inlet  |
| `outlet-temperature` | Number:Temperature | RO    | Water-temperature of the outlet |
| `ambient-temperature` | Number:Temperature | RO    | Ambient-temperature             |
| `target-temperature` | Number:Temperature | RW    | Target-temperature              |
| `silent-mode`        | Switch             | RW    | Turns the silent mode on/off    |
| `mode`               | String             | RW    | Operating mode                  |
| `consumption`        | Number:Power       | RO    | Current power consumption       |
| `error-message`      | String             | RO    | Shows the error                 |
| `error-is-active`    | Switch             | RO    | Error is active                 |

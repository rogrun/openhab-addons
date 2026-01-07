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
* `apiCallLimit` (default = 1450) The limit how often call the API (*) 
* `bufferApiCommands` (default = 450) The buffer for commands (*)
* `pollingInterval` (default = 0) How often the available devices should be queried in seconds (**) 
* `disablePolling` (default = OFF) Deactivates the polling to carry out the manual poll using an item


(*) Used to calculate refresh time in seconds.
(**) If set to 0, then the interval will be calculated by the binding.

## Thing Configuration

_All configurations are made in the UI_

## Channels

### `bridge`

| channel             | type   | RO/RW | description                                |
|---------------------|--------|-------|--------------------------------------------|
| `countApiCalls`     | Number | RO    | How often the API is called this day       |


### `device`

| channel              | type               | RO/RW | description                     |
|----------------------|--------------------|-------|---------------------------------|
| `powerState`         | Switch             | RW    | Power switch                    |
| `inletTemperature`   | Number:Temperature | RO    | Water-temperature of the inlet  |
| `outletTemperature`  | Number:Temperature | RO    | Water-temperature of the outlet |
| `ambientTemperature` | Number:Temperature | RO    | Ambient-temperature             |
| `targetTemperature`  | Number:Temperature | RW    | Target-temperature              |
| `silentMode`         | Switch             | RW    | Turns the silent mode on/off    |
| `mode`               | Number             | RW    | Operating mode                  |
| `consumption`        | Number:energy      | RO    | Consumption                     |
| `errorMessage`       | String             | RO    | Shows the Error                 |
| `errorIsActive`      | Switch             | RO    | Error is active                 |


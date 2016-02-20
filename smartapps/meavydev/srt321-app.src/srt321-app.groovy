/*
 * SRT321 Helper App by MeavyDev
 * Select the Switch to set the Network Device Id associated with the SRT321 Thermostat
 */ 
definition(
    name: "SRT321 App",
    namespace: "meavydev",
    author: "MeavyDev",
    description: "Associates the SRT321 with a switch",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences 
{
    input "associatedSwitch", "capability.switch", title: "Switch?", multiple: false, required: true
    input "thermostatSelected", "capability.thermostat", title: "SRT321 Thermostat?", multiple: false, required: true
}

def installed() 
{
    def networkId = " "+associatedSwitch.deviceNetworkId+" "
    def devId = Integer.parseInt(networkId.replaceAll(" ", ""), 16)

    log.debug "Installed - Id $devId"
	state.association = devId
    
    runIn(1, handler)
}

def updated() 
{
    def networkId = " "+associatedSwitch.deviceNetworkId+" " 
    def devId = Integer.parseInt(networkId.replaceAll(" ", ""), 16)

    log.debug "Updated - Id $devId"
	state.association = devId.toString()
    
    runIn(1, handler)
}


// No real need for a handler, but this was avoiding crashes
// Send the "Device Network Id" to the SRT321 thermostat so it can control the switch
// How long before SmartThings realises that having device preferences 
// with input "*" "capability.switch" is reasonable????
def handler() 
{
	log.debug "Setting thermostat association"
    
    thermostatSelected.setupDevice(state.association)
}
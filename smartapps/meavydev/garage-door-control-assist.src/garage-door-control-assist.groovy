/**
 * Garage Door Control Assist
 * by MeavyDev
 */
 
definition(
    name: "Garage Door Control Assist",
    namespace: "meavydev",
    author: "MeavyDev",
    description: "Momentary press a relay switch when the garage door control requests",
    category: "My Apps",
    iconUrl: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1",
    iconX2Url: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1",
    iconX3Url: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1")


preferences {
	section("When this garage door control is activated...") {
		input "garageActivate", "capability.garageDoorControl", title: "Garage Door Control", required: true, multiple: false
    }
    
    section("Relay to press...") {
    	input "relaySwitch", "capability.relaySwitch", title: "Relay Switch", required: true, multiple: false
    }
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(garageActivate, "doorcontrol", activateHandler)
}

def activateHandler(evt)
{
	log.debug "Garage Control $evt.value"
	pressed()
}

def pressed() 
{
	log.debug "Turning relay on"

    relaySwitch.on1()
    runIn(1, offHandler)
}

def offHandler() 
{
	log.debug "Turning relay off"

	relaySwitch.off1()
}
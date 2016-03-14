/** 
 * Virtual Momentary Relay
 * by MeavyDev
 */
 
 definition(
    name: "Virtual Momentary Relay",
    namespace: "meavydev",
    author: "MeavyDev",
    description: "Momentary press a relay switch from a virtual switch press",
    category: "My Apps",
    iconUrl: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1",
    iconX2Url: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1",
    iconX3Url: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1")


preferences {
	section("When this virtual switch is pressed...") {
		input "virtualToggle", "capability.switch", title: "Virtual Switch", required: true, multiple: false
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
	subscribe(virtualToggle, "switch.on", pressedHandler)
}

def pressedHandler(evt)
{
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
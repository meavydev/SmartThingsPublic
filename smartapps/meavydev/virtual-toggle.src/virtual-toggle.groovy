/** 
 * Virtual Toggle
 * by MeavyDev
 */
 
definition(
    name: "Virtual Toggle",
    namespace: "meavydev",
    author: "MeavyDev",
    description: "Toggle a real switch from a virtual switch press",
    category: "My Apps",
    iconUrl: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1",
    iconX2Url: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1",
    iconX3Url: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1")


preferences {
	section("When this virtual switch is pressed...") {
		input "virtualToggle", "capability.switch", title: "Virtual Switch", required: true, multiple: false
    }
    
    section("Switch to toggle...") {
    	input "toggleSwitch", "capability.switch", title: "Toggle Switch", required: true, multiple: false
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
	subscribe(virtualToggle, "switch.on", pressed)
}


def pressed(evt) {
	log.debug "$evt.value"
  
	if (toggleSwitch.currentSwitch == "off") 
    {
        toggleSwitch.on()
    } 
    else if (toggleSwitch.currentSwitch == "on") 
    {
        toggleSwitch.off()
    }
}
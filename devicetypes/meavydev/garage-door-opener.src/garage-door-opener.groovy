/**
 *  Garage Door Opener using SmartApp controlled relay
 *  by MeavyDev
 */
metadata {
	definition (name: "Garage Door Opener", namespace: "meavydev", author: "MeavyDev") {
		capability "Actuator"
		capability "Door Control"
        capability "Garage Door Control"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Sensor"
        capability "Polling"
	}

	simulator {
		
	}

	tiles {
		standardTile("toggle", "device.door", width: 2, height: 2) {
			state("closed", label:'${name}', action:"door control.open", icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821", nextState:"opening")
			state("open", label:'${name}', action:"door control.close", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e", nextState:"closing")
			state("opening", label:'${name}', action:"door control.close", icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e")
			state("closing", label:'${name}', action:"door control.open", icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e")
			
		}
		standardTile("open", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'open', action:"door control.open", icon:"st.doors.garage.garage-opening"
		}
		standardTile("close", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'close', action:"door control.close", icon:"st.doors.garage.garage-closing"
		}
		standardTile("refresh", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "toggle"
		details(["toggle", "open", "close", "refresh"])
	}
}
preferences {    
    section("Garage door sensor...") {
    	input "doorContact", "capability.contactSensor", title: "Garage door sensor", required: true, multiple: false
	}
    
    section("Garage door motion...") {
    	input "doorMotion", "capability.accelerationSensor", title: "Garage door motion", required: true, multiple: false
	}
}


def activateRelay()
{
	def timeNow = now()
	log.debug "sending activate event: $timeNow"

    sendEvent(name: "doorcontrol", value: timeNow)
}

def installed()
{
	subscribe(doorContact, "contact", contactHandler)
    subscribe(doorMotion, "acceleration", motionHandler)
}

def updated()
{
	subscribe(doorContact, "contact", contactHandler)
    subscribe(doorMotion, "acceleration", motionHandler)
}
    
def contactHandler(evt)
{
	log.debug "door contact $evt.value"

	if (evt.value == "open")
    {
    	finishOpening()
    }
    else
    {
        finishClosing()
    }
}

def motionHandler(evt)
{
	log.debug "door motion: $evt.value"
    if (evt.value == "active")
    {
        if (doorContact.currentContact == "open")
        {
            sendEvent(name: "door", value: "closing")
        }
        else
        {
            sendEvent(name: "door", value: "opening")
        }
    }
    else
    {
        if (doorContact.currentContact == "open")
        {
            sendEvent(name: "door", value: "open")
        }
        else
        {
            sendEvent(name: "door", value: "closed")
        }
    }
}

def noMotionHandler(evt)
{
	log.debug "no door motion: $evt.value"
    if (evt.value == "active")
    {
        if (doorContact.currentContact == "open")
        {
            sendEvent(name: "door", value: "closing")
        }
        else
        {
            sendEvent(name: "door", value: "opening")
        }
    }
    else
    {
        if (doorContact.currentContact == "open")
        {
            sendEvent(name: "door", value: "open")
        }
        else
        {
            sendEvent(name: "door", value: "closed")
        }
    }
}

def parse(String description) 
{
	log.trace "parse($description)"
}

def open() 
{
    activateRelay()
}

def close() 
{
    activateRelay()
}

def finishOpening() 
{
    sendEvent(name: "door", value: "open")
    sendEvent(name: "contact", value: "open")
}

def finishClosing() 
{
    sendEvent(name: "door", value: "closed")
    sendEvent(name: "contact", value: "closed")
}

def updateState()
{
	def isOpen = doorContact.currentContact == "open" ? "open" : "closed"
	log.debug "Garage door $isOpen"
    sendEvent(name: "door", value: isOpen)
    sendEvent(name: "contact", value: isOpen)
}

def refresh() 
{
	updateState()
}

def poll() 
{
	updateState()
}
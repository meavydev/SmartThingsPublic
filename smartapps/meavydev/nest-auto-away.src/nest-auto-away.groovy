/**
 *  Nest Auto Away
 *  by MeavyDev
 */
definition(
    name: "Nest Auto Away",
    namespace: "meavydev",
    author: "MeavyDev",
    description: "Nest changes Home/Away status based on separate leave and arrive Presence.",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2421186/nestaway.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2421186/nestaway%402x.png")


preferences {
  section("When I arrive..."){
		input "presenceArrive", "capability.presenceSensor", title: "Who?", multiple: true
	}

  section("When I leave..."){
		input "presenceLeave", "capability.presenceSensor", title: "Who?", multiple: true
	}
    
  section("Change these thermostats modes...") {
    input "thermostats", "capability.thermostat", multiple: true
  }
}

def installed()
{
	subscribe(presenceArrive, "presence", presenceArriveHandler)
	subscribe(presenceLeave, "presence", presenceLeaveHandler)
}

def updated()
{
	unsubscribe()
	subscribe(presenceArrive, "presence", presenceArriveHandler)
	subscribe(presenceLeave, "presence", presenceLeaveHandler)
}

def presenceArriveHandler(evt)
{
	log.debug "presenceArriveHandler $evt.name: $evt.value"

	if (evt.value == "present")
    {
        thermostats?.present()
        log.debug "Nest is set to Home."
        sendPush("Nest set Home")
	}
    else
    {
		log.debug "PresenceArrive is Away."
	}
}    

def presenceLeaveHandler(evt)
{
	log.debug "presenceLeaveHandler $evt.name: $evt.value"

	if (evt.value == "present")
    {
		log.debug "PresenceLeave is Home."
	}
    else
    {
        thermostats?.away()
        log.debug "Nest is set to Away."
        sendPush("Nest set Away")		
	}
}
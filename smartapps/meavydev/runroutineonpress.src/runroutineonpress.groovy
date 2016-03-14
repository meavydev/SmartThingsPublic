/** 
 * Run Routine on Press
 * by MeavyDev
 */

definition(
name: "RunRoutineOnPress",
namespace: "meavydev",
author: "MeavyDev",
description: "Monitor a switch and activate Routine when pressed.",
category: "My Apps",
iconUrl: "http://icons.iconarchive.com/icons/icons8/ios7/512/Very-Basic-Home-Filled-icon.png",
iconX2Url: "http://icons.iconarchive.com/icons/icons8/ios7/512/Very-Basic-Home-Filled-icon.png"
)

preferences {
    page(name: "configure")
}

def configure() {
    dynamicPage(name: "configure", title: "Configure Switch and Routine", install: true, uninstall: true) {
            section("Select your switch") {
                    input "routineSwitch", "capability.switch",required: true
            }

            def actions = location.helloHome?.getPhrases()*.label
            if (actions) {
            actions.sort()
                    section("Routine") {
                            log.trace actions
                input "routine", "enum", title: "Routine to execute when switch on", options: actions, required: true
                    }
            }
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
	subscribe(routineSwitch, "switch.on", pressed)
}

def pressed(evt)
{
	def isOn = routineSwitch.currentSwitch == "on"
	log.debug "Routine switch: $evt.value state: $isOn"
	if (isOn)
    {
      	location.helloHome?.execute(settings.routine)
    }
}
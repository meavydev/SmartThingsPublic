/* 
 * Motion control with a switch override (for night mode)
 * by MeavyDev
 */

metadata {
	definition (name: "Gated Motion Sensor", namespace: "meavydev", author: "MeavyDev") {
		capability "Motion Sensor"        
	}

	simulator {
	}

	preferences {
	  section("Motion sensor..."){
			input "motion", "capability.motionSensor", title: "Motion?", multiple: true
		}

	  section("Gating switch that needs to be Off to allow motion events..."){
			input "motionAllowedOff", "capability.switch", title: "Switch?", multiple: false
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
			}
		}

		main(["motion"])
		details(["motion"])
	}
}

def installed()
{
	subscribe(motion, "motion", motionHandler)
}

def updated()
{
	subscribe(motion, "motion", motionHandler)
}

def motionHandler(evt)
{
	log.debug "motionHandler $evt.name: $evt.value"
	def current = motion.currentValue("motion")

	def motionValue = motion.find{it.currentMotion == "active"}
	def motionAllowed = motionAllowedOff.currentSwitch == "off"
	
    def value = "inactive"
    
	if(motionValue && motionAllowed){
		value = "active"
	}

	def event = [
		name:   "motion",
		value:  value,
	]

	log.debug "Sending motion event: (${event})"
	sendEvent(event)
}
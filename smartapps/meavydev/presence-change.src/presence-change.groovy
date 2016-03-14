/**
 * Presence Change
 * by MeavyDev
 */
definition(
    name: "Presence Change",
    namespace: "meavydev",
    author: "MeavyDev",
    description: "Web App controlling simulated presence changes.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text_presence.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text_presence@2x.png"
)

preferences {
	section("When a presence sensor arrives or departs this location..") {
		input "presenceSensor", "capability.presenceSensor", title: "Which sensor?"
	}
}


def installed() {
}

def updated() {
	unsubscribe()
}

mappings {
    path("/presence/:command") {
        action: [PUT: "updatePresence"]
    }
}

def updatePresence() {
    def cmd = params.command
    log.debug "command: $cmd"
    if (cmd == "1")
    {
    	presenceSensor.arrived()
    }
    else
    {
   		presenceSensor.departed()   
    }
}
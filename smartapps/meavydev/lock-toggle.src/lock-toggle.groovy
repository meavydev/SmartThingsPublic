/**
 * Lock Toggle 
 * by MeavyDev
 */

definition(
    name: "Lock Toggle",
    namespace: "meavydev",
    author: "MeavyDev",
    description: "Locks / Unlocks a lock.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("When this virtual switch is pressed ...") {
		input "virtualToggle", "capability.switch", title: "Virtual Switch", required: true, multiple: false
    }
	section("Lock/Unlock the lock...") {
		input "doorLock","capability.lock", multiple: true
        input("recipients", "contact", title: "Send notifications to") {
            input "spam", "enum", title: "Send Me Notifications?", options: ["Yes", "No"]
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
	subscribe(virtualToggle, "switch.on", pressed)
}

def pressed(evt) 
{
	toggleLock()	
}

def toggleLock()
{
	def lockState = doorLock.currentLock
    def isLocked = lockState.contains ("locked")
	log.debug "Toggle Lock: lock is currently $lockState / $isLocked"
   
	if (isLocked) 
    {
    	log.debug "Unlocking door"
        doorLock.unlock()
    	sendMessage("Door unlocked")
    } 
    else
    {
        log.debug "Locking door"
        doorLock.lock()
    	sendMessage("Door locked")
    }
}

def sendMessage(msg) {

    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (spam == "Yes") {
            sendPush msg
        }
    }
}

mappings 
{
    path("/press") 
    {
        action: [PUT: "updatePressed"]
    }
}

def updatePressed() 
{
    log.debug "updatePressed"
	toggleLock()
}
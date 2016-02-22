/* 
 * SRT321 Thermostat Scheduler by MeavyDev
 * Parent App for SRT thermostat schedules
 */
definition(
    name: "SRT321 Thermostat Scheduler",
    namespace: "meavydev/parent",
    author: "MeavyDev",
    description: "SRT Thermostat Schedule parent app",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@3x.png"
)


preferences 
{
    // The parent app preferences are pretty simple: just use the app input for the child app.
    page(name: "mainPage", title: "Thermostat Schedules", install: true, uninstall: true,submitOnChange: true) 
    {
        section 
        {
            app(name: "schedule", appName: "SRT321 Thermostat Schedule", namespace: "meavydev/schedules", title: "Create New Schedule", multiple: true)
        }
    }
}

def installed() 
{
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() 
{
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() 
{
    // nothing needed here, since the child apps will handle preferences/subscriptions
    // this just logs some messages for demo/information purposes
    log.debug "there are ${childApps.size()} child smartapps"
    childApps.each {child ->
        log.debug "child app: ${child.label}"
    }
}
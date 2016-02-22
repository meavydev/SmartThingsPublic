/* 
 * SRT321 Thermostat Schedule by MeavyDev
 * Child App for each SRT thermostat heat setpoint scheduled change
 * Can be daily / specific days with an optional mode override.
 */
definition(
    name: "SRT321 Thermostat Schedule",
    namespace: "meavydev/schedules",
    author: "MeavyDev",
    description: "Control the SRT321 Thermostat Set Point",
    category: "My Apps",

    // the parent option allows you to specify the parent app in the form <namespace>/<app name>
    parent: "meavydev/parent:SRT321 Thermostat Scheduler",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@3x.png"
)


preferences 
{
    page name: "mainPage", title: "Thermostat Schedule", install: false, uninstall: true, nextPage: "namePage"
    page name: "namePage", title: "Thermostat Schedule", install: true, uninstall: true
}

def installed() 
{
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() 
{
    log.debug "Updated with settings: ${settings}"
    unschedule()
    initialize()
}

def initialize() 
{
    // if the user did not override the label, set the label to the default
    if (!overrideLabel) 
    {
        app.updateLabel(defaultLabel())
    }
    
    unschedule() // bug in ST platform, doesn't clear on running
    def scheduleTime = timeToday(time, location.timeZone)
    def timeNow = now() + (2*1000) // ST platform has resolution of 1 minutes, so be safe and check for 2 minutes) 
    log.debug "Current time is ${(new Date(timeNow)).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}, scheduled time is ${scheduleTime.format("EEE MMM dd yyyy HH:mm z", location.timeZone)}"
    if (scheduleTime.time < timeNow) 
    { // If we have passed current time we're scheduled for next day
        log.debug "Current scheduling check time $scheduleTime has passed, scheduling check for tomorrow"
        scheduleTime = scheduleTime + 1 // Next day schedule
    }
    log.debug "Scheduling Temp change for " + scheduleTime.format("EEE MMM dd yyyy HH:mm z", location.timeZone)
    schedule(scheduleTime, setTheTemp)
}

// main page to select lights, the action, and turn on/off times
def mainPage() 
{
    dynamicPage(name: "mainPage") 
    {
        section ("Configuration")
        {
            thermostatInputs()
            temperatureInput()
            modeInput()
        }
        
        timeInput()
    }
}

// page for allowing the user to give the schedule a custom name
def namePage() 
{
    if (!overrideLabel) 
    {
        // if the user selects to not change the label, give a default label
        def l = defaultLabel()
        log.debug "will set default label of $l"
        app.updateLabel(l)
    }
    dynamicPage(name: "namePage") 
    {
        if (overrideLabel) 
        {
            section("Schedule name") 
            {
                label title: "Enter custom name", defaultValue: app.label, required: false
            }
        } 
        else 
        {
            section("Schedule name") 
            {
                paragraph app.label
            }
        }
        section 
        {
            input "overrideLabel", "bool", title: "Edit schedule name", defaultValue: "false", required: "false", submitOnChange: true
        }
    }
}

// inputs to select the thermostat
def thermostatInputs() 
{
    input "thermostats", "capability.thermostat", title: "Which thermostats do you want to control?", multiple: true, submitOnChange: true
}

// input to set the thermostat temperature
def temperatureInput() 
{
	input "heatingSetpoint", "decimal", title: "Thermostat set point", multiple: false, submitOnChange: true
}

// input to only run in a given mode
def modeInput() 
{
	input "smartThingsMode", "mode", title: "Only run when in this Mode", multiple: false, required: false, submitOnChange: true
}


// input for selecting the schedule time
def timeInput() 
{
    section("Time ") 
    {
        input "dayOfWeek", "enum",
            title: "Which day of the week?",
            required: true,
            multiple: true,
            options: [
            'All Week',
            'Monday to Friday',
            'Saturday & Sunday',
            'Monday',
            'Tuesday',
            'Wednesday',
            'Thursday',
            'Friday',
            'Saturday',
            'Sunday'
            ],
            defaultValue: 'All Week'
		input "time", "time", title: "At this time"
    }
}

// a method that will set the default label of the schedule.
// It uses the thermostats selected and temperature to create the schedule label
def defaultLabel() 
{
    def scheduleLabel = settings.thermostats.size() == 1 ? settings.thermostats[0].displayName : settings.thermostats[0].displayName + ", etc..."

	if (smartThingsMode)
    {
    	"Set $scheduleLabel to $settings.heatingSetpoint when in mode $settings.smartThingsMode"
    }
    else
    {
        "Set $scheduleLabel to $settings.heatingSetpoint"
    }
}


def setTheTemp() 
{
    def doChange = false
    Calendar localCalendar = Calendar.getInstance()
    localCalendar.setTimeZone(location.timeZone)
    int currentDayOfWeek = localCalendar.get(Calendar.DAY_OF_WEEK)

    // Check the condition under which we want this to run now
    // This set allows the most flexibility.
    if(dayOfWeek.contains('All Week')) 
    {
        doChange = true
    }
    else if((dayOfWeek.contains('Monday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.MONDAY) 
    {
        doChange = true
    }

    else if((dayOfWeek.contains('Tuesday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.TUESDAY) 
    {
        doChange = true
    }

    else if((dayOfWeek.contains('Wednesday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.WEDNESDAY) 
    {
        doChange = true
    }

    else if((dayOfWeek.contains('Thursday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.THURSDAY) 
    {
        doChange = true
    }

    else if((dayOfWeek.contains('Friday') || dayOfWeek.contains('Monday to Friday')) && currentDayOfWeek == Calendar.instance.FRIDAY) 
    {
        doChange = true
    }

    else if((dayOfWeek.contains('Saturday') || dayOfWeek.contains('Saturday & Sunday')) && currentDayOfWeek == Calendar.instance.SATURDAY) 
    {
        doChange = true
    }

    else if((dayOfWeek.contains('Sunday') || dayOfWeek.contains('Saturday & Sunday')) && currentDayOfWeek == Calendar.instance.SUNDAY) 
    {
        doChange = true
    }

    // some debugging in order to make sure things are working correclty
    log.debug "Calendar DOW: " + currentDayOfWeek
    log.debug "Configured DOW(s): " + dayOfWeek

	boolean modeOK = settings.smartThingsMode ? settings.smartThingsMode == location.currentMode : true;
    
    log.debug "Set $settings.heatingSetpoint with Mode OK: $modeOK for $location.currentMode"
    
    // If we have hit the condition to schedule this then lets do it
    if (doChange == true && modeOK == true)
    {
        thermostats.each 
        {
            it.setHeatingSetpoint(settings.heatingSetpoint)
        }
    }
    else 
    {
        log.debug "Temp change not scheduled for today."
    }

    log.debug "Scheduling next check"

    initialize() // Setup the next check schedule
}
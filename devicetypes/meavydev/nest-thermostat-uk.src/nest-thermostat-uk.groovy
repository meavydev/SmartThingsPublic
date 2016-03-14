/**
 * Nest UK V3 Thermostat with hot-water reporting
 * by MeavyDev
 *
 *  Original Author: patrick@patrickstuart.com
 *  Original Code: https://github.com/smartthings-users/device-type.nest
 */

preferences {
    input("username", "text", title: "Username", description: "Your Nest username (usually an email address)")
    input("password", "password", title: "Password", description: "Your Nest password")
    input("serial", "text", title: "Serial #", description: "The serial number of your thermostat")
}
 
 // for the UI
metadata {
    definition (name: "Nest Thermostat UK", namespace: "meavydev", author: "MeavyDev") {
        capability "Polling"
        capability "Relative Humidity Measurement"
        capability "Thermostat"
        capability "Actuator"
		capability "Sensor"
        capability "Refresh"
		capability "Configuration"
        capability "Location Mode"

        attribute "presence", "string"

        command "away"
        command "present"
        command "setPresence"
        command "setTempUp"
        command "setTempDown"
        command "setTemperature"
	}
    simulator {
    
    }

	tiles (scale: 2)
    {
        multiAttributeTile(name:"heatingSetpoint", type: "thermostat", width: 6, height: 4, canChangeIcon: true)
        {
            tileAttribute ("device.heatingSetpoint", key: "PRIMARY_CONTROL") 
            {
                attributeState("default", unit:"C", label:'${currentValue}°')
            }
            
            tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") 
            {
                attributeState("default", action: "setTemperature")
            }
            
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") 
            {
            	attributeState("default", label:'${currentValue}%', unit:"Humidity")
            }
           
            tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") 
            {
                attributeState("off", label:'${name}')
                attributeState("heat", label:'${name}')
            }
            
  			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") 
            {
    			attributeState("default", label:'${currentValue}', unit:"dC")
			}
            
            tileAttribute("device.thermostatMode", key: "OPERATING_STATE") 
            {
                attributeState("off", backgroundColor:"#44b621")
                attributeState("hot water", backgroundColor:"#7f5807")
                attributeState("heat", backgroundColor:"#ffa81e")
                attributeState("heat and hot water", backgroundColor:"#ff0000")
            }
        }
       
        standardTile("setPresence", "device.presence", inactiveLabel: false, decoration: "flat", width: 2, height: 2) 
        {
            state "present", label:'${name}', action:"away", icon: "st.Home.home2"
            state "not present", label:'${name}', action:"present", icon: "st.Transportation.transportation5"
        }
        
        valueTile("temperature", "device.temperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) 
        {
            state "default", label:'${currentValue}C', unit:"C"
        }
        
        valueTile("hotwater", "device.hotwaterMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) 
        {
            state "default", label:'Hot Water: ${currentValue}'
        }
        
        
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) 
        {
            tileAttribute ("device.battery", key: "PRIMARY_CONTROL")
            {
                state "battery", label:'Battery ${currentValue}V'
            }
        }
        
        standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2)
        {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}

        main "heatingSetpoint"
        details(["heatingSetpoint", "temperature", "hotwater", "setPresence", "refresh"])
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Recieved a parse request for: $description"
    
}

def setTempUp() 
{ 
    def newtemp = device.currentValue("heatingSetpoint").toInteger() + 1
    sendEvent(name: 'heatingSetpoint', value: newtemp)
    setTargetTemp(newtemp)
}

def setTempDown() 
{ 
    def newtemp = device.currentValue("heatingSetpoint").toInteger() - 1
    sendEvent(name: 'heatingSetpoint', value: newtemp)
    setTargetTemp(newtemp)
}

def setTemperature(temp) 
{
	sendEvent(name: 'heatingSetpoint', value: temp)
    setTargetTemp(temp)
}

// handle commands

def setTargetTemp(temp) 
{
    api('temperature', ['target_change_pending': true, 'target_temperature': device.currentValue("heatingSetpoint")]) 
    {
	}
}

def away() 
{
	log.debug "Away Nest"
    setPresence('away')
}

def present() 
{
	log.debug "Home Nest"
    setPresence('present')
}

def setPresence(status) 
{
    log.debug "Set Presence: $status"
    api('presence', ['away': status == 'away', 'away_timestamp': new Date().getTime(), 'away_setter': 0]) 
    {   
        def awayMode = status == 'away' ? 'not present' : 'present'
        log.debug "setPresence sending away: $awayMode"
        sendEvent(name: 'presence', value: awayMode)
    }
}

def refresh() 
{
	log.debug "nest refresh"
}

def poll() 
{
    log.debug "Executing 'poll'"
    api('status', []) {
    log.debug data.shared
        data.device = it.data.device.getAt(settings.serial)
        data.shared = it.data.shared.getAt(settings.serial)
        data.structureId = it.data.link.getAt(settings.serial).structure.tokenize('.')[1]
        data.structure = it.data.structure.getAt(data.structureId)
        
        // data.device.fan_mode = data.device.fan_mode == 'duty-cycle'? 'circulate' : data.device.fan_mode
        def awayMode = data.structure.away ? 'not present' : 'present'
        log.debug "Away: $data.structure.away value: $awayMode"
        //log.debug "Shared:   $data.shared"
        //log.debug "Device:   $data.device"

        def hotwater = data.device.hot_water_active
        log.debug "Hot water : $hotwater"
        sendEvent(name: 'hotwaterMode', value: hotwater ? "on" : "off")
        
        def batteryLevel = data.device.battery_level
        log.debug "Battery Level : $batteryLevel"
        sendEvent(name: 'battery', value: batteryLevel, unit: "V")
        
        def heat = data.shared.hvac_heater_state
        
        def mode = "off"
        if (heat && hotwater)
        {
        	mode = "heat and hot water"
        }
        else if (heat)
        {
        	mode = "heat"
        }
        else if (hotwater)
        {
        	mode = "hot water"
        }
        
        log.debug "Mode $mode : $data.shared.hvac_heater_state"
        sendEvent(name: 'thermostatMode', value: mode)
        
        sendEvent(name: 'humidity', value: data.device.current_humidity)
        String currentTemp = String.format("%2.1f", data.shared.current_temperature as Float)
        sendEvent(name: 'temperature', value: currentTemp, state: data.device.target_temperature_type)

        sendEvent(name: 'heatingSetpoint', value: (data.shared.target_temperature) as Double).round()

        sendEvent(name: 'presence', value: awayMode)
        
        //ambient_temperature_f?
        //has_leaf?
        //name = tstat name
    }
}

def api(method, args = [], success = {}) 
{
    if (method == null)
    {
        return
    }
    if(!isLoggedIn()) {
        log.debug "Need to login"
        login(method, args, success)
        return
    }

    def methods = [
        'status': [uri: "/v2/mobile/${data.auth.user}", type: 'get'],
        'temperature': [uri: "/v2/put/shared.${settings.serial}", type: 'post'],
        'presence': [uri: "/v2/put/structure.${data.structureId}", type: 'post']
    ]

    def request = methods.getAt(method)
    log.debug method
    //Potentially this is the only spot that triggers from Mode Changes
    log.debug "Current mode = ${location.mode}"

    doRequest(request.uri, args, request.type, success)
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) 
{
    log.debug "Calling $type : $uri : $args"
    
    if(uri.charAt(0) == '/') 
    {
        uri = "${data.auth.urls.transport_url}${uri}"
    }
    
    def params = [
        uri: uri,
        headers: [
            'X-nl-protocol-version': 1,
            'X-nl-user-id': data.auth.userid,
            'Authorization': "Basic ${data.auth.access_token}"
        ],
        body: args
    ]
    
    try 
    {
        if(type == 'post') 
        {
            httpPostJson(params, success)
        } 
        else if (type == 'get') 
        {    
            httpGet(params, success)
        }
    } 
    catch (Throwable e) 
    {
        login()
    }
}

def login(method = null, args = [], success = {}) 
{    
    def params = [
        uri: 'https://home.nest.com/user/login',
        body: [username: settings.username, password: settings.password]
    ]   
    
    httpPost(params) {response -> 
        data.auth = response.data
        data.auth.expires_in = Date.parse('EEE, dd-MMM-yyyy HH:mm:ss z', response.data.expires_in).getTime()
        log.debug data.auth
        
        api(method, args, success)
    }
}

def isLoggedIn() 
{
    if(!data.auth) 
    {
        log.debug "No data.auth"
        return false
    }
    
    def now = new Date().getTime();
    return data.auth.expires_in > now
}

def cToF(temp) 
{
    return temp * 1.8 + 32
}

def fToC(temp) 
{
    return (temp - 32) / 1.8
}

def configure() 
{
	log.debug "Configure() nest hit"
}
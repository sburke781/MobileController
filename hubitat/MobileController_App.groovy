import com.hubitat.app.DeviceWrapper;

definition(
    name: "Mobile Controller",
    namespace: "simnet",
    author: "Simon Burke (@sburke781)",
    description: "Capture the status and automate your mobile devices",
    category: "",
    iconUrl: "",
    iconX2Url: "",
    documentationLink: "https://github.com/sburke781/MobileController/blob/master/README.md",
    singleInstance: true
){}

preferences {
    page name:"mainPage"
    page name:"pageNewMobileDevice"
    page name:"pageCreateMobileDevice"
    page name:"pageDeviceConfiguration"
    page name:"pageApplyConfiguration"
}

// ####################################################################
// Common Methods

void installed() { updated() }
void initialize() {}
void uninstalled() {}

void updated() {
    log.info "updated: subscribing to mode changes"
    unsubscribe();
    subscribe(location, "mode", modeHandler)
    log.info "Mobile Controller App Updated"
}

// ####################################################################
// App API Mappings
//  API calls available to the mobile devices to interact with this App

mappings { 
    path("/setBattery") { action: [ POST: "setBattery" ] }
    path("/deviceHeartbeat") { action: [ POST: "deviceHeartbeat" ] }
    path("/reportWifiGroup") { action: [ POST: "reportWifiGroup" ] }
    path("/reportVpnStatus") { action: [ POST: "reportVpnStatus" ] }
    path("/fullStatusReport") { action: [POST: "fullStatusReport" ] }
    path("/modeChange") { action: [POST: "modeChange" ] }
    path("/call/incoming") { action: [POST: "callIncoming" ] }
    path("/call/started") { action: [POST: "callStarted" ] }
    path("/call/ended") { action: [POST: "callEnded" ] }
    path("/messages/unread") { action: [POST: "messagesUnread" ] }
    path("/messages/noneUnread") { action: [POST: "messagesNoneUnread" ] }
    path("/settings/callMonitoring") { action: [POST: "reportCallMonitoring" ] }
    path("/settings/messageMonitoring") { action: [POST: "reportMessageMonitoring" ] }
    path("/settings/bluetoothMonitoring") { action: [POST: "reportBluetoothMonitoring" ] }
    path("/settings/wifiMonitoring") { action: [POST: "reportWifiMonitoring" ] }
    path("/settings/allowVPN") { action: [POST: "allowVPN" ] }
    path("/bt/statusReport") { action: [POST: "btStatusReport" ] }
}

// ####################################################################
// Event Handlers

void modeHandler(evt) {
    log.info "modeHandler: mode changed to ${evt.value}";
    getChildDevices()?.each { dev ->
        dev.syncMode(evt.value);
    }
}

// ####################################################################
// Utility Methods

def getLocalUri() {
    return getFullLocalApiServerUrl();
}

def getCloudUri() {
    return "${getApiServerUrl()}/${hubUID}/apps/${app.id}"
}

// callHEDevice
//   Generic utility method used throughout the App to make calls to HE mobile devices based on a HTTP Request the App received from the device
//   Parameters:
//     method - name of both the calling method in the App and the method to call on the HE device
//     methodDesc - a short, user understandable description used in a warning log (if one is produced), e.g. "Battery reading"
//     passthrough - can be either an empty string, where no parameter is required for the HE device method being called,
//        or in the format "body[.xxx]" (either the full body or optionally, where xxx is the path/name of a JSON element in the request body Map)
//        identifying the information to be passed to the device method
//     request - the HTTP request object received from the mobile device, containing the device network id in the body of
//        the request, plus any other relevant data such as status update information

void callHEDevice(String method, String methodDesc, String passthrough, request){
    Map body = new groovy.json.JsonSlurper().parseText(request.body);
    debugLog("${method}: Device DNI = ${body.deviceId}, full HTTP Request body = ${body}");
    
    DeviceWrapper device = getChildDevices()?.find { it.deviceNetworkId == body.deviceId };
    if(device != null) { device."${method}(${passthrough})"; }
    else {
      warnLog("${methodDesc} received from a device that could not be found");
      debugLog("${method}: Device ${deviceNetworkId} could not be found");
    }
}

// ####################################################################
// Reporting of Call and Message Monitoring Events

void callIncoming()       { callHEDevice('callIncoming', 'Incoming call event', '', request) }
void callStarted()        { callHEDevice('callStarted', 'Call started event', '', request) } 
void callEnded()          { callHEDevice('callEnded', 'Call ended event', '', request) }
void messagesUnread()     { callHEDevice('messagesUnread', 'Messages unread event', '', request) }
void messagesNoneUnread() { callHEDevice('messagesNoneUnread', 'No unread messages event', '', request) }

// ####################################################################
// Reporting of Changes in Settings on the Device
//   Methods relating to reports from the mobile device when a change has been applied to settings
//   on the mobile device, such as the user choosing to allow call monitoring or bluetooth monitoring
//   These reports will trigger methods on the HE device to reflect this setting change having been made

void reportCallMonitoring()      { callHEDevice('callMonitoring', 'Call Monitoring', 'body.callMonitoring', request) }
void reportMessageMonitoring()   { callHEDevice('reportMessageMonitoring', 'Message Monitoring change', 'body.messageMonitoring', request) }
void reportBluetoothMonitoring() { callHEDevice('reportBluetoothMonitoring', 'Bluetooth Monitoring change', 'body.bluetoothMonitoring', request) }
void reportWifiMonitoring()      { callHEDevice('reportWifiMonitoring', 'Wi-Fi Monitoring change', 'body.wifiMonitoring', request) }
void reportAllowVPN()            { callHEDevice('reportAllowVPN', 'Allow VPN change', 'body.allowVPN', request) }

// ####################################################################
// Status Report and Heartbeat Methods
//   Methods relating to status information received from a mobile device,
//   typically to be recorded on the HE Device, such as battery percentage,
//   Wi-Fi Connection status, bluetooth devices, VPN status, etc

void setBattery()       { callHEDevice('setBattery', 'Battery reading', 'body.battery', request) }
void deviceHeartbeat()  { callHEDevice('deviceHeartbeat', 'Device Heartbeat', '', request) }
void reportWifiGroup()  { callHEDevice('reportWifiGroup', 'Wi-Fi Group Update', 'body.wifiGroup', request) }
void reportVpnStatus()  { callHEDevice('reportVpnStatus', 'VPN Status Update', 'body.vpnStatus', request) }
void fullStatusReport() { callHEDevice('fullStatusReport', 'Full Status Report', 'body', request) }
void btStatusReport()   { callHEDevice('btStatusReport', 'Bluetooth Status Report', 'body', request) }

// ####################################################################
// HE Control Methods
//   Methods relating to the mobile device initiating a change in the HE or some kind of automation trigger,
//   like a change in the HE mode (if allowed)

// Trigger HE Mode Change

def modeChange() {
    def body = new groovy.json.JsonSlurper().parseText(request.body);
    
    def device = getChildDevices()?.find { it.deviceNetworkId == body.deviceId };
    
    if(device != null && device.getCanControlHEMode()) {
        setLocationMode(body.mode)
    }
    else {
      warnLog("Mode Change request received from a device that could not be found");
      debugLog("modeChange: Device ${deviceNetworkId} could not be found");
    }
}

// ####################################################################
// App UI
//   Methods used to generate the pages and other UI elements in the App

// Tooltip Methods
String getZindexToggle(String setting, int low = 10, int high = 50) {
    return "<style> div:has(label[for^='settings[${setting}]']) { z-index: ${low}; } div:has(label):has(div):has(span):hover { z-index: ${high}; } </style>";
}

String getTooltipHTML(String heading, String tooltipText, String hrefURL, String hrefLabel='View Documentation'){
    return "<span class='help-tip'> <p> <span class='help-tip-header'>${heading}</span> <br/>${tooltipText}<br/> <a href='${hrefURL}' target='_blank'>${hrefLabel}</a> </p> </span>";
}

// Pages

def mainPage(){
    
    if(!state.accessToken){	
        createAccessToken()	
    }
    def hub = location.hubs[0];
    
    dynamicPage (name: "mainPage", title: "", install: true, uninstall: true) {
        if (app.getInstallationState() == 'COMPLETE') {   
            section("", hideable: false, hidden: false){
                href     name: "hrefMobileDevicesConfig",
                         page: "pageNewMobileDevice",
                       params: [:],
                        title: "Add New Mobile Device",
                  description: "",
                        state:  null
            }
            section("Configure Existing Mobile Devices", hideable: true, hidden: true){
              List<DeviceWrapper> existingDevices = getChildDevices();
              existingDevices?.each { dev ->
                  href(name: "deviceConfig", title: "Configure ${dev.getDisplayName()}", description: "Update device configuration", page: "pageDeviceConfiguration", params: ['deviceId' : dev.getDeviceNetworkId()], width: 4)
              }
            }
            section("Change Application Name", hideable: true, hidden: true){
               input "nameOverride", "text", title: "", multiple: false, required: false, submitOnChange: true, defaultValue: app.getLabel()
               if(nameOverride != app.getLabel) app.updateLabel(nameOverride)
            }
            section("Logging Preferences", hideable: true, hidden: true){
                // Logging Preferences
                input(name: "DebugLogging", type: "bool", title:"Enable Debug Logging", description:"Debug Logging will automatically turn off after 30 minutes", displayDuringSetup: true, defaultValue: false)
                input(name: "WarnLogging",  type: "bool", title:"Enable Warning Logging",                                                                         displayDuringSetup: true, defaultValue: true )
                input(name: "ErrorLogging", type: "bool", title:"Enable Error Logging",                                                                           displayDuringSetup: true, defaultValue: true )
                input(name: "InfoLogging",  type: "bool", title:"Enable Description Text (Info) Logging",                                                         displayDuringSetup: true, defaultValue: false)
            }
            section("Additional Information", hideable: true, hidden: true) {
		      paragraph "Cloud EndPoint: " + getCloudUri()
              paragraph "Local EndPoint: " + getLocalUri()
		    }
            
        } else {
		    section("") {
		      paragraph title: "Click Done", "Please click Done to install app before continuing"
		    }
	    }
    }
    
}

def pageNewMobileDevice(params) {
    dynamicPage (name: "pageNewMobileDevice", title: "New Mobile Device", nextPage: "pageCreateMobileDevice", install: false, uninstall: false) {
        section("") {
            
            input ("newMobileName", "string", title: "New Mobile Device Name",     required: true, submitOnChange: true)
            input ("newMobileIPAddress", "string", title: "New Mobile Device IP Address",     required: true, submitOnChange: true)
        }
    }
}

def pageCreateMobileDevice(params) {
    String newDevId = createMobileDevice();
    
    dynamicPage (name: "pageCreateMobileDevice", title: "New Mobile Device", nextPage: "pageDeviceConfiguration", install: false, uninstall: false) {
        section("") {
            paragraph "New device has been created.  Please click Next to configure your new mobile device"
        }
    }
}

def pageDeviceConfiguration(params) {
    
    DeviceWrapper mobileDevice = getChildDevice(params.deviceId);
    log.debug(mobileDevice.DeviceVPNAddress);
    String tooltipStyle = "<style> /* The icon */ .help-tip{     /* HE styling overrides */ 	box-sizing: content-box; 	white-space: collapse; 	 	display: inline-block; 	margin: auto; 	vertical-align: text-top; 	text-align: center; 	border: 2px solid white; 	border-radius: 50%; 	width: 16px; 	height: 16px; 	font-size: 12px; 	 	cursor: default; 	color: white; 	background-color: #2f4a9c; } /* Add the icon text, e.g. question mark */ .help-tip:before{     white-space: collapse; 	content:'?';     font-family: sans-serif;     font-weight: normal;     color: white; 	z-index: 10; } /* When hovering over the icon, display the tooltip */ .help-tip:hover p{     display:block;     transform-origin: 100% 0%;     -webkit-animation: fadeIn 0.5s ease;     animation: fadeIn 0.5s ease; } /* The tooltip */ .help-tip p {     /* HE styling overrides */ 	box-sizing: content-box; 	 	/* initially hidden */ 	display: none; 	 	position: relative; 	float: right; 	width: 178px; 	height: auto; 	left: 50%; 	transform: translate(204px, -90px); 	border-radius: 3px; 	box-shadow: 0 0px 20px 0 rgba(0,0,0,0.1);	 	background-color: #FFFFFF; 	padding: 12px 16px; 	z-index: 999; 	 	color: #37393D; 	 	text-align: center; 	line-height: 18px; 	font-family: sans-serif; 	font-size: 12px; 	text-rendering: optimizeLegibility; 	-webkit-font-smoothing: antialiased; 	 } .help-tip p a { 	color: #067df7; 	text-decoration: none; 	z-index: 100; } .help-tip p a:hover { 	text-decoration: underline; } .help-tip-header {     font-weight: bold; 	color: #6482de; } /* CSS animation */ @-webkit-keyframes fadeIn {     0% { opacity:0; }     100% { opacity:100%; } } @keyframes fadeIn {     0% { opacity:0; }     100% { opacity:100%; } } </style>";
    app.updateSetting("btMonitor", [value:mobileDevice.getBluetoothMonitoring()]);
    app.updateSetting("wifiMonitor", [value:mobileDevice.getWifiMonitoring()]);
    app.updateSetting("callMonitor", [value:mobileDevice.getCallMonitoring()]);
    app.updateSetting("msgMonitor", [value:mobileDevice.getMessageMonitoring()]);
    app.updateSetting("allowVPN", [value:mobileDevice.getAllowVPN()]);
    app.updateSetting("controlModes", [value:mobileDevice.getCanControlHEMode()]);
        
    dynamicPage (name: "pageDeviceConfiguration", title: "Mobile Device Configuration", nextPage: "pageApplyConfiguration", install: false, uninstall: false) {
        section("") {
            paragraph "Select the permissions you want to grant for Mobile Controller on your mobile device: ${tooltipStyle}"
            input ("btMonitor", "bool", title: "Monitor Bluetooth Connections? ${getZindexToggle('btMonitor')} ${getTooltipHTML('Bluetooth Monitoring', 'Allow Mobile Controller to detect devices paired to the mobile device.  Child devices will be created in HE to capture the connection status for each bluetooth device.', 'https://github.com/sburke781/MobileController/blob/master/Settings.md#bluetooth-monitoring')}",     required: true, submitOnChange: true)
            input ("wifiMonitor", "bool", title: "Monitor Wi-Fi Connections? ${getZindexToggle('wifiMonitor')} ${getTooltipHTML('Wi-Fi Monitoring','Allow Mobile Controller to detect connections to a specified list of Wi-Fi networks, allowing easy switching between local and cloud communications and contributing to presence detection.', 'https://github.com/sburke781/MobileController/blob/master/Settings.md#wi-fi-monitoring')}",     required: true, submitOnChange: true)
            input ("callMonitor", "bool", title: "Monitor Calls? ${getZindexToggle('callMonitor')} ${getTooltipHTML('Call Monitoring', 'Allow Mobile Controller to detect incoming, ongoing and missed calls, reporting the call status to HE.', 'https://github.com/sburke781/MobileController/blob/master/Settings.md#call-monitoring')}",     required: true, submitOnChange: true)
            input ("msgMonitor", "bool", title: "Monitor Messages? ${getZindexToggle('msgMonitor')} ${getTooltipHTML('Message Monitoring', 'Allow Mobile Controller to detect new or unread SMS/MMS messages on the mobile device, reporting the status back to HE.', 'https://github.com/sburke781/MobileController/blob/master/Settings.md#message-monitoring')}",     required: true, submitOnChange: true)
            input ("syncModes", "bool", title: "Synchronize HE Modes? ${getZindexToggle('syncModes')} ${getTooltipHTML('Synchronizing HE Modes', 'Changes to the HE mode will be communicated to and stored on the mobile device, allowing use of the mode in custom automations on the mobile device.', 'https://github.com/sburke781/MobileController/blob/master/Settings.md#synchronizing-he-modes')}",     required: true, submitOnChange: true)
            input ("controlModes", "bool", title: "Allow Control of HE Mode? ${getZindexToggle('controlModes')} ${getTooltipHTML('Control HE Modes', 'Allows changes to the HE mode to be initiated on the mobile device through elements such as a home-screen widget.', 'https://github.com/sburke781/MobileController/blob/master/Settings.md#control-he-modes')}",     required: true, submitOnChange: true)
            input ("cloudComms", "bool", title: "Allow Cloud Communication? ${getZindexToggle('cloudComms')} ${getTooltipHTML('Cloud Communication', 'Allows the mobile controller to send status updates and other commands from the mobile device when not connected to the HE hub over a local Wi-Fi or VPN connection.', 'https://github.com/sburke781/MobileController/blob/master/Settings.md#cloud-communication')}",     required: true, submitOnChange: true)
            input ("allowVPN", "bool", title: "Allow VPN Communication? ${getZindexToggle('allowVPN')} ${getTooltipHTML('VPN Communication', 'If a VPN connection is available (connected) on the mobile device, this will be used when communicating from HE to the mobile device.', 'https://github.com/sburke781/MobileController/blob/master/Settings.md#vpn-connection')}",     required: true, submitOnChange: true)
            input ("vpnIP", "string", title: "Mobile Device IP Address on VPN",     required: false, submitOnChange: true)
            paragraph "Click Next to apply the configuration settings to your mobile device"
        }
    }
}

def pageApplyConfiguration(params) {
    DeviceWrapper newMobileDevice = getChildDevice(state.deviceId);
    if(newMobileDevice != null) {
      newMobileDevice.configure();
      newMobileDevice.configureBulkPermissions(btMonitor, wifiMonitor, callMonitor, msgMonitor);
    }
    dynamicPage (name: "pageApplyConfiguration", title: "Mobile Device Configuration", nextPage: "mainPage", install: false, uninstall: false) {
        section("") {
            paragraph "Your mobile device has been configured.  Please click Next to return to the Main Manu"
        }
    }
}

// ####################################################################
// Device Methods

String createMobileDevice() {
    def dni = UUID.randomUUID().toString();
    def newMobileDevice = addChildDevice("simnet", "Mobile Device", dni, 1234, ["name": "${newMobileName}", isComponent: false]);
    newMobileDevice.updateSetting("DeviceIPAddress",[value: "${newMobileIPAddress}", type: 'string']);
    state.deviceId = newMobileDevice.getDeviceNetworkId();
    return newMobileDevice.getDeviceNetworkId();
}

//Logging Utility methods
void debugLog(debugMessage) {
	if (DebugLogging == true) {log.debug(debugMessage)}	
}

void errorLog(errorMessage) {
    if (ErrorLogging == true) { log.error(errorMessage)}  
}

void infoLog(infoMessage) {
    if(InfoLogging == true) {log.info(infoMessage)}    
}

void warnLog(warnMessage) {
    if(WarnLogging == true) {log.warn(warnMessage)}    
}

void debugOff() {

   log.warn("Disabling debug logging");
   device.updateSetting("DebugLogging", [value:"false", type:"bool"])
}

void updated_debugTimout() {
    if (DebugLogging) {
     log.debug "updated: Debug logging has been turned on and will be automatically disabled in ${debugAutoDisableMinutes} minutes"
     runIn(debugAutoDisableMinutes*60, "debugOff")
   }
   else { unschedule("debugOff") }
}

import groovy.json.JsonOutput;
import groovy.transform.Field

@Field static final Integer debugAutoDisableMinutes = 30


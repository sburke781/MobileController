definition(
    name: "Mobile Controller",
    namespace: "simnet",
    author: "Simon Burke (@sburke781)",
    description: "Capture the status and automate your mobile devices",
    category: "",
    iconUrl: "",
    iconX2Url: "",
    singleInstance: true
){}

preferences {
    page name:"mainPage"
    page name:"pageMobileDevicesConfig"
    page name:"pageCreateMobileDevice"
}

def installed() {
    updated()
}

def updated() {
    log.info "updated: subscribing to mode changes"
    unsubscribe();
    subscribe(location, "mode", modeHandler)
    log.info "Mobile Controller App Updated"
}

def modeHandler(evt) {
    log.info "modeHandler: mode changed to ${evt.value}";
    getChildDevices()?.each { dev ->
        dev.syncMode(evt.value);
    }
}

def initialize() {
}

def uninstalled() { 
}

// App API Mappings

mappings { 
    path("/setBattery") { action: [ POST: "setBattery" ] }
    path("/deviceHeartbeat") { action: [ POST: "deviceHeartbeat" ] }
    path("/reportWifiGroup") { action: [ POST: "reportWifiGroup" ] }
}

// Status Report Methods

def setBattery() { 
    def body = new groovy.json.JsonSlurper().parseText(request.body);
    //debugLog("setBattery: Device DNI = ${body.deviceId}, battery reading = ${body.battery}");
    
    def device = getChildDevices()?.find { it.deviceNetworkId == body.deviceId };
    if(device != null) { device.setBattery(body.battery); }
    else {
      //warnLog("setBattery: Battery reading received from a device that could not be found");
      //debugLog("setBattery: Device ${deviceNetworkId} could not be found");
    }
}

def deviceHeartbeat() {
    def body = new groovy.json.JsonSlurper().parseText(request.body);
    //debugLog("deviceHeartbeat: Device DNI = ${body.deviceId}");
    
    def device = getChildDevices()?.find { it.deviceNetworkId == body.deviceId };
    
    if(device != null) { device.deviceHeartbeat() }
    else {
      //warnLog("deviceHeartbeat: heartbeat received from a device that could not be found");
      //debugLog("deviceHeartbeat: Device ${deviceNetworkId} could not be found");
    }
}

def reportWifiGroup() {
    def body = new groovy.json.JsonSlurper().parseText(request.body);
    //debugLog("reportWifiGroup: Device DNI = ${body.deviceId}, Wi-Fi Group = ${body.wifiGroup}");
    
    def device = getChildDevices()?.find { it.deviceNetworkId == body.deviceId };
    
    if(device != null) { device.reportWifiGroup(body.wifiGroup); }
    else {
      //warnLog("reportWifiGroup: Wi-Fi Group update received from a device that could not be found");
      //debugLog("reportWifiGroup: Device ${deviceNetworkId} could not be found");
    }
    
}

// Utility Methods

def getLocalUri() {
    return getFullLocalApiServerUrl();
}

def getCloudUri() {
    return "${getApiServerUrl()}/${hubUID}/apps/${app.id}"
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
                //pageMobileDevicesConfig
                href     name: "hrefMobileDevicesConfig",
                         page: "pageMobileDevicesConfig",
                       params: [:],
                        title: "Configure New Mobile Device",
                  description: "",
                        state:  null
            }
            section("Existing Mobile Devices", hideable: true, hidden: true){
              List<com.hubitat.app.DeviceWrapper> existingDevices = getChildDevices();
              existingDevices?.each { dev ->
                  paragraph "<a href='http://${hub.getDataValue('localIP')}/device/edit/${dev.getId()}' title='${dev.getDisplayName()}' target='_blank'>" + dev.getDisplayName() + "</a>"
              }
            }
            section("Change Application Name", hideable: true, hidden: true){
               input "nameOverride", "text", title: "", multiple: false, required: false, submitOnChange: true, defaultValue: app.getLabel()
               if(nameOverride != app.getLabel) app.updateLabel(nameOverride)
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

def pageMobileDevicesConfig(params) {
    dynamicPage (name: "pageMobileDevicesConfig", title: "Mobile Device Configuration", nextPage: "pageCreateMobileDevice", install: false, uninstall: false) {
        section("") {
            
            input ("newMobileName", "string", title: "New Mobile Device Name",     required: true, submitOnChange: true)
            input ("newMobileIPAddress", "string", title: "New Mobile Device IP Address",     required: true, submitOnChange: true)
        }
    }
}

def pageCreateMobileDevice(params) {
    createMobileDevice();
    
    dynamicPage (name: "pageCreateMobileDevice", title: "Mobile Device Configuration", nextPage: "mainPage", install: false, uninstall: false) {
        section("") {
            paragraph "New device has been created.  Please click Next to return to the main page"
        }
    }
}


// Device Methods

def createMobileDevice() {
    def dni = "mobileDevice${(int)((new Date()).getTime())}";
    def newMobileDevice = addChildDevice("simnet", "Mobile Device", dni, 1234, ["name": "${newMobileName}", isComponent: false]);
    newMobileDevice.updateSetting("DeviceIPAddress",[value: "${newMobileIPAddress}", type: 'string']);
    newMobileDevice.configure();
}

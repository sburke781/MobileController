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

mappings { 
    path("/setBattery") { action: [ POST: "setBattery" ] }
    path("/deviceHeartbeat") { action: [ POST: "deviceHeartbeat" ] }
}

def setBattery() { 
    //log.info "setBattery: params = $params"
    //log.info "setBattery: request = $request"
    //log.info "setBattery: request body = $request.body"
    def body = new groovy.json.JsonSlurper().parseText(request.body);
    //log.info "setBattery: Device ID = $body.deviceId"
    //log.info "setBattery: Battery = $body.battery"
    def device = getChildDevices()?.find { it.deviceNetworkId == body.deviceId };
    device.setBattery(body.battery);
}

def deviceHeartbeat() {
    //log.info "deviceHeartbeat: params = $params"
    //log.info "deviceHeartbeat: request = $request"
    //log.info "deviceHeartbeat: request body = $request.body"
    def body = new groovy.json.JsonSlurper().parseText(request.body);
    //log.info "deviceHeartbeat: Device ID = $body.deviceId"
    def device = getChildDevices()?.find { it.deviceNetworkId == body.deviceId };
    //log.info "deviceHeartbeat: device DNI = ${device.deviceNetworkId}";
    
    device.deviceHeartbeat();
}

def getLocalUri() {
    return getFullLocalApiServerUrl();
}

def getCloudUri() {
    return "${getApiServerUrl()}/${hubUID}/apps/${app.id}"
}

def mainPage(){
    
    if(!state.accessToken){	
        createAccessToken()	
    }
    
    dynamicPage (name: "mainPage", title: "", install: true, uninstall: true) {
        if (app.getInstallationState() == 'COMPLETE') {   
            section("Mobile Devices", hideable: true, hidden: false){
                //pageMobileDevicesConfig
                href     name: "hrefMobileDevicesConfig",
                         page: "pageMobileDevicesConfig",
                       params: [:],
                        title: "Mobile Devices Configuration",
                  description: "<span style=\"padding-left: 33px;\">Click to configure...</span>",
                        state:  null
            }
            section("Change Application Name", hideable: true, hidden: true){
               input "nameOverride", "text", title: "New Name for Application", multiple: false, required: false, submitOnChange: true, defaultValue: app.getLabel()
               if(nameOverride != app.getLabel) app.updateLabel(nameOverride)
            }
            section("Cloud EndPoint") {
		      paragraph getCloudUri()
		    }
            section("Local EndPoint") {
		      paragraph getLocalUri()
		    }
            
        } else {
		    section("") {
		      paragraph title: "Click Done", "Please click Done to install app before continuing"
		    }
	    }
    }
    
    /*
    def localUri = getLocalUri()
    def cloudUri = getCloudUri()
    

    return dynamicPage(name: "pageMain", install: true,  uninstall: true, refreshInterval:0) {
        
        section("Webhooks") {
            paragraph("<ul><li><strong>Local</strong>: <a href='${localUri}' target='_blank' rel='noopener noreferrer'>${localUri}</a></li></ul>")
            paragraph("<ul><li><strong>Cloud</strong>: <a href='${cloudUri}' target='_blank' rel='noopener noreferrer'>${cloudUri}</a></li></ul>")
            
        }   
    }    
    */
    
}

def pageMobileDevicesConfig(params) {
    dynamicPage (name: "pageMobileDevicesConfig", title: "Mobile Devices Configuration", install: false, uninstall: false) {
        section("") {
            
            input ("mobileDevice", "device.MCMobileDevice",    title: "Mobile Devices:", multiple: true, required: false, submitOnChange: true)
            input ("newMobileName", "string", title: "New Mobile Device Name",     required: true, submitOnChange: true)
            input ("newMobileIPAddress", "string", title: "New Mobile Device IP Address",     required: true, submitOnChange: true)
            input ("createMobileDevice", "button", title: "Create Mobile Device")
        }
    }
}

def appButtonHandler(btn) {
      switch(btn) {
          case "createMobileDevice":  createMobileDevice()
               break
      }
}

def createMobileDevice() {
    def dni = "mobileDevice${new Date()}";
    def newMobileDevice = addChildDevice("simnet", "MC Mobile Device", dni, 1234, ["name": "${newMobileName}", isComponent: false]);
    newMobileDevice.updateSetting("DeviceIPAddress",[value: "${newMobileIPAddress}", type: 'string']);
}

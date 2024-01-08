/**
 *  Mobile Controller - Mobile Device Driver
 *
 *  Copyright 2022 Simon Burke
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2022-09-11  Simon Burke    Original Creation
  */

public static String mcURLPath() { return "mc"; }

metadata {
	definition (name: 'Mobile Device', namespace: 'simnet', author: 'Simon Burke') {
        
        capability 'Battery'
        //capability 'MotionSensor'
        //capability 'PresenceSensor'
        capability 'AudioVolume'
        capability "MusicPlayer"
        capability 'Notification'
        capability 'Configuration'
        
        
        // Battery Sensor Attributes
        attribute 'battery', 'number'
        attribute 'batteryStatus', 'ENUM["Charging", "Discharging", "Idle"]'
        
        //Motion Sensor Attribute
        attribute 'motion', 'ENUM ["inactive", "active"]'
        
        // Audio Volume Attributes
        attribute 'mute', 'ENUM ["unmuted", "muted"]'
        attribute 'volume', 'NUMBER' //, unit:'%'
        
        //Presence Sensor Attribute
        attribute 'presence', 'ENUM ["present", "not present"]'
        
        //Switch Capability Attribute
        attribute 'switch', 'ENUM ["on", "off"]'
        
        //Network Attributes
        attribute 'wifiGroup', 'string'  // Name for the group the current SSID sits in, e.g. HOME, WORK, etc
        
        
        //Call and Message Attributes
        /*
        attribute 'callStatus', 'ENUM["Incoming", "InCall", "Idle", "unknown"]'
        attribute 'missedCall', 'ENUM["yes", "no", "unknown"]'
        attribute 'unreadMessage', 'ENUM["yes", "no", "unknown"]'
        */
        
        // General Attributes
        attribute 'lastUpdated', 'date'
        attribute 'heartbeat', 'date'
        attribute 'brightness', 'number'
        
        // Battery Sensor Command
        command 'setBattery', [[name:'batteryReading', type: 'NUMBER', description: 'Enter the new battery reading (%)' ] ]
        
        //Motion Sensor Capability Commands
        //command 'active'
        //command 'inactive'
        
        //Presence Sensor Commands (not part of the capability)
        //command 'present'
        //command 'notPresent'
        
        // Audio Volume Capability Commands
        command 'mute'
        command 'setVolume', [[name:'volumelevel', type: 'NUMBER', description: 'Enter the new volume value (0-25)' ] ]
        command 'unmute'
        command 'volumeDown'
        command 'volumeUp'
        
        // Music Player Capability Commands
        command 'previousTrack'
        command 'nextTrack'
        command 'pause'
        command 'play'
        command 'stop'
        command 'setLevel', [[name:'volumelevel', type: 'NUMBER', description: 'Enter the new volume value (0-25)' ] ]
        
        // These commands will not be supported        
        //command 'playText', [[name:'text', type: 'string', description: 'Enter the text to play' ] ]
        //command 'playTrack', [[name:'trackuri', type: 'string', description: 'Enter the track URL/URI to play' ] ]
        //command 'restoreTrack', [[name:'trackuri', type: 'string', description: 'Enter the track URL/URI to restore' ] ]
        //command 'resumeTrack', [[name:'trackuri', type: 'string', description: 'Enter the track URL/URI to play' ] ]
        //command 'setTrack', [[name:'trackuri', type: 'string', description: 'Enter the track URL/URI' ] ]
        
        // These commands are listed under the Audio Volume capability commands
        //command 'mute'
        //command 'unmute'
        
        //Notification Capability Command
        command 'deviceNotification', [[name:'text', type: 'STRING', description: 'Enter the notification text' ] ]
        
        //Custom Notification commands
        //Send Notification with Title Provided
        command 'customDeviceNotification', [[name:'title', type: 'STRING', description: 'Enter the title for the notification' ], [name:'text', type: 'STRING', description: 'Enter the notification text' ] ]
        //Cancel a notification sent to the device
        command 'cancelNotification', [[name:'title', type: 'STRING', description: 'Enter the title for the notification to cancel' ] ]
        
        //Notification Settings commands
        command 'notificationVibrate'
        command 'notificationSound'
        command 'notificationMute'
        
        //Do Not Disturb commands
        command 'doNotDisturbOn', [[name:'setting', type: 'STRING', description: 'Enter the do not disturb setting (noInt, priority or alarms)' ] ]
        command 'doNotDisturbOff'
        
        //Alarm Commands
        command 'dismissAlarm', [[name:'label', type: 'STRING', description: 'Enter the label for the alarm to dismiss' ] ]
        
        //Custom Screen Commands
        command 'turnScreenOn'
        command 'turnScreenOff'
        command 'setBrightness', [[name:'brightnessVal', type: 'NUMBER', description: 'Enter the new brightness value (%)' ] ]
        
        command 'configureHomeWifiList', [[name:'ssidList', type: 'STRING', description: 'List of SSIDs to track under the HOME Group, separated by a /' ] ]
        command 'configureAltWifiList',  [[name:'ssidList', type: 'STRING', description: 'List of SSIDs to track under the ALTERNATE Group, separated by a /' ] ]
        command 'deviceHeartbeat'
        
        
    }    
    
    preferences {
    
      // Platform and authentication Preferences
        def CommandMethodOptions = []
            CommandMethodOptions << ["Tasker"   : "Direct Tasker HTTP Request" ]
            CommandMethodOptions << ["AutoRemote"  : "AutoRemote Message"]
            //CommandMethodOptions << ["MacroDroid" : "MacroDroid"   ]
        
      // Device Preferences
      input name: "CommandMethod", type: "enum",     title: "Command Method", description: "Method for sending commands to the mobile device", displayDuringSetup: true, required: true, multiple: false, options: CommandMethodOptions, defaultValue: "Tasker"
      input(name: "DeviceIPAddress", type: "string", title:"Mobile Device IP Address", displayDuringSetup: true, defaultValue: "")
      input(name: "Port", type: "number", title:"Port Number", description: "Port used when sending HTTP calls to the mobile device", displayDuringSetup: true, defaultValue: 1821)
      input(name: "ARPort", type: "number", title:"Auto-Remote Port Number", description: "Port used when sending Auto-Remote messages to the mobile device", displayDuringSetup: true, defaultValue: 1817)
              
      input(name: "CloudComm", type: "bool", title:"Use Cloud Communications?", description: "Turn on to use cloud communications back to HE when not on Wi-Fi", displayDuringSetup: true, defaultValue: false)
      
      input(name: "SyncHEMode", type: "bool", title:"Sync HE Mode?", description: "Turn on to send HE mode updates to the mobile device", displayDuringSetup: true, defaultValue: false)
      
      //input(name: "TrackCallStatus", type: "bool", title:"Track Mobile Device Call Status?", description: "Turn on to track whether the mobile device is in a call or not", displayDuringSetup: true, defaultValue: false)
      //input(name: "TrackMissedCalls", type: "bool", title:"Track Mobile Device Missed Calls?", description: "Turn on to track whether the mobile device has any missed calls", displayDuringSetup: true, defaultValue: false)
      
      
    }
}

//Common Methods
void installed() { initialized() }

void initialized() {

 state.warningCount = 0
 //Detect whether battery status exists, if not, set to Idle
  //sendEvent(name: "batteryStatus", value: "Idle");
}

void updated() {
    //Use library method to set schedule to disable debug logging if turned on
    updated_debugTimout();
}

//Configure Method
void configure() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/hecloudendpoint", parent.getCloudUri()); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_he_cloud_api_endpoint", parent.getCloudUri()); }
    if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/helocalendpoint", parent.getLocalUri()); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_he_local_api_endpoint", parent.getLocalUri()); }
    if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/heaccesstoken", parent.state.accessToken); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_he_access_token", parent.state.accessToken); }
    if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/hedeviceid", "${device.deviceNetworkId}"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_he_device_id", "${device.deviceNetworkId}"); }
}

// Send Wifi Networks considered part of the Home network
def configureHomeWifiList(String ssidList) {
    
  // Send Home Wifi SSID List
  if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/homewifilist", "${ssidList}"); }
  if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_home_wifi_list", "${ssidList}"); }   
}

// Send Wifi Networks considered part of the Alternate network, e.g. Work
def configureAltWifiList(String ssidList) {
    
  // Send Alternate Wifi SSID List
  if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/altwifilist", "${ssidList}"); }
  if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_alt_wifi_list", "${ssidList}"); }   
}

// Network methods
void reportWifiGroup(String groupName) {
    sendEvent(name: 'wifiGroup', value: groupName);
    debugLog("reportWifiGroup: Device is now on the ${groupName} network");

    setLastUpdate();
}

void syncMode(String newMode) {
    if(SyncHEMode) {
        if(CommandMethod == "Tasker"){ sendTaskerCommand("mode/record", newMode); }
        if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mode", newMode); }
    }
}

//Battery Sensor Methods
void setBattery(Number pbattery) {
    debugLog("setBattery: New battery reading is ${pbattery}");
    
    String vbatteryStatus = getBatteryStatus()

    //Check that the reading is within the 0 - 100 range for a percentage value
    if(pbattery >= 0 && pbattery <= 100) {
        if (getBattery() == null || getBattery() == pbattery) { vbatteryStatus = 'Idle' }
        else {
            if (getBattery() > pbattery) { vbatteryStatus = 'Discharging' }
            else { vbatteryStatus = 'Charging' }
        }
        //Update the battery and batteryStatus attributes
        sendEvent(name: 'battery',       value: pbattery)
        sendEvent(name: 'batteryStatus', value: vbatteryStatus)
        debugLog("setBattery: Battery attribute updated to ${pbattery} and status to ${vbatteryStatus}");
        
        setLastUpdate();

        //Reset warning count if there have been previous warnings
        if (state.warningCount > 0) {
            state.warningCount = 0
            infoLog('setBattery: warning count reset')
        }
    }
    // If the battery reading is outside the 0 - 100 range, log a warning and leave the current reading in place
    //   use the warning count state variable to make sure we don't spam the logs with repeated warnings
    else {
        if (state.warningCount < 10) {
            state.warningCount = state.warningCount + 1
            warnLog("setBattery: Warning (${state.warningCount}) - battery level outside of 0-100 range, device not updated.  Battery value provided = ${pBattery}")
        }
    }
}

Number getBattery()       { return device.currentValue('battery') }

String getBatteryStatus() { return device.currentValue('batteryStatus') }

//Brightness Methods
void setBrightness(Number brightnessVal) {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("screen/setBrightness", "${brightnessVal}"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("setBrightness", "${brightnessVal}"); }
    sendEvent(name: 'brightness', value: brightnessVal);
    
    infoLog("Device brightness updated to ${brightnessVal}");
    setLastUpdate();
}

//Audio Volume Methods
void mute() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("volume/media/mute", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaVolumeMute", ""); }
    
    infoLog("Device has been muted");
    setLastUpdate();
}

void unmute() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("volume/media/unmute", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaVolumeUnmute", ""); }
    
    infoLog("Device has been unmuted");
    setLastUpdate();
}

void setVolume(volumelevel) { 
    if(CommandMethod == "Tasker"){ sendTaskerCommand("volume/media/set", "${volumelevel}"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("setMediaVolume", "${volumelevel}"); }
    
    infoLog("Device media volume has been adjusted to ${volumelevel}");
    setLastUpdate();
}

void volumeDown() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("volume/media/down", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaVolumeDown", ""); }
    
    infoLog("Device media volume has been turned down");
    setLastUpdate();
}

void volumeUp() { 
    if(CommandMethod == "Tasker"){ sendTaskerCommand("volume/media/up", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaVolumeUp", ""); }
    
    infoLog("Device media volume has been turned up");
    setLastUpdate();
}


//Music Player Methods

void previousTrack() {
 
    if(CommandMethod == "Tasker"){ sendTaskerCommand("media/previousTrack", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaPreviousTrack", ""); }
    
    infoLog("Previous Track was selected");
    setLastUpdate();
}

void nextTrack() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("media/nextTrack", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaNextTrack", ""); }
    
    infoLog("Next Track was selected");
    setLastUpdate();
}

void pause() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("media/pause", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaPause", ""); }
    
    infoLog("Pause was selected");
    setLastUpdate();
}

void play() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("media/play", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaPlay", ""); }
    
    infoLog("Play was selected");
    setLastUpdate();
}

void stop() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("media/stop", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaStop", ""); }
    
    infoLog("Stop was selected");
    setLastUpdate();
}


void setLevel(volumelevel) {
    
    setVolume(volumelevel);
}
        
        
//Notification Settings Methods
void notificationVibrate() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/vibrate", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("notifyVibrate", ""); }
    
    infoLog("Notifications will now use Vibrate setting");
    setLastUpdate();
}

void notificationSound() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/sound", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("notifySound", ""); }
    
    infoLog("Notifications will now use Sound setting");
    setLastUpdate();
}

void notificationMute() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/mute", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("notifyMute", ""); }
    
    infoLog("Notifications will now use Mute setting");
    setLastUpdate();
}






//Notification Methods
void deviceNotification(String text) {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/notify", "Mobile Controller||${text}"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("notify", "Mobile Controller||${text}"); }
    
    infoLog("Device notification has been sent");
    debugLog("deviceNotification: Notification text = ${text}");
    setLastUpdate();
}

void customDeviceNotification(String title, String text){
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/notify", "${title}||${text}"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("notify", "${title}||${text}"); }
    
    infoLog("Device custom notification has been sent");
    debugLog("customDeviceNotification: Notification title = ${title}, text = ${text}");
    setLastUpdate();
}

void cancelNotification(String title) {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/cancel", title); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("cancelNotification", title); }
    
    infoLog("Device notification has been cancelled");
    debugLog("cancelNotification: Notification cancelled with title = ${title}");
    setLastUpdate();
}

//Do Not Disturb Methods

void doNotDisturbOn(String setting) {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("donotdisturb", setting); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("setDoNotDisturb", setting); }
    
    infoLog("Do Not Disturb has been turned on");
    setLastUpdate();    
}

void doNotDisturbOff() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("donotdisturb", "allowAll"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("setDoNotDisturb", "allowAll"); }
    
    infoLog("Do Not Disturb has been turned off");
    setLastUpdate();
}


//Custom Screen Methods
void turnScreenOn() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("screen/on", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("screenOn", ""); }
    
    infoLog("Device screen has been turned on");
    setLastUpdate();
}

void turnScreenOff() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("screen/off", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("screenOff", ""); }
    
    infoLog("Device screen has been turned off");
    setLastUpdate();
}

// Alarm Methods
def dismissAlarm(String label){
   if(CommandMethod == "Tasker"){ sendTaskerCommand("alarm/dismiss", label); }
   if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("dismissAlarm", label); }
   
   infoLog("Alarm has been dismissed");
   debugLog("dismissAlarm: Alarm has been dismissed with label = ${label}");
   setLastUpdate();
}

//Device Heartbeat
def deviceHeartbeat() {
 
    Date heartbeatDate = new Date()
    sendEvent(name: 'heartbeat', value: heartbeatDate.format('dd/MM/yyyy HH:mm'));
    
}

//Last Update
def setLastUpdate() {
    //Update the lastUpdated attribute value
    Date lastUpdate = new Date()
    sendEvent(name: 'lastUpdated', value : lastUpdate.format('dd/MM/yyyy HH:mm'))
}

//Tasker and AutoRemote Command Methods
def sendTaskerCommand(String path, String body){
    debugLog("sendTaskerCommand: Path = ${path}, Body = ${body}, IP Address = ${DeviceIPAddress}, Port = ${Port}, MC URL = ${mcURLPath()}");
    try {
        httpPost([uri: "http://${DeviceIPAddress}:${Port}/${mcURLPath()}/${path}", contentType: "application/json", body: "${body}"]) { resp ->
            debugLog("sendTaskerCommand: Command sent.  Response = ${resp.data}")
        }
    }
    catch(Exception e) {
        errorLog("sendTaskerCommand: ${e}");
    }
}

def sendAutoRemoteCommand(String command, String value){
    try {
        httpGet([uri: "http://${DeviceIPAddress}:${ARPort}/?message=${command}=:=${java.net.URLEncoder.encode(value)}"]) { resp ->
            debugLog("sendAutoRemoteCommand: Command sent.  Response = ${resp.data}")
        }
    }
    catch(Exception e){
        errorLog("sendAutoRemoteCommand: ${e}");
    }
}

#include simnet.logging
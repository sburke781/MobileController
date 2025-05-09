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
        
        capability 'AudioVolume'
        capability "MusicPlayer"
        capability 'Notification'
        capability 'Configuration'
        
        
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
        attribute 'wifiGroup', 'string'  // Name for the group the current SSID sits in, e.g. HOME, ALT(ERNATE), AWAY etc
        attribute 'vpn', 'ENUM["on", "off"]'
        
        //Call and Message Attributes
        attribute 'callStatus', 'ENUM["Incoming", "InCall", "Idle", "unknown"]'
        attribute 'unreadMessage', 'ENUM["yes", "no", "unknown"]'
        
        
        // General Attributes
        attribute 'heartbeat', 'date'
        attribute 'screen', 'string'    // Screen status, on or off
        attribute 'brightness', 'number'
        attribute 'ringVolume', 'number'
        attribute 'doNotDisturb', 'string'
        attribute 'notificationAlert', 'string'
        
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
        command 'playText', [[name:'text', type: 'string', description: 'Enter the text to play' ] ]
        
        // These commands are not currently supported
        
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
        
        //Custom App Launch Commands
        command 'launchApp', [[name:'appName', type: 'STRING', description: 'Name of the App to launch on the mobile device' ] ]
        
        //Network Commands
        command 'configureHomeWifiList', [[name:'ssidList', type: 'STRING', description: 'List of SSIDs to track under the HOME Group, separated by a /' ] ]
        command 'configureAltWifiList',  [[name:'ssidList', type: 'STRING', description: 'List of SSIDs to track under the ALTERNATE Group, separated by a /' ] ]
        
        //Heartbeat and General Status Report
        command 'deviceHeartbeat'
        command 'runFullStatusReport'
    }    
    
    preferences {
    
      // Platform and authentication Preferences
        def CommandMethodOptions = []
            CommandMethodOptions << ["Tasker"   : "Direct Tasker HTTP Request" ]
            CommandMethodOptions << ["AutoRemote"  : "AutoRemote Message"]
            //CommandMethodOptions << ["MacroDroid" : "MacroDroid"   ]
        
      // Device Preferences
      input name: "CommandMethod", type: "enum",     title: "Command Method", description: "Method for sending commands to the mobile device", displayDuringSetup: true, required: true, multiple: false, options: CommandMethodOptions, defaultValue: "Tasker"
      input(name: "DeviceIPAddress", type: "string", title:"Mobile Device Local IP Address", displayDuringSetup: true, defaultValue: "")
      input(name: "DeviceVPNAddress", type: "string", title:"Mobile Device VPN IP Address", displayDuringSetup: true, defaultValue: "")
      input(name: "Port", type: "number", title:"Port Number", description: "Port used when sending HTTP calls to the mobile device", displayDuringSetup: true, defaultValue: 1821)
      input(name: "ARPort", type: "number", title:"Auto-Remote Port Number", description: "Port used when sending Auto-Remote messages to the mobile device", displayDuringSetup: true, defaultValue: 1817)
              
      input(name: "CloudComm", type: "bool", title:"Use Cloud Communications?", description: "Turn on to use cloud communications back to HE when not on Wi-Fi", displayDuringSetup: true, defaultValue: false)
      
      input(name: "SyncHEMode", type: "bool", title:"Sync HE Mode?", description: "Turn on to send HE mode updates to the mobile device", displayDuringSetup: true, defaultValue: false)
      input(name: "CanControlHEMode", type: "bool", title:"Control HE Mode?", description: "Allow device to control HE mode", displayDuringSetup: true, defaultValue: false)
    }
}

//Custom App Launch Methods
void launchApp(String appName) {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("app/launch", appName); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("appLaunch", appName); }
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

boolean getCanControlHEMode() {
   return CanControlHEMode; 
}

// Configuration Methods - Applying Configuration Changes on the Mobile Device

// Base Configuration - Local and Cloud URL's, HE App Access Token and DNI
void configure() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/hecloudendpoint", parent.getCloudUri()); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_he_cloud_api_endpoint", parent.getCloudUri()); }
    if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/helocalendpoint", parent.getLocalUri()); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_he_local_api_endpoint", parent.getLocalUri()); }
    if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/heaccesstoken", parent.state.accessToken); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_he_access_token", parent.state.accessToken); }
    if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/hedeviceid", "${device.deviceNetworkId}"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_he_device_id", "${device.deviceNetworkId}"); }
    
    // Now that the device is configured, get an initial report of the status of the device
    runFullStatusReport();
}

private void configureBulkPermissions(boolean btMonitor, boolean wifiMonitor, boolean callMonitor, boolean msgMonitor) {
  
  // Bulk Permission Update
  if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/bulk", "{\"bt_monitor\": \"${btMonitor}\", \"wifi_monitor\": \"${wifiMonitor}\", \"call_monitor\": \"${callMonitor}\", \"message_monitor\": \"${msgMonitor}\" }"); }
  if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_config_bulk", "${btMonitor}, ${wifiMonitor}, ${callMonitor}, ${msgMonitor}"); }
  
}

void configureCallMonitoring() {
    
  // Prompt For Call Monitoring Permission
  if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/callmonitoring", ""); }
  if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_call_monitor", ""); }     
}

void configureMessageMonitoring() {
    
  // Prompt For Message Monitoring Permission
  if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/messagemonitoring", ""); }
  if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_message_monitor", ""); }     
}

void configureBluetoothMonitoring() {
    
  // Prompt For Bluetooth Monitoring Permission
  if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/bluetoothmonitoring", ""); }
  if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_bluetooth_monitor", ""); }     
}

void configureWifiMonitoring() {
    
  // Prompt For Wi-Fi Monitoring Permission
  if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/wifimonitoring", ""); }
  if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_wifi_monitor", ""); }     
}

void configureHomeWifiList(String ssidList) {
    
  // Send Wifi Networks considered part of the Home network
  if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/homewifilist", "${ssidList}"); }
  if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_home_wifi_list", "${ssidList}"); }   
}

void configureAltWifiList(String ssidList) {

  // Send Wifi Networks considered part of the Alternate network, e.g. Work
  if(CommandMethod == "Tasker"){ sendTaskerCommand("configuration/altwifilist", "${ssidList}"); }
  if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_alt_wifi_list", "${ssidList}"); }   
}

// Status Reporting

// Initiate a full status report on the mobile device
void runFullStatusReport() {
    
  // Initiate a Full Status Report from the mobile device
  if(CommandMethod == "Tasker"){ sendTaskerCommand("status/fullreport", ""); }
  if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mc_status_full", ""); }
}

// Process the results from a full status report
void fullStatusReport(results) {
    if(results.battery) { setBattery(results.battery) }
    if(results.brightness || results.brightness == 0) { updateBrightness(results.brightness) }
    if(results.screen) { updateScreen(results.screen) }
    if(results.ringVolume || results.ringVolume == 0) { updateRingVolume(results.ringVolume) }
    if(results.mediaVolume || results.mediaVolume == 0) { updateMediaVolume(results.mediaVolume) }
    if(results.mediaMuted) { updateMediaMuted(results.mediaMuted) }
    if(results.dnd) { updateDnd(results.dnd) }
    if(results.notificationAlert) { updateNotificationAlert(results.notificationAlert) }

/*

Battery - %BATT
Screen Brightness - %BRIGHT
Screen On/Off - %SCREEN
Ring Volume - %VOLR
Media Volume - %VOLM
Media Muted - %VOLM = 0
Do Not Disturb - %INTERRUPT (off (all), none (noInt), priority or alarms)
Notification Type - %SILENT (on = silent, vibrate = vibrate only, off = sound)

*/

}

// Process the results from a bluetooth status report
void btStatusReport(results) {
    debugLog("btStautsReport: ${results}");
    boolean btDevFound = false;
    results.btDevices?.each { dev ->
        debugLog("btStatusReport: ${dev.address}, ${dev.name}, ${dev.alias}, ${dev.paired}, ${dev.connected}, ${dev.battery}");
        
        btDevFound = false;
        getChildDevices()?.each { child ->
            if (dev.address == child.getDataValue("Mac")) {
                btDevFound = true;
                child.statusReport(dev.paired, dev.connected, dev.battery);
            }
        }
        if (!btDevFound) { createBTDevice(dev.address, dev.name, dev.alias, dev.paired, dev.connected, dev.battery) }
    }
    
    // If we are doing a full status report, cleanup any bluetooth devices no longer paired with the mobile device
    if(results.reportType == 'full') {
        getChildDevices()?.each { child ->
            btDevFound = false;
            results.btDevices?.each { dev ->
                if (dev.address == child.getDataValue("Mac")) {
                    btDevFound = true;
                }
            }
            if (!btDevFound) {
                debugLog("btStatusReport: Bluetooth device ${child.getName()} is unpaired from ${device.getName()}");
                child.disconnected();
                child.unpaired();
                infoLog("Bluetooth device ${child.getName()} has been unpaired from ${device.getName()}");
            }
        }
    }
}

// Create a new bluetooth child device
void createBTDevice(String mac, String name, String alias, String paired, String connected, String battery) {
    String dni = UUID.randomUUID().toString();
    com.hubitat.app.DeviceWrapper newBtDevice = addChildDevice("Mobile Bluetooth Device", dni, ["name": "${name}", isComponent: true]);
    newBtDevice.setLabel(alias);
    newBtDevice.updateDataValue("Mac", mac);
    newBtDevice.statusReport(paired, connected, battery);
}

// Process the results from a VPN status report
void reportVpnStatus(String vpnStatus) {
    if(vpnStatus == "on") { vpnOn() } else { vpnOff() }
}

// Call Monitoring Methods

void callIncoming() {
    
    sendEvent(name: 'callStatus', value: 'Incoming');
    debugLog("callIncoming: Incoming call detected");
    setLastUpdated();
}

void callStarted() {
    
    sendEvent(name: 'callStatus', value: 'InCall');
    debugLog("callStarted: Call Started");
    setLastUpdated();
}

void callEnded() {
    
    sendEvent(name: 'callStatus', value: 'Idle');
    debugLog("callEnded: Call Ended");
    setLastUpdated();
}

// Message Monitoring Methods

void messagesUnread() {
    
    sendEvent(name: 'unreadMessage', value: 'yes');
    debugLog("messagesUnread: Unread message detected");
    setLastUpdated();
}

void messagesNoneUnread() {
    
    sendEvent(name: 'unreadMessage', value: 'no');
    debugLog("messagesNoneUnread: No unread messages detected");
    setLastUpdated();
}

// Report (Record) Configuration Changes Made on the Mobile Device

void reportCallMonitoring(String setting) {
    state.callMonitor = setting;
}

void reportMessageMonitoring(String setting) {
    state.messageMonitor = setting;
}

void reportBluetoothMonitoring(String setting) {
    state.bluetoothMonitor = setting;
}

void reportWifiMonitoring(String setting) {
    state.wifiMonitor = setting;
}

// Return Current Monitoring Settings

String getCallMonitoring() {
    return state.callMonitor;
}

String getMessageMonitoring() {
    return state.messageMonitor;
}

String getBluetoothMonitoring() {
    return state.bluetoothMonitor;
}

String getWifiMonitoring() {
    return state.wifiMonitor;
}

// Network methods

void reportWifiGroup(String groupName) {
    sendEvent(name: 'wifiGroup', value: groupName);
    debugLog("reportWifiGroup: Device is now on the ${groupName} network");

    setLastUpdated();
}

void vpnOn() {

    sendEvent(name: 'vpn', value: 'on');
    debugLog("vpnOn: VPN turned on");
    setLastUpdated();
}

void vpnOff() {

    sendEvent(name: 'vpn', value: 'off');
    debugLog("vpnOff: VPN turned off");
    setLastUpdated();
}

// Mode Methods

void syncMode(String newMode) {
    if(SyncHEMode) {
        if(CommandMethod == "Tasker"){ sendTaskerCommand("mode/record", newMode); }
        if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mode", newMode); }
    }
}

//Brightness Methods

void setBrightness(Number brightnessVal) {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("screen/setBrightness", "${brightnessVal}"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("setBrightness", "${brightnessVal}"); }
    //sendEvent(name: 'brightness', value: brightnessVal);
    
    //infoLog("Device brightness updated to ${brightnessVal}");
    //setLastUpdated();
    debugLog("setBrightness: Request to adjust screen brightness to ${brightnessVal} sent");
}

void updateBrightness(Number brightnessVal) {
    
    sendEvent(name: 'brightness', value: brightnessVal);
    debugLog("updateBrightness: Device brightness reported as ${brightnessVal}");
    setLastUpdated();
}

//Audio Volume Methods

void mute() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("volume/media/mute", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaVolumeMute", ""); }
    
    infoLog("Device has been muted");
    setLastUpdated();
}

void unmute() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("volume/media/unmute", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaVolumeUnmute", ""); }
    
    infoLog("Device has been unmuted");
    setLastUpdated();
}

void setVolume(volumelevel) { 
    if(CommandMethod == "Tasker"){ sendTaskerCommand("volume/media/set", "${volumelevel}"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("setMediaVolume", "${volumelevel}"); }
    
    infoLog("Device media volume has been adjusted to ${volumelevel}");
    setLastUpdated();
}

void volumeDown() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("volume/media/down", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaVolumeDown", ""); }
    
    infoLog("Device media volume has been turned down");
    setLastUpdated();
}

void volumeUp() { 
    if(CommandMethod == "Tasker"){ sendTaskerCommand("volume/media/up", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaVolumeUp", ""); }
    
    infoLog("Device media volume has been turned up");
    setLastUpdated();
}

void updateMediaVolume(Number mediaVolumeVal) {
    sendEvent(name: 'volume', value: mediaVolumeVal);
    debugLog("updateMediaVolume: Media Volume status reported as ${mediaVolumeVal}");
    setLastUpdated();
}

void updateMediaMuted(String mediaMutedVal) {
    sendEvent(name: 'mute', value: mediaMutedVal);
    debugLog("updateMediaMuted: Media Muted status reported as ${mediaMutedVal}");
    setLastUpdated();
}

// Ring Volume Methods

void updateRingVolume(Number ringVolumeVal) {
    sendEvent(name: 'ringVolume', value: ringVolumeVal);
    debugLog("updateRingVolume: Ring Volume status reported as ${ringVolumeVal}");
    setLastUpdated();
}

// Music Player Methods

void previousTrack() {
 
    if(CommandMethod == "Tasker"){ sendTaskerCommand("media/previousTrack", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaPreviousTrack", ""); }
    
    infoLog("Previous Track was selected");
    setLastUpdated();
}

void nextTrack() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("media/nextTrack", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaNextTrack", ""); }
    
    infoLog("Next Track was selected");
    setLastUpdated();
}

void pause() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("media/pause", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaPause", ""); }
    
    infoLog("Pause was selected");
    setLastUpdated();
}

void play() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("media/play", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaPlay", ""); }
    
    infoLog("Play was selected");
    setLastUpdated();
}

void stop() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("media/stop", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("mediaStop", ""); }
    
    infoLog("Stop was selected");
    setLastUpdated();
}

void setLevel(volumelevel) {
    
    setVolume(volumelevel);
}

void playText(String text) {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/tts", "${text}"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("tts", "${text}"); }
    
    infoLog("Device TTS notification has been sent");
    debugLog("deviceNotification: TTS Notification text = ${text}");
    setLastUpdated();
}
// Notification Alert Settings Methods

void notificationVibrate() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/vibrate", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("notifyVibrate", ""); }
    
    infoLog("Notifications will now use Vibrate setting");
    setLastUpdated();
}

void notificationSound() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/sound", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("notifySound", ""); }
    
    infoLog("Notifications will now use Sound setting");
    setLastUpdated();
}

void notificationMute() {
    
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/mute", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("notifyMute", ""); }
    
    infoLog("Notifications will now use Mute setting");
    setLastUpdated();
}

void updateNotificationAlert(String notifyAlertVal) {
    String notifyAlertValDerived = "";
    switch(notifyAlertVal) {
        case "on": 
          notifyAlertValDerived = "mute";
          break;
        case "vibrate":
          notifyAlertValDerived = "vibrate";
          break;
        case "off":
          notifyAlertValDerived = "sound";
          break;
        default:
          notifyAlertValDerived = "";
          break;
    }
    
    sendEvent(name: 'notificationAlert', value: notifyAlertValDerived);
    debugLog("updateNotificationAlert: Notification Alert status reported as ${notifyAlertVal}, derived as ${notifyAlertValDerived}");
    setLastUpdated();
}

// Notification Methods

void deviceNotification(String text) {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/notify", "Mobile Controller||${text}"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("notify", "Mobile Controller||${text}"); }
    
    infoLog("Device notification has been sent");
    debugLog("deviceNotification: Notification text = ${text}");
    setLastUpdated();
}

void customDeviceNotification(String title, String text){
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/notify", "${title}||${text}"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("notify", "${title}||${text}"); }
    
    infoLog("Device custom notification has been sent");
    debugLog("customDeviceNotification: Notification title = ${title}, text = ${text}");
    setLastUpdated();
}

void cancelNotification(String title) {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("notifications/cancel", title); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("cancelNotification", title); }
    
    infoLog("Device notification has been cancelled");
    debugLog("cancelNotification: Notification cancelled with title = ${title}");
    setLastUpdated();
}

// Do Not Disturb Methods

void doNotDisturbOn(String setting) {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("donotdisturb", setting); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("setDoNotDisturb", setting); }
    
    infoLog("Do Not Disturb has been turned on");
    setLastUpdated();    
}

void doNotDisturbOff() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("donotdisturb", "allowAll"); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("setDoNotDisturb", "allowAll"); }
    
    infoLog("Do Not Disturb has been turned off");
    setLastUpdated();
}

void updateDnd(String dndStatus) {
    String dndStatusDerived = "";
    switch (dndStatus) {
        case "all":
          dndStatusDerived = "allowAll";
          break;
        case "none":
          dndStatusDerived = "noInt";
          break;
        case "priority":
        case "alarms":
          dndStatusDerived = dndStatus;
          break;
        default:
            dndStatusDerived = "";
            break;
    }
    
    sendEvent(name: 'doNotDisturb', value: dndStatusDerived);
    debugLog("updateDnd: Device Do Not Disturb status reported as ${dndStatus}, derived as ${dndStatusDerived}");
    if(dndStatusDerived == "") { warnLog("updateDnd: Unknown Do Not Disturb status reported (${dndStatus})") }
    setLastUpdated();
}

// Custom Screen Methods

void turnScreenOn() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("screen/on", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("screenOn", ""); }
    
    infoLog("Device screen has been turned on");
    setLastUpdated();
}

void turnScreenOff() {
    if(CommandMethod == "Tasker"){ sendTaskerCommand("screen/off", ""); }
    if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("screenOff", ""); }
    
    infoLog("Device screen has been turned off");
    setLastUpdated();
}

void updateScreen(String screenStatus) {
        
    sendEvent(name: 'screen', value: screenStatus);
    debugLog("updateScreen: Device screen status reported as ${screenStatus}");
    setLastUpdated();
}

// Alarm Methods

void dismissAlarm(String label){
   if(CommandMethod == "Tasker"){ sendTaskerCommand("alarm/dismiss", label); }
   if(CommandMethod == "AutoRemote"){ sendAutoRemoteCommand("dismissAlarm", label); }
   
   infoLog("Alarm has been dismissed");
   debugLog("dismissAlarm: Alarm has been dismissed with label = ${label}");
   setLastUpdated();
}

//Device Heartbeat

void deviceHeartbeat() {
 
    Date heartbeatDate = new Date()
    sendEvent(name: 'heartbeat', value: heartbeatDate.format('dd/MM/yyyy HH:mm'));
    
}

// Mobile Device Command Methods (Tasker and AutoRemote)

String getDeviceIP() {
    
    if(device.currentValue("vpn") == "on") { DeviceVPNAddress } else { DeviceIPAddress }
}

void sendTaskerCommand(String path, String body){
    String taskerURI = "http://${getDeviceIP()}:${Port}/${mcURLPath()}/${path}";
    debugLog("sendTaskerCommand: Tasker URI = ${taskerURI}");
    
    try {
        httpPost([uri: taskerURI, contentType: "application/json", body: body]) { resp ->
            debugLog("sendTaskerCommand: Command sent.  Response = ${resp.data}")
        }
    }
    catch(Exception e) {
        errorLog("sendTaskerCommand: ${e}");
    }
}

void sendAutoRemoteCommand(String command, String value){
    try {
        httpGet([uri: "http://${getDeviceIP()}:${ARPort}/?message=${command}=:=${java.net.URLEncoder.encode(value)}"]) { resp ->
            debugLog("sendAutoRemoteCommand: Command sent.  Response = ${resp.data}")
        }
    }
    catch(Exception e){
        errorLog("sendAutoRemoteCommand: ${e}");
    }
}

#include simnet.logging
#include simnet.battery
#include simnet.lastUpdated
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


metadata {
	definition (name: 'MC Mobile Device', namespace: 'simnet', author: 'Simon Burke') {
        
        capability 'Battery'
        capability 'MotionSensor'
        capability 'PresenceSensor'
        capability 'AudioVolume'
        capability 'Notification'
        
        // Battery Sensor Attributes
        attribute 'battery', 'number'
        attribute 'batteryStatus', 'string'
        
        //Motion Sensor Attribute
        attribute 'motion', 'ENUM ["inactive", "active"]'
        
        // Audio Volume Attributes
        attribute 'mute', 'ENUM ["unmuted", "muted"]'
        attribute 'volume', 'NUMBER' //, unit:'%'
        
        //Presence Sensor Attribute
        attribute 'presence', 'ENUM ["present", "not present"]'
        
        //Network Attributes
        attribute 'wifiNetwork', 'string' // Wi-Fi Netowrk SSID the mobile device is currently connected to
        
        // General Attributes
        attribute 'lastUpdated', 'date'
        attribute 'heartbeat', 'date'
        attribute 'brightness', 'number'
        
        // Battery Sensor Command
        command 'setBattery', [[name:'batteryReading', type: 'NUMBER', description: 'Enter the new battery reading (%)' ] ]
        
        //Motion Sensor Capability Commands
        command 'active'
        command 'inactive'
        
        //Presence Sensor Commands (not part of the capability)
        command 'present'
        command 'notPresent'
        
        // Audio Volume Capability Commands
        command 'mute'
        command 'setVolume', [[name:'volumelevel', type: 'NUMBER', description: 'Enter the new volume value (%)' ] ]
        command 'unmute'
        command 'volumeDown'
        command 'volumeUp'
        
        //Notification Capability Command
        command 'deviceNotification', [[name:'text', type: 'STRING', description: 'Enter the notification text' ] ]
        
        //Custom Notification command to cancel a notification send to the device
        command 'cancelNotification', [[name:'title', type: 'STRING', description: 'Enter the title for the notification to cancel' ] ]
        
        command 'setBrightness', [[name:'brightnessVal', type: 'NUMBER', description: 'Enter the new brightness value (%)' ] ]
        
        //command 'setWifiNetwork', [[name:'wifiNetwork', type: 'STRING', description: 'Enter the new Wi-Fi Network' ] ]
        command 'deviceHeartbeat'
    }    
    
    /*
  preferences {
    
    // Logging Preferences
    input(name: "DebugLogging", type: "bool", title:"Enable Debug Logging",                   displayDuringSetup: true, defaultValue: false)
    input(name: "WarnLogging",  type: "bool", title:"Enable Warning Logging",                 displayDuringSetup: true, defaultValue: true )
    input(name: "ErrorLogging", type: "bool", title:"Enable Error Logging",                   displayDuringSetup: true, defaultValue: true )
    input(name: "InfoLogging",  type: "bool", title:"Enable Description Text (Info) Logging", displayDuringSetup: true, defaultValue: false)
  }
	*/
    preferences {
    
      // Device Preferences
      input(name: "DeviceIPAddress", type: "string", title:"Mobile Device IP Address", displayDuringSetup: true, defaultValue: "")
      
      input(name: "AppId", type: "number", title:"Maker API App ID", description: "App Id of the Maker API Instance", displayDuringSetup: true, defaultValue: 0)
      input(name: "AccessToken", type: "password", title:"Maker API Access Token", description: "Access Token of the Maker API Instance", displayDuringSetup: true, defaultValue: "")
      input(name: "CloudURL", type: "string", title:"Maker API Cloud URL", description: "Start of the Cloud URL used for Maker API", displayDuringSetup: true, defaultValue: "")
      input(name: "CloudComm", type: "bool", title:"Use Cloud Communications?", description: "Turn on to use cloud communications back to HE when not on Wi-Fi", displayDuringSetup: true, defaultValue: false)
      
      input(name: "SyncHEMode", type: "bool", title:"Sync HE Mode?", description: "Turn on to send HE mode updates to the mobile device", displayDuringSetup: true, defaultValue: false)
      input(name: "TrackWIFINetwork", type: "bool", title:"Track Mobile Device Wi-Fi Network?", description: "Record Wi-Fi Network SSID of the mobile device (only whitelisted SSID's)", displayDuringSetup: true, defaultValue: false)
      input(name: "WIFINetWhitelist", type: "string", title:"Wi-Fi Network SSID Whitelist", description: "List of Wi-Fi Network SSID's to track, separated by a /", displayDuringSetup: true, defaultValue: "")
      
      
    }
}

//Common Methods
void installed() { initialized() }

void initialized() {

 state.warningCount = 0
 //Detect whether battery status exists, if not, set to Idle
  //sendEvent(name: "batteryStatus", value: "Idle");
}

//Motion Sensor Methods
void active() {
    sendEvent(name: 'motion', value: 'active');
    debugLog('active: Device reported as being active');

    //Update the lastUpdated attribute value
    Date lastUpdate = new Date()
    sendEvent(name: 'lastUpdated', value : lastUpdate.format('dd/MM/yyyy HH:mm'))
}

void inactive() {
    sendEvent(name: 'motion', value: 'inactive');
    infoLog('inactive: Device reported as being inactive');
    
    //Update the lastUpdated attribute value
    Date lastUpdate = new Date()
    sendEvent(name: 'lastUpdated', value : lastUpdate.format('dd/MM/yyyy HH:mm'))
}

//Presence Sensor Methods
void present() {
    sendEvent(name: 'presence', value: 'present');
    debugLog('present: Device is present');

    //Update the lastUpdated attribute value
    Date lastUpdate = new Date()
    sendEvent(name: 'lastUpdated', value : lastUpdate.format('dd/MM/yyyy HH:mm'))
}

void notPresent() {
    sendEvent(name: 'presence', value: 'not present');
    debugLog('notPresent: Device is not present');

    //Update the lastUpdated attribute value
    Date lastUpdate = new Date()
    sendEvent(name: 'lastUpdated', value : lastUpdate.format('dd/MM/yyyy HH:mm'))
}

//Battery Sensor Methods
void setBattery(Number pbattery) {
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
        //Update the lastUpdated attribute value
        Date lastUpdate = new Date()
        sendEvent(name: 'lastUpdated', value : lastUpdate.format('dd/MM/yyyy HH:mm'))

        //Reset warning count if there have been previous warnings
        if (state.warningCount > 0) {
            state.warningCount = 0
            log.info('setBattery: warning count reset')
        }
    }
    // If the battery reading is outside the 0 - 100 range, log a warning and leave the current reading in place
    //   use the warning count state variable to make sure we don't spam the logs with repeated warnings
    else {
        if (state.warningCount < 10) {
            state.warningCount = state.warningCount + 1
            log.warn("setBattery: Warning (${state.warningCount}) - battery level outside of 0-100 range, device not updated.  Battery value provided = ${pBattery}")
        }
    }
}

Number getBattery()       { return device.currentValue('battery') }

String getBatteryStatus() { return device.currentValue('batteryStatus') }

//Brightness Methods
void setBrightness(Number brightnessVal) {
    sendEvent(name: 'brightness', value: brightnessVal);
    infoLog("setBrightness: Device brightness updated, new value = ${brightnessVal}");
    
    //Update the lastUpdated attribute value
    Date lastUpdate = new Date()
    sendEvent(name: 'lastUpdated', value : lastUpdate.format('dd/MM/yyyy HH:mm'))
}

//Audio Volume Methods
void mute() { }

void setVolume(volumelevel) { }

void unmute() { }

void volumeDown() { }

void volumeUp() { }

//Notification Methods
void deviceNotification(String text) {
}

void cancelNotification(String title) {
}

//Wi-Fi Network Methods
void updateWifiNetwork(String wifiNetwork) {
    sendEvent(name: 'wifiNetwork', value: wifiNetwork);
    infoLog("updateWifiNetwork: Device Wi-Fi Network updated, new value = ${wifiNetwork}");
    
    //Update the lastUpdated attribute value
    Date lastUpdate = new Date()
    sendEvent(name: 'lastUpdated', value : lastUpdate.format('dd/MM/yyyy HH:mm'))
}

//Device Heartbeat
def deviceHeartbeat() {
 
    Date lastUpdate = new Date()
    sendEvent(name: 'heartbeat', value: lastUpdate.format('dd/MM/yyyy HH:mm'));
    
}

#include simnet.logging
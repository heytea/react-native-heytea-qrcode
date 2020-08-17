"use strict";
import { NativeModules,NativeEventEmitter,Platform } from "react-native";
const { HeyTeaQRCode } = NativeModules;
const QRCodeManagerEmitter = new NativeEventEmitter(HeyTeaQRCode);

export const scanQRCode = () => {
    const {scanQRCode} = HeyTeaQRCode
    if (Platform.OS === 'android') {
      return scanQRCode()
    }else {
        scanQRCode()
        return new Promise((resolve) => {
            QRCodeManagerEmitter.addListener(
                'ScanQRCodeInfoNotification',
                info => {
                  resolve(info)
                },
              );
        })
    }
}

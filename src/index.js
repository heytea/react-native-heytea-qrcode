"use strict";
import { NativeModules,NativeEventEmitter,Platform } from "react-native";
const { HeyTeaQRCode } = NativeModules;
const QRCodeManagerEmitter = new NativeEventEmitter(HeyTeaQRCode);

export const scanQRCode = (lang) => {
    const {scanQRCode} = HeyTeaQRCode
    if (Platform.OS === 'android') {
      return scanQRCode(lang)
    }else {
        scanQRCode(lang)
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

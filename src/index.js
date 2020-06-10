"use strict";
import { NativeModules,NativeEventEmitter } from "react-native";
const { HeyTeaQRCode } = NativeModules;
const QRCodeManagerEmitter = new NativeEventEmitter(HeyTeaQRCode);

export const iOSScanQRCode = () => {

    const {scanQRCode} = HeyTeaQRCode
    scanQRCode()
    return new Promise((resolve) => {
        QRCodeManagerEmitter.addListener(
            'ScanQRCodeInfoNotification',
            info => {
              const { qrCodeResult } = info;
              resolve(qrCodeResult)
            },
          );
    })

}

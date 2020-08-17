# react-native-heytea-qrcode
扫二维码/从相册加载二维码 rn插件

install

```
yarn add @heytea/react-native-heytea-qrcode
```

use

```js
export interface ScanResult {
    result: string;
    scanType: string;
}

/**
 * 扫码哦
 * @returns {Promise<ScanResult | null>} 扫码结果
 */
export function scanQRCode(): Promise<ScanResult | null>;
```

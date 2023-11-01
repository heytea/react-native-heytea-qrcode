export interface ScanResult {
    result: string;
    scanType: string;
}

/**
 * 扫码哦
 * @returns {Promise<ScanResult | null>} 扫码结果
 */
export function scanQRCode(lang:string): Promise<ScanResult | null>;

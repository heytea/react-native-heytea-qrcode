package com.heyteago.qrcode;

import java.util.HashMap;
import java.util.Map;

public class LangUtil {

    private static final Map<String, Map<String, String>> langMap = new HashMap<>();

    static {
        Map<String, String> zhMap = new HashMap<>();
        zhMap.put("scan_qrcode", "扫描二维码");
        zhMap.put("open_flash_light", "打开闪光灯");
        zhMap.put("close_flash_light", "关闭闪光灯");
        zhMap.put("album", "相册");
        zhMap.put("album_permission_tips", "从相册选择需要读取存储权限");
        zhMap.put("select_qrCode_pic", "选择二维码图片");
        zhMap.put("scanning", "正在扫描...");
        zhMap.put("authorize_album_access", "授权相册访问");
        zhMap.put("authorize_album_access_tips", "允许访问相册，以便您扫描小票二维码，了解制茶进度及开具发票。");
        zhMap.put("disagree", "不同意");
        zhMap.put("agree", "同意");
        zhMap.put("unable_access_album", "无法访问相册");
        zhMap.put("enable_album_permissions_tips", "请前往系统设置, 开启相册权限。");
        zhMap.put("go_open", "去开启");
        zhMap.put("qr_barcode_scanning", "二维码/条形码扫描");
        zhMap.put("scan_failed", "扫码失败");
        zhMap.put("unable_turn_on_flash","暂时无法开启闪光灯");

        Map<String, String> hkMap = new HashMap<>();
        hkMap.put("scan_qrcode", "掃描二維碼");
        hkMap.put("open_flash_light", "打開閃光燈");
        hkMap.put("close_flash_light", "關閉閃光燈");
        hkMap.put("album", "相册");
        hkMap.put("album_permission_tips", "從相册選擇需要讀取存儲許可權");
        hkMap.put("select_qrCode_pic", "選擇二維碼圖片");
        hkMap.put("scanning", "正在掃描...");
        hkMap.put("authorize_album_access", "授權相册訪問");
        hkMap.put("authorize_album_access_tips", "允許訪問相冊，以便您掃描二維碼，了解製茶進度及開具發票。");
        hkMap.put("disagree", "不同意");
        hkMap.put("agree", "同意");
        hkMap.put("unable_access_album", "無法訪問相冊");
        hkMap.put("enable_album_permissions_tips", "請前往系統設置，開啟相冊權限。");
        hkMap.put("go_open", "去開啟");
        hkMap.put("qr_barcode_scanning", "二維碼/條碼掃描");
        hkMap.put("scan_failed", "掃碼失敗");
        hkMap.put("unable_turn_on_flash","Unable to turn on flash temporarily");

        Map<String, String> usMap = new HashMap<>();
        usMap.put("scan_qrcode", "Scan QR code");
        usMap.put("open_flash_light", "Flash On");
        usMap.put("close_flash_light", "Flash off");
        usMap.put("album", "Album");
        usMap.put("album_permission_tips", "Select the required read storage permissions from the album");
        usMap.put("select_qrCode_pic", "Select QRCode pic");
        usMap.put("scanning", "Scanning...");
        usMap.put("authorize_album_access", "Authorize album access");
        usMap.put("authorize_album_access_tips", "Allow access to the album so that you can scan the receipt QR code, check the tea production progress, and issue invoices。");
        usMap.put("disagree", "Disagree");
        usMap.put("agree", "Agree");
        usMap.put("unable_access_album", "Unable to access album");
        usMap.put("enable_album_permissions_tips", "Please go to system settings and enable album permissions。");
        usMap.put("go_open", "Go to enable");
        usMap.put("qr_barcode_scanning", "QR code/barcode scanning");
        usMap.put("scan_failed", "Scan failed");
        usMap.put("unable_turn_on_flash","暫時無法開啟閃光燈");

        Map<String, String> jaMap = new HashMap<>();
        jaMap.put("scan_qrcode", "QRコードをスキャン");
        jaMap.put("open_flash_light", "フラッシュON");
        jaMap.put("close_flash_light", "フラッシュOFF");
        jaMap.put("album", "アルバム");
        jaMap.put("album_permission_tips", "アルバムから選択するにはアクセス権限をオンにする必要があります");
        jaMap.put("select_qrCode_pic", "スキャンする写真を選択してください");
        jaMap.put("scanning", "スキャン中…");
        jaMap.put("authorize_album_access", "アルバムへのアクセスを許可する");
        jaMap.put("authorize_album_access_tips", "アルバムへのアクセスを許可すると、レシートのQRコードをスキャンすることで製作ステータスを確認したり領収書を受け取ったりすることができます。");
        jaMap.put("disagree", "同意しない");
        jaMap.put("agree", "同意する");
        jaMap.put("unable_access_album", "アルバムへのアクセス権限がありません");
        jaMap.put("enable_album_permissions_tips", "設定よりアルバムへのアクセスを許可してください。");
        jaMap.put("go_open", "設定へ");
        jaMap.put("qr_barcode_scanning", "QRコード/バーコードのスキャン");
        jaMap.put("scan_failed", "スキャン失敗");
        jaMap.put("unable_turn_on_flash","フラッシュをONにできません");

        langMap.put("zh-CN", zhMap);
        langMap.put("zh-HK", hkMap);
        langMap.put("en-US", usMap);
        langMap.put("ja-JP", jaMap);
    }

    public static String getString(String lang, String key) {
        Map<String, String> map = langMap.get(lang);
        if (map == null) {
            map = langMap.get("en-US");
        }
        return map.get(key);
    }
}

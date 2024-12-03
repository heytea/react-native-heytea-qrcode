//
//  HQRCodeVc.m
//  heyteago
//
//  Created by MarvinX on 2018/9/2.
//  Copyright © 2018年 marvin. All rights reserved.
//

#import "HQRCodeVc.h"
#import <SGQRCode.h>
#import "MBProgressHUD+Heytea.h"
#import "RCTQRCodeScan.h"
#import <Photos/Photos.h>

@interface HQRCodeVc () {
    SGQRCodeObtain *obtain;
    NSDictionary *_langDic;
}

@property (nonatomic, strong) SGQRCodeScanView *scanView;
@property (nonatomic, strong) UIButton *flashlightBtn;
@property (nonatomic, strong) UILabel *promptLabel;
@property (nonatomic, assign) BOOL isSelectedFlashlightBtn;
@property (nonatomic, strong) UIView *bottomView;

@end

@implementation HQRCodeVc

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.view.backgroundColor = [UIColor blackColor];
    obtain = [SGQRCodeObtain QRCodeObtain];
    [self setupQRCodeScan];
    [self setupNavigationBar];
    [self.view addSubview:self.scanView];
    [self.view addSubview:self.promptLabel];
    [self.view addSubview:self.bottomView];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationController.navigationBarHidden = NO;
    [obtain startRunningWithBefore:nil completion:nil];
}


- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [self.scanView addTimer];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.scanView removeTimer];
    [self removeFlashlightBtn];
    self.navigationController.navigationBarHidden = YES;
    [obtain stopRunning];
}

/**
 扫一扫    Scan    掃一掃
 相册    Album    相冊
 无法访问相册    Unable to access the album    無法訪問相冊
 请前往系统设置, 开启相册权限。    Please go to system settings and enable album permissions.    請前往系統設置，開啟相冊權限
 去开启    Go to enable    去開啟
 授权相册访问    Authorize album access    授權相冊訪問
 允许访问相册，以便您扫描小票二维码，了解制茶进度及开具发票。    Allow access to the album so that you can scan the receipt QR code, check the tea production progress, and issue invoices.    允許訪問相冊，以便您掃描二維碼，了解製茶進度及開具發票
 未发现二维码    No QR code found    未發現二維碼
 将二维码放入框内, 即可自动扫描    Place the QR code inside the frame, and it will be automatically scanned.    將二維碼放入框內，即可自動掃描
 */

- (void)setLang:(NSString *)lang{
    _langDic = @{
        @"scan":@"Scan",
        @"album":@"Album",
        @"notAlbum":@"Unable to access the album",
        @"notAlbumDes":@"Please go to system settings and enable album permissions.",
        @"open":@"Go to enable",
        @"authAlbum":@"Authorize album access",
        @"authAlbumDes":@"Allow access to the album so that you can scan the receipt QR code, check the tea production progress, and issue invoices.",
        @"notQr":@"No QR code found",
        @"scanDes":@"Place the QR code inside the frame, and it will be automatically scanned.",
        @"agree":@"Agree",
        @"disagree":@"Disagree",
    };
    if([lang isEqualToString:@"zh-CN"]){
        _langDic = @{
            @"scan":@"扫一扫",
            @"album":@"相册",
            @"notAlbum":@"无法访问相册",
            @"notAlbumDes":@"请前往系统设置, 开启相册权限",
            @"open":@"去开启",
            @"authAlbum":@"授权相册访问",
            @"authAlbumDes":@"允许访问相册，以便您扫描小票二维码，了解制茶进度及开具发票。",
            @"notQr":@"未发现二维码",
            @"scanDes":@"将二维码放入框内, 即可自动扫描",
            @"agree":@"同意",
            @"disagree":@"不同意",
        };
    }else if([lang isEqualToString:@"zh-HK"]){
        _langDic = @{
            @"scan":@"掃一掃",
            @"album":@"相冊",
            @"notAlbum":@"無法訪問相冊",
            @"notAlbumDes":@"請前往系統設置，開啟相冊權限",
            @"open":@"去開啟",
            @"authAlbum":@"授權相冊訪問",
            @"authAlbumDes":@"允許訪問相冊，以便您掃描二維碼，了解製茶進度及開具發票",
            @"notQr":@"未發現二維碼",
            @"scanDes":@"將二維碼放入框內，即可自動掃描",
            @"agree":@"同意",
            @"disagree":@"不同意",
        };
    }else if([lang isEqualToString: @"jp"]){
         _langDic = @{
            @"scan":@"スキャン",
            @"album":@"アルバム",
            @"notAlbum":@"アルバムを訪問できません",
            @"notAlbumDes":@"設定にてアルバムの使用権限をオンにしてください",
            @"open":@"設定へ",
            @"authAlbum":@"アルバムの使用権限をオンにする",
            @"authAlbumDes":@"アルバムへのアクセスを許可すると、レシートのQRコードをスキャンすることで製作ステータスを確認したり領収書を受け取ったりすることができます。",
            @"notQr":@"QRコードが見つかりませんでした",
            @"scanDes":@"QRコードを枠内に入れると、自動でスキャンされます",
            @"agree":@"同意する",
            @"disagree":@"同意しない",
        };
    }
}

- (void)setupQRCodeScan {
    __weak typeof(self) weakSelf = self;

    SGQRCodeObtainConfigure *configure = [SGQRCodeObtainConfigure QRCodeObtainConfigure];
    configure.sampleBufferDelegate = YES;
    [obtain establishQRCodeObtainScanWithController:self configure:configure];
    [obtain setBlockWithQRCodeObtainScanResult:^(SGQRCodeObtain *obtain, NSString *result) {
        if (result) {
            [obtain stopRunning];
            [[NSNotificationCenter defaultCenter] postNotificationName:@"QRCodeInfoNotification" object:nil userInfo:@{@"result":result,@"scanType":@"QR_CODE"}];
            [weakSelf.navigationController popViewControllerAnimated:YES];
        }
    }];
    [obtain setBlockWithQRCodeObtainScanBrightness:^(SGQRCodeObtain *obtain, CGFloat brightness) {
        if (brightness < - 1) {
            [weakSelf.view addSubview:weakSelf.flashlightBtn];
        } else {
            if (weakSelf.isSelectedFlashlightBtn == NO) {
                [weakSelf removeFlashlightBtn];
            }
        }
    }];
}


- (void)setupNavigationBar {
    self.navigationItem.title = _langDic[@"scan"];
    UIColor *color = [UIColor whiteColor];
    NSDictionary *dic = [NSDictionary dictionaryWithObject:color forKey:NSForegroundColorAttributeName];
    self.navigationController.navigationBar.titleTextAttributes = dic;

    self.navigationController.navigationBar.translucent = YES;
    [self.navigationController.navigationBar setShadowImage:[UIImage new]];
    [self.navigationController.navigationBar setBackgroundImage:[UIImage new] forBarMetrics:UIBarMetricsDefault];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:_langDic[@"album"] style:UIBarButtonItemStylePlain target:self action:@selector(rightBarButtonItemAction)];
    [self.navigationItem.rightBarButtonItem setTintColor:[UIColor whiteColor]];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[[UIImage imageNamed:@"whiteBackArrow"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] style:UIBarButtonItemStylePlain target:self action:@selector(leftBarButtonItemAction)];
}

- (void)leftBarButtonItemAction {
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)rightBarButtonItemAction {
    PHAuthorizationStatus status = [PHPhotoLibrary authorizationStatus];
    if(status == PHAuthorizationStatusDenied){
        //拒绝
        UIAlertController *alertC = [UIAlertController alertControllerWithTitle:_langDic[@"notAlbum"] message:_langDic[@"notAlbumDes"] preferredStyle:(UIAlertControllerStyleAlert)];
        UIAlertAction *alertA = [UIAlertAction actionWithTitle:_langDic[@"open"] style:(UIAlertActionStyleDefault) handler:^(UIAlertAction * _Nonnull action) {
            NSURL *url = [NSURL URLWithString:@"App-Prefs:root=Privacy&path=PHOTOS"];
            if([[UIApplication sharedApplication]canOpenURL:url]){
                [[UIApplication sharedApplication]openURL:url];
            }
            
        }];
        
        [alertC addAction:alertA];
        [self presentViewController:alertC animated:YES completion:nil];
    }else if(status == PHAuthorizationStatusAuthorized){
        [self openAlbum];
    }else{
        // 其他情况
        UIAlertController *alertC = [UIAlertController alertControllerWithTitle:_langDic[@"authAlbum"] message:_langDic[@"authAlbumDes"] preferredStyle:(UIAlertControllerStyleAlert)];
        UIAlertAction *alertA = [UIAlertAction actionWithTitle:_langDic[@"agree"] style:(UIAlertActionStyleDefault) handler:^(UIAlertAction * _Nonnull action) {
            [self openAlbum];
        }];
        UIAlertAction *alertB = [UIAlertAction actionWithTitle:_langDic[@"disagree"] style:(UIAlertActionStyleDefault) handler:^(UIAlertAction * _Nonnull action) {
            
        }];
        
        [alertC addAction:alertA];
        [alertC addAction:alertB];
        [self presentViewController:alertC animated:YES completion:nil];
    }
}

-(void)openAlbum{
    __weak typeof(self) weakSelf = self;
    [obtain establishAuthorizationQRCodeObtainAlbumWithController:nil];
    if (obtain.isPHAuthorization == YES) {
        [self.scanView removeTimer];
    }
    [obtain setBlockWithQRCodeObtainAlbumDidCancelImagePickerController:^(SGQRCodeObtain *obtain) {
        [weakSelf.view addSubview:weakSelf.scanView];
    }];
    [obtain setBlockWithQRCodeObtainAlbumResult:^(SGQRCodeObtain *obtain, NSString *result) {

        if (result == nil) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                [MBProgressHUD HT_hideHUDForView:weakSelf.view];
                [MBProgressHUD HT_showMBProgressHUDWithOnlyMessage:_langDic[@"notQr"] delayTime:1.8];
            });
        } else {
            [[NSNotificationCenter defaultCenter] postNotificationName:@"QRCodeInfoNotification" object:nil userInfo:@{@"result":result,@"scanType":@"QR_CODE"}];
            [weakSelf.navigationController popViewControllerAnimated:YES];
        }

    }];
}

- (SGQRCodeScanView *)scanView {
    if (!_scanView) {
        _scanView = [[SGQRCodeScanView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, 0.9 * self.view.frame.size.height)];
    }
    return _scanView;
}

- (void)removeScanningView {
    [self.scanView removeTimer];
    [self.scanView removeFromSuperview];
    self.scanView = nil;
}


- (UILabel *)promptLabel {
    if (!_promptLabel) {
        _promptLabel = [[UILabel alloc] init];
        _promptLabel.backgroundColor = [UIColor clearColor];
        CGFloat promptLabelX = 20;
        CGFloat promptLabelY = 0.73 * self.view.frame.size.height;
        CGFloat promptLabelW = self.view.frame.size.width - 40;
        CGFloat promptLabelH = 38;
        _promptLabel.frame = CGRectMake(promptLabelX, promptLabelY, promptLabelW, promptLabelH);
        _promptLabel.textAlignment = NSTextAlignmentCenter;
        _promptLabel.font = [UIFont boldSystemFontOfSize:13.0];
        _promptLabel.textColor = [[UIColor whiteColor] colorWithAlphaComponent:0.6];
        _promptLabel.text = _langDic[@"scanDes"];
        _promptLabel.numberOfLines = 2;

    }
    return _promptLabel;
}

- (UIView *)bottomView {
    if (!_bottomView) {
        _bottomView = [[UIView alloc] initWithFrame:CGRectMake(0, CGRectGetMaxY(self.scanView.frame), self.view.frame.size.width, self.view.frame.size.height - CGRectGetMaxY(self.scanView.frame))];
        _bottomView.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.5];
    }
    return _bottomView;
}

- (UIButton *)flashlightBtn {
    if (!_flashlightBtn) {
        // 添加闪光灯按钮
        _flashlightBtn = [UIButton buttonWithType:(UIButtonTypeCustom)];
        CGFloat flashlightBtnW = 30;
        CGFloat flashlightBtnH = 30;
        CGFloat flashlightBtnX = 0.5 * (self.view.frame.size.width - flashlightBtnW);
        CGFloat flashlightBtnY = 0.55 * self.view.frame.size.height;
        _flashlightBtn.frame = CGRectMake(flashlightBtnX, flashlightBtnY, flashlightBtnW, flashlightBtnH);
        [_flashlightBtn setBackgroundImage:[UIImage imageNamed:@"SGQRCodeFlashlightOpenImage"] forState:(UIControlStateNormal)];
        [_flashlightBtn setBackgroundImage:[UIImage imageNamed:@"SGQRCodeFlashlightCloseImage"] forState:(UIControlStateSelected)];
        [_flashlightBtn addTarget:self action:@selector(flashlightBtn_action:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _flashlightBtn;
}

- (void)flashlightBtn_action:(UIButton *)button {
    if (button.selected == NO) {
        [obtain openFlashlight];
        self.isSelectedFlashlightBtn = YES;
        button.selected = YES;
    } else {
        [self removeFlashlightBtn];
    }
}

- (void)removeFlashlightBtn {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self->obtain closeFlashlight];
        self.isSelectedFlashlightBtn = NO;
        self.flashlightBtn.selected = NO;
        [self.flashlightBtn removeFromSuperview];
    });
}

- (void)dealloc {
    [self removeScanningView];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



@end

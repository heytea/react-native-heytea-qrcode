//
//  MBProgressHUD+Heytea.h
//  heyteago
//
//  Created by 肖怡宁 on 2020/6/3.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "MBProgressHUD.h"

NS_ASSUME_NONNULL_BEGIN

@interface MBProgressHUD (Heytea)
+ (MBProgressHUD *)HT_showMBProgressHUDWithModifyStyleMessage:(NSString *)message toView:(UIView *)view;

+ (MBProgressHUD *)HT_showMBProgressHUDWithSystemComesStyleMessage:(NSString *)message toView:(UIView *)view;

+ (MBProgressHUD *)HT_showMBProgressHUD10sHideWithModifyStyleMessage:(NSString *)message toView:(UIView *)view;


+ (void)HT_showMBProgressHUDOfSuccessMessage:(NSString *)message toView:(UIView *)view;


+ (void)HT_showMBProgressHUDOfErrorMessage:(NSString *)message toView:(UIView *)view;


+ (void)HT_hideHUDForView:(UIView *)view;

+ (void)HT_showMBProgressHUDWithOnlyMessage:(NSString *)message delayTime:(CGFloat)time;

@end

NS_ASSUME_NONNULL_END

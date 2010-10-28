//
//  CrashController.h
//  CrashKit
//
//  Created by Parveen Kaler on 10-08-02.
//  Copyright 2010 Smartful Studios Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@class CrashLogger;

@interface CrashController : NSObject  {
    NSString *_appName;
}

@property(nonatomic,retain) NSString *appName;

+ (CrashController*)sharedInstance;

- (NSArray*)callstackAsArray;

@end

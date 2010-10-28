//
//  CrashController.m
//  CrashKit
//
//  Created by Parveen Kaler on 10-08-02.
//  Copyright 2010 Smartful Studios Inc. All rights reserved.
//

#import "CrashController.h"
#include <signal.h>
#include <execinfo.h>
#import "ASIFormDataRequest.h"

static CrashController *sharedInstance = nil;

#pragma mark C Functions 
void sighandler(int signal) {
    const char* names[NSIG];
    names[SIGABRT] = "SIGABRT";
    names[SIGBUS] = "SIGBUS";
    names[SIGFPE] = "SIGFPE";
    names[SIGILL] = "SIGILL";
    names[SIGPIPE] = "SIGPIPE";
    names[SIGSEGV] = "SIGSEGV";
    
    CrashController *crash = [CrashController sharedInstance];
    NSArray *arr = [crash callstackAsArray];
    
    NSMutableString *description = [NSMutableString stringWithCapacity:0];
    [description appendString:@"Signal "];
    [description appendString:[NSString stringWithUTF8String:names[signal]]];
    for ( NSString *stack in arr ) {
        [description appendString:@"\n"];
        [description appendString:stack];
    }
    
    [crash performSelectorOnMainThread:@selector(handleError:) withObject:description waitUntilDone:YES];
}

void uncaughtExceptionHandler(NSException *exception) {
    CrashController *crash = [CrashController sharedInstance];
    NSArray *arr = [crash callstackAsArray];
    
    NSMutableString *description = [NSMutableString stringWithCapacity:0];
    [description appendFormat:@"Exception %@ %@", [exception name], [exception reason]];
    for ( NSString *stack in arr ) {
        [description appendString:@"\n"];
        [description appendString:stack];
    }
    
    [crash performSelectorOnMainThread:@selector(handleError:) withObject:description waitUntilDone:YES];
}

@implementation CrashController

@synthesize appName = _appName;

#pragma mark Singleton methods

+ (CrashController*)sharedInstance {
    @synchronized(self) {
        if (sharedInstance == nil)
            sharedInstance = [[CrashController alloc] init];
    }
    
    return sharedInstance;
}

+ (id)allocWithZone:(NSZone *)zone {
    @synchronized(self) {
        if (sharedInstance == nil) {
            sharedInstance = [super allocWithZone:zone];
            return sharedInstance;
        }
    }
    
    return nil;
}

- (id)copyWithZone:(NSZone *)zone {
    return self;
}

- (id)retain {
    return self;
}

- (unsigned)retainCount {
    return UINT_MAX;
}

- (void)release {}

- (id)autorelease {
    return self;
}

#pragma mark Lifetime methods

- (id)init {
    if ((self = [super init])) {
        signal(SIGABRT, sighandler);
        signal(SIGBUS, sighandler);
        signal(SIGFPE, sighandler);
        signal(SIGILL, sighandler);
        signal(SIGPIPE, sighandler);    
        signal(SIGSEGV, sighandler);
        
        NSSetUncaughtExceptionHandler(&uncaughtExceptionHandler);
    }
    
    return self;
}

- (void)dealloc {
    signal(SIGABRT, SIG_DFL);
    signal(SIGBUS, SIG_DFL);
    signal(SIGFPE, SIG_DFL);
    signal(SIGILL, SIG_DFL);
    signal(SIGPIPE, SIG_DFL);
    signal(SIGSEGV, SIG_DFL);
    
    NSSetUncaughtExceptionHandler(NULL);
    
    [_appName dealloc];
    
    [super dealloc];
}

#pragma mark methods
- (NSArray*)callstackAsArray {
    void* callstack[128];
    const int numFrames = backtrace(callstack, 128);
    char **symbols = backtrace_symbols(callstack, numFrames);
    
    NSMutableArray *arr = [NSMutableArray arrayWithCapacity:numFrames];
    for (int i = 0; i < numFrames; ++i) {
        [arr addObject:[NSString stringWithUTF8String:symbols[i]]];
    }
    
    free(symbols);
    
    return arr;
}

- (void)sendReport:(NSString *)description {
    ASIFormDataRequest *req = [ASIFormDataRequest requestWithURL:[NSURL URLWithString:@"https://archilogic-logger.appspot.com/register"]];
    [req setPostValue:_appName forKey:@"appName"];
    [req setPostValue:[[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString *)kCFBundleVersionKey] forKey:@"versionName"];
    [req setPostValue:description forKey:@"description"];
    [req startSynchronous];
}

- (void)handleError:(NSString *)description {  
    signal(SIGABRT, SIG_DFL);
    signal(SIGBUS, SIG_DFL);
    signal(SIGFPE, SIG_DFL);
    signal(SIGILL, SIG_DFL);
    signal(SIGPIPE, SIG_DFL);
    signal(SIGSEGV, SIG_DFL);
    
    NSSetUncaughtExceptionHandler(NULL);
    
    [self sendReport:description];
}

@end

//
//  MapDocAppDelegate.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "MapDocAppDelegate.h"
#import "MapDocViewController.h"
#import "HistoryObject.h"
#import "FileUtil.h"

@implementation MapDocAppDelegate

@synthesize window;
@synthesize viewController;

- (int)parseId:(NSURL *)url {
    return [[[url path] substringFromIndex:1] intValue];
}

- (void)view:(int)docId {
    if ( [FileUtil existsDocument:docId] ) {
        [viewController showImage:docId page:0];
    } else {
        if ( [[DownloadManager instance] hasDownloading] ) {
            [[[[UIAlertView alloc] initWithTitle:@"Downloading file exist" message:nil delegate:nil cancelButtonTitle:@"OK"
                               otherButtonTitles:nil] autorelease] show];
            return;
        }
        
        loading = [[[UIAlertView alloc] initWithTitle:@"\n\nLoading...\nThis process will take few minutes..." message:nil
                                             delegate:nil cancelButtonTitle:nil otherButtonTitles:nil] autorelease];
        [loading show];
        
        [DownloadManager instance].delegate = self;
        [[DownloadManager instance] startDownload:docId];
    }
}

- (void)continueView {
    HistoryObject *history = [FileUtil history];
    if ( !history ) {
        [[[[UIAlertView alloc] initWithTitle:@"No history" message:nil delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] autorelease] show];
        return;
    }

    [viewController showImage:history.documentId page:history.page];
}

- (void)deleteCache:(int)docId {
    [FileUtil deleteCache:docId];
    
    [[[[UIAlertView alloc] initWithTitle:@"Cache deleted" message:nil delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] autorelease] show];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
    // Override point for customization after app launch
    [window addSubview:viewController.view];
    [window makeKeyAndVisible];

	return YES;
}

- (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url {
    NSString *host = [url host];
    
    if ( [host compare:@"view"] == NSOrderedSame ) {
        [self view:[self parseId:url]];
    } else if ( [host compare:@"continue"] == NSOrderedSame ) {
        [self continueView];
    } else if ( [host compare:@"deleteCache" ] == NSOrderedSame ) {
        [self deleteCache:[self parseId:url]];
    }

    return YES;
}

#pragma mark DownloadManagerDelegate

- (void)initDownloadCompleted:(int)docId {
    [viewController showImage:docId page:0];
    
    [loading dismissWithClickedButtonIndex:0 animated:YES];
    loading = nil;
}

- (void)dealloc {
    [viewController release];
    [window release];
    
    [super dealloc];
}

@end

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

@implementation MapDocAppDelegate

@synthesize window;
@synthesize viewController;

- (id)parseId:(NSURL *)url {
    return [[url path] substringFromIndex:1];
}

- (void)view:(id)docId
{
    if ( [_datasource existsDocument:docId] ) {
        [viewController showImage:docId page:0];
    } else {
        if ( [_datasource hasDownloading] ) {
            [[[[UIAlertView alloc] initWithTitle:@"Downloading file exist" message:nil delegate:nil cancelButtonTitle:@"OK"
                               otherButtonTitles:nil] autorelease] show];
            return;
        }
        
        loading = [[[UIAlertView alloc] initWithTitle:@"\n\nLoading...\nThis process will take few minutes..." message:nil
                                             delegate:nil cancelButtonTitle:nil otherButtonTitles:nil] autorelease];
        [loading show];
     
		_datasource.downloadManagerDelegate = self;
        [_datasource startDownload:docId baseUrl:nil];
    }
}

- (void)view2:(NSURL*)url {

    // urlから接続先URL, docidを分解する
	NSString *baseUrl = [NSString stringWithFormat:@"http://%@%@", [url host], [url path]];
	
	
	NSArray *a = [[url query] componentsSeparatedByString:@"&"];
	NSMutableDictionary *param = [[NSMutableDictionary alloc] init];
	for (NSString *s in a) {
		NSArray *keyAndValue = [s componentsSeparatedByString:@"="];
		// TODO unescape
		[param setObject:[keyAndValue objectAtIndex:1] forKey:[keyAndValue objectAtIndex:0]];
	}

	// docidを作る
	NSString *docId = [NSString stringWithFormat:@"%@_%@", [param objectForKey:@"p"], [param objectForKey:@"v"]];

	if ( [_datasource existsDocument:docId] ) {
        [viewController showImage:docId page:0];
    } else {
        if ( [_datasource hasDownloading] ) {
            [[[[UIAlertView alloc] initWithTitle:@"Downloading file exist" message:nil delegate:nil cancelButtonTitle:@"OK"
                               otherButtonTitles:nil] autorelease] show];
            return;
        }
        
        loading = [[[UIAlertView alloc] initWithTitle:@"\n\nLoading...\nThis process will take few minutes..." message:nil
                                             delegate:nil cancelButtonTitle:nil otherButtonTitles:nil] autorelease];
        [loading show];
        
		//DownloadManager *dm = [DownloadManager instance];
        //dm.delegate = self;
		//dm.baseUrl = baseUrl;
		_datasource.downloadManagerDelegate = self;
        [_datasource startDownload:docId baseUrl:baseUrl];
    }


}


- (void)continueView {
    HistoryObject *history = [_datasource history];
    if ( !history ) {
        [[[[UIAlertView alloc] initWithTitle:@"No history" message:nil delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] autorelease] show];
        return;
    }

    [viewController showImage:history.documentId page:history.page];
}

- (void)deleteCache:(id)docId {
    [_datasource deleteCache:docId];
    
    [[[[UIAlertView alloc] initWithTitle:@"Cache deleted" message:nil delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] autorelease] show];
}

- (void)downloaded {
    [viewController showBookshelfDeletion];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
    // Override point for customization after app launch

	// UstDocDatasourceを標準とする
	_datasource = [[UstDocDatasource alloc] init];
	viewController.datasource = _datasource;
	
	[window addSubview:viewController.view];
    [window makeKeyAndVisible];
    viewController.window = window;

	return YES;
}

- (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url
{
	NSLog(@"url : %@", url);
	
	if ([[url scheme] compare:@"mapdoc"] != NSOrderedSame) return NO;
    
	NSString *host = [url host];
    if ( [host compare:@"view"] == NSOrderedSame ) {
        [self view:[self parseId:url]];
    } else if ( [host compare:@"continue"] == NSOrderedSame ) {
        [self continueView];
    } else if ( [host compare:@"downloaded"] == NSOrderedSame ) {
        [self downloaded];
    } else {
		// URLに記載されているホストからダウンロードを試みる
        [self view2:url];
	}
	
    return YES;
//	return [self application:application openURL:url sourceApplication:nil annotation:nil];
}

/*
- (BOOL) application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
	//url = [NSURL URLWithString:@"mapdoc://test.md-dc.jp/book/dl/exec/0000005x/0000002a/docnext/7nxhdsa51hcbx1fg/?p=000ghnpc&v=00001bte"];
	
	NSLog(@"url : %@", url);
	
	if ([[url scheme] compare:@"mapdoc"] != NSOrderedSame) return NO;
    
	NSString *host = [url host];
    if ( [host compare:@"view"] == NSOrderedSame ) {
        [self view:[self parseId:url]];
    } else if ( [host compare:@"continue"] == NSOrderedSame ) {
        [self continueView];
    } else if ( [host compare:@"downloaded"] == NSOrderedSame ) {
        [self downloaded];
    } else {
		// URLに記載されているホストからダウンロードを試みる
        [self view2:url];
	}
	
    return YES;
}
*/
#pragma mark DownloadManagerDelegate

- (void)didMetaInfoDownloadFinished:(id)docId {
    [viewController showImage:docId page:0];
    
    [loading dismissWithClickedButtonIndex:0 animated:YES];
    loading = nil;
}

- (void)didMetaInfoDownloadFailed:(id)docId error:(NSError*)error {
	// メタ情報のダウンロード失敗
	NSLog(@"metainfo download failed : %@", docId);
	assert(0);
//	[viewController showImage:docId page:0];
//    [loading dismissWithClickedButtonIndex:0 animated:YES];
//    loading = nil;
}

- (void)dealloc {
    [viewController release];
    [window release];
    
    [super dealloc];
}

@end

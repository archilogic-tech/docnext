//
//  MapDocAppDelegate.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/22.
//  Copyright __MyCompanyName__ 2010. All rights reserved.
//

#import "MapDocAppDelegate.h"
#import "MapDocViewController.h"
#import "ImageViewController.h"
#import "BookshelfDeletionViewController.h"

// to be removed.
#import "UstDocDatasource.h"


@implementation MapDocAppDelegate

@synthesize window;
@synthesize viewController;
@synthesize datasource = _datasource;

- (id)parseId:(NSURL *)url {
	NSString *tmp = [[url path] substringFromIndex:1];
	NSArray *a = [tmp componentsSeparatedByString:@","];
	return a;
}

- (void)view:(id<NSObject>)metaDocumentId
{
    if ( [_datasource existsDocument:metaDocumentId] ) {
		DocumentContext *dc = [[DocumentContext alloc] init];
		dc.documentId = metaDocumentId;

		ImageViewController *ic = [ImageViewController createViewController:_datasource];
		ic.documentContext = dc;

		[ic showInViewController:self.viewController];
		[dc release];
    } else {
        [_datasource startDownload:metaDocumentId baseUrl:nil];
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

		DocumentContext *dc = [[DocumentContext alloc] init];
		dc.documentId = docId;
		
		ImageViewController *ic = [ImageViewController createViewController:_datasource];
		ic.documentContext = dc;
		[ic showInViewController:self.viewController];
		[dc release];

	} else {
        [_datasource startDownload:docId baseUrl:baseUrl];
    }
}

- (void)continueView
{
    DocumentContext *history = [_datasource history];
    if ( !history ) {
        [[[[UIAlertView alloc] initWithTitle:@"No history" message:nil delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] autorelease] show];
        return;
    }
	
	ImageViewController *ic = [ImageViewController createViewController:_datasource];
	ic.documentContext = history;
	[ic showInViewController:self.viewController];
}

- (void)downloaded {
	BookshelfDeletionViewController *c = [BookshelfDeletionViewController createViewController:_datasource];

	UINavigationController *nc = self.viewController;
    [nc pushViewController:c animated:YES];
}

- (void)checkVersion
{
	NSUserDefaults *d = [NSUserDefaults standardUserDefaults];

	NSString *fileSystemVersion = [d stringForKey:@"APP_VERSION"];

	// TODO for debug...
	//fileSystemVersion = nil;
	
	if (!fileSystemVersion) {
		// 初回起動
		[_datasource updateSystemFromVersion:fileSystemVersion toVersion:APP_VERSION];
		[d setObject:APP_VERSION forKey:@"APP_VERSION"];
	} else {
		int tmp = [APP_VERSION compare:fileSystemVersion];
		if (tmp == NSOrderedDescending) {
			NSLog(@"updating cache...");
			[_datasource updateSystemFromVersion:fileSystemVersion toVersion:APP_VERSION];
			[d setObject:APP_VERSION forKey:@"APP_VERSION"];
		} else if (tmp == NSOrderedAscending) {
			// アプリが古いバージョンになった
			assert(0);
		} else {
			// 同じ
		}
	}
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
    // Override point for customization after app launch

	// TODO UstDocDatasourceを標準とする
	_datasource = [[UstDocDatasource alloc] init];
	viewController.datasource = _datasource;
	_datasource.downloadManagerDelegate = viewController;

	[self checkVersion];
	
	[window addSubview:viewController.view];
    [window makeKeyAndVisible];

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

- (void)dealloc {
	[viewController release];
	[window release];
    
	[super dealloc];
}

- (void)applicationDidReceiveMemoryWarning:(UIApplication *)application
{
	[_datasource didReceiveMemoryWarning];
}


@end

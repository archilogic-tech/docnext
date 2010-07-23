//
//  DownloadUtil.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/20.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "DownloadManager.h"
#import "Const.h"
#import "ASIHTTPRequest.h"
#import "FileUtil.h"
#import "ZipArchive.h"

@interface DownloadManager ()
- (void)downloadNextPage:(int)docId page:(int)page px:(int)px py:(int)py;
@end


@implementation DownloadManager

@synthesize delegate;

+ (DownloadManager *)instance {
    static DownloadManager *_instance = nil;
    if ( !_instance ) {
        _instance = [[DownloadManager alloc] init];

        if ( [_instance hasDownloading] ) {
            [_instance resume];
        }
    }
    
    return _instance;
}

- (void)startDownload:(int)docId {
    NSString *url = [NSString stringWithFormat:@"%@download?documentId=%d" , ServerEndpoint , docId];
    ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:url]];
    request.delegate = self;
    request.didFinishSelector = @selector(downloadFinish:);
    request.downloadDestinationPath = [FileUtil getFullPath:[NSString stringWithFormat:@"%d.zip" , docId]];
    request.userInfo = [NSMutableDictionary dictionaryWithCapacity:0];
    [request.userInfo setValue:[NSNumber numberWithInt:docId] forKey:@"id"];
    
    [request startAsynchronous];
}

- (BOOL)hasDownloading {
    return [FileUtil existsDownloadStatus];
}

- (void)resume {
    DownloadStatusObject *downloadStatus = [FileUtil downloadStatus];
    
    [self downloadNextPage:downloadStatus.docId page:downloadStatus.downloadedPage px:downloadStatus.downloadedPx
                        py:downloadStatus.downloadedPy];
}

- (void)downloadPage:(int)docId page:(int)page px:(int)px py:(int)py {
    NSString *type = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ? @"iPad" : @"iPhone";
    int level = [[UIScreen mainScreen] scale] == 2.0 ? 1 : 0;
    
    NSString *url = [NSString stringWithFormat:@"%@getPage?type=%@&documentId=%d&page=%d&level=%d&px=%d&py=%d" ,
                     ServerEndpoint , type , docId , page , level , px , py];
    ASIHTTPRequest *request = [ASIHTTPRequest requestWithURL:[NSURL URLWithString:url]];
    request.delegate = self;
    request.didFinishSelector = @selector(downloadPageFinish:);
    request.downloadDestinationPath = [FileUtil getFullPath:[NSString stringWithFormat:@"%d/images/%@-%d-%d-%d-%d.jpg" , docId , type , page , level , px , py]];
    request.userInfo = [NSMutableDictionary dictionaryWithCapacity:0];
    [request.userInfo setValue:[NSNumber numberWithInt:docId] forKey:@"id"];
    [request.userInfo setValue:[NSNumber numberWithInt:page] forKey:@"page"];
    [request.userInfo setValue:[NSNumber numberWithInt:px] forKey:@"px"];
    [request.userInfo setValue:[NSNumber numberWithInt:py] forKey:@"py"];
    
    [request startAsynchronous];
}

- (void)downloadComplete:(int)docId {
    if ( [delegate respondsToSelector:@selector(downloadCompleted:)] ) {
        [delegate downloadCompleted:docId];
    }
    [FileUtil deleteDownloadStatus];
    
    NSMutableArray *downloaded = [NSMutableArray arrayWithArray:[FileUtil downloadedIds]];
    [downloaded addObject:[NSString stringWithFormat:@"%d" , docId]];
    [FileUtil saveDownloadedIds:downloaded];
}

- (void)downloadNextPage:(int)docId page:(int)page px:(int)px py:(int)py {
    int pages = [FileUtil pages:docId];
    
    if ( [[UIScreen mainScreen] scale] == 2.0 ) {
        if ( [delegate respondsToSelector:@selector(downloadProgressed:downloaded:)] ) {
            [delegate downloadProgressed:docId downloaded:(1.0 * ( page + 1.0 ) / pages)];
        }
        
        if ( px < 1 ) {
            [self downloadPage:docId page:page px:(px + 1) py:py];
        } else if ( py < 1 ) {
            [self downloadPage:docId page:page px:0 py:(py + 1)];
        } else if ( page + 1 < pages ) {
            [self downloadPage:docId page:(page + 1) px:0 py:0];
        } else {
            [self downloadComplete:docId];
        }
    } else {
        if ( [delegate respondsToSelector:@selector(downloadProgressed:downloaded:)] ) {
            [delegate downloadProgressed:docId downloaded:(1.0 * ( page + 1.0 ) / pages)];
        }
        
        if ( page + 1 < pages ) {
            [self downloadPage:docId page:(page + 1) px:0 py:0];
        } else {
            [self downloadComplete:docId];
        }
    }
}

- (void)updateDownloadStatus:(int)docId page:(int)page px:(int)px py:(int)py {
    DownloadStatusObject *downloadStatus = [[DownloadStatusObject new] autorelease];
    downloadStatus.docId = docId;
    downloadStatus.downloadedPage = page;
    downloadStatus.downloadedPx = px;
    downloadStatus.downloadedPy = py;
    
    [FileUtil saveDownloadStatus:downloadStatus];
}

#pragma mark ASIHTTPRequest didFinishSelector

- (void)downloadFinish:(ASIHTTPRequest *)request {
    int docId = [[request.userInfo objectForKey:@"id"] intValue];
    
    NSString *zipName = [NSString stringWithFormat:@"%d.zip" , docId];
    
    NSString *dirName = [NSString stringWithFormat:@"%d/" , docId];
    [FileUtil delete:dirName];
    
    ZipArchive *zip = [[ZipArchive new] autorelease];
    if ( [zip UnzipOpenFile:[FileUtil getFullPath:zipName]] ) {
        [zip UnzipFileTo:[FileUtil getFullPath:dirName] overWrite:YES];
        [zip UnzipCloseFile];
    }

    // Take care this way is depended on the fact 2x2 is max
    [self updateDownloadStatus:docId page:-1 px:1 py:1];

    [self downloadPage:docId page:0 px:0 py:0];
    
    if ( [delegate respondsToSelector:@selector(initDownloadCompleted:)] ) {
        [delegate initDownloadCompleted:docId];
    }
}

- (void)downloadPageFinish:(ASIHTTPRequest *)request {
    int docId = [[request.userInfo objectForKey:@"id"] intValue];
    int page = [[request.userInfo objectForKey:@"page"] intValue];
    int px = [[request.userInfo objectForKey:@"px"] intValue];
    int py = [[request.userInfo objectForKey:@"py"] intValue];
    
    [self updateDownloadStatus:docId page:page px:px py:py];

    [self downloadNextPage:docId page:page px:px py:py];
}

#pragma mark ASIHTTPRequestDelegate

- (void)requestFailed:(ASIHTTPRequest *)request {
    NSLog( @"Request Failed: %@" , [[request error] localizedDescription] );
}

@end

//
//  DownloadUtil.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/20.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SampleDatasource.h"
#import "DownloadManagerDelegate.h"

@class SampleDatasource;

@interface MediaDoDownloadManager : NSObject {
    id<NSObject,DownloadManagerDelegate> delegate;
	SampleDatasource *_datasource;
}

@property(nonatomic,assign) id<NSObject,DownloadManagerDelegate> delegate;
@property(nonatomic,retain) SampleDatasource *datasource;

- (void)startMetaInfoDownload:(id)docId baseUrl:(NSString*)baseUrl;
- (void)resume;



@end

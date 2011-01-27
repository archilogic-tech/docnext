//
//  PageDownloadOperation.h
//  MapDoc
//
//  Created by sakukawa on 11/01/27.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UstDocDownloadManager.h"


@interface PageDownloadOperation : NSOperation {

	NSString *_url;
	NSString *_destination;
}

@property (nonatomic, copy) NSString *url;
@property (nonatomic, copy) NSString *destination;

@end

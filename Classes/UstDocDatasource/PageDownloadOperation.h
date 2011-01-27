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
	/*
	id<NSObject> _metaDocumentId;
	id<NSObject> _documentId;
	UstDocDownloadManager *_downloadManager;
	BOOL _finished;
	 */
}

@property (nonatomic, copy) NSString *url;
@property (nonatomic, copy) NSString *destination;
/*
@property (nonatomic, copy) id<NSObject> metaDocumentId;
@property (nonatomic, copy) id<NSObject> documentId;
@property (nonatomic, retain) UstDocDownloadManager *downloadManager;
*/
@end

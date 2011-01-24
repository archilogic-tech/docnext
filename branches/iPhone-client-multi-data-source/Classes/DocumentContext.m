//
//  DocumentContext.m
//  MapDoc
//
//  Created by sakukawa on 11/01/19.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import "DocumentContext.h"
#import "MapDocAppDelegate.h"

@implementation DocumentContext

@synthesize currentPage = _currentPage;
@synthesize currentIndex = _currentIndex;


- (id)init
{
	if ((self = [super init])) {

		// 暫定
		MapDocAppDelegate *d = (MapDocAppDelegate*)[UIApplication sharedApplication].delegate;
		_datasource = [d.datasource retain];
		
		UIDevice *device = [UIDevice currentDevice];					//Get the device object
		[device beginGeneratingDeviceOrientationNotifications];			//Tell it to start monitoring the accelerometer for orientation
		NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];	//Get the notification centre for the app
		[nc addObserver:self											//Add yourself as an observer
			   selector:@selector(didInterfaceOrientationChanged:)
				   name:UIDeviceOrientationDidChangeNotification
				 object:device];

		_currentOrientation = [device orientation];
	}
	return self;
}

- (void)dealloc
{
	[_documentId release];
	[_datasource release];
	[super dealloc];
}


- (void)didInterfaceOrientationChanged:(NSNotification*)n
{
	UIInterfaceOrientation o = [[n object] orientation];
	if (o != _currentOrientation) {
		// 1画面あたりのページ数が変更されるので、論理ページをつくり直す
		[self buildPageHeads];
	}
	_currentOrientation = o;

	// 画面の向きが変わったら、論理ページを構築しなおす
	[self loadSinglePageInfo];
	[self buildPageHeads];
}

- (id<NSObject>)documentId
{
	return _documentId;
}

- (void)setDocumentId:(id <NSObject>)docId
{
	[_documentId release];

	// 複数の文書をもつ前提とする
	if ([docId isKindOfClass:[NSArray class]]) {
		_documentId = [docId retain];
	} else {
		_documentId = [[NSArray alloc] initWithObjects:docId, nil];
	}


	_currentPage = 0;
	
	// 文書IDが変わったら、論理ページを構築しなおす
	[self loadSinglePageInfo];
	[self buildPageHeads];
}


- (BOOL)isValidIndex:(int)i
{
	if (i < 0) return NO;
	
	for (NSArray *ph in _pageHeads) {
		int count = [ph count];
		if (i < count) return YES;
		i -= (count);
	}
	return NO;
/*	
	
    if ( i < 0 || i >= [_pageHeads count] ) {
        return NO;
    }
	return YES;
 */
}


- (BOOL)isValidPage:(int)i
{
	for (int j = 0; j < [_documentId count]; j++) {

		int n = [self totalPageWithDocumentOffset:j];
		if (i < n) return YES;
		i -= (n);
	}
	return NO;
}


- (int)currentPageByIndex:(int)i
{
	int page = 0;
	for (NSArray *ph in _pageHeads) {
		int count = [ph count];
		if (i < count) {
			int n = page + [[ph objectAtIndex:i] intValue];
			return n;
		}
		page += [[ph lastObject] intValue]+1;
		i -= (count);
	}
	return [[_pageHeads objectAtIndex:i] intValue];
}


- (int)currentIndexByPage:(int)page
{
	int index = 0;
	for (NSArray *ph in _pageHeads) {
		int count = [ph count];
		for ( int i = 0; i < count; i++ ) {
			int head = [[ph objectAtIndex:i] intValue];
			if ( head == page ) {
				return index + i;
			} else if ( head > page ) {
				return index + i - 1;
			}
		}
		index += count;
    }
	return index;// - 1;
	//    return [_pageHeads count] - 1;
}


- (void)setCurrentPage:(int)n
{
	if (![self isValidPage:n]) {
		NSLog(@"Invalid page : %d", n);
		return;
	}
	
	_currentPage = n;
	_currentIndex = [self currentIndexByPage:n];
}

- (void)setCurrentIndex:(int)n
{
	_currentIndex = n;
	_currentPage = [self currentPageByIndex:n];
}


- (int)totalPageWithDocumentOffset:(int)i
{
	NSString *did = [_documentId objectAtIndex:i];
	int totalPage = [_datasource pages:_documentId documentId:did];
	return totalPage;
}


- (int)totalPage
{
	int sum = 0;
	for (int i = 0; i < [_documentId count]; i++) {
		sum += [self totalPageWithDocumentOffset:i];
	}
	return sum;
}


- (BOOL)isSinglePage
{
//	int documentOffset = 0;
	int relativeIndex = _currentIndex;
	for (NSArray *tmp in _isSinglePage) {
		int count = [tmp count];
		if (relativeIndex < [tmp count]) {
			return [[tmp objectAtIndex:relativeIndex] boolValue];
		}
		relativeIndex -= (count);
//		documentOffset++;
	}
	return NO;

//	return ( [[_isSinglePage objectAtIndex:_currentIndex] boolValue] );
}

- (NSArray*)titles
{
	NSMutableArray *result = [NSMutableArray array];
	for (NSString *did in _documentId) {
		NSArray *tmp = [_datasource tocs:_documentId documentId:did];
		[result addObjectsFromArray:tmp];
//		[result addObject:tmp];
	}
	return result;
	//return [_datasource tocs:_documentId];
}

- (int)relativePage:(int*)page
{
	int relativePage = *page;
	int offset = 0;
	for (NSString *did in _documentId) {
		int count = [_datasource pages:_documentId documentId:did];
		if (relativePage < count) {
			*page = relativePage;
			return offset;
		}
		relativePage -= (count);
		offset++;
	}
	return -1;
}

- (NSString*)publisher
{
	// TODO 暫定的に最初の文書の情報を利用する
	id<NSObject> did = [_documentId objectAtIndex:0];

	NSString *r = [_datasource publisher:_documentId documentId:did];
	return r;
}


- (NSString*)titleWithPage:(int)page
{
	int relativePage = page;
	int documentOffset = [self relativePage:&relativePage];
	if (documentOffset < 0) return nil;
	
	id<NSObject> did = [_documentId objectAtIndex:documentOffset];
	
	TOCObject *toc = (TOCObject*)[_datasource toc:_documentId documentId:did page:relativePage];
	NSString *tmp = toc.text;
	return tmp;
}

- (NSString*)title
{
	return [self titleWithPage:_currentIndex];
}


- (NSString*)texts
{
	int relativePage = _currentPage;
	for (NSString *did in _documentId) {
		int count = [_datasource pages:_documentId documentId:did];
		if (relativePage < count) {
			// relativePageがfixした
			NSString *tmp = [_datasource texts:_documentId documentId:did page:relativePage];
			return tmp;
		}
		relativePage -= (count);
	}
	return nil;
}

- (NSArray*)freehand
{
	int relativePage = _currentPage;
	for (NSString *did in _documentId) {
		int count = [_datasource pages:_documentId documentId:did];
		if (relativePage < count) {
			// relativePageがfixした
			NSArray *r = [_datasource freehand:_documentId
									documentId:did
										  page:relativePage];
			return r;
		}
		relativePage -= (count);
	}
	return nil;
}

- (NSArray*)annotations
{
	int relativePage = _currentPage;
	for (NSString *did in _documentId) {
		int count = [_datasource pages:_documentId documentId:did];
		if (relativePage < count) {
			// relativePageがfixした
			NSArray *r = [_datasource annotations:_documentId
									   documentId:did
											 page:relativePage];
			return r;
		}
		relativePage -= (count);
	}
	return nil;
}

- (NSArray*)highlights
{
	int relativePage = _currentPage;
	for (NSString *did in _documentId) {
		int count = [_datasource pages:_documentId documentId:did];
		if (relativePage < count) {
			// relativePageがfixした
			NSArray *r = [_datasource highlights:_documentId
									  documentId:did
											page:relativePage];
			return r;
		}
		relativePage -= (count);
	}
	return nil;
}



- (NSArray*)region
{
	int relativePage = _currentPage;
	for (NSString *did in _documentId) {
		int count = [_datasource pages:_documentId documentId:did];
		if (relativePage < count) {
			// relativePageがfixした
			NSArray *r = [_datasource regions:_documentId
								   documentId:did
										 page:relativePage];
			return r;
		}
		relativePage -= (count);
	}
	return nil;
}

- (double)ratio
{
	// 暫定で0だけ
	id<NSObject> did = [_documentId objectAtIndex:0];
	double ratio = (double)[_datasource ratio:_documentId documentId:did];
	return ratio;
}

- (void)loadSinglePageInfo
{
	NSMutableArray *singlePageInfoList = [[NSMutableArray alloc] init];
	for (NSString *did in (NSArray*)_documentId) {

		NSArray *tmp = [_datasource singlePageInfo:_documentId documentId:did];
		[singlePageInfoList addObject:[NSSet setWithArray:tmp]];
	}
	_singlePageInfo = singlePageInfoList;
}

- (UIImage*)thumbnailWithIndex:(int)index
{
	int documentOffset = 0;
	int relativeIndex = index;
	for (NSArray *tmp in _isSinglePage) {
		int count = [tmp count];
		if (relativeIndex < [tmp count]) {

			return [_datasource thumbnail:_documentId documentId:[_documentId objectAtIndex:documentOffset] cover:relativeIndex];
//			return [[tmp objectAtIndex:_currentIndex] boolValue];
		}
		relativeIndex -= (count);
		documentOffset++;
	}
	return nil;
}

- (NSString*)imageTextWithPage:(int)page
{
	int relativePage = page;
	int documentOffset = [self relativePage:&relativePage];
	if (documentOffset < 0) return nil;
	
	id<NSObject> did = [_documentId objectAtIndex:documentOffset];

	NSString *text = [_datasource imageText:_documentId documentId:did page:relativePage];
	return text;
}

/////////////////////////////////////////////////////////



- (BOOL)isSinglePage:(int)page
{
	NSString *pageStr = [[NSNumber numberWithInt:page] stringValue];
	
	int documentOffset = 0;
	for (NSSet *spi in _singlePageInfo) {
		if ([spi containsObject:pageStr]) {
			return YES;
		}
/*
		for ( NSString *value in spi ) {
			if ( [value intValue] == page ) {
				return YES;
			}
		}
 */
		int count = [self totalPageWithDocumentOffset:documentOffset];
		page -= (count);
		documentOffset++;
	}    
    return NO;
}

- (void)buildPageHeads
{
	NSMutableArray *pageHeadsList = [[NSMutableArray alloc] init];
	NSMutableArray *isSinglePageList = [[NSMutableArray alloc] init];

	for (int documentOffset = 0; documentOffset < [_documentId count]; documentOffset++) {
		NSMutableArray *pageHeads = [NSMutableArray array];
		NSMutableArray *isSinglePage = [NSMutableArray array];
		
		int totalPage = [self totalPageWithDocumentOffset:documentOffset];
		
		BOOL isLandscape = UIInterfaceOrientationIsLandscape(_currentOrientation);
		
		for ( int page = 0 ; page < totalPage ; ) {
			[pageHeads addObject:[NSNumber numberWithInt:page]];
			if ( isLandscape &&
				![self isSinglePage:page] &&
				page + 1 < totalPage &&
				![self isSinglePage:(page + 1)] ) {
				
				[isSinglePage addObject:[NSNumber numberWithBool:NO]];
				page += 2;
			} else {
				[isSinglePage addObject:[NSNumber numberWithBool:YES]];
				page += 1;
			}
		}
		[pageHeadsList addObject:pageHeads];
		[isSinglePageList addObject:isSinglePage];
	}
    
    _pageHeads = pageHeadsList;
    _isSinglePage = isSinglePageList;

	_currentIndex = [self currentIndexByPage:_currentPage];
}


- (UIView*)getTileImageWithType:(NSString*)type page:(int)page column:(int)column row:(int)row resolution:(int)resolution
{
	int relativePage = page;
	for (NSString *did in _documentId) {
		
		int count = [_datasource pages:_documentId documentId:did];
		if (relativePage < count) {
			// relativePageがfixした
			return [_datasource getTileImageWithDocument:_documentId
											  documentId:did
													type:type
													page:relativePage
												  column:column
													 row:row
											  resolution:resolution];
		}
		relativePage -= (count);
	}
	return nil;
}





////////////////////////////////////////////////////////////////
/*
- (NSComparisonResult)compare:(DocumentContext *)anotherDocumentContext
{
	// TBD
	assert(0);
}
*/

@end

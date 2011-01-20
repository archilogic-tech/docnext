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

//@synthesize documentId = _documentId;
//@synthesize documentOffset = _documentOffset;
@synthesize currentPage = _currentPage;
@synthesize currentIndex = _currentIndex;


//done
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
	}
	return self;
}

// done
- (void)dealloc
{
	[_documentId release];
	[_datasource release];
	[super dealloc];
}

//done
- (void)didInterfaceOrientationChanged:(NSNotification*)n
{
	UIInterfaceOrientation o = [[n object] orientation];
	if (o != _currentOrientation) {
		// 1画面あたりのページ数が変更されるので、論理ページをつくり直す
		[self buildPageHeads];
	}
	_currentOrientation = o;
}

- (id<NSObject>)documentId
{
	if ([_documentId count] < 2) return [_documentId objectAtIndex:0];
	return _documentId;
}


// done
- (void)setDocumentId:(id <NSObject>)docId
{
	[_documentId release];

	// 複数の文書をもつ前提とする
	if ([docId isKindOfClass:[NSArray class]]) {
		_documentId = [docId retain];
	} else {
		_documentId = [[NSArray alloc] initWithObjects:docId, nil];
	}


//	_documentOffset = 0;
	_currentPage = 0;
	
	// 文書IDが変わったら、論理ページを構築しなおす
	[self loadSinglePageInfo];
	[self buildPageHeads];
}

//done
- (BOOL)isValidIndex:(int)i
{
	if (i < 0) return NO;
	
	for (NSArray *ph in _pageHeads) {
		int count = [ph count];
		if (i < count) return YES;
		i -= count;
	}
	return NO;
/*	
	
    if ( i < 0 || i >= [_pageHeads count] ) {
        return NO;
    }
	return YES;
 */
}

// done
- (BOOL)isValidPage:(int)i
{
	for (int j = 0; j < [_documentId count]; j++) {
//		NSString *did = [_documentId objectAtIndex:j];

		int n = [self totalPageWithDocumentOffset:j];
		if (i < n) return YES;
		i -= n;
	}
	return NO;
}


// done
- (int)currentPageByIndex:(int)i
{
	int page = 0;
	for (NSArray *ph in _pageHeads) {
		int count = [ph count];
		if (i < count) {
			int n = page + [[ph objectAtIndex:i] intValue];
			return n;
		}
		page += [[ph lastObject] intValue];
		i -= count;
	}
	return [[_pageHeads objectAtIndex:i] intValue];
}

// done
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
	return index - 1;
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

//done
- (int)totalPageWithDocumentOffset:(int)i
{
	NSString *did = [_documentId objectAtIndex:i];
	int totalPage = [_datasource pages:_documentId documentId:did];
	return totalPage;
}

//done
- (int)totalPage
{
	int sum = 0;
	for (int i = 0; i < [_documentId count]; i++) {
		sum += [self totalPageWithDocumentOffset:i];
	}
	return sum;
}

// done
- (BOOL)isSinglePage
{
	int documentOffset = 0;
	int relativeIndex = _currentIndex;
	for (NSArray *tmp in _isSinglePage) {
		int count = [tmp count];
		if (relativeIndex < [tmp count]) {
			return [[tmp objectAtIndex:_currentIndex] boolValue];
		}
		relativeIndex -= count;
		documentOffset++;
	}
	return NO;

//	return ( [[_isSinglePage objectAtIndex:_currentIndex] boolValue] );
}

// done
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

// done
- (NSString*)titleWithIndex:(int)index
{
	int relativePage = _currentPage;
	for (NSString *did in _documentId) {
		int count = [_datasource pages:_documentId documentId:did];
		if (relativePage < count) {
			// relativePageがfixした
			TOCObject *toc = (TOCObject*)[_datasource toc:_documentId documentId:did page:relativePage];
			NSString *tmp = toc.text;
			return tmp;
		}
		relativePage -= count;
	}
	return nil;
/*
	NSString *tmp = [_datasource toc:_documentId page:absolutePage].text;
	return tmp;
 */
}

// done
- (NSString*)title
{
	return [self titleWithIndex:_currentIndex];
}

// done
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
		relativePage -= count;
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
		relativePage -= count;
	}
	return nil;
}

// done
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
		relativePage -= count;
	}
	return nil;
}

// done
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
		relativePage -= count;
	}
	return nil;
}


// done
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
		relativePage -= count;
	}
	return nil;
}

// done
- (double)ratio
{
	// 暫定で0だけ
	id<NSObject> did = [_documentId objectAtIndex:0];
	double ratio = (double)[_datasource ratio:_documentId documentId:did];
	return ratio;
}

// done
- (void)loadSinglePageInfo
{
	NSMutableArray *singlePageInfoList = [[NSMutableArray alloc] init];
	for (NSString *did in (NSArray*)_documentId) {

		NSArray *tmp = [_datasource singlePageInfo:_documentId documentId:did];
		[singlePageInfoList addObject:[NSSet setWithArray:tmp]];
	}
	_singlePageInfo = singlePageInfoList;

//	singlePageInfo = [[_datasource singlePageInfo:_documentId] retain];
}

// done
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
		relativeIndex -= count;
		documentOffset++;
	}
	return nil;
	
	
	
}


/////////////////////////////////////////////////////////


// done
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
		page -= [self totalPageWithDocumentOffset:documentOffset];
		documentOffset++;
	}    
    return NO;
}

// done
- (void)buildPageHeads
{
	NSMutableArray *pageHeadsList = [[NSMutableArray alloc] init];
	NSMutableArray *isSinglePageList = [[NSMutableArray alloc] init];

	for (int documentOffset = 0; documentOffset < [_documentId count]; documentOffset++) {
//	for (NSString *did in _documentId) {
		NSMutableArray *pageHeads = [NSMutableArray array];
		NSMutableArray *isSinglePage = [NSMutableArray array];
		
		int totalPage = [self totalPageWithDocumentOffset:documentOffset];
//		int totalPage = [_datasource pages:_documentId];
		
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
		
		// TODO この数え方はダメ
		//		NSArray *tmp = [_datasource tocs:_documentId documentId:did];
		//int count = [tmp count];
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
		relativePage -= count;
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

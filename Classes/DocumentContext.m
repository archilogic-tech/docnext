//
//  DocumentContext.m
//  MapDoc
//
//  Created by sakukawa on 11/01/19.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import "DocumentContext.h"
#import "MapDocAppDelegate.h"
#import "DocumentSearchResult.h"

@interface DocumentContext (Private)
- (int)relativeIndex:(int*)index;
- (int)relativePage:(int*)page;
- (void)didInterfaceOrientationChanged:(UIInterfaceOrientation)n;
- (BOOL)isValidIndex:(int)i;
- (BOOL)isValidPage:(int)i;
- (int)currentPageByIndex:(int)i;
- (int)currentIndexByPage:(int)page;




// 外部
- (id)init;
- (void)dealloc;
- (BOOL)isEqualToMetaDocumentId:(id<NSObject>)did;
- (id<NSObject>)documentId;
- (void)setDocumentId:(id <NSObject>)docId;
- (void)setCurrentPage:(int)n;
- (void)setCurrentIndex:(int)n;
- (int)totalPage;
- (BOOL)isSingleIndex;

- (UIImage*)thumbnailWithPage:(int)page;

- (NSString*)documentTitle;
- (NSString*)publisher;


- (double)ratio;
- (NSArray*)titles;

- (NSString*)title;
- (NSString*)titleWithPage:(int)page;

- (NSString*)texts;

// ユーザ操作系
- (NSArray*)freehand;
- (NSArray*)annotations;
- (NSArray*)highlights;

- (UIView*)getTileImageWithType:(NSString*)type page:(int)page column:(int)column row:(int)row resolution:(int)resolution;

+ (DocumentContext *)objectWithDictionary:(NSDictionary *)dictionary;
- (NSDictionary *)toDictionary;


// 内部
//- (BOOL)isSinglePage:(int)page;
- (int)totalPageWithDocumentOffset:(int)i;





- (NSArray*)region;
- (NSString*)imageText;
- (NSString*)imageTextWithPage:(int)page;
- (NSArray*)imageTextSearch:(NSString*)term;


- (void)buildPageHeads;



@end






@implementation DocumentContext

@synthesize currentPage = _currentPage;
@synthesize currentIndex = _currentIndex;
@synthesize normalizedCurrentPage = _normalizedCurrentPage;

- (id)init
{
	if ((self = [super init])) {

		// TODO 暫定
		MapDocAppDelegate *d = (MapDocAppDelegate*)[UIApplication sharedApplication].delegate;
		_datasource = [d.datasource retain];

		UIDevice *device = [UIDevice currentDevice];

		[device beginGeneratingDeviceOrientationNotifications];
		NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];
		[nc addObserver:self
			   selector:@selector(didInterfaceOrientationChangeHandler:)
				   name:UIDeviceOrientationDidChangeNotification
				 object:device];

		_currentOrientation = [device orientation];
		_totalPage = -1;
	}
	return self;
}

- (void)dealloc
{
	[[NSNotificationCenter defaultCenter] removeObserver:self];

	[_documentId release];
	[_datasource release];
	[_pageHeads release];
	[_isSingleIndex release];
	[_metaDocumentInfoCache release];

	[super dealloc];
}

- (BOOL)isEqualToMetaDocumentId:(id<NSObject>)did
{
	NSString *d1 = [_documentId componentsJoinedByString:@","];
	NSString *d2 = [(NSArray*)did componentsJoinedByString:@","];
	return [d1 isEqualToString:d2];
}

- (void)didInterfaceOrientationChangeHandler:(NSNotification*)n
{
	UIInterfaceOrientation o = [[n object] orientation];
	[self didInterfaceOrientationChanged:o];
}


- (void)didInterfaceOrientationChanged:(UIInterfaceOrientation)o
{
	_currentOrientation = o;

	// 現在設定されているページを正規化する
	self.currentPage = self.currentPage;
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
	_currentIndex = 0;
	_normalizedCurrentPage = 0;
	_currentPage = 0;
	_totalPage = -1;

	// メタ情報を読み込んでおく
	// TODO 暫定的に最初の文書の情報を利用する
	[_metaDocumentInfoCache release];
	id<NSObject> did = [_documentId objectAtIndex:0];
	_metaDocumentInfoCache = [[_datasource info:_documentId documentId:did] retain];

	// landscape時のページを構築する
	[self buildPageHeads];
}


- (BOOL)isValidIndex:(int)i
{
	if (i < 0) return NO;
	int relativeIndex = i;
	int documentOffset = [self relativeIndex:&relativeIndex];
	return (documentOffset >= 0);
}

- (BOOL)isValidPage:(int)i
{
	if (i < 0) return NO;
	int relativePage = i;
	int documentOffset = [self relativePage:&relativePage];
	return (documentOffset >= 0);
}


- (int)currentPageByIndex:(int)i
{
	if (UIInterfaceOrientationIsPortrait(_currentOrientation)) return i;
	
	int relativeIndex = i;
	int documentOffset = [self relativeIndex:&relativeIndex];
	if (documentOffset < 0) return -1;
	
	NSArray *ph = [_pageHeads objectAtIndex:documentOffset];

	int sum = 0;
	for (int i = 0; i < documentOffset; i++) {
		sum += [self totalPageWithDocumentOffset:i];
	}
	int page = [[ph objectAtIndex:relativeIndex] intValue];
	return sum + page;
}



// ページを正規化する
- (int)currentIndexByPage:(int)page
{
	if (UIInterfaceOrientationIsPortrait(_currentOrientation)) return page;

	int index = 0;

	int documentOffset;
	for (documentOffset = 0; documentOffset < [_pageHeads count]; documentOffset++) {
		NSArray *ph = [_pageHeads objectAtIndex:documentOffset];

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
		page -= [self totalPageWithDocumentOffset:documentOffset];
    }
	return index-1;
}


- (void)setCurrentPage:(int)n
{
	if (![self isValidPage:n]) {
		NSLog(@"Invalid page : %d", n);
		return;
	}
	
	// まず、未正規化の_currentPageを設定する
	_currentPage = n;
	if (UIInterfaceOrientationIsPortrait(_currentOrientation)) {
		_currentIndex = n;
		return;
	}
	
	_currentIndex = [self currentIndexByPage:n];

	// _currentPageを正規化して設定しなおす
	int relativeIndex = _currentIndex;
	int documentOffset = [self relativeIndex:&relativeIndex];
	if (documentOffset < 0) {
		// ERR
		assert(0);
	}
	
	int sum = 0;
	for (int i = 0; i < documentOffset; i++) {
		sum += [self totalPageWithDocumentOffset:i];
	}
	
	NSArray *tmp = [_pageHeads objectAtIndex:documentOffset];
	int page = [[tmp objectAtIndex:relativeIndex] intValue]; // indexの先頭ページに正規化する

	_normalizedCurrentPage = page+sum;
	assert(_currentIndex >= 0);
}


- (void)setCurrentIndex:(int)n
{
	_currentIndex = n;
	_normalizedCurrentPage = _currentPage = [self currentPageByIndex:n];

	assert(_currentPage >= 0);
}



- (int)totalPageWithDocumentOffset:(int)i
{
	NSString *did = [_documentId objectAtIndex:i];
	int totalPage = [_datasource pages:_documentId documentId:did];
	return totalPage;
}


- (int)totalPage
{
	if (_totalPage > 0) return _totalPage;
	
	_totalPage = 0;
	for (int i = 0; i < [_documentId count]; i++) {
		_totalPage += [self totalPageWithDocumentOffset:i];
	}
	return _totalPage;
}



- (BOOL)isSingleIndex
{
	if (UIInterfaceOrientationIsPortrait(_currentOrientation)) return YES;
	
	int relativeIndex = _currentIndex;
	int documentOffset = [self relativeIndex:&relativeIndex];
	if (documentOffset < 0) return NO;

	NSArray *tmp = [_isSingleIndex objectAtIndex:documentOffset];
	return [[tmp objectAtIndex:relativeIndex] boolValue];
}

- (NSArray*)titles
{
	return [_datasource tocs:_documentId];
}

- (int)relativeIndex:(int*)index
{
	int relativeIndex = *index;
	int offset = 0;
	for (NSString *did in _documentId) {

		
		int count;
		if (UIInterfaceOrientationIsPortrait(_currentOrientation)) {
			count = [self totalPageWithDocumentOffset:offset];
		} else {
			count = [[_pageHeads objectAtIndex:offset] count];
		}

		if (relativeIndex < count) {
			*index = relativeIndex;
			return offset;
		}
		relativeIndex -= count;
		offset++;
	}
	return -1;
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

// 文書メタ情報

// メタ情報
- (NSString*)publisher
{
	NSString *r = [_metaDocumentInfoCache objectForKey:@"publisher"];
	return r;
}

- (double)ratio
{
	double r = [[_metaDocumentInfoCache objectForKey:@"ratio"] doubleValue];
	return r;
}

- (NSString*)documentTitle
{
    NSString *r = [_metaDocumentInfoCache objectForKey:@"title"];
	return r;
}

- (NSString*)title
{
	//	BOOL isLandscape = UIInterfaceOrientationIsLandscape(_currentOrientation);
	return [self titleWithPage:_normalizedCurrentPage];
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


- (NSString*)texts
{
//	BOOL isLandscape = UIInterfaceOrientationIsLandscape(_currentOrientation);

	int relativePage = _normalizedCurrentPage;
	int documentOffset = [self relativePage:&relativePage];
	if (documentOffset < 0) return nil;

	NSString *did = [_documentId objectAtIndex:documentOffset];
	NSMutableString *tmp = [NSMutableString stringWithString:[_datasource texts:_documentId documentId:did page:relativePage]];

	// TODO currentPageが!isSinglePageだったときの処理?
	return tmp;
}

- (NSArray*)freehand
{
	//	BOOL isLandscape = UIInterfaceOrientationIsLandscape(_currentOrientation);
	NSArray *r = [_datasource freehand:_documentId
								  page:_normalizedCurrentPage];
	
	// TODO currentPageが!isSinglePageだったときの処理?
	return r;
}

- (NSArray*)annotations
{
	//	BOOL isLandscape = UIInterfaceOrientationIsLandscape(_currentOrientation);
	
	int relativePage = _normalizedCurrentPage;
	int documentOffset = [self relativePage:&relativePage];
	if (documentOffset < 0) return nil;

	NSString *did = [_documentId objectAtIndex:documentOffset];
	NSArray *r = [_datasource annotations:_documentId
							   documentId:did
									 page:relativePage];
	
	// TODO currentPageが!isSinglePageだったときの処理?
	return r;
}

- (NSArray*)highlights
{
	//	BOOL isLandscape = UIInterfaceOrientationIsLandscape(_currentOrientation);
	NSArray *r = [_datasource highlights:_documentId
									page:_normalizedCurrentPage];
	
	// TODO currentPageが!isSinglePageだったときの処理?
	return r;
}

- (NSArray*)region
{
	//	BOOL isLandscape = UIInterfaceOrientationIsLandscape(_currentOrientation);
	
	int relativePage = _normalizedCurrentPage;
	int documentOffset = [self relativePage:&relativePage];
	if (documentOffset < 0) return nil;
	
	NSString *did = [_documentId objectAtIndex:documentOffset];
	NSArray *r = [_datasource regions:_documentId
						   documentId:did
								 page:relativePage];
	
	// TODO currentPageが!isSinglePageだったときの処理?
	return r;
}

- (UIImage*)thumbnailWithPage:(int)page
{
	int relativePage = page;
	int documentOffset = [self relativePage:&relativePage];
	if (documentOffset < 0) return nil;
	
	NSString *did = [_documentId objectAtIndex:documentOffset];
	
	UIImage *img = [_datasource thumbnail:_documentId documentId:did page:relativePage];
	return img;
}

- (NSString*)imageText
{
	//	BOOL isLandscape = UIInterfaceOrientationIsLandscape(_currentOrientation);
	return [self imageTextWithPage:_normalizedCurrentPage];
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



// landscape時のページの見出しリスト(論理ページ、物理ページ対応表)を作る
- (void)buildPageHeads
{
	/////////////////////////////////////	
//	[self loadSinglePageSet];
	NSMutableArray *singlePageInfoList = [NSMutableArray array];
	for (NSString *did in (NSArray*)_documentId) {
		NSArray *tmp = [_datasource singlePageInfoList:_documentId documentId:did];
		[singlePageInfoList addObject:[NSSet setWithArray:tmp]];
	}
	/////////////////////////////////////	

	NSMutableArray *pageHeadsList = [[NSMutableArray alloc] init];
	NSMutableArray *isSingleIndexList = [[NSMutableArray alloc] init];

	int documentCount = [_documentId count];
	for (int documentOffset = 0; documentOffset < documentCount; documentOffset++) {
		NSMutableArray *pageHeads = [NSMutableArray array];
		NSMutableArray *isSingleIndex = [NSMutableArray array];
		
		int totalPage = [self totalPageWithDocumentOffset:documentOffset];
		
		NSSet *spi = [singlePageInfoList objectAtIndex:documentOffset];
		for ( int page = 0 ; page < totalPage ; ) {
			[pageHeads addObject:[NSNumber numberWithInt:page]];
			if (![spi containsObject:[[NSNumber numberWithInt:page] stringValue]] &&
				(page + 1) < totalPage &&
				![spi containsObject:[[NSNumber numberWithInt:page+1] stringValue]] ) {

				[isSingleIndex addObject:[NSNumber numberWithBool:NO]];
				page += 2;
			} else {
				[isSingleIndex addObject:[NSNumber numberWithBool:YES]];
				page += 1;
			}
		}
		[pageHeadsList addObject:pageHeads];
		[isSingleIndexList addObject:isSingleIndex];
	}
    
	[_pageHeads release];
	[_isSingleIndex release];
	
    _pageHeads = pageHeadsList;
    _isSingleIndex = isSingleIndexList;

	_currentIndex = [self currentIndexByPage:_normalizedCurrentPage];
}


- (UIView*)getTileImageWithType:(NSString*)type page:(int)page column:(int)column row:(int)row resolution:(int)resolution
{
	int relativePage = page;
	int documentOffset = [self relativePage:&relativePage];
	if (documentOffset < 0) return nil;

	NSString *did = [_documentId objectAtIndex:documentOffset];
	UIView *v = [_datasource getTileImageWithDocument:_documentId
										   documentId:did
												 type:type
												 page:relativePage
											   column:column
												  row:row
										   resolution:resolution];
	return v;
}


- (NSArray*)imageTextSearch:(NSString*)term
{
	NSMutableArray *searchResult = [NSMutableArray array];

	int sum = 0;
	for (int i = 0; i < [(NSArray*)_documentId count]; i++) {

		NSString *did = [(NSArray*)_documentId objectAtIndex:i];
		NSArray *sr = [_datasource imageTextSearch:_documentId documentId:did query:term];
		if (sum > 0) {
			for (DocumentSearchResult *dsr in sr) {
				// ページ番号の調整
				dsr.page += sum;
			}
		}
		sum += [self totalPageWithDocumentOffset:i];
		[searchResult addObjectsFromArray:sr];
	}
	return searchResult;
}



+ (DocumentContext *)objectWithDictionary:(NSDictionary *)dictionary {
	
	DocumentContext *dc = [[DocumentContext alloc] init];
	dc.documentId = [dictionary objectForKey:@"documentId"];
	dc.currentPage = [[dictionary objectForKey:@"page"] intValue];
	[dc autorelease];
	
    return dc;
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithCapacity:0];
    
    [ret setObject:_documentId forKey:@"documentId"];
    [ret setObject:[NSString stringWithFormat:@"%d" , _normalizedCurrentPage] forKey:@"page"];
    
    return ret;
}

- (id)copyWithZone:(NSZone *)zone
{
    DocumentContext *clone = [[[self class] allocWithZone:zone] init];

    clone.documentId = self.documentId;
	clone.currentPage = self.currentPage;
	
    return  clone;
}

- (BOOL)saveHighlights:(NSArray *)data
{
	return [_datasource saveHighlights:_documentId page:_normalizedCurrentPage data:data];
}

- (BOOL)saveFreehand:(NSArray *)data
{
	return [_datasource saveFreehand:_documentId page:_normalizedCurrentPage data:data];
}


@end

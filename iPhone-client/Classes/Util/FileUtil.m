//
//  FileUtil.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/06/30.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "FileUtil.h"
#import "NSString+Data.h"
#import "JSON.h"
#import "BookmarkObject.h"
#import "Region.h"

@implementation FileUtil

#pragma mark common

+ (NSString *)getFullPath:(NSString *)fileName {
    NSString *documentsDirectory = [NSSearchPathForDirectoriesInDomains( NSDocumentDirectory , NSUserDomainMask , YES ) objectAtIndex:0];
    
    if ( !documentsDirectory ) {
        NSLog( @"Documents directory not found!" );
        return nil;
    }
    
    return [documentsDirectory stringByAppendingPathComponent:fileName];
}

+ (BOOL)write:(NSData *)data toFile:(NSString *)fileName {
    return [data writeToFile:[self getFullPath:fileName] atomically:YES];
}

+ (NSData *)read:(NSString *)fileName {
    return [[[NSData alloc] initWithContentsOfFile:[self getFullPath:fileName]] autorelease];
}

+ (BOOL)exists:(NSString *)fileName {
    return [[NSFileManager defaultManager] fileExistsAtPath:[self getFullPath:fileName]];
}

+ (BOOL)delete:(NSString *)fileName {
     return [[NSFileManager defaultManager] removeItemAtPath:[self getFullPath:fileName] error:nil];
}

#pragma mark custom

+ (BOOL)existsDocument:(int)documentId {
    return [FileUtil exists:[NSString stringWithFormat:@"%d/info.json" , documentId]];
}

+ (int)pages:(int)documentId {
    return [[[[NSString stringWithData:[FileUtil read:[NSString stringWithFormat:@"%d/info.json" , documentId]]] JSONValue] objectForKey:@"pages"] intValue];
}

+ (NSString *)title:(int)documentId {
    return [[[NSString stringWithData:[FileUtil read:[NSString stringWithFormat:@"%d/info.json" , documentId]]] JSONValue] objectForKey:@"title"];
}

+ (NSString *)publisher:(int)documentId {
    return [[[NSString stringWithData:[FileUtil read:[NSString stringWithFormat:@"%d/info.json" , documentId]]] JSONValue] objectForKey:@"publisher"];
}

+ (NSArray *)tocs:(int)documentId {
    NSMutableArray *ret = [NSMutableArray arrayWithCapacity:0];
    
    for ( NSDictionary *dic in [[NSString stringWithData:[FileUtil read:[NSString stringWithFormat:@"%d/toc.json" , documentId]]] JSONValue] ) {
        [ret addObject:[TOCObject objectWithDictionary:dic]];
    }
    
    return ret;
}

+ (TOCObject *)toc:(int)documentId page:(int)page {
    NSArray *tocs = [FileUtil tocs:documentId];
    
    for ( int index = [tocs count] - 1 ; index >= 0 ; index-- ) {
        TOCObject *toc = [tocs objectAtIndex:index];
        if ( toc.page <= page ) {
            return toc;
        }
    }
    
    assert(0);
}

+ (HistoryObject *)history {
    return [FileUtil exists:@"history.json"] ? [HistoryObject objectWithDictionary:[[NSString stringWithData:[FileUtil read:@"history.json"]] JSONValue]] : nil;
}

+ (void)saveHistory:(HistoryObject *)history {
    [FileUtil write:[[[history toDictionary] JSONRepresentation] dataUsingEncoding:NSUTF8StringEncoding] toFile:@"history.json"];
}

+ (DownloadStatusObject *)downloadStatus {
    return [DownloadStatusObject objectWithDictionary:[[NSString stringWithData:[FileUtil read:@"downloadStatus.json"]] JSONValue]];
}

+ (void)saveDownloadStatus:(DownloadStatusObject *)downloadStatus {
    [FileUtil write:[[[downloadStatus toDictionary] JSONRepresentation] dataUsingEncoding:NSUTF8StringEncoding] toFile:@"downloadStatus.json"];
}

+ (void)deleteDownloadStatus {
    [FileUtil delete:@"downloadStatus.json"];
}

+ (BOOL)existsDownloadStatus {
    return [FileUtil exists:@"downloadStatus.json"];
}

+ (NSArray *)downloadedIds {
    return [[NSString stringWithData:[FileUtil read:@"downloadedIds.json"]] JSONValue];
}

+ (void)saveDownloadedIds:(NSArray *)downloadedIds {
    [FileUtil write:[[downloadedIds JSONRepresentation] dataUsingEncoding:NSUTF8StringEncoding] toFile:@"downloadedIds.json"];
}

+ (void)deleteCache:(int)docId {
    [FileUtil delete:[NSString stringWithFormat:@"%d/" , docId]];
    [FileUtil delete:[NSString stringWithFormat:@"%d.zip" , docId]];
    if ( [FileUtil history].documentId == docId ) {
        [FileUtil delete:@"history.json"];
    }
    
    NSMutableArray *bookmarks = [NSMutableArray arrayWithCapacity:0];
    for ( NSDictionary *dic in [[NSString stringWithData:[FileUtil read:@"bookmarks.json"]] JSONValue] ) {
        BookmarkObject *obj = [BookmarkObject objectWithDictionary:dic];
        if ( obj.documentId != docId ) {
            [bookmarks addObject:dic];
        }
    }
    [FileUtil write:[[bookmarks JSONRepresentation] dataUsingEncoding:NSUTF8StringEncoding] toFile:@"bookmarks.json"];
    
    NSMutableArray *downloadedIds = [NSMutableArray arrayWithArray:[FileUtil downloadedIds]];
    for ( NSString *downloadedId in downloadedIds ) {
        if ( [downloadedId intValue] == docId ) {
            [downloadedIds removeObject:downloadedId];
            break;
        }
    }
    [FileUtil saveDownloadedIds:downloadedIds];
}

+ (NSString *)imageText:(int)docId page:(int)page {
    return [NSString stringWithData:[FileUtil read:[NSString stringWithFormat:@"%d/texts/%d.image.txt" , docId, page]]];
}

+ (NSArray *)regions:(int)docId page:(int)page {
    static int SIZEOF_DOUBLE = 8;
    static int N_REGION_FIELDS = 4;
    
    NSMutableArray *ret = [NSMutableArray arrayWithCapacity:0];
    
    NSData *data = [FileUtil read:[NSString stringWithFormat:@"%d/texts/%d.regions" , docId, page]];
    int len = data.length;
    for ( int pos = 0 ; pos < len ; pos += SIZEOF_DOUBLE * N_REGION_FIELDS ) {
        Region *region = [[Region new] autorelease];
        
        double value;
        [data getBytes:&value range:NSMakeRange(pos + SIZEOF_DOUBLE * 0, SIZEOF_DOUBLE)];
        region.x = value;
        [data getBytes:&value range:NSMakeRange(pos + SIZEOF_DOUBLE * 1, SIZEOF_DOUBLE)];
        region.y = value;
        [data getBytes:&value range:NSMakeRange(pos + SIZEOF_DOUBLE * 2, SIZEOF_DOUBLE)];
        region.width = value;
        [data getBytes:&value range:NSMakeRange(pos + SIZEOF_DOUBLE * 3, SIZEOF_DOUBLE)];
        region.height = value;
        
        [ret addObject:region];
    }
    
    return ret;
}

@end

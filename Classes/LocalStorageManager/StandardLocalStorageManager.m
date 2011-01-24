//
//  StandardLocalStorageManager.m
//  MapDoc
//
//  Created by sakukawa on 10/11/17.
//  Copyright 2010 Hagmaru Inc. All rights reserved.
//

#import "StandardLocalStorageManager.h"
#import "JSON.h"
#import "NSString+Data.h"


@implementation StandardLocalStorageManager

#pragma mark common

- (NSString *)getFullPath:(NSString *)fileName
{
    NSString *documentsDirectory = [NSSearchPathForDirectoriesInDomains( NSDocumentDirectory , NSUserDomainMask , YES ) objectAtIndex:0];
    
    if ( !documentsDirectory ) {
        NSLog( @"Documents directory not found!" );
        return nil;
    }

	NSString *result = [documentsDirectory stringByAppendingPathComponent:fileName];
	NSLog(@"DIR : %@", result);
    return result;
}

- (BOOL)write:(NSData *)data toFile:(NSString *)fileName
{
    return [data writeToFile:[self getFullPath:fileName] atomically:YES];
}

- (NSData *)read:(NSString *)fileName
{
    return [[[NSData alloc] initWithContentsOfFile:[self getFullPath:fileName]] autorelease];
}

- (BOOL)exists:(NSString *)fileName {
    return [[NSFileManager defaultManager] fileExistsAtPath:[self getFullPath:fileName]];
}

- (BOOL)remove:(NSString *)fileName {
	return [[NSFileManager defaultManager] removeItemAtPath:[self getFullPath:fileName] error:nil];
}

- (BOOL)existsForKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@.json", key];
	return [self exists:fileName];
}

- (BOOL)existsWithMetaDocumentId:(id<NSObject>)metaDocumentId
{
	NSString *fileName = [NSString stringWithFormat:@"%@/", [(NSArray*)metaDocumentId componentsJoinedByString:@","] ];
	fileName = [self getFullPath:fileName];
	BOOL r = YES;
	return [[NSFileManager defaultManager] fileExistsAtPath:fileName isDirectory:&r];
//	return [self exists:fileName] |	[self exists:[fileName stringByAppendingString:@".json"]];
}


- (BOOL)existsWithDocumentId:(id<NSObject>)docId forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@/%@", docId, key];
	return [self exists:fileName] |	[self exists:[fileName stringByAppendingString:@".json"]];
}

- (BOOL)removeForKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@.json", key];
	[self remove:fileName];

	fileName = [NSString stringWithFormat:@"%@", key];
	[self remove:fileName];
	return YES;
}

- (BOOL)removeWithDocumentId:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@/%@/%@.json", [(NSArray*)metaDocumentId componentsJoinedByString:@","], docId, key];
	[self remove:fileName];

	fileName = [NSString stringWithFormat:@"%@/%@/%@", [(NSArray*)metaDocumentId componentsJoinedByString:@","], docId, key];
	[self remove:fileName];
	return YES;
}

- (BOOL)removeWithDocumentId:(id<NSObject>)metaDocumentId// documentId:(id<NSObject>)docId
{
	NSString *mdid = (NSString*)metaDocumentId;
	if ([mdid isKindOfClass:[NSArray class]]) {
		mdid = [(NSArray*)mdid componentsJoinedByString:@","];
	}
	
	NSString *fileName = [NSString stringWithFormat:@"%@/", mdid];
	return [self remove:fileName];
}





- (BOOL)saveObject:(id)object forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@.json", key];
	return [self write:[[object JSONRepresentation] dataUsingEncoding:NSUTF8StringEncoding]
				toFile:fileName];
}

- (id)objectForKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@.json", key];
	NSData *data = [self read:fileName];
	return [[NSString stringWithData:data] JSONValue];
}

- (BOOL)saveObjectWithDocumentId:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId object:(id)object forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@/%@/%@.json", [(NSArray*)metaDocumentId componentsJoinedByString:@","], docId, key];
	return [self write:[[object JSONRepresentation] dataUsingEncoding:NSUTF8StringEncoding]
				toFile:fileName];
}

- (id)objectWithDocumentId:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@/%@/%@.json", [(NSArray*)metaDocumentId componentsJoinedByString:@","] , docId, key];
	NSData *data = [self read:fileName];
	return [[NSString stringWithData:data] JSONValue];
}


- (BOOL)saveData:(NSData*)data forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@", key];
	return [self write:data
				toFile:fileName];
}

- (NSData*)dataForKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@", key];
	id obj = [self read:fileName];
	return obj;
}

- (BOOL)saveDataWithDocumentId:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId data:(NSData*)data forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@/%@/%@", [(NSArray*)metaDocumentId componentsJoinedByString:@","], docId, key];
	return [self write:data
				toFile:fileName];
}

- (NSData*)dataWithDocumentId:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@/%@/%@", [(NSArray*)metaDocumentId componentsJoinedByString:@","], docId, key];
	NSData *data = [self read:fileName];
	return data;
}




@end

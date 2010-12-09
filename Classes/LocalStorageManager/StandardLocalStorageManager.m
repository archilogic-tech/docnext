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
//	NSLog(@"DIR : %@", documentsDirectory);

    return [documentsDirectory stringByAppendingPathComponent:fileName];
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

- (BOOL)removeWithDocumentId:(id<NSObject>)docId forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@/%@.json", docId, key];
	[self remove:fileName];

	fileName = [NSString stringWithFormat:@"%@/%@", docId, key];
	[self remove:fileName];
	return YES;
}

- (BOOL)removeWithDocumentId:(id<NSObject>)docId
{
	NSString *fileName = [NSString stringWithFormat:@"%@/", docId];
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

- (BOOL)saveObjectWithDocumentId:(id<NSObject>)docId object:(id)object forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@/%@.json", docId, key];
	return [self write:[[object JSONRepresentation] dataUsingEncoding:NSUTF8StringEncoding]
				toFile:fileName];
}

- (id)objectWithDocumentId:(id<NSObject>)docId forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@/%@.json", docId, key];
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

- (BOOL)saveDataWithDocumentId:(id<NSObject>)docId data:(NSData*)data forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@/%@", docId, key];
	return [self write:data
				toFile:fileName];
}

- (NSData*)dataWithDocumentId:(id<NSObject>)docId forKey:(id)key
{
	NSString *fileName = [NSString stringWithFormat:@"%@/%@", docId, key];
	NSData *data = [self read:fileName];
	return data;
}




@end

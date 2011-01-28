//
//  LocalStorageManager.h
//  MapDoc
//
//  Created by sakukawa on 11/01/28.
//  Copyright 2011 Hagmaru Inc. All rights reserved.
//

#import <UIKit/UIKit.h>


@protocol LocalStorageManager

- (BOOL)existsForKey:(id)key;
- (BOOL)existsWithMetaDocumentId:(id<NSObject>)metaDocumentId;
- (BOOL)existsWithMetaDocumentId:(id<NSObject>)metaDocumentId documentId:docId forKey:(id)key;

- (BOOL)removeForKey:(id)key;
- (BOOL)removeWithDocumentId:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId forKey:(id)key;
- (BOOL)removeWithDocumentId:(id<NSObject>)metaDocumentId;

- (id)objectForKey:(id)key;
- (id)objectWithDocumentId:(id<NSObject>)metaDocumentId forKey:(id)key;
- (BOOL)saveObject:(id)object forKey:(id)key;
- (BOOL)saveObjectWithDocumentId:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId object:(id)object forKey:(id)key;
- (BOOL)saveObjectWithDocumentId:(id<NSObject>)metaDocumentId object:(id)object forKey:(id)key;

- (NSData*)dataForKey:(id)key;
- (NSData*)dataWithDocumentId:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId forKey:(id)key;
- (BOOL)saveData:(NSData*)data forKey:(id)key;
- (BOOL)saveDataWithDocumentId:(id<NSObject>)metaDocumentId documentId:(id<NSObject>)docId data:(NSData*)data forKey:(id)key;

@end

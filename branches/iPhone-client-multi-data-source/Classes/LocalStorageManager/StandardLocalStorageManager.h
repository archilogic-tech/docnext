//
//  StandardLocalStorageManager.h
//  MapDoc
//
//  Created by sakukawa on 10/11/17.
//  Copyright 2010 Hagmaru Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HistoryObject.h"

/*!
    @class       StandardLocalStorageManager 
    @abstract    primitiveなlocal storageの操作を定義するクラス
    @discussion  すべてのコンポーネントは、このクラスを介してストレージを操作することが望まれる。すべての操作はkey and valueで行われる。
*/
@interface StandardLocalStorageManager : NSObject {

}

/** このメソッドは推奨されない */
- (NSString *)getFullPath:(NSString *)fileName;


/** 存在チェック */
- (BOOL)existsForKey:(id)key;

/** 存在チェック */
- (BOOL)existsWithDocumentId:(id<NSObject>)docId forKey:(id)key;

/** なんらかの方法でオブジェクトを保存する */
- (BOOL)saveObject:(id)object forKey:(id)key;

/** なんらかの方法でオブジェクトを保存する */
- (id)objectForKey:(id)key;

/** なんらかの方法でオブジェクトを保存する */
- (BOOL)saveObjectWithDocumentId:(id<NSObject>)docId object:(id)object forKey:(id)key;

/** なんらかの方法でオブジェクトを保存する */
- (id)objectWithDocumentId:(id<NSObject>)docId forKey:(id)key;

/** NSDataをそのまま保存する */
- (BOOL)saveData:(NSData*)data forKey:(id)key;

/** NSDataをそのまま保存する */
- (NSData*)dataForKey:(id)key;

/** NSDataをそのまま保存する */
- (BOOL)saveDataWithDocumentId:(id<NSObject>)docId data:(NSData*)data forKey:(id)key;

/** NSDataをそのまま保存する */
- (NSData*)dataWithDocumentId:(id<NSObject>)docId forKey:(id)key;

/** 削除 */
- (BOOL)removeForKey:(id)key;

/** 削除 */
- (BOOL)removeWithDocumentId:(id<NSObject>)docId forKey:(id)key;

/** 削除 */
- (BOOL)removeWithDocumentId:(id<NSObject>)docId;


@end

//
//  MarkupParser.h
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/05.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MarkupParseResult.h"

@interface MarkupParser : NSObject <NSXMLParserDelegate> {
    NSMutableString *text;
    NSMutableArray *rubys;
    NSMutableArray *textSizes;
    NSMutableArray *currentPath;
    int currentLine;
    int currentIndex;
    int rubyLocation;
    int rubyLength;
}

@property(nonatomic,retain) NSMutableString *text;
@property(nonatomic,retain) NSMutableArray *rubys;
@property(nonatomic,retain) NSMutableArray *textSizes;
@property(nonatomic,retain) NSMutableArray *currentPath;

- (MarkupParseResult *)parse:(NSString *)source;

@end

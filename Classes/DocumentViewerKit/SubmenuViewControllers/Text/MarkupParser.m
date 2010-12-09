//
//  MarkupParser.m
//  MapDoc
//
//  Created by Yoskaku Toyama on 10/07/05.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "MarkupParser.h"
#import "RubyMarker.h"
#import "TextSizeMarker.h"

@implementation MarkupParser

@synthesize text;
@synthesize rubys;
@synthesize textSizes;
@synthesize currentPath;

- (MarkupParseResult *)parse:(NSString *)source {
    self.text = [NSMutableString stringWithCapacity:0];
    self.rubys = [NSMutableArray arrayWithCapacity:0];
    self.textSizes = [NSMutableArray arrayWithCapacity:0];
    self.currentPath = [NSMutableArray arrayWithCapacity:0];
    currentLine = 0;
    currentIndex = 0;
    
    NSXMLParser *parser = [[[NSXMLParser alloc] initWithData:[source dataUsingEncoding:NSUTF8StringEncoding]] autorelease];
    parser.delegate = self;
    [parser parse];
    
    MarkupParseResult *ret = [[MarkupParseResult new] autorelease];
    ret.text = self.text;
    ret.rubys = self.rubys;
    ret.textSizes = self.textSizes;
    return ret;;
}

- (int)countLinefeed:(NSString *)string {
    int ret = 0;
    
    for ( int index = 0 ; index < [string length] ; index++ ) {
        if ( [string characterAtIndex:index] == '\n' ) {
            ret++;
        }
    }
    
    return ret;
}

#pragma mark NSXMLParserDelegate

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI
 qualifiedName:(NSString *)qualifiedName attributes:(NSDictionary *)attributeDict {
    [self.currentPath addObject:elementName];
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI
 qualifiedName:(NSString *)qName {
    [self.currentPath removeLastObject];
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError {
    NSLog(@"parseError: %@" , [parseError localizedDescription]);
}

- (void)parser:(NSXMLParser *)parser validationErrorOccurred:(NSError *)validError {
    NSLog(@"validationError: %@" , [validError localizedDescription]);
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string {
    NSString *current = [self.currentPath lastObject];
    if ( [current compare:@"fs"] == NSOrderedSame ) {
        [self.textSizes addObject:[TextSizeMarker markerWithData:[string floatValue] line:currentLine]];
    } else if ( [current compare:@"rb"] == NSOrderedSame ) {
        rubyLocation = currentIndex;
        rubyLength = [string length];
        
        [self.text appendString:string];
        currentIndex += [string length];
        currentLine += [self countLinefeed:string];
    } else if ( [current compare:@"rt"] == NSOrderedSame ) {
        [self.rubys addObject:[RubyMarker markerWithData:string location:rubyLocation length:rubyLength]];
    } else {
        [self.text appendString:string];
        currentIndex += [string length];
        currentLine += [self countLinefeed:string];
    }
}

- (void)dealloc {
    [text release];
    [rubys release];
    [textSizes release];
    [currentPath release];
    
    [super dealloc];
}

@end

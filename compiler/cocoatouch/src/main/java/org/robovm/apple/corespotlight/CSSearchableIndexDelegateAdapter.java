/*
 * Copyright (C) 2013-2015 RoboVM AB
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.robovm.apple.corespotlight;

/*<imports>*/
import java.io.*;
import java.nio.*;
import java.util.*;
import org.robovm.objc.*;
import org.robovm.objc.annotation.*;
import org.robovm.objc.block.*;
import org.robovm.rt.*;
import org.robovm.rt.annotation.*;
import org.robovm.rt.bro.*;
import org.robovm.rt.bro.annotation.*;
import org.robovm.rt.bro.ptr.*;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uniformtypeid.*;
/*</imports>*/

/*<javadoc>*/
/*</javadoc>*/
/*<annotations>*//*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CSSearchableIndexDelegateAdapter/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements CSSearchableIndexDelegate/*</implements>*/ {

    /*<ptr>*/
    /*</ptr>*/
    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*//*</constructors>*/
    /*<properties>*/
    
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @NotImplemented("searchableIndex:reindexAllSearchableItemsWithAcknowledgementHandler:")
    public void reindexAllSearchableItems(CSSearchableIndex searchableIndex, @Block Runnable acknowledgementHandler) {}
    @NotImplemented("searchableIndex:reindexSearchableItemsWithIdentifiers:acknowledgementHandler:")
    public void reindexSearchableItems(CSSearchableIndex searchableIndex, NSArray<NSString> identifiers, @Block Runnable acknowledgementHandler) {}
    @NotImplemented("searchableIndexDidThrottle:")
    public void searchableIndexDidThrottle(CSSearchableIndex searchableIndex) {}
    @NotImplemented("searchableIndexDidFinishThrottle:")
    public void searchableIndexDidFinishThrottle(CSSearchableIndex searchableIndex) {}
    /**
     * @since Available in iOS 11.0 and later.
     */
    @NotImplemented("dataForSearchableIndex:itemIdentifier:typeIdentifier:error:")
    public NSData getDataForSearchableIndex(CSSearchableIndex searchableIndex, String itemIdentifier, String typeIdentifier, NSError.NSErrorPtr outError) { return null; }
    /**
     * @since Available in iOS 11.0 and later.
     */
    @NotImplemented("fileURLForSearchableIndex:itemIdentifier:typeIdentifier:inPlace:error:")
    public NSURL getFileURLForSearchableIndex(CSSearchableIndex searchableIndex, String itemIdentifier, String typeIdentifier, boolean inPlace, NSError.NSErrorPtr outError) { return null; }
    /*</methods>*/
}

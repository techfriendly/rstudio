/*
 * GetActiveDocumentContextEvent.java
 *
 * Copyright (C) 2009-13 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.events;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.event.shared.EventHandler;

import org.rstudio.core.client.js.JavaScriptSerializable;
import org.rstudio.studio.client.application.events.CrossWindowEvent;
import org.rstudio.studio.client.workbench.views.source.editors.text.ace.Range;

@JavaScriptSerializable
public class GetActiveDocumentContextEvent
   extends CrossWindowEvent<GetActiveDocumentContextEvent.Handler>
{
   public static class Data extends JavaScriptObject
   {
      protected Data() {}
      
      public static final native Data create(String id,
                                             String path,
                                             String contents,
                                             String selection,
                                             Range range)
      /*-{
         return {
            "id": id,
            "path": path,
            "contents": contents,
            "selection": selection,
            "range": [range.start.row, range.start.column,
                      range.end.row, range.end.column]
         };
      }-*/;
      
      public final native String getId() /*-{ return this["id"]; }-*/;
      public final native String getPath() /*-{ return this["path"]; }-*/;
      public final native String getContents() /*-{ return this["contents"]; }-*/;
      public final native String getSelection() /*-{ return this["selection"]; }-*/;
      public final Range getRange()
      {
         JsArrayInteger range = getRangeInternal();
         return Range.create(
               range.get(0),
               range.get(1),
               range.get(2),
               range.get(3));
      }
      
      private final native JsArrayInteger getRangeInternal() /*-{ return this["range"]; }-*/;
   }
   
   public interface Handler extends EventHandler
   {
      void onGetActiveDocumentContext(GetActiveDocumentContextEvent event);
   }
   
   @Override
   public Type<Handler> getAssociatedType()
   {
      return TYPE;
   }

   @Override
   protected void dispatch(Handler handler)
   {
      handler.onGetActiveDocumentContext(this);
   }
   
   public static final Type<Handler> TYPE = new Type<Handler>();
   

}

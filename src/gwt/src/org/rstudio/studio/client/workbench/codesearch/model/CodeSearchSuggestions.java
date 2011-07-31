package org.rstudio.studio.client.workbench.codesearch.model;

import java.util.ArrayList;

import org.rstudio.core.client.Pair;
import org.rstudio.studio.client.common.SimpleRequestCallback;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.inject.Inject;

public class CodeSearchSuggestions
{
   @Inject
   public CodeSearchSuggestions(CodeSearchServerOperations server)
   {
      server_ = server;
   }
   
   public void request(final Request request, final Callback callback)
   {    
     
      
      // first see if we can serve the request from the cache
      for (int i=resultCache_.size() - 1; i >= 0; i--)
      {
         // get the previous result
         Pair<String,ArrayList<CodeSearchSuggestion>> res = resultCache_.get(i);
         
         // exact match of previous query
         if (request.getQuery().equals(res.first))
         {
            callback.onSuggestionsReady(request, new Response(res.second));
            return;
         }
         
         // if this query is a further refinement of a non-overflowed 
         // previous query then satisfy it by filtering the previous results
         if (res.second.size() <= request.getLimit() &&
             request.getQuery().startsWith(res.first))
         {
           
            // TODO: once we introduce pattern matching then we need to
            // reflect this in this codepath
            
            String queryLower = request.getQuery().toLowerCase();
           
            ArrayList<CodeSearchSuggestion> suggestions =
                                       new ArrayList<CodeSearchSuggestion>();
            for (int s=0; s<res.second.size(); s++)
            {
               
               CodeSearchSuggestion sugg = res.second.get(s);
               
               String functionName = sugg.getResult().getFunctionName();
              
               if (functionName.toLowerCase().startsWith(queryLower))
                  suggestions.add(sugg);
            }

            // return the suggestions
            cacheAndReturnSuggestions(request, suggestions, callback);
            return;
         } 
      }
      
      // failed to short-circuit via the cache, hit the server
      server_.searchCode(
            request.getQuery(),
            request.getLimit(),
            new SimpleRequestCallback<JsArray<CodeSearchResult>>() {
         
         @Override
         public void onResponseReceived(JsArray<CodeSearchResult> results)
         { 
            // read the response
            ArrayList<CodeSearchSuggestion> suggestions = 
                                    new ArrayList<CodeSearchSuggestion>();
            for (int i = 0; i<results.length(); i++) 
               suggestions.add(new CodeSearchSuggestion(results.get(i)));     
            
            // return suggestions
            cacheAndReturnSuggestions(request, suggestions, callback);                            
         }
      });
      
   }
     
   public CodeSearchResult resultFromSuggestion(Suggestion suggestion)
   {
      return ((CodeSearchSuggestion)suggestion).getResult();
   }
   
   public void clear()
   {
      resultCache_.clear();
   }
   
   private void cacheAndReturnSuggestions(
                           final Request request, 
                           final ArrayList<CodeSearchSuggestion> suggestions,
                           final Callback callback)
   {
      // cache the suggestions (up to 15 active result sets cached)
      // NOTE: the cache is cleared at the end of the search sequence 
      // (when the search box loses focus)
      if (resultCache_.size() > 15)
         resultCache_.remove(0);
      resultCache_.add(
        new Pair<String, ArrayList<CodeSearchSuggestion>>(
                                                     request.getQuery(), 
                                                     suggestions));
      
      // provide the suggestions to the caller
      callback.onSuggestionsReady(request, 
                                  new Response(suggestions)) ;
   }
   
   
   private final CodeSearchServerOperations server_ ;
   
   private ArrayList<Pair<String, ArrayList<CodeSearchSuggestion>>> 
      resultCache_ = new ArrayList<Pair<String,ArrayList<CodeSearchSuggestion>>>();
}

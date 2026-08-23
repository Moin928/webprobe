package com.webprobe.url;

import java.net.URI;

public class UrlNormalizer {
    
    public String normalize(String url){

       URI uri = URI.create(url.trim()); // remove whitespaces in the url
        
       // the components like schemes and host can have different casing
       // therefore we normalise them so equivalent URLs are represented consistently
       String scheme = uri.getScheme().toLowerCase();
       String host = uri.getHost().toLowerCase();
       
       int port= uri.getPort(); // -1 means that the url does not specify a port
       
       StringBuilder normalized = new StringBuilder();
       normalized.append(scheme)
       .append("://")
       .append(host);

       // keep the port only when the orignal URL explicitly contains one
       if(port != -1){
        normalized.append(":").append(port);
       }

       if(uri.getRawPath() != null && !uri.getRawPath().isEmpty()) {
        normalized.append(uri.getRawPath());
       } else {
        normalized.append("/");
       }

       // query parameters can affect the resource requested, so it must be preserved
       if (uri.getQuery() != null) {
        normalized.append("?").append(uri.getRawQuery());
       }

       return normalized.toString();
    }

}
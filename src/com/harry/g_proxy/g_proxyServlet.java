/*
 * Copyright Harry Liu
 * 
 * http://code.google.com/p/g-proxy/
 * 
 * This is an free proxy based on Google App Engine.
 * 
 */
package com.harry.g_proxy;

import java.io.File;
import java.io.FileInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

@SuppressWarnings("serial")
public class g_proxyServlet extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		try{
			//find the URL
			String realUrl = getUrl(req);
			if (realUrl.startsWith("/")) realUrl = realUrl.substring(1);
			

			//whether is the Home page
			if (realUrl.isEmpty()) {
				returnHome(resp);
				return;
			}
			
			System.out.println("The inputed URI:"+realUrl);
			
			//form the URL
			URL url = new URL(realUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			String contentType = connection.getContentType();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK && contentType.toLowerCase().contains("text")) {
                // replace every links inside
    			replaceLinkAndReturnContent(url.openStream(), resp.getOutputStream());
            } else {
            	//visit the URL
    			retrieveAndReturnUrlContent(url.openStream(), resp.getOutputStream());
            }

		}
		catch(Exception e){
			System.out.println(e);
			OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream());
	        writer.write(e.toString());
	        writer.flush();
	        writer.close();
		}
	}
	
	// /mywebapp/servlet/MyServlet/a/b;c=123?d=789
	private String getUrl(HttpServletRequest req) throws UnsupportedEncodingException {
        String reqUri = req.getRequestURI().toString();
        String queryString = req.getQueryString();   // d=789
        if (queryString != null) {
            reqUri += "?"+queryString;
        }
        return URLDecoder.decode(reqUri, "UTF-8");
    }
	
	private void returnHome(HttpServletResponse resp) throws Exception{
        // Get the absolute path of the image
        ServletContext sc = getServletContext();
        String filename = sc.getRealPath("index.html");
    
        // Get the MIME type of the image
        String mimeType = sc.getMimeType(filename);
        if (mimeType == null) {
            sc.log("Could not get MIME type of "+filename);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
    
        // Set content type
        resp.setContentType(mimeType);
    
        // Set content size
        File file = new File(filename);
        resp.setContentLength((int)file.length());
    
        // Open the file and output streams
        FileInputStream in = new FileInputStream(file);
        OutputStream out = resp.getOutputStream();
    
        // Copy the contents of the file to the output stream
        byte[] buf = new byte[1024];
        int count = 0;
        while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }
        in.close();
        out.close();
	}
	
	private void retrieveAndReturnUrlContent(InputStream in, OutputStream out) throws Exception{
        // Copy the contents of the file to the output stream
        byte[] buf = new byte[1024];
        int count = 0;
        System.out.println("print out the stream:");
        while ((count = in.read(buf)) >= 0) {
        	System.out.println(count);
            out.write(buf, 0, count);
        }
        in.close();
        out.close();
	}
	
	private void replaceLinkAndReturnContent(InputStream in, OutputStream out) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuffer sBuffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
        	sBuffer.append(line);
        }
        reader.close();
        String content = sBuffer.toString();
        
        //replace
        content.replaceAll("src=\"http://", "src=\"https://g-proxy.appspot.com/http://");
        content.replaceAll("src=\"https://", "src=\"https://g-proxy.appspot.com/https://");
        content.replaceAll("href=\"http://", "href=\"https://g-proxy.appspot.com/http://");
        content.replaceAll("href=\"https://", "href=\"https://g-proxy.appspot.com/https://");        
        content.replaceAll("src='http://", "src='https://g-proxy.appspot.com/http://");
        content.replaceAll("src='https://", "src='https://g-proxy.appspot.com/https://");
        content.replaceAll("href='http://", "href='https://g-proxy.appspot.com/http://");
        content.replaceAll("href='https://", "href='https://g-proxy.appspot.com/https://");
        
        OutputStreamWriter writer = new OutputStreamWriter(out);
        writer.write(content);
        writer.flush();
        writer.close();
	}
}

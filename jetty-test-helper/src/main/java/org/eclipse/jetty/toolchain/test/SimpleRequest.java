//
//  ========================================================================
//  Copyright (c) 1995-2015 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.toolchain.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Properties;

/**
 * A super simple http request mechanism for simple testing purposes.
 * <p>
 * It is not meant to be robust or even configurable.
 * <p>
 * Do not use this for large responses, as the entire response body is always assumed to fit in a single {@link String}
 * object.
 */
public class SimpleRequest
{
    private URI baseUri;

    /**
     * Setup a SimpleRequest initiator against serverURI
     * 
     * @param serverURI
     *            the server URI to base all requests off of
     */
    public SimpleRequest(URI serverURI)
    {
        this.baseUri = serverURI;
    }

    /**
     * Initiate a simple GET request on the path specified.
     * 
     * @param relativePath
     *            the relative path of the serverURI to resource for this GET request
     * @return the response contents
     * @throws IOException
     *             if unable to communicate with server
     */
    public String getString(String relativePath) throws IOException
    {
        URI uri = this.baseUri.resolve(relativePath);
        System.out.println("GET (String): " + uri.toASCIIString());

        InputStream in = null;
        InputStreamReader reader = null;
        HttpURLConnection connection = null;

        try
        {
            connection = (HttpURLConnection)uri.toURL().openConnection();
            connection.connect();
            if (HttpURLConnection.HTTP_OK != connection.getResponseCode())
            {
                String body = getPotentialBody(connection);
                String err = String.format("GET request failed (%d %s) %s%n%s",connection.getResponseCode(),connection.getResponseMessage(),
                        uri.toASCIIString(),body);
                throw new IOException(err);
            }
            in = connection.getInputStream();
            reader = new InputStreamReader(in);
            StringWriter writer = new StringWriter();
            IO.copy(reader,writer);
            return writer.toString();
        }
        finally
        {
            IO.close(reader);
            IO.close(in);
        }
    }

    /**
     * Initiate a simple GET request on the path specified, returning the response contents as a {@link Properties}
     * object.
     * <p>
     * This expects that the GET response will be in the form of a Properties text file, in a format that is suitable
     * for {@link Properties#load(InputStream)} use.
     * 
     * @param relativePath
     *            the relative path of the serverURI to resource for this GET request
     * @return the response contents as a {@link Properties} object.
     * @throws IOException
     *             if unable to communicate with server
     */
    public Properties getProperties(String relativePath) throws IOException
    {
        URI uri = this.baseUri.resolve(relativePath);
        System.out.println("GET (Properties): " + uri.toASCIIString());

        InputStream in = null;
        HttpURLConnection connection = null;

        try
        {
            connection = (HttpURLConnection)uri.toURL().openConnection();
            connection.connect();
            if (HttpURLConnection.HTTP_OK != connection.getResponseCode())
            {
                String body = getPotentialBody(connection);
                String err = String.format("GET request failed (%d %s) %s%n%s",connection.getResponseCode(),connection.getResponseMessage(),
                        uri.toASCIIString(),body);
                throw new IOException(err);
            }
            in = connection.getInputStream();
            Properties props = new Properties();
            props.load(in);
            return props;
        }
        finally
        {
            IO.close(in);
        }
    }

    /**
     * Attempt to obtain the body text if available. Do not throw an exception if body is unable to be fetched.
     * 
     * @param connection
     *            the connection to fetch the body content from.
     * @return the body content, if present.
     */
    private String getPotentialBody(HttpURLConnection connection)
    {
        InputStream in = null;
        InputStreamReader reader = null;
        try
        {
            in = connection.getInputStream();
            reader = new InputStreamReader(in);
            StringWriter writer = new StringWriter();
            IO.copy(reader,writer);
            return writer.toString();
        }
        catch (IOException e)
        {
            return "<no body:" + e.getMessage() + ">";
        }
        finally
        {
            IO.close(reader);
            IO.close(in);
        }
    }
}

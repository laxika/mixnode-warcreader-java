package com.mixnode.warcreader.record;

import com.mixnode.warcreader.WarcFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.io.DefaultHttpResponseParser;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.io.IdentityInputStream;
import org.apache.http.impl.io.SessionInputBufferImpl;
import org.apache.http.message.AbstractHttpMessage;

/**
 * An implementation of WarcContentBlock interface to handle contents block's of WARC responses.
 * This class also implements HttpResponse interface. User can cast the warcRecord.getContentBlock
 * of a response WARC to HttpResponse and process it.
 *
 * @author Hadi Jooybar
 */
public class ResponseContentBlock extends AbstractHttpMessage implements HttpResponse,
    WarcContentBlock {

  private static final int BUFFER_SIZE = 1024;

  /**
   * HTTP/S protocol version
   */
  private final ProtocolVersion protocolVersion;

  /**
   * HTTP/S Status line
   */
  private final StatusLine statusLine;

  /**
   * HTTP/S entity
   */
  private final HttpEntity entity;

  /**
   * private constructor from a HttpResponse
   *
   * @param response input HttpResponse
   */
  private ResponseContentBlock(final HttpResponse response) throws IOException {
    protocolVersion = response.getProtocolVersion();
    statusLine = response.getStatusLine();
    setHeaders(response.getAllHeaders());
    entity = response.getEntity();
  }

  /**
   * HttpResponse.getProtocolVersion implementation
   */
  public ProtocolVersion getProtocolVersion() {
    return protocolVersion;
  }


  @Override
  public String toString() {
    return "\nResponse status line: " + this.statusLine +
        "\nResponse headers: " + Arrays.toString(this.getAllHeaders());
  }

  /**
   * Create a ResponseContentBlock from a content block stream of a response WARC
   *
   * @param stream Response WARC's content block stream
   * @return Output ContentBlock
   */
  public static ResponseContentBlock createWarcRecord(final BoundedInputStream stream)
      throws IOException {
    SessionInputBufferImpl buffer = new SessionInputBufferImpl(new HttpTransportMetricsImpl(),
        BUFFER_SIZE, 0, null, null);
    buffer.bind(stream);
    final DefaultHttpResponseParser responseParser = new DefaultHttpResponseParser(buffer);
    final HttpResponse response;
    try {
      response = responseParser.parse();
    } catch (HttpException e) {
      throw new WarcFormatException("Can't parse the response", e);
    }
    final BasicHttpEntity entity = new BasicHttpEntity();
    entity.setContent(new IdentityInputStream(buffer));
    Header contentTypeHeader = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
    if (contentTypeHeader != null) {
      entity.setContentType(contentTypeHeader);
    }
    response.setEntity(entity);

    return new ResponseContentBlock(response);
  }

  public HttpEntity getEntity() {
    return entity;
  }

  public Locale getLocale() {
    return null;
  }

  public StatusLine getStatusLine() {
    return statusLine;
  }

  public void setEntity(HttpEntity arg0) {
  }

  public void setLocale(Locale arg0) {
  }

  public void setReasonPhrase(String arg0) throws IllegalStateException {
  }

  public void setStatusCode(int arg0) throws IllegalStateException {
  }

  public void setStatusLine(StatusLine arg0) {
  }

  public void setStatusLine(ProtocolVersion arg0, int arg1) {
  }

  public void setStatusLine(ProtocolVersion arg0, int arg1, String arg2) {
  }

  public InputStream payload() throws IOException {
    return getEntity().getContent();
  }
}

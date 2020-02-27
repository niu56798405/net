package com.x.http.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.x.tools.Strings;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;

/**
 * 
 * http request
 * 
 * @author 
 *
 */
public class Request {
    
    private InetAddress address;
    private HttpHeaders headers;
    private HttpMethod method;
    
    private Content content;
    private Params params;
    
    private transient String uri;
    private transient String path;
    private transient int qmpos;
    private transient int nParams;
    
    private transient List<Throwable> causes;
   
    public Request(InetAddress address, String uri, HttpHeaders headers, HttpMethod method) {
        this.address = address;
        this.uri = uri;
        this.qmpos = uri.indexOf('?');
        this.headers = headers;
        this.method = method;
    }

    public List<Throwable> causes() {
        return causes;
    }
    
    public byte[] content() {
        return this.content == null ? null : this.content.bytes;
    }

    public void appendDecoderResult(DecoderResult dr) {
        if(dr.isSuccess()) {
            return;
        }
        if(causes == null) {
            causes = new ArrayList<Throwable>();
        }
        causes.add(dr.cause());
    }

    public String remoteHost() {
        return this.address.getHostAddress();
    }
    
    public InetAddress address() {
        return this.address;
    }
    
    public String getHeader(String name) {
        return this.headers.get(name);
    }
    
    public HttpHeaders headers() {
        return this.headers;
    }
    
    public HttpMethod method() {
        return this.method;
    }
    
    public String uri() {
        return this.uri;
    }
    
    public String queryString() {
        return this.uri.substring(this.qmpos+1);
    }
    
    public String realPath() {//真实路径
        int st = 0;
        int nd = (qmpos == -1 ? uri.length() : qmpos);
        return nd > st ? uri.substring(st, nd) : "";
    }
    
    /**
     * Returns the decoded path string of the URI.
     */
    public String path() {
        if(path == null) {
            decodePath();
        }
        return path;
    }
    
    private Params params() {
        if(params == null) {
            decodeParams();
        }
        return params;
    }
    
    public Set<String> paramNames() {
        return params().keySet();
    }
    
    public List<String> getParamValues(String name) {
        return params().get(name);
    }
    
    public String getParam(String name) {
        return params().get(name, 0);
    }
    public String getParam(String name, String def) {
        String val = getParam(name);
        return Strings.isEmpty(val) ? def : val;
    }

    public int getParamAsInt(String name) {
        return getParamAsInt(name, -1);
    }
    public int getParamAsInt(String name, int def) {
        String param = getParam(name);
        return Strings.isEmpty(param) ? def : Integer.parseInt(param);
    }
    
    public long getParamAsLong(String name) {
        return getParamAsLong(name, -1);
    }
    public long getParamAsLong(String name, long def) {
        String param = getParam(name);
        return Strings.isEmpty(param) ? def : Long.parseLong(param);
    }
    
    private void decodePath() {
        int len = uri.length();
        if(len == 0) {
            path = "";
        } else {
            int end = (qmpos == -1 ? len : qmpos);
            path = Strings.trim(uri, 0, end, '/');
        }
    }
    
    private void decodeParams() {
        int paramst = qmpos + 1;
        if (qmpos == -1 || uri.length() == paramst) {
            params = EMPTY;
        } else {
            decodeParams(uri.substring(paramst));
        }
    }
    
    private void decodeParams(String s) {
        this.params = new Params();
        nParams = 0;
        String name = null;
        int pos = 0; // Beginning of the unprocessed region
        int i;       // End of the unprocessed region
        char c;  // Current character
        for (i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if (c == '=' && name == null) {
                if (pos != i) {
                    name = decodeComponent(s.substring(pos, i), CharsetUtil.UTF_8);
                }
                pos = i + 1;
                // http://www.w3.org/TR/html401/appendix/notes.html#h-B.2.2
            } else if (c == '&' || c == ';') {
                if (name == null && pos != i) {
                    // We haven't seen an `=' so far but moved forward.
                    // Must be a param of the form '&a&' so add it with
                    // an empty value.
                    if (!addParam(decodeComponent(s.substring(pos, i), CharsetUtil.UTF_8), "")) {
                        return;
                    }
                } else if (name != null) {
                    if (!addParam(name, decodeComponent(s.substring(pos, i), CharsetUtil.UTF_8))) {
                        return;
                    }
                    name = null;
                }
                pos = i + 1;
            }
        }
        if (pos != i) {  // Are there characters we haven't dealt with?
            if (name == null) {     // Yes and we haven't seen any `='.
                addParam(decodeComponent(s.substring(pos, i), CharsetUtil.UTF_8), "");
            } else {                // Yes and this must be the last value.
                addParam(name, decodeComponent(s.substring(pos, i), CharsetUtil.UTF_8));
            }
        } else if (name != null) {  // Have we seen a name without value?
            addParam(name, "");
        }
    }

    private boolean addParam(String name, String value) {
        if(nParams ++ > 256) {
            return false;
        }
        params.add(name, value);
        return true;
    }
    
    public static String decodeComponent(final String s, final Charset charset) {
        if (s == null) {
            return "";
        }
        final int size = s.length();
        boolean modified = false;
        for (int i = 0; i < size; i++) {
            final char c = s.charAt(i);
            if (c == '%' || c == '+') {
                modified = true;
                break;
            }
        }
        if (!modified) {
            return s;
        }
        final byte[] buf = new byte[size];
        int pos = 0;  // position in `buf'.
        for (int i = 0; i < size; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '+':
                    buf[pos++] = ' ';  // "+" -> " "
                    break;
                case '%':
                    if (i == size - 1) {
                        throw new IllegalArgumentException("unterminated escape" + " sequence at end of string: " + s);
                    }
                    c = s.charAt(++i);
                    if (c == '%') {
                        buf[pos++] = '%';  // "%%" -> "%"
                        break;
                    }
                    if (i == size - 1) {
                        throw new IllegalArgumentException("partial escape" + " sequence at end of string: " + s);
                    }
                    c = decodeHexNibble(c);
                    final char c2 = decodeHexNibble(s.charAt(++i));
                    if (c == Character.MAX_VALUE || c2 == Character.MAX_VALUE) {
                        throw new IllegalArgumentException("invalid escape sequence `%" + s.charAt(i - 1) + s.charAt(i) + "' at index " + (i - 2) + " of: " + s);
                    }
                    c = (char) (c * 16 + c2);
                    // Fall through.
                default:
                    buf[pos++] = (byte) c;
                    break;
            }
        }
        return new String(buf, 0, pos, charset);
    }
    
    private static char decodeHexNibble(final char c) {
        if ('0' <= c && c <= '9') {
            return (char) (c - '0');
        } else if ('a' <= c && c <= 'f') {
            return (char) (c - 'a' + 10);
        } else if ('A' <= c && c <= 'F') {
            return (char) (c - 'A' + 10);
        } else {
            return Character.MAX_VALUE;
        }
    }
    
    public class Content extends OutputStream {
    	public byte[] bytes;
    	private int count;
    	public Content(int capacity) {
    		bytes = new byte[capacity];
		}
    	public Content grow(int capacity) {
    		//assert count == bytes.length;
    		bytes = Arrays.copyOf(bytes, bytes.length + capacity);
    		return this;
    	}
		@Override
		public void write(int b) throws IOException {
			bytes[count] = (byte) b;
	        ++ count;
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			System.arraycopy(b, off, bytes, count, len);
	        count += len;
		}
    }

    public void appendContent(ByteBuf content) {
        int readableBytes = content.readableBytes();
		if(this.content == null) {
        	this.content = new Content(readableBytes);
        } else {
        	this.content.grow(readableBytes);
        }
        try {
            content.readBytes(this.content, readableBytes);
        } catch (IOException e) {
            this.appendDecoderResult(DecoderResult.failure(e));
        }
    }

    public boolean isSuccess() {
        return this.causes == null;
    }
    
    static final Params EMPTY = new Params();
    static class Params extends LinkedHashMap<String, List<String>> {
        private static final long serialVersionUID = 1L;
        public void add(String name, String value) {
            List<String> vals = this.get(name);
            if(vals == null) {
                vals = new ArrayList<>();
                this.put(name, vals);
            }
            vals.add(value);
        }
        public String get(String name, int i) {
            List<String> vals = this.get(name);
            return (vals != null && vals.size() > i) ? vals.get(i) : null;
        }

    }

}

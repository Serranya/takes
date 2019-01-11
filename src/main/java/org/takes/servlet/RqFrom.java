/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.takes.servlet;

import com.jcabi.aspects.Tv;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import javax.servlet.http.HttpServletRequest;
import org.takes.Request;

/**
 * Request from {@link HttpServletRequest}.
 *
 * @since 2.0
 * @todo #866:30min Servlet request adapter is not unit-tested.
 *  There should be tests for reading headers and body from servlet request.
 *  See https://github.com/yegor256/takes/pull/865 discussion for details.
 */
final class RqFrom implements Request {
    /**
     * Servlet request.
     */
    private final HttpServletRequest sreq;

    /**
     * Ctor.
     * @param request Servlet request
     */
    RqFrom(final HttpServletRequest request) {
        this.sreq = request;
    }

    @Override
    public Iterable<String> head() {
        final Collection<String> head = new LinkedList<>();
        head.add(new RqFrom.HttpHead(this.sreq).toString());
        head.add(new RqFrom.HttpHost(this.sreq).toString());
        final Enumeration<String> names = this.sreq.getHeaderNames();
        while (names.hasMoreElements()) {
            final String header = names.nextElement();
            head.add(
                String.format(
                    "%s: %s",
                    header,
                    this.sreq.getHeader(header)
                )
            );
        }
        head.add(
            String.format(
                "X-Takes-LocalAddress: %s",
                this.sreq.getLocalAddr()
            )
        );
        head.add(
            String.format(
                "X-Takes-RemoteAddress: %s",
                this.sreq.getRemoteAddr()
            )
        );
        return head;
    }

    @Override
    public InputStream body() throws IOException {
        return this.sreq.getInputStream();
    }

    /**
     * Http request first line: method, uri, version.
     */
    private static final class HttpHead {
        /**
         * Servlet request.
         */
        private final HttpServletRequest req;

        /**
         * Ctor.
         * @param request Servlet request
         */
        HttpHead(final HttpServletRequest request) {
            this.req = request;
        }

        @Override
        public String toString() {
            final StringBuilder bld = new StringBuilder(Tv.TWENTY)
                .append(this.req.getMethod())
                .append(' ');
            final String uri = this.req.getRequestURI();
            if (uri == null) {
                bld.append('/');
            } else {
                bld.append(uri);
            }
            final String query = this.req.getQueryString();
            if (query != null) {
                bld.append('?').append(query);
            }
            return bld.toString();
        }
    }

    /**
     * Host header line from request.
     */
    private static final class HttpHost {
        /**
         * Default http port.
         */
        private static final int PORT_DEFAULT = 80;

        /**
         * Servlet request.
         */
        private final HttpServletRequest req;

        /**
         * Ctor.
         * @param request Servlet request.
         */
        private HttpHost(final HttpServletRequest request) {
            this.req = request;
        }

        @Override
        public String toString() {
            final StringBuilder bld = new StringBuilder(Tv.HUNDRED);
            bld.append("Host: ").append(this.req.getServerName());
            final int port = this.req.getServerPort();
            if (port != RqFrom.HttpHost.PORT_DEFAULT) {
                bld.append(':').append(port);
            }
            return bld.toString();
        }
    }
}

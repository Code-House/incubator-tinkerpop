package com.tinkerpop.gremlin.client.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;
import com.tinkerpop.gremlin.server.RequestMessage;
import com.tinkerpop.gremlin.server.ServerTokens;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A temporary, basic and probably slightly flawed client from Gremlin Server. Not meant for use outside of testing.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class WebSocketConnection {
    private final URI uri;
    private Channel ch;
    private static final EventLoopGroup group = new NioEventLoopGroup();

    protected static ConcurrentHashMap<UUID, ArrayBlockingQueue<Optional<JsonNode>>> responses = new ConcurrentHashMap<>();

    public WebSocketConnection(final String uri) {
        this.uri = URI.create(uri);
    }

    void putResponse(final UUID requestId, final Optional<JsonNode> response) {
        if (!responses.containsKey(requestId)) {
            // probably a timeout if we get here... ???
            System.out.println(String.format("No queue found in the response map: %s", requestId));
            return;
        }

        try {
            final ArrayBlockingQueue<Optional<JsonNode>> queue = responses.get(requestId);
            if (queue != null) {
                queue.put(response);
            }
            else {
                // no queue for some reason....why ???
                System.out.println(String.format("No queue found in the response map*: %s", requestId));
            }
        }
        catch (InterruptedException e) {
            // just trap this one ???
            System.out.println("Error reading the queue in the response map.");
            e.printStackTrace();
        }
    }

    public void open() throws Exception {
        Bootstrap b = new Bootstrap();
        String protocol = uri.getScheme();
        if (!"ws".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        final WebSocketConnectionHandler handler =
                new WebSocketConnectionHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, false, HttpHeaders.EMPTY_HEADERS), this);

        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("http-codec", new HttpClientCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
                        pipeline.addLast("ws-handler", handler);
                    }
                });

        ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        handler.handshakeFuture().sync();
    }

    public void close() throws InterruptedException {
        ch.writeAndFlush(new CloseWebSocketFrame());
        ch.closeFuture().sync();
        //group.shutdownGracefully();
    }

    public Iterator<JsonNode> eval(final String gremlin) throws GremlinClientException {
        final RequestMessage msg = new RequestMessage(ServerTokens.OPS_EVAL);
        msg.requestId = UUID.randomUUID();
        msg.args = new HashMap<String, Object>() {{
            put(ServerTokens.ARGS_GREMLIN, gremlin);
            put(ServerTokens.ARGS_ACCEPT, "application/json");
        }};

        final ArrayBlockingQueue<Optional<JsonNode>> responseQueue = new ArrayBlockingQueue<>(256);
        final UUID requestId = msg.requestId;
        responses.put(requestId, responseQueue);

        String textFrame;
        try {
            textFrame = RequestMessage.Serializer.json(msg);
        } catch (IOException e) {
            throw new GremlinClientException(GremlinClientErrorCodes.REQUEST_SERIALIZATION_ERROR, "WebSocketConnection could not serialize: " + msg);
        }

        ch.writeAndFlush(new TextWebSocketFrame(textFrame));
        //System.out.println("Sending: " + textFrame);

        return new BlockingIterator(requestId);
    }

    /**
     * The BlockingIterator iterates over the queue of results returned from a request.  It will wait for a result
     * to appear in the queue and block on hasNext() until it does.  It will watch for a termination event to
     * learn that the results have finished streaming.
     */
    class BlockingIterator implements Iterator {
        private final ArrayBlockingQueue<Optional<JsonNode>> queue;
        private final UUID requestId;
        private JsonNode current;

        public BlockingIterator(final UUID requestId) {
            this.requestId = requestId;
            this.queue = responses.get(requestId);
        }

        @Override
        public boolean hasNext() {
            try {
                final Optional<JsonNode> node = queue.poll(8000, TimeUnit.MILLISECONDS);
                if (node == null) {
                    System.out.println("time elapsed before a result was ready");

                    // TODO: Is this correct?
                    return false;
                }

                if (!node.isPresent()) {
                    responses.remove(requestId);
                    return false;
                }

                // todo: better job with types
                this.current = node.get().get("result");
                return true;
            } catch (InterruptedException ie) {
                //ie.printStackTrace();
                System.out.println("hmmm...interrupted while waiting");

                // todo: this isn't right...at least not exactly.  how do we terate out of a timeout.
                return false;
            }
        }

        @Override
        public JsonNode next() {
            return current;
        }
    }
}
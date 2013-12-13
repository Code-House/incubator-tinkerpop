package com.tinkerpop.gremlin.client.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
class WebSocketConnectionHandler extends SimpleChannelInboundHandler<Object> {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private final WebSocketConnection client;

    public WebSocketConnectionHandler(final WebSocketClientHandshaker handshaker, final WebSocketConnection client) {
        this.handshaker = handshaker;
        this.client = client;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        //System.out.println("WebSocket Client disconnected!");
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            // web socket client connected
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            final FullHttpResponse response = (FullHttpResponse) msg;
            throw new GremlinClientException(GremlinClientErrorCodes.RESPONSE_DESERIALIZATION_ERROR,
                    "Unexpected FullHttpResponse (getStatus=" + response.getStatus() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        final WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            final TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            final JsonNode node = mapper.readTree(textFrame.text());
            System.out.println("Received: " + textFrame.text());
            client.putResponse(UUID.fromString(node.get("requestId").asText()), Optional.of(node));
        } else if (frame instanceof PongWebSocketFrame) {
        } else if (frame instanceof CloseWebSocketFrame)
            ch.close();
        else if (frame instanceof BinaryWebSocketFrame) {
            // a binary frame witht he requestid in it basically represents the termination of a particular
            // results sets serialization process.  at this point the iteration on the client side can be killed.
            // pushing in an empty object to the stream will tell the client-side iterator to stop interpreting
            // results on this request
            final ByteBuf bb = frame.content();
            final UUID requestId = new UUID(bb.readLong(), bb.readLong());
            client.putResponse(requestId, Optional.empty());
        }

    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();

        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }

        ctx.close();
    }
}

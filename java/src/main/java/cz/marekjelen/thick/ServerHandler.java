package cz.marekjelen.thick;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ServerHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private ServerEnvironment serverEnvironment;
    private ByteBuf inputBuffer;
    private Map<String, Object> env;
    private WebSocketServerHandshaker handShaker;

    @Override
    public void messageReceived(ChannelHandlerContext context, Object message) throws Exception {
        if (message instanceof HttpRequest) {
            onRequest(context, (HttpRequest) message);
        } else if (message instanceof HttpChunk) {
            onChunk(context, (HttpChunk) message);
        } else if (message instanceof WebSocketFrame){
            onWebSocket(context, (WebSocketFrame) message);
        }
    }

    private void onRequest(ChannelHandlerContext context, HttpRequest request) throws Exception {

        if(request.containsHeader("Upgrade") && request.getHeader("Upgrade").equals("websocket")){
            // ToDo: WebSockets path
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:9292/websockets", null, false);
            handShaker = wsFactory.newHandshaker(request);
            if (handShaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(context.channel());
            } else {
                handShaker.handshake(context.channel(), request);
            }


            // Websockets will be handled as normal requests, when user connects PUT method will be send
            // to notify of new connection, when data will come from client, POST method request will be send
            // and once the connection closes, a request with DELETE method is used.
            // There has to be a WebSocket environment create to represent each opened socket, that will be
            // passed down the the application. Application will then have reference to send data back to the
            // browser.

            // Also this Websocket handling chunk of code has to be integrated with the application to check on
            // authentication&authorization and other things before accepting the connection.

            env = buildEnvironment();

            env.put("REQUEST_METHOD", "PUT");
            env.put("PATH_INFO", "/thick/websockets"); // ToDo: path selection
            env.put("QUERY_STRING", "id=xyz"); // ToDo: id generation?

            serverEnvironment.getApplication().call(env);

            return;
        }

        ServerResponse response = new ServerResponse(context);

        File staticFile = new File("public", request.getUri());

        if (staticFile.isFile()) {
            response.sendFile(staticFile);
            return;
        }

        env = buildEnvironment();

        String[] queryString = request.getUri().split("\\?", 2);

        inputBuffer = Unpooled.buffer();
        inputBuffer.writeBytes(request.getContent());

        env.put("REQUEST_METHOD", request.getMethod().getName());
        env.put("PATH_INFO", queryString[0]);
        env.put("QUERY_STRING", queryString.length == 1 ? "" : queryString[1]);

        env.put("SERVER_NAME", "localhost"); // ToDo: Be more precise!
        env.put("SERVER_PORT", "8080"); // ToDo: Be more precise!

        if (request.getHeaderNames().contains(HttpHeaders.Names.CONTENT_LENGTH)) {
            long content_length = HttpHeaders.getContentLength(request);
            if (content_length > 0) {
                env.put("CONTENT_LENGTH", content_length);
            }
        }

        for (String headerName : request.getHeaderNames()) {
            String rackName = headerName.replaceAll("\\-", "_").toUpperCase();
            if (!rackName.equals("CONTENT_TYPE") && !rackName.equals("CONTENT_LENGTH")) {
                rackName = "HTTP_" + rackName;
            }
            env.put(rackName, request.getHeader(headerName));
        }

        env.put("thick.response", response);

        if (request.getTransferEncoding().isSingle()) {
            handleRequest();
        }

    }

    private Map<String, Object> buildEnvironment() {
        Map<String, Object> env = new HashMap<String, Object>();

        env.put("rack.version", new int[]{1, 1});
        env.put("rack.url_scheme", "http"); // ToDo: add support for HTTPs
        env.put("rack.errors", System.err); // ToDo: where to write errors?
        env.put("rack.multithread", true);
        env.put("rack.multiprocess", false);
        env.put("rack.run_once", false);

        env.put("SCRIPT_NAME", "");

        return env;
    }

    private void onChunk(ChannelHandlerContext context, HttpChunk chunk) {
        inputBuffer.writeBytes(chunk.getContent());
        if (chunk.isLast()) {
            handleRequest();
        }
    }

    private void handleRequest() {
        env.put("rack.input", new ByteBufInputStream(inputBuffer));
        serverEnvironment.getApplication().call(env);
    }

    private void onWebSocket(ChannelHandlerContext context, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handShaker.close(context.channel(), (CloseWebSocketFrame) frame);
            // ToDo: notify application about closed WebSocket
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            context.channel().write(new PongWebSocketFrame(frame.getBinaryData()));
            return;
        }
        // ToDo: provide interface to push WebSocket data to application (text, binary, continuation)
    }

    public void setEnvironment(ServerEnvironment env) {
        serverEnvironment = env;
    }
}

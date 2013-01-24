package cz.marekjelen.thick;

import io.netty.buffer.ByteBuf;
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
    private WebSocketEnvironment webSocketEnvironment;
    private ServerResponse response;

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

        response = new ServerResponse(context);

        if(request.containsHeader("Upgrade") && request.getHeader("Upgrade").equals("websocket")){
            env = buildEnvironment(request);

            env.put("PATH_INFO", "/thick/websockets"); // ToDo: path selection
            env.put("QUERY_STRING", "id=xyz"); // ToDo: id generation?
            env.put("rack.input", inputBuffer);

            webSocketEnvironment = new WebSocketEnvironment(context, request);
            env.put("thick.websocket", webSocketEnvironment);
            env.put("thick.response_bypass", true);

            env.put("REQUEST_METHOD", "PUT");

            serverEnvironment.getApplication().call(env);

            return;
        }

        File staticFile = new File("public", request.getUri());

        if (staticFile.isFile()) {
            response.sendFile(staticFile);
            return;
        }

        env = buildEnvironment(request);

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

        if (request.getTransferEncoding().isSingle()) {
            handleRequest();
        }

    }

    private Map<String, Object> buildEnvironment(HttpRequest request) {
        Map<String, Object> env = new HashMap<String, Object>();

        env.put("rack.version", new int[]{1, 1});
        env.put("rack.url_scheme", "http"); // ToDo: add support for HTTPs
        env.put("rack.errors", System.err); // ToDo: where to write errors?
        env.put("rack.multithread", true);
        env.put("rack.multiprocess", false);
        env.put("rack.run_once", false);

        HijackIO hijack = new HijackIO(null);

        env.put("rack.hijack?", true);
        env.put("rack.hijack", hijack);
        env.put("rack.hijack_io", hijack);

        env.put("SCRIPT_NAME", "");

        for (String headerName : request.getHeaderNames()) {
            String rackName = headerName.replaceAll("\\-", "_").toUpperCase();
            if (!rackName.equals("CONTENT_TYPE") && !rackName.equals("CONTENT_LENGTH")) {
                rackName = "HTTP_" + rackName;
            }
            env.put(rackName, request.getHeader(headerName));
        }

        env.put("thick.response", response);

        return env;
    }

    private void onChunk(ChannelHandlerContext context, HttpChunk chunk) {
        inputBuffer.writeBytes(chunk.getContent());
        if (chunk.isLast()) {
            handleRequest();
        }
    }

    private void handleRequest() {
        env.put("rack.input", inputBuffer);
        serverEnvironment.getApplication().call(env);
    }

    private void onWebSocket(ChannelHandlerContext context, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            webSocketEnvironment.closed((CloseWebSocketFrame) frame);
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            context.channel().write(new PongWebSocketFrame(frame.getBinaryData()));
            return;
        }
        if (frame instanceof ContinuationWebSocketFrame){
            inputBuffer.writeBytes(frame.getBinaryData());
        }else{
            inputBuffer = Unpooled.buffer();
        }
        if (frame instanceof TextWebSocketFrame || frame instanceof BinaryWebSocketFrame){
            inputBuffer.writeBytes(frame.getBinaryData());
        }
        if(frame.isFinalFragment()){
            webSocketEnvironment.getHandler().on_data(new String(inputBuffer.array()));
        }
    }

    public void setEnvironment(ServerEnvironment env) {
        serverEnvironment = env;
    }
}

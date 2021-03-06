package cz.marekjelen.thick;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ServerHandler extends SimpleChannelInboundHandler<Object> {

    private ServerEnvironment serverEnvironment;
    private ByteBuf inputBuffer;
    private Map<String, Object> env;
    private WebSocketEnvironment webSocketEnvironment;
    private ServerResponse response;
    private Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.logger.debug("New connection");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        this.logger.debug("Closed connection");
    }

    @Override
    public void channelRead0(ChannelHandlerContext context, Object message) throws Exception {
        this.logger.info(message.getClass().getName());
        if(message instanceof HttpRequest){
            onRequest(context, (HttpRequest) message);
        }else if(message instanceof HttpContent){
            onChunk(context, (HttpContent) message);
        }else if (message instanceof WebSocketFrame){
            onWebSocket(context, (WebSocketFrame) message);
        }
    }

    private void onRequest(ChannelHandlerContext context, HttpRequest request) throws Exception {

        response = new ServerResponse(context);
        inputBuffer = Unpooled.buffer();

        if(request.headers().contains("Upgrade") && request.headers().get("Upgrade").equals("websocket")){
            this.logger.info("Upgrading");
            env = buildEnvironment(request);

            env.put("PATH_INFO", "/thick/websockets"); // ToDo: path selection
            env.put("QUERY_STRING", "id=xyz"); // ToDo: id generation?
            env.put("rack.input", inputBuffer);

            DefaultFullHttpRequest req = new DefaultFullHttpRequest(request.getProtocolVersion(), request.getMethod(), request.getUri());
            req.headers().set(request.headers());

            webSocketEnvironment = new WebSocketEnvironment(context, req);
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

        env.put("thick.response", response);

        String[] queryString = request.getUri().split("\\?", 2);

        env.put("rack.version", new int[] {1,1});
        env.put("rack.url_scheme", "http"); // ToDo: add support for HTTPs
        env.put("rack.errors", System.err); // ToDo: where to write errors?
        env.put("rack.multithread", true);
        env.put("rack.multiprocess", false);
        env.put("rack.run_once", false);

        env.put("REQUEST_METHOD", request.getMethod().name());
        env.put("SCRIPT_NAME", "");
        env.put("PATH_INFO", queryString[0]);
        env.put("QUERY_STRING", queryString.length == 1 ? "" : queryString[1]);

        env.put("SERVER_NAME", "localhost"); // ToDo: Be more precise!
        env.put("SERVER_PORT", "8080"); // ToDo: Be more precise!

        if(request.headers().contains(HttpHeaders.Names.CONTENT_LENGTH)){
            long content_length = HttpHeaders.getContentLength(request);
            if (content_length > 0) {
                env.put("CONTENT_LENGTH", content_length);
            }
        }

        this.logger.debug("Request processed, waiting for request body");

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

        for(String headerName : request.headers().names()){
            String rackName = headerName.replaceAll("\\-", "_").toUpperCase();
            if (!rackName.equals("CONTENT_TYPE") && !rackName.equals("CONTENT_LENGTH")) {
                rackName = "HTTP_" + rackName;
            }
            env.put(rackName, request.headers().get(headerName));
        }

        env.put("thick.response", response);

        return env;
    }

    private void onChunk(ChannelHandlerContext context, HttpContent chunk) {
        this.logger.debug("Request body chunk processed");
        inputBuffer.writeBytes(chunk.content());
        if(chunk instanceof LastHttpContent){
            if(this.webSocketEnvironment == null) handleRequest();
        }
    }

    private void handleRequest() {
        this.logger.debug("Request finished, call Ruby application");
        env.put("rack.input", inputBuffer);
        serverEnvironment.getApplication().call(env);
    }

    private void onWebSocket(ChannelHandlerContext context, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            frame.retain();
            webSocketEnvironment.closed((CloseWebSocketFrame) frame);
            return;
        }

        if (frame instanceof PingWebSocketFrame) {
            context.channel().write(new PongWebSocketFrame(frame.content()));
            return;
        }

        if (frame instanceof ContinuationWebSocketFrame){
            inputBuffer.writeBytes(frame.content());
        }else{
            inputBuffer = Unpooled.buffer();
        }

        if (frame instanceof TextWebSocketFrame){
            inputBuffer.writeBytes(frame.content());
        }

        if(frame instanceof BinaryWebSocketFrame){
            inputBuffer.writeBytes(frame.content());
        }

        if(frame.isFinalFragment()){
            webSocketEnvironment.getHandler().on_data(inputBuffer.toString(Charset.defaultCharset()));
        }
    }

    public void setEnvironment(ServerEnvironment env) {
        serverEnvironment = env;
    }
}

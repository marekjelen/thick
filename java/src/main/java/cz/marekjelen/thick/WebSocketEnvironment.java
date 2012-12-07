package cz.marekjelen.thick;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.util.HashMap;
import java.util.Map;

public class WebSocketEnvironment {

    private Map<Object, Object> attributes = new HashMap<Object, Object>();
    private ChannelHandlerContext context;
    private WebSocketServerHandshaker handShaker;
    private final HttpRequest request;

    public WebSocketEnvironment(ChannelHandlerContext context, HttpRequest request) {
        this.context = context;
        this.request = request;
    }

    public void handshake(String url){
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(url, null, false);
        handShaker = wsFactory.newHandshaker(request);
        if (handShaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(context.channel());
        } else {
            handShaker.handshake(context.channel(), request);
        }
    }

    public void write(String data){
        context.channel().write(new TextWebSocketFrame(data));
    }

    public void close(){
        context.channel().write(new CloseWebSocketFrame());
    }

    public void closed(CloseWebSocketFrame frame){
        handShaker.close(context.channel(), frame);
    }

    public void addAttribute(Object key, Object value){
        attributes.put(key, value);
    }

    public void getAttribute(Object key){
        attributes.get(key);
    }

    public void removeAttribute(Object key){
        attributes.remove(key);
    }

}

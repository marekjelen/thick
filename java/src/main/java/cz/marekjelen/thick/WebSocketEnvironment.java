package cz.marekjelen.thick;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.util.HashMap;
import java.util.Map;

public class WebSocketEnvironment {

    private Map<Object, Object> attributes = new HashMap<Object, Object>();
    private ChannelHandlerContext context;
    private WebSocketServerHandshaker handShaker;
    private HttpRequest request;
    private WebSocketHandler handler;

    public WebSocketEnvironment(ChannelHandlerContext context, HttpRequest request) {
        this.context = context;
        this.request = request;
    }

    public void handShake(String url){
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(url, null, false);
        handShaker = wsFactory.newHandshaker(request);
        if (handShaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(context.channel());
        } else {
            handShaker.handshake(context.channel(), (FullHttpRequest) request);
            this.handler.on_open();
        }
    }

    public void setHandler(WebSocketHandler handler){
        this.handler = handler;
        this.handler.setup(context);
    }

    public WebSocketHandler getHandler() {
        return handler;
    }

    public void closed(CloseWebSocketFrame frame){
        handShaker.close(context.channel(), frame);
        context.channel().close();
        this.handler.on_close();
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

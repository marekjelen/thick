package cz.marekjelen.thick;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public abstract class WebSocketHandler {

    private ChannelHandlerContext context;

    public void setup(ChannelHandlerContext context){
        this.context = context;
    }

    public void write(String data){
        context.channel().write(new TextWebSocketFrame(data));
    }

    public void close(){
        context.channel().write(new CloseWebSocketFrame());
    }

    public abstract void on_open();
    public abstract void on_data(String data);
    public abstract void on_close();

}

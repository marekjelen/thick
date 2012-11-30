package cz.marekjelen.thick;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

public class ServerResponse extends DefaultHttpResponse {

    private ChannelHandlerContext context;
    private ByteBuf buffer;
    private boolean chunked = false;
    private boolean sent = false;

    public ServerResponse(ChannelHandlerContext context) {
        super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        this.context = context;
        this.buffer = Unpooled.buffer();
        setContent(buffer);
    }

    public void setStatus(int status){
        this.setStatus(HttpResponseStatus.valueOf(status));
    }

    public void chunked(){
        this.chunked = true;
        setTransferEncoding(HttpTransferEncoding.CHUNKED);
    }

    public void streamed(){
        this.chunked = true;
        setTransferEncoding(HttpTransferEncoding.STREAMED);
    }

    public void writeContent(String data){
        if(this.sent){
            HttpChunk chunk = new DefaultHttpChunk(Unpooled.wrappedBuffer(data.getBytes()));
            context.write(chunk);
        }else{
            buffer.writeBytes(data.getBytes());
        }
    }

    public ServerResponse send(){
        ChannelFuture future = context.write(this);
        this.sent = true;
        if(!chunked){
            future.addListener(ChannelFutureListener.CLOSE);
        }
        return this;
    }

    public void close(){
        context.write(HttpChunk.LAST_CHUNK);
    }

}

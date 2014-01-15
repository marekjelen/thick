package cz.marekjelen.thick;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;

public class ServerResponse extends DefaultHttpResponse {

    private ChannelHandlerContext context;
    private ByteBuf buffer;
    private boolean chunked = false;
    private boolean sent = false;

    public ServerResponse(ChannelHandlerContext context) {
        super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        this.context = context;
        this.buffer = Unpooled.buffer();
    }

    public void setStatus(int status) {
        this.setStatus(HttpResponseStatus.valueOf(status));
    }

    public void chunked() {
        this.chunked = true;
        this.headers().set(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
    }

    public void streamed() {
        this.chunked = true;
    }

    public boolean isChunked() {
        return chunked;
    }

    public ServerResponse send(){
        context.write(this);

        if(!chunked){
            context.write(new DefaultLastHttpContent(buffer)).addListener(ChannelFutureListener.CLOSE);
        }else{
            context.write(new DefaultHttpContent(buffer));
            context.flush();
        }

        this.sent = true;
        return this;
    }

    public void sendFile(String path) throws IOException {
        sendFile(new File(path), false);
    }

    public void sendFile(File file) throws IOException {
        sendFile(file, false);
    }

    public void sendFile(String path, boolean attachment) throws IOException {
        sendFile(new File(path), attachment);
    }

    public void sendFile(File file, boolean attachment) throws IOException {
        HttpHeaders.setContentLength(this, file.length());
        if(attachment){
            this.headers().set("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        }
        this.headers().set(HttpHeaders.Names.CONTENT_TYPE, URLConnection.getFileNameMap().getContentTypeFor(file.getName()));
        context.write(this);
        context.channel().write(new DefaultFileRegion(new FileInputStream(file).getChannel(), 0, file.length()));
    }

    public void writeContent(String data){
        if(this.sent){
            context.write(new DefaultHttpContent(Unpooled.wrappedBuffer(data.getBytes())));
            context.flush();
        }else{
            buffer.writeBytes(data.getBytes());
        }
    }

    public void close(){
        context.write(new DefaultLastHttpContent());
    }

}

package cz.marekjelen.thick;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;

public class ServerResponse extends DefaultHttpResponse {

    private ChannelHandlerContext context;
    private boolean chunked = false;
    private Logger logger = LoggerFactory.getLogger("Response " + this.hashCode());

    public ServerResponse(ChannelHandlerContext context) {
        super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        this.context = context;
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

    public void send(){
        context.write(this);
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
        context.write(new DefaultHttpContent(Unpooled.wrappedBuffer(data.getBytes())));
        context.flush();
    }

    public void close(){
        context.write(new DefaultLastHttpContent()).addListener(ChannelFutureListener.CLOSE);
        context.flush();
    }

}

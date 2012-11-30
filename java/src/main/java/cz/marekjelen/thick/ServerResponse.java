package cz.marekjelen.thick;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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

    public boolean isChunked() {
        return chunked;
    }

    public void writeContent(String data){
        if(this.sent){
            context.write(new DefaultHttpChunk(Unpooled.wrappedBuffer(data.getBytes())));
            context.flush();
        }else{
            buffer.writeBytes(data.getBytes());
        }
    }

    public ServerResponse send(){
        if(!chunked){
            setContent(buffer);
            ChannelFuture future = context.write(this);
            future.addListener(ChannelFutureListener.CLOSE);
        }else{
            context.write(this);
            context.write(new DefaultHttpChunk(buffer));
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
            this.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        }
        setHeader(HttpHeaders.Names.CONTENT_TYPE, URLConnection.getFileNameMap().getContentTypeFor(file.getName()));
        context.write(this);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        ChunkedFile chunkedFile = new ChunkedFile(randomAccessFile, 0, file.length(), 8192);
        context.write(chunkedFile).addListener(ChannelFutureListener.CLOSE);
    }

    public void close(){
        context.write(HttpChunk.LAST_CHUNK);
    }

}

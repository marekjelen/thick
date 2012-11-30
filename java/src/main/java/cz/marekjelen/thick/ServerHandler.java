package cz.marekjelen.thick;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ServerHandler extends ChannelInboundMessageHandlerAdapter<HttpRequest> {

    private ServerEnvironment serverEnvironment;

    @Override
    public void messageReceived(ChannelHandlerContext context, HttpRequest request) throws Exception {
        onRequest(context, request);
    }

    private void onRequest(ChannelHandlerContext context, HttpRequest request) throws Exception {

        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        File staticFile = new File("public", request.getUri());

        if(staticFile.isFile()){

            HttpHeaders.setContentLength(response, staticFile.length());

            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            response.setHeader(HttpHeaders.Names.CONTENT_TYPE, mimeTypesMap.getContentType(staticFile.getPath()));

            context.write(response);

            RandomAccessFile randomAccessFile = new RandomAccessFile(staticFile, "r");
            ChunkedFile chunkedFile = new ChunkedFile(randomAccessFile, 0, staticFile.length(), 8192);
            context.write(chunkedFile).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        HashMap<String, Object> env = new HashMap<String, Object>();

        String[] queryString = request.getUri().split("\\?", 2);
        HashSet<ByteBuf> input = new HashSet<ByteBuf>();

        env.put("rack.version", new int[] {1,1});
        env.put("rack.url_scheme", "http"); // ToDo: add support for HTTPs
        env.put("rack.input", input);
        env.put("rack.errors", System.err); // ToDo: where to write errors?
        env.put("rack.multithread", true);
        env.put("rack.multiprocess", false);
        env.put("rack.run_once", false);

        env.put("REQUEST_METHOD", request.getMethod().getName());
        env.put("SCRIPT_NAME", "");
        env.put("PATH_INFO", queryString[0]);
        env.put("QUERY_STRING", queryString.length == 1 ? "" : queryString[1]);
        env.put("SERVER_NAME", "localhost"); // ToDo: Be more precise!
        env.put("SERVER_PORT", "8080"); // ToDo: Be more precise!

        env.put("thick.async", null);
        env.put("thick.response", response);

        if(request.getHeaderNames().contains(HttpHeaders.Names.CONTENT_LENGTH)){
            long content_length = HttpHeaders.getContentLength(request);
            if(content_length > 0){
                env.put("CONTENT_LENGTH", content_length);
            }
        }

        for(String headerName : request.getHeaderNames()){
            String rackName = headerName.replaceAll("\\-", "_").toUpperCase();
            if(!rackName.equals("CONTENT_TYPE") && !rackName.equals("CONTENT_LENGTH")){
                rackName = "HTTP_" + rackName;
            }
            env.put(rackName, request.getHeader(headerName));
        }

        String body = serverEnvironment.getApplication().call(env);

        response.setContent(Unpooled.wrappedBuffer(body.getBytes()));
        context.write(response).addListener(ChannelFutureListener.CLOSE);

    }

    public void setEnvironment(ServerEnvironment env) {
        serverEnvironment = env;
    }
}

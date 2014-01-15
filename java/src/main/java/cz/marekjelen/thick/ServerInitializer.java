package cz.marekjelen.thick;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private ServerEnvironment serverEnvironment;

    public ServerInitializer(ServerEnvironment env) {
        serverEnvironment = env;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("codec-http", new HttpServerCodec());

        ServerHandler handler = new ServerHandler();
        handler.setEnvironment(serverEnvironment);

        pipeline.addLast("handler", handler);
    }

}

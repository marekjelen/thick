package cz.marekjelen.thick;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class Server {

    private ServerEnvironment serverEnvironment;
    private ServerBootstrap serverBootstrap;

    public Server(ServerEnvironment env) {

        serverEnvironment = env;
        serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(serverEnvironment.getAddress(), serverEnvironment.getPort()))
                .childHandler(new ServerInitializer(serverEnvironment));

    }

    public ServerEnvironment getServerEnvironment() {
        return serverEnvironment;
    }

    public void start() throws InterruptedException {
        serverBootstrap.bind().sync().channel().closeFuture().sync();
    }

    public void stop() {
        serverBootstrap.shutdown();
    }

}

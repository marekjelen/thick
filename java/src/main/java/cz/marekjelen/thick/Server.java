package cz.marekjelen.thick;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Server {

    private final NioEventLoopGroup boss;
    private final NioEventLoopGroup worker;
    private ServerEnvironment serverEnvironment;
    private ServerBootstrap serverBootstrap;
    private Channel channel;
    private Logger logger = LoggerFactory.getLogger(Server.class);

    public Server(ServerEnvironment env) {
        this.serverEnvironment = env;

        this.logger.info("Bootstrapping server");

        this.serverBootstrap = new ServerBootstrap();

        this.boss = new NioEventLoopGroup();
        this.worker = new NioEventLoopGroup();

        this.serverBootstrap.group(this.boss, this.worker)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(this.serverEnvironment.getAddress(), this.serverEnvironment.getPort()))
                .childHandler(new ServerInitializer(this.serverEnvironment));
    }

    public ServerEnvironment getServerEnvironment() {
        return serverEnvironment;
    }

    public Channel asyncStart() throws InterruptedException {
        this.logger.debug("Asynchronously starting server");
        this.channel = serverBootstrap.bind().sync().channel();
        return this.channel;
    }

    public void start() throws InterruptedException {
        asyncStart();
        this.logger.info("Server started @ http://{}:{}", this.serverEnvironment.getAddress(), this.serverEnvironment.getPort());
        this.channel.closeFuture().sync();
    }

    public void stop(){
        this.logger.debug("Stopping server");
        this.channel.close().awaitUninterruptibly();
        this.boss.shutdownGracefully();
        this.worker.shutdownGracefully();
    }

}

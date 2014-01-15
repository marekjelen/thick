package cz.marekjelen.thick;

import io.netty.channel.ChannelHandlerContext;


// http://www.ruby-doc.org/core-1.9.3/IO.html

public class HijackIO {

    private ChannelHandlerContext context;

    public HijackIO(ChannelHandlerContext context) {
        this.context = context;
    }

    public HijackIO call(){
        return this;
    }

    public String read(){
        return null;
    }

    public String read(int length){
        return null;
    }

    public Object read(int length, Object buffer){
        return null;
    }

    public String readNonblock(int length){
        return null;
    }

    public Object readNonblock(int length, Object buffer){
        return null;
    }

    public int write(String buffer){
        return 0;
    }

    public int writeNonblock(String buffer){
        return 0;
    }

    public void flush(){
        this.context.flush();
    }

    public void close(){

    }

    public void closeRead(){

    }

    public void closeWrite(){

    }

    public boolean isClosed(){
        return false;
    }

}

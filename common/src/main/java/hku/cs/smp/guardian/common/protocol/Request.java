package hku.cs.smp.guardian.common.protocol;

import io.netty.channel.ChannelHandlerContext;

public abstract class Request extends Message {

    protected ChannelHandlerContext context;

    @Override
    public void setContext(ChannelHandlerContext ctx) {
        this.context = ctx;
    }

    public void response(Response response) {
        context.channel().writeAndFlush(response);
    }

    public abstract Response getResponse();


}

package hku.cs.smp.guardian.common.protocol;

import io.netty.channel.ChannelHandlerContext;

public abstract class Response extends Message {

    @Override
    final public void setContext(ChannelHandlerContext ctx) {

    }
}

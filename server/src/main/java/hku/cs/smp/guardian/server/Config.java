package hku.cs.smp.guardian.server;


import hku.cs.smp.guardian.common.connection.Server;
import hku.cs.smp.guardian.common.protocol.InquiryRequest;
import hku.cs.smp.guardian.common.protocol.InquiryResponse;
import hku.cs.smp.guardian.common.protocol.TagRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.logging.Logger;

@Configuration
public class Config {
    private static Logger logger = Logger.getLogger(Config.class.getName());

    @Autowired
    CallsService callsService;

    @Bean
    Server server() {
        Server server = new Server();
        server.ofType(TagRequest.class).subscribe(message -> {
            callsService.tag(message.getPhone(), message.getTag());
            logger.info(String.format("[phone:%s, tag:%s]", message.getPhone(), message.getTag()));
            message.response(message.getResponse());
        });

        server.ofType(InquiryRequest.class).subscribe(message -> {
            Map<String, Integer> result = callsService.inquiry(message.getPhoneNumber());
            InquiryResponse response = message.getResponse();
            response.setResult(result);
            message.response(response);
        });
        return server;
    }
}

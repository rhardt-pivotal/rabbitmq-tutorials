package org.springframework.amqp.tutorials.tut6;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rhardt on 7/18/17.
 */
@Configuration
@RefreshScope
public class ConfigServerConfig {

    @Value("${defaultNumber:0}")
    private String defNumber;

    public String getDefNumber(){
        return defNumber;
    }

    public void setDefNumber(String dn){
        this.defNumber = dn;
    }


}

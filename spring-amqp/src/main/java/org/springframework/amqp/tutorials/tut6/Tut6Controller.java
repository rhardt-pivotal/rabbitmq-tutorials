package org.springframework.amqp.tutorials.tut6;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by rhardt on 7/18/17.
 */
@Controller
public class Tut6Controller {

    @Autowired
    private ConfigServerConfig config;

    @RequestMapping("/")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("Default number is: "+config.getDefNumber());
    }

}

package com.utiltools.imageConverter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestEndpoint {

    @GetMapping("/app")
    public String testMethod(){
        return "up";
    }
}

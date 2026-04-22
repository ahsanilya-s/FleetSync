package com.fleetsync.fleetsync.controller;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("api/test")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, FleetSync!";
    }
    @GetMapping("/sheikh")
    public String sheikh() {
        return "Hello, Sheikh!";
    }
}

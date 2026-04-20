package com.example.erudio.controllers;

import com.example.erudio.model.Greetin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private static final String template = "Hello , %s!";
    private final AtomicLong counter = new AtomicLong();


    // http://localhost:8080/greeting?name=Alexandre
    @RequestMapping("/greeting")
    public Greetin greetin(
            @RequestParam(value = "name", defaultValue = "Word")
            String name){
        return new Greetin(counter.incrementAndGet(),String.format(template, name));

    }

}

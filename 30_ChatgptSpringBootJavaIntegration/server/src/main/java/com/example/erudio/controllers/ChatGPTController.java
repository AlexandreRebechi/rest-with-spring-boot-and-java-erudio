package com.example.erudio.controllers;

import com.example.erudio.services.ChatGPTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bot")
public class ChatGPTController {

    @Autowired
    private ChatGPTService service;

    // HOST /bot/chat
    @GetMapping("/chat")
    public String chat(@RequestParam("prompt") String prompt) {
        return service.chat(prompt);
    }
}

package com.bytemarket.bytemarket_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController {

    private static String discordLink = "https://discord.gg/exemplo";

    @GetMapping("/discord")
    public ResponseEntity<String> getDiscord() {
        return ResponseEntity.ok(discordLink);
    }

    @PostMapping("/discord")
    public ResponseEntity<Void> updateDiscord(@RequestBody String newLink) {
        discordLink = newLink;
        return ResponseEntity.ok().build();
    }
}
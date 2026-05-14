package com.fleetsync.fleetsync.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class VehicleController {

}

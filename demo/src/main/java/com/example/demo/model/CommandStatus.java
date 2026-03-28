package com.example.demo.model;

public enum CommandStatus {
    PENDING,   // Waiting to be sent to the light
    RUNNING,   // Currently transmitting to the smart hub
    DONE,      // Light acknowledged the change
    FAILED     // Hub unreachable / Device offline
}
package io.github.kappa243.remitly2025.controllers;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SwiftCodeRequest {

    @Size(min = 11, max = 11)
    @Pattern(regexp = "^[A-Z0-9]{11}$")
    String swiftCode;
}

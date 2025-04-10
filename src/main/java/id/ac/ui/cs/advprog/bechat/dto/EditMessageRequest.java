package id.ac.ui.cs.advprog.bechat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditMessageRequest {

    @NotBlank
    private String content;
}


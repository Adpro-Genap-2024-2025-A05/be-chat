package id.ac.ui.cs.advprog.bechat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class SendMessageRequest {

    @NotNull(message = "Session ID tidak boleh null")
    private UUID sessionId;

    @NotNull(message = "Sender ID tidak boleh null")
    private UUID senderId;

    @NotBlank(message = "Konten pesan tidak boleh kosong")
    private String content;

}

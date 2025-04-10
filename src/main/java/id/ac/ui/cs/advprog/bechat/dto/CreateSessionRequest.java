package id.ac.ui.cs.advprog.bechat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateSessionRequest {

    @NotNull
    private UUID user1Id;

    @NotNull
    private UUID user2Id;
}

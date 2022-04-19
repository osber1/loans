package io.osvaldas.api.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessage {

    private String clientId;

    private String fullName;

    private String email;
}

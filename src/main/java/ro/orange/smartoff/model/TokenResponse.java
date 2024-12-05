package ro.orange.smartoff.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString
@Getter
@Setter
public class TokenResponse {
    private String refresh_token;
    private String access_token;
}

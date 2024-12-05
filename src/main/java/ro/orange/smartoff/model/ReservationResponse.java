package ro.orange.smartoff.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ReservationResponse {
    private String userEmail;
    private String parkingElementId;
    private String alias;
    private String basementZoneName;
    private String basementName;
}

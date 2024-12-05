package ro.orange.smartoff.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationRequest {
    private String start;
    private String end;
    private Integer parkingElementId;
    private String plateNumber;
    private Boolean isElectricVehicle;
}

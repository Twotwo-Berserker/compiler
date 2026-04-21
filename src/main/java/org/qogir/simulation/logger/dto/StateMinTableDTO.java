package org.qogir.simulation.logger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateMinTableDTO {
    private Integer step;
    private String splitBy;
    private String stateSets;

}

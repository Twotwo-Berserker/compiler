package org.qogir.simulation.logger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateSet {
    /*0-no accept 1-accept*/
    private Set<Integer> curStateSet;
    private Integer type;
}

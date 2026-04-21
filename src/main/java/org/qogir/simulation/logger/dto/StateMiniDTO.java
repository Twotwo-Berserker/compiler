package org.qogir.simulation.logger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.qogir.simulation.logger.dto.diagramsInfos.StateInfosDTO;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateMiniDTO {
    private ArrayList<StateMinTableDTO> stateMinTableDTOS = new ArrayList<>();
    private StateInfosDTO stateInfos;
}

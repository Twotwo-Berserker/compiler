package org.qogir.simulation.logger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.qogir.simulation.logger.dto.diagramsInfos.StateInfosDTO;
import org.qogir.simulation.logger.dto.diagramsInfos.TreeInfosDTO;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThompsonDTO {
    private ArrayList<TreeInfosDTO> treeInfos;
    private StateInfosDTO stateInfos;
}

package org.qogir.simulation.logger.dto.diagramsInfos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateInfosDTO {
    private ArrayList<NodeInfosDTO> nodeInfos;
    private ArrayList<EdgesDTO> edges;
}

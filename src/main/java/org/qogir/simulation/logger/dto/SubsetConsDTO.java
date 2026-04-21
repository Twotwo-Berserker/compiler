package org.qogir.simulation.logger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.qogir.simulation.logger.dto.diagramsInfos.StateInfosDTO;

import java.util.ArrayList;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsetConsDTO {
    private ArrayList<SubsetConsTableColsInfo> tableColsInfos = new ArrayList<>();
    private ArrayList<Map<String, String>> subsetConsTableDTOS = new ArrayList<>();
    private StateInfosDTO stateInfos;
}

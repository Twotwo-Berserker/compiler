package org.qogir.simulation.logger.dto.diagramsInfos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EdgesDTO {
    private int from;
    private int to;
    private String label;
}

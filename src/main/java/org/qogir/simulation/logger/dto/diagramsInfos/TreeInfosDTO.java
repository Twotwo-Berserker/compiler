package org.qogir.simulation.logger.dto.diagramsInfos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeInfosDTO {
    private Integer key;
    private Integer parent;
    private String name;
    private Integer type;
    private String category;
    private String color;
}

package org.qogir.simulation.logger.dto.diagramsInfos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeInfosDTO {
    private int id;
    private String label;
    private String type;
    private String color;

    public NodeInfosDTO(int id, String label, String type) {
        this.id = id;
        this.label = label;
        this.type = type;
    }
}

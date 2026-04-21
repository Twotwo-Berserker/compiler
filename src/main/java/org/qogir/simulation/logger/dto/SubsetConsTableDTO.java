package org.qogir.simulation.logger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
//一行数据
public class SubsetConsTableDTO {
    private Map<String, String> rowData = new HashMap<>();
}

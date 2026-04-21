package org.qogir.simulation.logger.dto.diagramsInfos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.qogir.simulation.scanner.Token;

import java.util.ArrayDeque;
import java.util.ArrayList;

@Data
@AllArgsConstructor
public class ScanDTO {
    //reset when used scan
    public static Integer count = 0;

    private Integer stepNo;

    private ArrayList<DfaState> curStates = new ArrayList<>();

    private ArrayDeque<Token> tokens = new ArrayDeque<>();

    private Integer inputPos;

    public ScanDTO() {
        stepNo = ++count;
    }

    public void addCurStates(String regexId, Integer curStateId) {
        curStates.add(new DfaState(regexId, curStateId));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class DfaState {
        private String regexId;
        private Integer curStateId;

    }


}

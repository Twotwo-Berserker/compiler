package org.qogir.simulation.logger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@Data
@AllArgsConstructor
public class StateMiniTableRowDTO {
    //reset when used minimization
    public static Integer count = 0;

    private Integer step;
    private String splitBy;
    private ArrayList<StateSet> stateSets = new ArrayList<>();

    public StateMiniTableRowDTO() {
        step = ++count;
    }

    public ArrayList<String> curStateSet() {
        ArrayList<String> sets = new ArrayList<>();
        for (StateSet s : stateSets) {
            sets.add(s.getCurStateSet().toString());
        }
        return sets;
    }

    public HashMap<String, Integer> typeMap() {
        HashMap<String, Integer> types = new HashMap<>();
        for (StateSet s : stateSets) {
            types.put(s.getCurStateSet().toString(), s.getType());
        }
        return types;
    }

    public void addStateSet(Set<Integer> curStateSet, Integer type) {
        stateSets.add(new StateSet(curStateSet, type));
    }
}


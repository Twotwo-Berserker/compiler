package org.qogir.simulation.logger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;

import java.util.ArrayDeque;

@Data
public class ConstructionLogger<T> {

    private ArrayDeque<T> stepQueue;

    private ArrayDeque<ArrayDeque<T>> stepQueues;

    public ConstructionLogger() {
        stepQueue = new ArrayDeque<>();
        stepQueues = new ArrayDeque<>();
    }

    public void addStep(T t) {
        stepQueue.add(t);
    }

    public String returnStepQueue() {
        return JSONObject.toJSONString(stepQueue, SerializerFeature.DisableCircularReferenceDetect);
    }

    public String returnStepQueues() {
        return JSONObject.toJSONString(stepQueues, SerializerFeature.DisableCircularReferenceDetect);
    }

}

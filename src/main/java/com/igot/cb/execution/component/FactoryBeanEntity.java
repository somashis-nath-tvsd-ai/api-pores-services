package com.igot.cb.execution.component;

import lombok.Getter;

import javax.persistence.Entity;
import java.util.Map;

@Getter
public class FactoryBeanEntity {

    private final Map<String, Object> beansMap;

    public FactoryBeanEntity(Map<String, Object> beansMap) {
        this.beansMap = beansMap;
    }

    public void printBeans() {
        beansMap.forEach((key, bean) -> {
            System.out.println("Key: " + key + ", Bean: " + bean.getClass().getSimpleName());
        });
    }
}

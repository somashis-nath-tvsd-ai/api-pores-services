package com.function.execution.basic;

import com.function.execution.component.util.Utilities;
import com.function.execution.component.service.UtilityService;

import java.util.List;

public class DefaultUtilityServiceImpl implements UtilityService {
    @Override
    public void preSaveUtility(List<Utilities> utilityList, Object dataObject) {

    }

    @Override
    public void afterSaveUtility(List<Utilities> utilityList, Object dataObject) {

    }

    @Override
    public void preUpdateUtility(List<Utilities> utilityList, Object dataObject) {

    }

    @Override
    public void afterUpdateUtility(List<Utilities> utilityList, Object dataObject) {

    }

    @Override
    public Object afterReadUtility(List<Utilities> utilityList, Object dataObject) {
        return null;
    }

    @Override
    public void preDeleteUtility(List<Utilities> utilityList, Object dataObject) {

    }

    @Override
    public void afterDeleteUtility(List<Utilities> utilityList, Object dataObject) {

    }
}

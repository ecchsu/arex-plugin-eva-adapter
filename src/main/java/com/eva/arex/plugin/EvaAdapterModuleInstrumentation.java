package com.eva.arex.plugin;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

/**
 * Main entry point for AREX plugin to instrument com.eva.adapter packages
 * This class will be discovered by AREX agent via SPI
 */
@AutoService(ModuleInstrumentation.class)
public class EvaAdapterModuleInstrumentation extends ModuleInstrumentation {

    public EvaAdapterModuleInstrumentation() {
        super("plugin-eva-adapter");
        //Log when plugin is constructed
        System.out.println("[AREX] plugin-eva-adapter called!!");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        System.out.println("[AREX] plugin-eva-adapter instrumentationTypes() called!!");
        // Return all instrumentation implementations
        return Arrays.asList(
            new EvaAdapterPackageInstrumentation()
        );
    }
}
